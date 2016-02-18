/******************************************************************************
* Copyright (c) 2008-2010 Joel Becker. All Rights Reserved.
* http://tech.joelbecker.net
*
*    This is free software; you can redistribute it and/or modify
*    it under the terms of the GNU General Public License
*    version 3, as published by the Free Software Foundation.
*
*    This is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU General Public License for more details.
*
*    You should have received a copy of the GNU General Public
*    License along with this source file; if not, write to the Free Software
*    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
******************************************************************************/
package net.joelbecker.vision.blob.detector;


import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.media.format.VideoFormat;

import net.joelbecker.video.processing.RgbVideoEffect;
import net.joelbecker.vision.blob.Blob;
import net.joelbecker.vision.blob.BlobManager;

/**
 * <p>Detects and labels all pixel blobs in the input video frame, which must be an
 * 8-bit threshold image where each pixel is either 0 or 255. The output is a
 * 16-bit image, where each pixel value is a blob label number, or 0 for no label.
 * The labels are guaranteed to increase from 1 to n, without any skipped labels,
 * and where n is the number of blobs found/labeled.</p>
 * 
 * <p>The actual video output is RGB, which each label colored a different color (but
 * after a certain number of labels, the colors repeat). The label image may be
 * obtained via getLastLableImage().</p>
 */
public class FourNeighborBlobDetector extends RgbVideoEffect {

	public static final int MAX_LABELS = 320 * 240 / 4;
	
	private BlobManager blobManager;
	private int minBlobSize = 16;
	private int maxBlobSize = Integer.MAX_VALUE;
	//private int[] blobPixelCount = new int[MAX_LABELS + 1];
	private int[] changeLabel = new int[MAX_LABELS + 1];
	private int[] labelImage = new int[0];
	private VideoFormat format;
	private int frameNumber = -1;
	
	public FourNeighborBlobDetector(BlobManager mgr, int maxLabels, int minBlobSize, int maxBlobSize) {
		blobManager = mgr;
	}
	
	public String getName() {
		return "Four-Neighbor Blob Detector";
	}

	public int getMinBlobSize() {
		return minBlobSize;
	}
	
	public void setMinBlobSize(int minBlobSize) {
		this.minBlobSize = minBlobSize;
	}
	
	public int getMaxBlobSize() {
		return maxBlobSize;
	}
	
	public void setMaxBlobSize(int maxBlobSize) {
		this.maxBlobSize = maxBlobSize;
	}
	
	/**
	 * <p>Do the processing of the video frame, labeling each pixel of the blobs
	 * of the threshold input. A blob is a set of pixels all of which neighbor
	 * one or more other pixels in the blob. The effect is that of a traditional
	 * floodfill on each blob of pixels.</p>
	 * 
	 * <p>This method is an entire processing chain of itself.</p>
	 * 
	 * <table>
	 * <tr>
	 *   <td><b>Process</b></td>
	 *   <td><b>labelImage</b></td>
	 * </tr>
	 * <tr>
	 *   <td>Partial blobs are labeled. Equivalences are found, for later resolution.</td>
	 *   <td>A contiguous set of label values throughout. The actual full blobs may contain multiple labels.</td>
	 * </tr>
	 * <tr>
	 *   <td>Equivalences are compressed so that indirection is always single-level.</td>
	 *   <td>n/a</td>
	 * </tr>
	 * <tr>
	 *   <td>Labels to be mapped to are made contiguous.</td>
	 *   <td>n/a (labelImage still has contiguous, unmapped labels.)</td>
	 * </tr>
	 * <tr>
	 *   <td>Equivalences are resolved in the labelImage.</td>
	 *   <td>Blob parts all have the same label, and thereby become one blob. The labels are no longer contiguous (they are sparse).</td>
	 * </tr>
	 * <tr>
	 *   <td></td>
	 *   <td></td>
	 * </tr>
	 * <tr>
	 *   <td></td>
	 *   <td></td>
	 * </tr>
	 * <tr>
	 *   <td></td>
	 *   <td></td>
	 * </tr>
	 * </table>
	 */
	protected boolean processRGB(byte[] bin, byte[] bout, VideoFormat format) {
		List<Blob> blobs;
		int nLabels;

		//System.out.println("\n######## FourNeighborBlobDetector ########\n");
		int size = initFrame(format);
		
		// Label the input buffer pixels
		nLabels = doLabeling(bin, format, size);
		
		if (nLabels > 0) {
			// Make label equivalences point directly to final equivalent (single level of indirection)
			compressLabelEquivalences(nLabels);
			
			// Make the labels contiguous (no skipped label values)
			//nLabels = makeLabelsContiguous(nLabels);
			
			resolveLabelEquivalences(bout, format, nLabels);
	
			blobs = createBlobList(format, size, nLabels);
			blobs = filterBlobsBySize(blobs);	// TODO: move to BlobManager delegate filter?
		} else {
			blobs = new ArrayList<Blob>(0);
		}
		blobManager.updateBlobs(blobs);
		return true;
	}

