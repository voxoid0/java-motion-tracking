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

import javax.media.MediaLocator;

import net.joelbecker.video.processing.RgbVideoEffect;

/**
 * A camera that applies a processing chain to a given input.
 * 
 * @author Joel
 * @date Sep 23, 2010
 */
public interface IProcessingCamera {

	/**
	 * Opens the video stream at the given location.
	 * @return true if successful.
	 */
	boolean open(MediaLocator ml);

	/**
	 * Returns true if the camera is open.
	 * @return true if the camera is open.
	 */
	boolean isOpen();

	/**
	 * Closes the video input to the processing chain.
	 */
	void close();

	/**
	 * Gets the video processing chain.
	 * @return An array of {@link RgbVideoEffect}s.
	 */
	RgbVideoEffect[] getProcessingChain();

	/**
	 * Returns the first {@link RgbVideoEffect} in the chain that is an instance of the given class, or null if no such effect is in the chain.
	 * @param rgbVideoEffectClass The class of the desired effect.
	 * @return The effect, or null if non-existent.
	 */
	RgbVideoEffect getEffect(Class<? extends RgbVideoEffect> rgbVideoEffectClass);

	//void startRecording();

}
