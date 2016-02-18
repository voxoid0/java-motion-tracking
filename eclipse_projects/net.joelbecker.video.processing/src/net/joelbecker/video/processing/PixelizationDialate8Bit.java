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
import java.util.Arrays;

import javax.media.format.VideoFormat;

public class PixelizationDialate8Bit extends RgbVideoEffect {

	private int pixelSize = 2;
	
	public PixelizationDialate8Bit() {
	}

	/**
	 * @return the pixelSize
	 */
	public int getPixelSize() {
		return pixelSize;
	}

	/**
	 * @param pixelSize the pixelSize to set
	 */
	public void setPixelSize(int pixelSize) {
		this.pixelSize = pixelSize;
	}

	@Override
	public String getName() {
		return "Pixelization Dialate (8-bit)";
	}

	@Override
	protected boolean processRGB(byte[] bin, byte[] bout, VideoFormat format) {
		int rowSize = pixelSize * format.getSize().width;
		int offset;
		int rowOffset = 0;
		int x, y;
		int sx, sy;
		int pixOff;
		Arrays.fill(bout, 0, bout.length, (byte) 0);
		for (y = 0; y < format.getSize().height; y += pixelSize) {
			offset = rowOffset;
			for (x = 0; x < format.getSize().width; x += pixelSize) {
				
				// If any input pixel within the square is on (255), fill the corresponding square in the output.
				pixOff = offset;
			inputSearch:
				for (sy = 0; sy < pixelSize; sy++) {
					for (sx = 0; sx < pixelSize; sx++) {
						
						// If we found a non-zero input pixel
						if (bin[pixOff + sx] != 0) {
							
							// Fill the output square
							pixOff = offset;
							for (sy = 0; sy < pixelSize; sy++) {
								for (sx = 0; sx < pixelSize; sx++) {
									bout[pixOff + sx] = (byte) 255;
								}
								pixOff += format.getSize().width;
							}
							
							break inputSearch;	// Don't bother checking any more pixels in the input square!
						}
					}
					pixOff += format.getSize().width;
				}
				
				offset += pixelSize;
			}
			rowOffset += rowSize;
		}
				
		return true;
	}
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
