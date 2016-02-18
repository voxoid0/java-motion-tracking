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

import java.awt.Dimension;

import javax.media.Format;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;

/**
 * Codec which gives access to the video frame buffers that run through it.
 * It does this by copying each input frame, since the actual buffers that
 * go in and come out of a codec may not be cached.
 * 
 * @author Joel Becker
 * <br>4/18/2009
 */
public class BufferAccessor extends RgbVideoEffect {

	private byte[] buffer;
	private Dimension size;
	
	public BufferAccessor() {
		supportedIns = new Format[] { new RGBFormat() };
		supportedOuts = new Format[] { new RGBFormat() };
	}

	public byte[] getBuffer() {
		return buffer;
	}
	
	public Dimension getBufferSize() {
		return size;
	}
	public String getName() {
		return "Video Buffer Accessor";
	}

	/**
	 * If either the R, G, or B components of a pixel pass the threshold, then
	 * the corresponding byte in the output buffer is set to 100% (255).
	 * NOTE: Output is a byte-per-pixel format, NOT 3 bytes per pixel.
	 */
	protected boolean processRGB(byte[] bin, byte[] bout, VideoFormat format) {
		size = format.getSize();
		if (buffer == null) {
			buffer = new byte[bin.length];
		}
		System.arraycopy(bin, 0, buffer, 0, bin.length);
		return false;
	}
	
	
	@Override
	protected void updateImage(byte[] bout, VideoFormat vformat) {
		super.updateImage(buffer, vformat);	// use extracted buffer instead of output
	}
}
