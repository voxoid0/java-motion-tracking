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

import javax.media.format.VideoFormat;

public class BackgroundUpdater extends RgbVideoEffect {
	private BufferAccessor buffAcc;
	private long frameNumber = 0;
	private byte[] background = null;	// until we know the frame dimensions
	private static final int BACKGROUND_GRAB_TIME = 1; //30; // frames
	
	public BackgroundUpdater(BufferAccessor feedBufferAccessor) {
		buffAcc = feedBufferAccessor;
	}
	
	public byte[] getBackground() {
		return background;
	}
	
	public String getName() {
		return "Background Image Updater";
	}
	
	public void resetBackground() {
		frameNumber = BACKGROUND_GRAB_TIME - 1;
	}
	
	public boolean isCapturing() {
		return frameNumber <= BACKGROUND_GRAB_TIME;
	}
	
	/**
	 * Updates the background buffer.
	 * For now, we just copy the first frame verbatim (ASSUMES there are no moving
	 * objects in the first frame.
	 * @param bin MUST BE from the output of the BlobDetector or the RgbThresholdEffect.
	 */
	@Override
	protected boolean processRGB(byte[] bin, byte[] bout, VideoFormat format) {
		if (background == null) {
			background = new byte[format.getSize().width * format.getSize().height * 3];
		}
		if (frameNumber < BACKGROUND_GRAB_TIME) {
			System.out.print(frameNumber == 0 ? "Capturing background" : ".");
			
			if(buffAcc.getBuffer() != null) {
				System.out.println("Grabbing snapshot for background image");
				System.arraycopy(buffAcc.getBuffer(), 0, background, 0, bin.length);
			} else {
				--frameNumber;	// we didn't get that frame, so back up and don't count it
			}
		} else {
			//if(buffAcc.getBuffer() != null) {
				int p = 0;	// background pixel pos
				int b = 0;	// blob byte pos
				int comp;	// color component (rgb, 0-2)
				for (p = 0; p < background.length; p+=3, b++) {
					if (bin[b] == 0) {	/// Only update pixels where no moving object is detected
						//background[i] = buffAcc.getBuffer()[i];
						
						for (comp = 0; comp < 3; comp++) {
							int pix = p + comp;
							if(((int) background[pix] & 0xff) < ((int) (buffAcc.getBuffer()[pix] & 0xff))) {
								++background[pix];
							} else if(((int) background[pix] & 0xff) > ((int) buffAcc.getBuffer()[pix] & 0xff)) {
								--background[pix];
							}
						}
						
					}
				}
			//}
		}
		++frameNumber;
		return false;
	}
	
	@Override
	protected void updateImage(byte[] bout, VideoFormat vformat) {
		super.updateImage(background, vformat);	// use background image instead of output
	}
	
}
