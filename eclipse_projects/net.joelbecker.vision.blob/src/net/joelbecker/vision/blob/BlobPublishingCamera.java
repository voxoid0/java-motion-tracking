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
package net.joelbecker.vision.blob;

import net.joelbecker.video.processing.BackgroundUpdater;
import net.joelbecker.video.processing.BufferAccessor;
import net.joelbecker.video.processing.PixelizationDialate8Bit;
import net.joelbecker.video.processing.RgbDiffEffect;
import net.joelbecker.video.processing.RgbThresholdEffect;
import net.joelbecker.video.processing.RgbVideoEffect;
import net.joelbecker.video.processing.camera.AbstractProcessingCamera;
import net.joelbecker.vision.blob.detector.FourNeighborBlobDetector;

/**
 * Panel that streams video from a given source through a video processing
 * chain.
 * 
 * TODO Use noise-based threshold (higher threshold for noisier pixels)
 * 
 * @author Joel Becker
 */
public class BlobPublishingCamera extends AbstractProcessingCamera {

	protected BlobManager blobManager;

	protected BufferAccessor bufferAccessor;
	protected BackgroundUpdater backgroundUpdater;	
	
	
	public BlobPublishingCamera() {
		this.blobManager = new BlobManager();
		//videoImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
	}
	
	
	protected RgbVideoEffect[] createEffectChain() {
		bufferAccessor = new BufferAccessor();
		backgroundUpdater = new BackgroundUpdater(bufferAccessor);
		return new RgbVideoEffect[] {
				bufferAccessor,
				new RgbDiffEffect(backgroundUpdater),
				new RgbThresholdEffect(),
				new PixelizationDialate8Bit(),
				backgroundUpdater,
				new FourNeighborBlobDetector(blobManager, 254, 256, 640*480)
		};
	}

	/**
	 * Gets the {@link BlobManager} for this camera.
	 * @return the {@link BlobManager} for this camera.
	 */
	public BlobManager getBlobManager() {
		return blobManager;
	}
	
}
