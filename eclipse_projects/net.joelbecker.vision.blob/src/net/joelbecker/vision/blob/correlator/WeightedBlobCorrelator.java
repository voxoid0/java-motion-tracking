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

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import net.joelbecker.vision.blob.Blob;

public class WeightedBlobCorrelator implements BlobCorrelator {

	private static final double NO_CONFIDENCE = -Double.MAX_VALUE;
	private double minCorrelationConfidence = NO_CONFIDENCE;
	private int nextLabel = 1;
	private double prevNow = 0.0;
	
	public enum Info {
		POSITION, SIZE, VELOCITY
	};
	
	/** Weight of correlation relevance for each type of information (see {@link Info}). */
	private double infoWeight[];
	
	/**
	 * Confidence of correlation between each pair of new and old blob, from the
	 * previous correlation operation (see correlate()).
	 */
	private double confidence[][];
	private int hcNew;
	private int hcOld;

	/** Constructor. */
	public WeightedBlobCorrelator() {
		infoWeight = new double[] {
			1, 1, 10
		};
	}
	
	/** {@inheritDoc} */
	@Override
	public List<Blob> correlate(List<Blob> newBlobs, List<Blob> oldBlobs) {
		List<Blob> correlated;
		double now = (double) System.nanoTime() / 1000000000.0;
		double timeSinceLastUpdate = prevNow == 0.0 ? 0.0 : now - prevNow;
		confidence = new double[newBlobs.size()][oldBlobs.size()];
		
		//// Set new blobs' update time to now, since we're "updating" them, and so we can have a consistent timestamp for all of them.
		for (Blob newBlob : newBlobs) {
			newBlob.timeLastUpdated = now;
		}
		
		//// Calculate the confidence of correlation between each pair of new/old blobs.
		calcAllConfidences(newBlobs, oldBlobs);
		
		//// Find best correlations
		int maxCorrelations = Math.max(newBlobs.size(), oldBlobs.size());
		if (newBlobs.size() > oldBlobs.size()) {
			maxCorrelations = oldBlobs.size();
			correlated = new ArrayList<Blob>(newBlobs);
		} else {
			maxCorrelations = newBlobs.size();
			correlated = new ArrayList<Blob>(oldBlobs);
		}
		int nCorrelations = 0;
		double highestConfidence;
		do {
			highestConfidence = findHighestConfidence(newBlobs, oldBlobs);
			
			//// If the correlation is good enough, carry it through
			if (highestConfidence > minCorrelationConfidence) {
				
				//// Correlate them
				Blob correlatedBlob = correlate(newBlobs.get(hcNew), oldBlobs.get(hcOld));
				int index = newBlobs.size() > oldBlobs.size() ? hcNew : hcOld;
				correlated.set(index, correlatedBlob);
				++nCorrelations;
				
				//// Clear their confidence mappings (so the next-highest confidence may be found, without repeating any blobs
				for (int n = 0; n < newBlobs.size(); n++) {
					confidence[n][hcOld] = NO_CONFIDENCE;
				}
				for (int o = 0; o < oldBlobs.size(); o++) {
					confidence[hcNew][o] = NO_CONFIDENCE;
				}
			}
		} while (nCorrelations < maxCorrelations && highestConfidence > minCorrelationConfidence);
		
		if (oldBlobs.size() > newBlobs.size()) {
			
			//// For each of the blobs that didn't have a corresponding new one, update its missing time 
			for (Blob blob : correlated) {
				if (blob.timeLastUpdated != now) {
					blob.timeMissing += timeSinceLastUpdate;
				}
			}
		} else {
			
			//// Assign unused labels to new ones that were not correlated to old ones (new ones whose age is 0)
			for (Blob blob : correlated) {
				if (blob.timeLastUpdated == now) {
					blob.label = nextLabel++;
				}
			}
		}
		
		prevNow = now;
		return correlated;
	}

	private double findHighestConfidence(List<Blob> newBlobs,
			List<Blob> oldBlobs) {
		double highestConfidence;
		highestConfidence = NO_CONFIDENCE;
		for (int n = 0; n < newBlobs.size(); n++) {
			for (int o = 0; o < oldBlobs.size(); o++) {
				if (confidence[n][o] > highestConfidence) {
					highestConfidence = confidence[n][o];
					hcNew = n;
					hcOld = o;
				}
			}
		}
		return highestConfidence;
	}

	private void calcAllConfidences(List<Blob> newBlobs, List<Blob> oldBlobs) {
		int n = 0;
		for (Blob newBlob : newBlobs) {
			int o = 0;
			for (Blob oldBlob : oldBlobs) {
				confidence[n][o] = calculateConfidence(newBlob, oldBlob);
				++o;
			}
			++n;
		}
	}

	private Blob correlate(Blob newBlob, Blob oldBlob) {
		oldBlob.bounds = newBlob.bounds;
		oldBlob.velocity = new Point2D.Double(
				newBlob.bounds.getCenterX() - oldBlob.bounds.getCenterX(),
				newBlob.bounds.getCenterY() - oldBlob.bounds.getCenterY());
		oldBlob.timeMissing = 0.0;
		oldBlob.frameLabel = newBlob.frameLabel;
		return oldBlob;
	}
	
	private double calculateConfidence(Blob newBlob, Blob oldBlob) {
		double predictedX = oldBlob.bounds.getCenterX() + oldBlob.velocity.x;
		double predictedY = oldBlob.bounds.getCenterY() + oldBlob.velocity.y;
			// TODO make Y fraction of importance of X (since objects tend to move more horizontally in camera's view typically)
		double dx = newBlob.bounds.getCenterX() - predictedX;
		double dy = newBlob.bounds.getCenterY() - predictedY;
		double deltaDist = Math.sqrt(dx*dx + dy*dy);	// leave distance squared?
		
		double dw = Math.abs(newBlob.bounds.getWidth() - oldBlob.bounds.getWidth());
		double dh = Math.abs(newBlob.bounds.getHeight() - oldBlob.bounds.getHeight());
		
		double c = 
			infoWeight[Info.POSITION.ordinal()] * deltaDist +
			infoWeight[Info.SIZE.ordinal()] * (dw + dh);
		return -c;
	}
}