	/**
	 * @param format
	 * @return
	 */
	private int initFrame(VideoFormat format) {
		int i;
		frameNumber++;
		
		this.format = format;
		blobManager.setVideoSize(format.getSize());
		
		//// Initialize
		for (i = 1; i <= MAX_LABELS; i++) {
			// Set equivalence array to have each label assigned to itself (no re-mappings yet)
			changeLabel[i] = i;
			
			// Reset label pixel count
			//blobPixelCount[i] = 0;
		}
		
		// Make sure the label image is the same size as the video
		int size = format.getSize().width * format.getSize().height;
		if (size != labelImage.length) {
			labelImage = new int[size];
		}
		Arrays.fill(labelImage, 0);
		return size;
	}

	/**
	 * Creates the final list of detected blobs. The labels must be contiguous.
	 * @param format
	 * @param size
	 * @param maxLabel
	 * @return
	 */
	private List<Blob> createBlobList(VideoFormat format, int size,
			int maxLabel) {
		List<Blob> blobs;
		int i;
		int x;
		int y;
		
		//// Create blobs list.
		/* blobs[0] is label 1, blobs[1] is label 2, etc. */
		// blobs =  new ArrayList<Blob>(maxLabel);
		blobs = Arrays.asList(new Blob[maxLabel + 1]);
		for(i = 1; i <= maxLabel; i++) {
			int lbl = changeLabel[i];
			if (blobs.get(lbl - 1) == null) {
				blobs.set(lbl - 1, new Blob(lbl, 
						new Rectangle(
								format.getSize().width, format.getSize().height, -1, -1))
						//blobPixelCount[lbl])
				);
			}
			/*
			blobs.add(new Blob(	i + 1, 
								new Rectangle(
										format.getSize().width, format.getSize().height, -1, -1),
								blobPixelCount[i])
			);
			*/
		}
		//assert(blobs.size() == maxLabel);

		// Find the rectangular boundaries of each blob
		i = format.getSize().width;
		for(y = 1; y < format.getSize().height; y++) {
			i++; // skip first pixel in row
			for(x = 1; x < format.getSize().width - 1; x++, i++) {
				if (labelImage[i] != 0) {
					int index = labelImage[i] - 1;
					//if (index < nLabels) {
						Blob blob = blobs.get(index);
						//assert(blob != null);
						//assert(blob.bounds != null);
						++blob.pixelCount;
						if (x < blob.bounds.x)
							blob.bounds.x = x;
						if (y < blob.bounds.y)
							blob.bounds.y = y;
						if (x > blob.bounds.x + blob.bounds.width)
							blob.bounds.width = x - blob.bounds.x;
						if (y > blob.bounds.y + blob.bounds.height)
							blob.bounds.height = y - blob.bounds.y;
					//}
				}
			}
			i++; // skip last pixel in row
		}
		//assert(i == size);
		
		//// Flip blobs vertically to correct pixel coordinates of frame image ( 0,0 = top-left)
		for (Blob b : blobs) {
			if (b != null) {
				b.bounds.y = format.getSize().height-1 - (b.bounds.y + b.bounds.height);
			}
		}
		return blobs;
	}

	/**
	 * <p>Resolves the label equivalences in the labelImage.</p>
	 * <p>Note: if the labels were contiguous before, they remain so afterwards.</p>
	 * @param bout
	 * @param format
	 * @param nLabels
	 */
	private void resolveLabelEquivalences(byte[] bout, VideoFormat format, int nLabels) {
		int i;
		int x;
		int y;
		/* Now scan and resolve the labels in the label image accordingly. */
		i = format.getSize().width;
		for(y = 1; y < format.getSize().height; y++) {
			i++; // skip first pixel in row
			for(x = 1; x < format.getSize().width - 1; x++, i++) {
				if (labelImage[i] != 0) {
					labelImage[i] = changeLabel[labelImage[i]];
					
					// FIXME no need to duplicate to labelImage and bout both. (except to show output)
					if (labelImage[i] <= 255) {
						bout[i] = (byte) labelImage[i];
					}
				}
				assert(labelImage[i] <= nLabels);
			}
			i++; // skip last pixel in row
		}
	}

