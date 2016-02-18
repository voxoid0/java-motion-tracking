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
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

import javax.media.Buffer;
import javax.media.Effect;
import javax.media.Format;
import javax.media.PlugIn;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;

/**
 * RGB video effect.
 * @author Joel Becker
 *
 */
public abstract class RgbVideoEffect implements Effect {

	public static final String PROP_VIDEO_SIZE = "videoSize";
	
	protected BufferedImage displayImage;
	
	private List<VideoFrameListener> frameListenerList;
	
	// We pretend that our effects will always input and output RGB
	protected Format supportedIns[] = new Format[] { new RGBFormat() };
	protected Format supportedOuts[] = new Format[] { new RGBFormat() };

	private Format input = null, output = null;
	
	/** For profiling: total processing time ever. */
	private double totalTime;
	
	/** For profiling: total number of calls to processRGB(). */
	private long nCalls;

	/** Whether or not the effect is active. */
	private boolean active;
	
	/** Size of the incoming video frames. */
	private Dimension videoSize = new Dimension();
	
	private PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
	
	
	public RgbVideoEffect() {
		active = true;
		displayImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		frameListenerList = new ArrayList<VideoFrameListener>();
	}
	
	public Dimension getVideoSize() {
		return videoSize;
	}
	
	public int process(Buffer in, Buffer out) {

		if (in.getFormat() instanceof VideoFormat && in.getData() != null) {
			byte[] bin;
			byte[] bout;
			if (in.getData() instanceof byte[]) {
				bin = (byte[]) in.getData();
			} else if (in.getData() instanceof int[]) {
				int[] iin = (int[]) in.getData();
				bin = new byte[iin.length * 3];
				int bi, ii;
				for (bi = 0, ii = 0; bi < bin.length; bi += 3, ii++) {
					int v = iin[ii];
					bin[bi + 2] = (byte) (v & 0xff);
					bin[bi + 1] = (byte) ((v >> 8) & 0xff);
					bin[bi] = (byte) ((v >> 16) & 0xff);
				}
			} else {
				return PlugIn.BUFFER_PROCESSED_FAILED;
			}
//			byte[] bin = (byte[]) in.getData();
//			byte[] bout;
			if (!(out.getData() instanceof byte[]) || ((byte[])out.getData()).length < bin.length) {
				bout = new byte[bin.length];
				out.setData(bout);
			} else {
				bout = (byte[]) out.getData();
			}

			VideoFormat vformat = (VideoFormat) in.getFormat();
			if (vformat.getSize().width != videoSize.width
					|| vformat.getSize().height != videoSize.height) {
				videoSize = vformat.getSize();
				propSupport.firePropertyChange(PROP_VIDEO_SIZE, null, videoSize);
			}
			
			//// Assure output buffer is large enough
			if(bout == null || bout.length < bin.length) {
			}
			
			byte[] buffToDraw = bout;
			boolean processed = false;
			if (active) {
				long startTime = System.nanoTime();
				processed = processRGB(bin, bout, vformat);
				long stopTime = System.nanoTime();
				totalTime += (stopTime - startTime) / 1.0e9;
				++nCalls;
			}
			
			if(!processed) {
				// Swap the data between the input & output.
				Object data = in.getData();
				in.setData(out.getData());
				out.setData(data);
				buffToDraw = bin;
			}
			
			//// Update frame image available to UI
			if (frameListenerList.size() > 0) {
				
				//// Assure the image is the proper size
				if (displayImage.getWidth() != vformat.getSize().width || displayImage.getHeight() != vformat.getSize().height) {
					displayImage = new BufferedImage(vformat.getSize().width, vformat.getSize().height,
							BufferedImage.TYPE_INT_RGB);
				}
				
				updateImage(buffToDraw, vformat);
				notifyVideoFrameListeners();
			}
		}
		
		// Copy the input attributes to the output
		//out.setFormat(in.getFormat());
		//out.setLength(in.getLength());
		//out.setOffset(in.getOffset());
		
		return BUFFER_PROCESSED_OK;
	}

	protected abstract boolean processRGB(byte[] bin, byte[] bout, VideoFormat format);
	
	protected void updateImage(byte[] bout, VideoFormat vformat) {
		synchronized (displayImage) {
			WritableRaster rast = displayImage.getRaster();
			int[] pixel = new int[] {0, 0, 0, 255};
			int p = 0;
			for (int y = vformat.getSize().height - 1; y >= 0; y--) {
				for (int x = 0; x < vformat.getSize().width; x++) {
					pixel[0] = bout[p++];
					pixel[1] = bout[p++];
					pixel[2] = bout[p++];
					rast.setPixel(x, y, pixel);
				}
			}
		}
	}
	
	/*
	public void setDisplayImage(BufferedImage image) {
		displayImage = image;
	}*/
	public BufferedImage getDisplayImage() {
		return displayImage;
	}
	
	public void addVideoFrameListener(VideoFrameListener listener) {
		synchronized(frameListenerList) {
			frameListenerList.add(listener);
		}
	}
	
	public void removeVideoFrameListener(VideoFrameListener listener) {
		synchronized(frameListenerList) {
			frameListenerList.remove(listener);
			/*
			if(frameListenerList.size() < 0) {
				displayImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
			}*/
		}
	}
	
	/**
	 * Gets the average time per frame that this effect takes to process, in seconds.
	 * @return Average time in seconds.
	 */
	public double getAvgProcessingTime() {
		return totalTime == 0.0 ? 0.0 : (double) nCalls / totalTime;
	}
	
	public Object[] getControls() {
		return new Object[0];
	}

	public Object getControl(String type) {
		return null;
	}
	
	public abstract String getName();

	// No op.
	public void open() {
	}

	// No op.
	public void close() {
	}

	// No op.
	public void reset() {
	}

	public Format[] getSupportedInputFormats() {
//		return new Format[] {
//				new RGBFormat(new Dimension(640, 480), 640 * 480 * 4, Byte.class, 29.9f, 8, 1, 2, 3)
//		};
		return supportedIns;
	}

	public Format[] getSupportedOutputFormats(Format in) {
		if (in == null)
			return supportedOuts;
		else {
			// If an input format is given, we use that input format
			// as the output since we are not modifying the bit stream
			// at all.
			Format outs[] = new Format[1];
			outs[0] = in;
			return outs;
		}
	}

	public Format setInputFormat(Format format) {
		input = format;
		return input;
	}

	public Format setOutputFormat(Format format) {
		output = format;
		return output;
	}
	
	/**
	 * Returns true if the effect is active; false if deactivated.
	 * @return true if the effect is active; false if deactivated.
	 */
	public boolean isActive() {
		return active;
	}
	
	/**
	 * Actives or deactivates this effect. If deactivated, the effect will simply
	 * pass the video frames through to the next effect in the processing chain.
	 * @param active True if the effect is active.
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	
	/** Notifies the frame listeners of a new frame. */
	private void notifyVideoFrameListeners() {
		synchronized(frameListenerList) {
			for(VideoFrameListener listener : frameListenerList) {
				listener.newVideoFrame(displayImage);
			}
		}
	}
}
