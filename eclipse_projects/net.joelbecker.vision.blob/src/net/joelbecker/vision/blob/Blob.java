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


import java.awt.Rectangle;
import java.awt.geom.Point2D;

public class Blob {
	public static final int NOT_CORRELATED = -1;
	public int label;
	public int frameLabel;
	public int frameNumber;
	public Rectangle bounds;
	public int pixelCount;
	//public Blob previous;		// previous Blob that this one corresponds to
	public double timeCreated;	// timestamp in seconds
	public double timeLastUpdated;			// age in seconds
	public double timeMissing;	// total time that the blob has been missing (0 if not missing)
	public Point2D.Double velocity;	// pixels per second
	
	/**
	 * Constructor for a new Blob that is not correlated with a previous one.
	 * @param label
	 * @param bounds
	 */
	public Blob(int frameLabel, Rectangle bounds) {
		this.label = NOT_CORRELATED;
		this.frameLabel = frameLabel;
		this.bounds = bounds;
		//this.pixelCount = pixelCount;
		this.velocity = new Point2D.Double(0.0, 0.0);
		this.timeCreated = (double) System.nanoTime() / 1000000000.0;
		this.timeLastUpdated = this.timeCreated;
		this.timeMissing = 0.0;
	}
}
