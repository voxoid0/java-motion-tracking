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


import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import net.joelbecker.vision.blob.correlator.BlobCorrelator;
import net.joelbecker.vision.blob.correlator.WeightedBlobCorrelator;

/**
 * <p>
 * Manages {@link Blob}s, including the uncorrelated and correlated blobs, for a
 * single camera. Upon a blob update (via updateBlobs()), correlation and
 * filtering takes place automatically.
 * </p>
 * 
 * @author Joel R. Becker <br>
 *         8/10/09
 */
public class BlobManager {
	private List<Blob> uncorrelatedBlobList;
	private List<Blob> correlatedBlobList;
	private Dimension videoSize;
	private double blobAgeOfAcceptance;
	private double maxBlobMissingTime;
	private BlobCorrelator correlator;
	private List<BlobManagerListener> listeners;
	
	public BlobManager() {
		correlator = new WeightedBlobCorrelator();
		uncorrelatedBlobList = new ArrayList<Blob>();
		correlatedBlobList = new ArrayList<Blob>();
		videoSize = new Dimension(0, 0);
		blobAgeOfAcceptance = 0.333;
		maxBlobMissingTime = 1.0;
		listeners = new Vector<BlobManagerListener>();
	}
	
	/**
	 * Returns a list of the correlated blobs (which have reached the age of acceptance).
	 * @return a list of the correlated blobs.
	 */
	public List<Blob> getBlobList()
	{
		//return new ArrayList<Blob>(correlatedBlobList);
		double now = System.nanoTime() / 1.0e9;
		ArrayList<Blob> list = new ArrayList<Blob>();
		Iterator<Blob> iter = correlatedBlobList.iterator();
		while (iter.hasNext()) {
			Blob blob = iter.next();
			if (now - blob.timeCreated > blobAgeOfAcceptance) {
			// && blob.timeMissing <= 0.0) {
				list.add(blob);
			}
		}
		return list;
	}
	
	/**
	 * Returns a list of the uncorrelated (per-frame) blobs.
	 * @return a list of the uncorrelated (per-frame) blobs.
	 */
	public List<Blob> getUncorrelatedBlobList() {
		return new ArrayList<Blob>(uncorrelatedBlobList);
	}
	
	public Dimension getVideoSize() {
		return videoSize;
	}
	
	public void setVideoSize(Dimension size) {
		videoSize.width = size.width;
		videoSize.height = size.height;
	}
	
	/**
	 * Updates the blobs with the given newly-detected blobs, correlating the
	 * new blobs to the previous ones, and also filtering blobs.
	 */
	public void updateBlobs(List<Blob> blobUpdates) {
		
		uncorrelatedBlobList = blobUpdates;
		correlatedBlobList = correlator.correlate(blobUpdates, correlatedBlobList);
		// TODO notify listeners of new blobs
		removeOldBlobs();
		
		//// Notify listeners of update
		for (BlobManagerListener l : listeners)
			l.blobsUpdated();
	}
	
	/**
	 * @return the blobAgeOfAcceptance
	 */
	public double getBlobAgeOfAcceptance() {
		return blobAgeOfAcceptance;
	}

	/**
	 * @param blobAgeOfAcceptance the blobAgeOfAcceptance to set
	 */
	public void setBlobAgeOfAcceptance(double blobAgeOfAcceptance) {
		this.blobAgeOfAcceptance = blobAgeOfAcceptance;
	}

	/**
	 * @return the maxBlobMissingTime
	 */
	public double getMaxBlobMissingTime() {
		return maxBlobMissingTime;
	}

	/**
	 * @param maxBlobMissingTime the maxBlobMissingTime to set
	 */
	public void setMaxBlobMissingTime(double maxBlobMissingTime) {
		this.maxBlobMissingTime = maxBlobMissingTime;
	}

	public void addListener(BlobManagerListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(BlobManagerListener listener) {
		listeners.remove(listener);
	}
	
	private void removeOldBlobs() {
		Iterator<Blob> iter = correlatedBlobList.iterator();
		while (iter.hasNext()) {
			Blob blob = iter.next();
			if (blob.timeMissing > maxBlobMissingTime) {
				iter.remove();
				
				//// Notify listeners of blob loss
				for (BlobManagerListener l : listeners)
					l.blobLost(blob);
			}
		}
	}
}
