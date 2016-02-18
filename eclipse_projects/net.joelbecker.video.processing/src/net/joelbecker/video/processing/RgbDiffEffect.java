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
import javax.media.Format;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;

public class RgbDiffEffect extends RgbVideoEffect {

	private BackgroundUpdater bgUpdater;
	
	public RgbDiffEffect(BackgroundUpdater bgUpdater) {
		supportedIns = new Format[] { new RGBFormat() };
		supportedOuts = new Format[] { new RGBFormat() };
		this.bgUpdater = bgUpdater;
	}
	
	public String getName() {
		return "RGB-Difference from Background Image";
	}

	protected boolean processRGB(byte[] bin, byte[] bout, VideoFormat format) {
		if(bgUpdater.getBackground() != null) {	
			//// Calculate difference between input and background
			int now, before, diff;
			//long totalAmt = 0;
			for(int i = 0; i < bin.length; i++) {
				now = (int) bin[i] & 0xff;
				before = (int) bgUpdater.getBackground()[i] & 0xff;
				diff = Math.abs(now - before);
				bout[i] = (byte)(diff);
			}
			
		} else {
			//// Make output show no differences
			for(int i = 0; i < bin.length; i++) {
				bout[i] = 0;
			}
		}
		return true;
	}
}
