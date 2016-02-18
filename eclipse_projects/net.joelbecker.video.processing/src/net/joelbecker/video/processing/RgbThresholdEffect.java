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
package net.joelbecker.video.processing;

import java.awt.image.WritableRaster;

import javax.media.format.VideoFormat;

/**
 * Applies a brightness threshold to each pixel in the RGB input.
 * Input: 24-bit color
 * Output: 8-bit on/off (0/255)
 * @author Joel R. Becker
 *
 */
public class RgbThresholdEffect extends RgbVideoEffect {

	//private char threshold[] = new char[] {64, 64, 64};
	protected char threshold = 64;
	
	/** Number of pixels that passed the threshold. */
	protected int passCount;
	
	/** Ratio of pixels passing to pixels not passing. */
	protected float passRatio;

	
	public RgbThresholdEffect() {
	}
	
	public String getName() {
		return "RGB Threshold";
	}

	public void setThreshold(char threshold) {
		this.threshold = threshold;
	}
	
	public char getThreshold() {
		return threshold;
	}
	/**
	 * If either the R, G, or B components of a pixel pass the threshold, then
	 * the corresponding byte in the output buffer is set to 100% (255).
	 * NOTE: Output is a byte-per-pixel format, NOT 3 bytes per pixel.
	 */
	protected boolean processRGB(byte[] bin, byte[] bout, VideoFormat format) {
		/*
		if(supportedOuts == null) {
			supportedOuts = new Format[] { new IndexedColorFormat(format.getSize(),
					format.getMaxDataLength(), byte[].class, format.getFrameRate(),
					format.getSize().width, 8,
					LabelColors.REDS, LabelColors.GREENS, LabelColors.BLUES)
			};
		}*/
		passCount = 0;
		int p = 0;
		// TODO bin.length / 3 ??
		for(int i = 0; i < bin.length; i+=3, p++) {
			if ((char) bin[i] > threshold || (char) bin[i+1] > threshold || (char) bin[i+2] > threshold) {
				bout[p] = (byte) 255;
				passCount++;
			} else {
				bout[p] = 0;
			}
		}
		passRatio = (float) passCount / (float) (format.getSize().width * format.getSize().height);
		//System.out.println(String.format("Threshold pass: %.2f", passRatio*100));
				
		return true;
	}
	
	/** Returns the number of pixels that passed in the last frame. */
	public int getPassCount() {
		return passCount;
	}
	
	/** Returns the ratio of pixels passing to pixels not passing, in the last frame. */
	public float getPassRatio() {
		return passRatio;
	}
	
	/** {@inheritDoc} */
	protected void updateImage(byte[] bout, VideoFormat vformat) {
		synchronized (displayImage) {			
			//// Copy pixels to image
			WritableRaster rast = displayImage.getRaster();
			int[] pixel = new int[] {0, 0, 0, 255};
			int p = 0;
			for (int y = vformat.getSize().height - 1; y >= 0; y--) {
				for (int x = 0; x < vformat.getSize().width; x++) {
					pixel[0] = pixel[1] = pixel[2] = bout[p++];
					rast.setPixel(x, y, pixel);
				}
			}
		}
	}
}