	/**
	 * Filters out blobs that are too small or large (according to pixel count),
	 * and also gets rid of null entries in the blob list.
	 * @param maxLabel Highest label value used
	 */
	private List<Blob> filterBlobsBySize(List<Blob> blobsIn) {
		List<Blob> blobsOut = new Vector<Blob>();
		
		for (Blob b : blobsIn) {
			if (b != null) {
				//System.out.println(String.format("Blob #%d's pixel count = %d", b.label, b.pixelCount));
				if (b.pixelCount >= minBlobSize && b.pixelCount <= maxBlobSize) {
					blobsOut.add(b);
					//System.out.println(String.format("Blob label %d PASSED with %d pixels", b.label, b.pixelCount));
				}
			}
		}
		return blobsOut;
		/* Eliminate blobs that are too small or large in pixel count */
		/*
		int i;
		for (i = 1; i <= maxLabel; i++) {
			if (blobPixelCount[i] < minBlobSize || blobPixelCount[i] > maxBlobSize) {
				if (blobPixelCount[i] > 2)
					System.out.println(String.format("Blob labeled %d is to small or big, at %d pixels", i, blobPixelCount[i]));
				changeLabel[i] = 0;	// we will change pixels labeled this label to no label (0)
			}
		}*/
	}

	/**
	 * Points all equivalences directly to their end equivalent. E.g., if 101
	 * equates to 72, and 72 equates to 3, then we want 101 to equate directly
	 * to 3. This way we can make a single pass over the label image to resolve
	 * the equivalences.
	 * 
	 * @param maxLabel
	 */
	private void compressLabelEquivalences(int maxLabel) {
		int i;
		int j;
		
		/*
		i = format.getSize().width;
		int highest = -1;
		for(int y = 1; y < format.getSize().height; y++) {
			i++; // skip first pixel in row
			for(int x = 1; x < format.getSize().width - 1; x++, i++) {
				if (labelImage[i] > highest) {
					highest = labelImage[i];
				}
			}
			i++; // skip last pixel in row
		}
		assert(highest == maxLabel);*/
		
		/*
		if (highest > maxLabel) {
			System.out.println("" + highest + " > " + maxLabel + " !!");
		}*/

		/* First, 
		 */
		for (i = maxLabel; i > 0; i--) {
			
			j = i;
			//System.out.print("Label " + j);
			while (changeLabel[j] != j) {
				j = changeLabel[j];
				//System.out.print(" to " + j);
			}
			//System.out.print("\n");
			
			if (j != i) {
				//System.out.println(String.format("\tLabel %d --> %d", i, j));
				changeLabel[i] = j;
			
				// Transfer old label's count onto the one it is being changed to
				//blobPixelCount[j] += blobPixelCount[i];
				//blobPixelCount[i] = 0;
				
			}
		}
	}

	/**
	 * @param bin
	 * @param format
	 * @param size
	 * @return The total number of labels used, which is also the highest label value used (since they're contiguous).
	 */
	private int doLabeling(byte[] bin, VideoFormat format, int size) {
		int i;
		int x;
		int y;
		int curLabel = 1;
		//// Label!
		// Do everything in the output array
		//System.arraycopy(bin, 0, bout, 0, bin.length);	// TODO Necessary ???
		
		i = format.getSize().width;	// start on 2nd pixel of 2nd row (because we check upper neighbors and left neighbors, so we don't want to check upper neighbors of the top row and read past the beginning of the buffer.
		for(y = 1; y < format.getSize().height; y++) {
			++i; // skip first pixel in row
			for(x = 1; x < format.getSize().width - 1; x++, i++) {
				if(bin[i] == (byte) 255) {
					
					/*
					 * If there are neighboring labels, and they are all equal, use that label for
					 * this pixel. If there are different neighbor labels, use the lowest and equate
					 * the other labels with that one. Otherwise if there are no neighboring labels,
					 * use a new label.
					 */
					int lowestLabel;// = 0;
					
					// Left neighbor
					//if (labelImage[i - 1] != 0)
						lowestLabel = labelImage[i - 1];
					
					// Upper-left neighbor
					lowestLabel = checkNeighborLabel(format,
							labelImage[i - format.getSize().width - 1],
							lowestLabel);
					
					// Upper neighbor
					lowestLabel = checkNeighborLabel(format,
							labelImage[i - format.getSize().width],
							lowestLabel);
					
					// Upper-right neighbor
					lowestLabel = checkNeighborLabel(format,
							labelImage[i - format.getSize().width + 1],
							lowestLabel);
					
					//// If there was a neighbor label, use the appropriate neighbor label
					if (lowestLabel != 0) {			
						labelImage[i] = lowestLabel;
						
						//// Change neighbors to the decided label as well!
						updateNeighbor(i - 1, lowestLabel);
						updateNeighbor(i - format.getSize().width - 1, lowestLabel);
						updateNeighbor(i - format.getSize().width, lowestLabel);
						updateNeighbor(i - format.getSize().width + 1, lowestLabel);
						
					//// Otherwise, use new label
					} else {
						labelImage[i] = curLabel;
						//++blobPixelCount[curLabel];
						curLabel++;						// Change next label to use
						if (curLabel > MAX_LABELS) {
							y = Integer.MAX_VALUE - 1;	// break out of both x AND Y loops. (I know, it's messy; need to refactor...)
							break;
						}
					}
				} else {
					labelImage[i] = 0;
				}
			} // next x
			i++; // skip last pixel in row
		} // next y
		assert(i == size);
		return curLabel - 1;
	}

