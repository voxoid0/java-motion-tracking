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

/**
 * Listens for {@link BlobManager} events, such as when a new blob is detected,
 * or when a blob has been lost.
 * @author Joel R. Becker
 * <br>8/24/09
 */
public interface BlobManagerListener {
	
	/**
	 * Called when a new blob has been detected (and correlated/reached age of acceptance).
	 * @param blob The new blob that was detected.
	 */
	void newBlobDetected(Blob blob);
	
	/**
	 * Called when a blob has been lost (and has reached the maximum missing time).
	 * @param blob The blob that was lost.
	 */
	void blobLost(Blob blob);
	
	/**
	 * Called when the blobs have been updated (via updateBlobs()).
	 */
	void blobsUpdated();
}
