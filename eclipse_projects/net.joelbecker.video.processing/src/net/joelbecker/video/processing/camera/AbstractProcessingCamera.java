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
package net.joelbecker.video.processing.camera;

import java.io.IOException;

import javax.media.ConfigureCompleteEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.DataSink;
import javax.media.EndOfMediaEvent;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoDataSinkException;
import javax.media.PrefetchCompleteEvent;
import javax.media.Processor;
import javax.media.RealizeCompleteEvent;
import javax.media.ResourceUnavailableEvent;
import javax.media.UnsupportedPlugInException;
import javax.media.control.TrackControl;
import javax.media.format.VideoFormat;

import net.joelbecker.video.processing.BackgroundUpdater;
import net.joelbecker.video.processing.BufferAccessor;
import net.joelbecker.video.processing.RgbVideoEffect;

/**
 * <p>
 * Abstract implementation of an {@link IProcessingCamera} which only requires
 * the implementation of <code>createEffectChain()</code>.
 * </p>
 * 
 * @author Joel Becker
 * @date Sep 23, 2010
 */
public abstract class AbstractProcessingCamera implements IProcessingCamera, ControllerListener {

	private Processor processor;
	private Object waitSync = new Object();
	private boolean stateTransitionOK = true;
	private RgbVideoEffect effectChain[];

	protected BufferAccessor bufferAccessor;
	protected BackgroundUpdater backgroundUpdater;	
	
	
	public AbstractProcessingCamera() {
	}
	
	/* (non-Javadoc)
	 * @see net.joelbecker.video.processing.camera.IProcessingCamera#open(javax.media.MediaLocator)
	 */
	public boolean open(MediaLocator ml) {

		try {
			processor = Manager.createProcessor(ml);
		} catch (Exception e) {
			System.err
					.println("Failed to create a processor from the given url: "
							+ e);
			return false;
		}

		processor.addControllerListener(this);

		// Put the Processor into configured state.
		processor.configure();
		if (!waitForState(Processor.Configured)) {
			System.err.println("Failed to configure the processor.");
			return false;
		}

		// So I can use it as a player.
		processor.setContentDescriptor(null);

		// Obtain the track controls.
		TrackControl tc[] = processor.getTrackControls();

		if (tc == null) {
			System.err
					.println("Failed to obtain track controls from the processor.");
			return false;
		}

		// Search for the track control for the video track.
		TrackControl videoTrack = null;

		for (int i = 0; i < tc.length; i++) {
			if (tc[i].getFormat() instanceof VideoFormat) {
				videoTrack = tc[i];
				break;
			}
		}

		if (videoTrack == null) {
			System.err.println("The input media does not contain a video track.");
			return false;
		}

		System.err.println("Video format: " + videoTrack.getFormat());

		// Create the video codec chain.
		effectChain = createEffectChain();
		
		try {
			videoTrack.setCodecChain(effectChain);
		} catch (UnsupportedPlugInException e) {
			System.err.println("The process does not support effects.");
		}

		// Create the blob processing chain
		
		
		
		// Realize the processor.
		processor.prefetch();
		if (!waitForState(Processor.Prefetched)) {
			System.err.println("Failed to realize the processor.");
			return false;
		}

		// Start the processor.
		processor.start();

		//setVisible(true);
		return true;
	}

	/* (non-Javadoc)
	 * @see net.joelbecker.video.processing.camera.IProcessingCamera#isOpen()
	 */
	public boolean isOpen() {
		return (processor != null && processor.getState() == Processor.Started);
	}
	
	protected abstract RgbVideoEffect[] createEffectChain();

	/* (non-Javadoc)
	 * @see net.joelbecker.video.processing.camera.IProcessingCamera#close()
	 */
	public void close() {
		processor.close();
		processor = null;
	}
	
	/* (non-Javadoc)
	 * @see net.joelbecker.video.processing.camera.IProcessingCamera#getProcessingChain()
	 */
	public RgbVideoEffect[] getProcessingChain() {
		return effectChain;
	}
	
	/* (non-Javadoc)
	 * @see net.joelbecker.video.processing.camera.IProcessingCamera#getEffect(java.lang.Class)
	 */
	public RgbVideoEffect getEffect(Class<? extends RgbVideoEffect> rgbVideoEffectClass) {
		for (RgbVideoEffect effect : effectChain) {
			if (rgbVideoEffectClass.isInstance(effect)) {
				return effect;
			}
		}
		return null;
	}
	
	/**
	 * Block until the processor has transitioned to the given state. Return
	 * false if the transition failed.
	 */
	private boolean waitForState(int state) {
		synchronized (waitSync) {
			try {
				while (processor.getState() != state && stateTransitionOK)
					waitSync.wait();
			} catch (Exception e) {
			}
		}
		return stateTransitionOK;
	}

	/* (non-Javadoc)
	 * @see net.joelbecker.video.processing.camera.IProcessingCamera#startRecording()
	 */
	public void startRecording() {
		DataSink dataSink;
		try {
			dataSink = Manager.createDataSink(processor.getDataOutput(),
					new MediaLocator("out.avi"));
			try {
				dataSink.open();
				dataSink.start();
			} catch (IOException e) {
				
			}
		} catch (NoDataSinkException e) {
			
		}
	}
	
	/**
	 * Controller Listener.
	 */
	public void controllerUpdate(ControllerEvent evt) {

		if (evt instanceof ConfigureCompleteEvent
				|| evt instanceof RealizeCompleteEvent
				|| evt instanceof PrefetchCompleteEvent) {
			synchronized (waitSync) {
				stateTransitionOK = true;
				waitSync.notifyAll();
			}
		} else if (evt instanceof ResourceUnavailableEvent) {
			synchronized (waitSync) {
				stateTransitionOK = false;
				waitSync.notifyAll();
			}
		} else if (evt instanceof EndOfMediaEvent) {
			processor.setMediaTime(Processor.RESET);
			processor.start();
			//processor.close();
			//System.exit(0);
		}
	}
}
