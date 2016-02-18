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
package net.joelbecker.vision.blob.correlator;

import java.util.List;

import net.joelbecker.vision.blob.Blob;

/**
 * <p>Correlates new blobs with those from a previous video frame. That is, it it
 * essentially creates persistent (multi-frame) blobs from uncorrelated blobs,
 * which only last one frame.</p>
 * 
 * <p>The resulting blobs have a different set of labels completely. While the
 * per-frame blobs are labeled from 1-n, where n is the number of blobs in that
 * frame, a correlated blob is labeled arbitrarily, as well as uniquely from any
 * previous correlated blobs.</p>
 *  
 * @author Joel R. Becker
 * <br>8/10/09
 */
public interface BlobCorrelator {
	/**
	 * Correlates the new blobs with the previous blobs.
	 * @param newBlobs The new Blobs
	 * @param previousBlobs The previous Blobs to correlate the new ones with.
	 * @return The correlated list of Blobs.
	 */
	List<Blob> correlate(List<Blob> newBlobs, List<Blob> previousBlobs);
}
