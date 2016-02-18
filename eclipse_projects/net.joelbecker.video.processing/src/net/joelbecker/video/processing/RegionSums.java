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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;

import javax.media.format.VideoFormat;

/**
 * This video frame processor calculates the sum of the pixel values inside each
 * of numerous regions. The regions are defined by a "region indices image", which
 * contains a byte index corresponding to each pixel in the input image. Clients
 * can be notified of each new processed frame/sums by registering a listener
 * through <code>addPropertyChangeListener(PROP_SUMS, listener)</code>. The input
 * image is passed through unchanged to the next processor in the chain.
 * 
 * @author Joel
 * @date Sep 23, 2010
 */
public class RegionSums extends RgbVideoEffect {

	/** Pixel value indicating no region index. */ 
	public static final byte NO_INDEX = (byte) 0xff;
	
	/** The sums property. */
	public static final String PROP_SUMS = "sums";
	
	
	/** Sum of each region indexed from 0 to 254 (255 is a non-region). */
	private float sums[] = new float[255];
	
	/** Image of region indices. An index is 0-254. 255 is a non-index (no region). */
	private byte regionIndicesImage[];
	
	private PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
	

	public float[] getSums() {
		return sums;
	}
	
	public void setRegionIndicesImage(byte[] image) {
		regionIndicesImage = image;
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propSupport.addPropertyChangeListener(listener);
	}
	
	public void addPropertyChangeListener(String propName, PropertyChangeListener listener) {
		propSupport.addPropertyChangeListener(propName, listener);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propSupport.removePropertyChangeListener(listener);
	}
	
	public static byte[] createRegionIndicesArrayFromImage(BufferedImage image) {
		Raster raster = image.getRaster();
		byte[] img = new byte[raster.getWidth() * raster.getHeight() * 3];
		int[] pixel = new int[1];
		int bi = 0;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				raster.getPixel(x, y, pixel);
				Color color = new Color(pixel[0]);
				img[bi++] = (byte) color.getRed();
				img[bi++] = (byte) color.getGreen();
				img[bi++] = (byte) color.getBlue();
			}
		}
		return img;
	}
	
	public String getName() {
		return "Region Sums";
	}

	/**
	 * If either the R, G, or B components of a pixel pass the threshold, then
	 * the corresponding byte in the output buffer is set to 100% (255).
	 * NOTE: Output is a byte-per-pixel format, NOT 3 bytes per pixel.
	 */
	protected boolean processRGB(byte[] bin, byte[] bout, VideoFormat format) {
		if (regionIndicesImage != null) {
			if (regionIndicesImage.length != bin.length) {
				throw new IllegalArgumentException("Region indices image has a different size from the input image.");
			}
			Arrays.fill(sums, 0f);
			for (int i = 0; i < bin.length; i++) {
				if (regionIndicesImage[i] != NO_INDEX) {
					sums[ regionIndicesImage[i] ] += (float) bin[i];
				}
			}
		}
		propSupport.firePropertyChange(PROP_SUMS, null, sums);
		return false;
	}
}