	/**
	 * Change mapping to map labels into a contiguous label sequence (without
	 * skipping any label values).
	 * 
	 * @param maxLabel
	 * @return The total number of labels, which is also the highest label value.
	 */
	private int makeLabelsContiguous(int maxLabel) {
		int i;
		int curLabel;
		curLabel = 1;
		for (i = 1; i <= maxLabel; i++) {
			if (changeLabel[i] != 0) {
				if (changeLabel[i] == i) {
					changeLabel[i] = curLabel;
					curLabel++;	// use next label next time
				} else {
					//// Change this mapping to use the mapped-to label's new value.
					// Note: since labels are always mapped to lower label
					// values, we can safely assume that changeLabel[i] < i, and
					// thus that changeLabel[i] has already been made part of
					// the contiguous labels.
					assert(changeLabel[i] < i);
					changeLabel[i] = changeLabel[changeLabel[i]];
				}
			}
		}
		return curLabel - 1;
	}

	private void updateNeighbor(int neighborIndex, int lowestLabel) {
		int neighbor = labelImage[neighborIndex];
		
		// If the neighbor is greater than the lowest found, map it to the lowest
		if (neighbor != 0 && neighbor > lowestLabel) {
			//labelImage[i - 1] = lowestLabel;
			
			// If the neighbor is already mapped to another, map that one to the lowest as well 
			if (changeLabel[neighbor] != neighbor)
				changeLabel[ changeLabel[neighbor] ] = lowestLabel;
			
			// Map the neighbor to the lowest
			changeLabel[neighbor] = lowestLabel;
		}
	}

	/**
	 * Checks if the neighbor label is lower than the lowest yet found. If it is lower,
	 * the previous is mapped to it and the lower is returned. Otherwise, the same
	 * lowest yet found will be returned.
	 * 
	 * @param format
	 * @param neighbor The neighbor label
	 * @param lowestLabelYet The lowest label found so far
	 * @return The lowest of the two if both are active
	 */
	private int checkNeighborLabel(VideoFormat format, int neighbor, int lowestLabelYet) {
		if (neighbor != 0) {
			if (lowestLabelYet == 0) {
				lowestLabelYet = neighbor;
			} else if (neighbor < lowestLabelYet) {
				changeLabel[lowestLabelYet] = neighbor;
				lowestLabelYet = neighbor;
			}
				//changeLabel[p] = neighborLabel;
		}
		return lowestLabelYet;
	}
	
	protected void updateImage(byte[] bout, VideoFormat vformat) {
		synchronized (displayImage) {			
			//// Copy pixels to image
			WritableRaster rast = displayImage.getRaster();
			int[] pixel = new int[] {0, 0, 0, 255};
			int p = 0;
			int label;
			for (int y = vformat.getSize().height - 1; y >= 0; y--) {
				for (int x = 0; x < vformat.getSize().width; x++) {
					label = (int) labelImage[p] & 0xFF;
					pixel[0] = LabelColors.REDS[label];
					pixel[1] = LabelColors.GREENS[label];
					pixel[2] = LabelColors.BLUES[label];
					rast.setPixel(x, y, pixel);
					++p;
				}
			}
		}
	}
	
	public int[] getLastLabelImage() {
		return labelImage;
	}
	public Dimension getLastLabelImageSize() {
		return format.getSize();
	}
	
	public int getCurrentFrameNumber() {
		return frameNumber;
	}
}
