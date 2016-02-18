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
package net.joelbecker.vision.blob.ui.swing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.List;

import com.sun.imageio.plugins.common.ImageUtil;

import net.joelbecker.video.processing.ui.swing.VideoPanel;
import net.joelbecker.vision.blob.Blob;
import net.joelbecker.vision.blob.BlobPublishingCamera;

public class BlobVideoPanel extends VideoPanel {

	private static final Color DEFAULT_UNCORRELATED_BLOB_COLOR = Color.BLUE;

	private static final Color DEFAULT_BLOB_COLOR = Color.RED;

	private static final Color DEFAULT_HIDDEN_BLOB_COLOR = Color.YELLOW;
	
	private static final Font DEFAULT_BLOB_LABEL_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 10); 

	/** The source {@link BlobPublishingCamera}. */
	private BlobPublishingCamera camera;
	
	private Color uncorrelatedBlobColor = DEFAULT_UNCORRELATED_BLOB_COLOR;
	
	private Color blobColor = DEFAULT_BLOB_COLOR;
	
	private Color hiddenBlobColor = DEFAULT_HIDDEN_BLOB_COLOR;
	
	private Font blobLabelFont = DEFAULT_BLOB_LABEL_FONT;
	
	private boolean drawBlobs = true;
	
	private boolean flipVideo = false;
	
	
	public BlobVideoPanel(BlobPublishingCamera camera) {
		if (camera == null) {
			throw new NullPointerException();
		}
		this.camera = camera;
	}
	/*
	private Rectangle2D.Double toImageCoord(Rectangle2D.Double)) {
		
	}*/
	
	public void setDrawBlobs(boolean drawBlobs) {
		this.drawBlobs = drawBlobs;
	}
	
	public void setFlipVideo(boolean flip) {
		this.flipVideo = flip;
	}
	
	/**
	 * Overridden to draw blob rectangles and labels into video frame before displaying.
	 */
	@Override
	public void newVideoFrame(BufferedImage image) {
		
		if (drawBlobs) {
			Graphics2D gr = (Graphics2D) image.getGraphics();
			
			gr.setFont(blobLabelFont);
			
			//// Draw uncorrelated blobs
			List<Blob> ublobs = camera.getBlobManager().getUncorrelatedBlobList();
			gr.setColor(uncorrelatedBlobColor);
			gr.setPaint(uncorrelatedBlobColor);
			for (Blob b : ublobs) {
				Rectangle bounds = b.bounds;
				gr.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
				//gr.drawString(Integer.toString(b.frameLabel), bounds.x, bounds.y + 10);
			}
			
			//// Draw correlated (possibly hidden) blobs
			List<Blob> cblobs = camera.getBlobManager().getBlobList();
			for (Blob b : cblobs) {
				Rectangle bounds = b.bounds;
				if (b.timeMissing > 0.0) {
					gr.setColor(hiddenBlobColor);
					//gr.setPaint(uncorrelatedBlobColor);
				} else {
					gr.setColor(blobColor);
					//gr.setPaint(uncorrelatedBlobColor);
				}
				gr.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
				gr.drawString(Integer.toString(b.label), bounds.x, bounds.y - 1);
			}
		}
		
		if (flipVideo) {
			
			//// Flip vertically
			BufferedImage cpy = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
			AffineTransform xform = new AffineTransform();
			xform.scale(1.0, -1.0);
			xform.translate(0.0, -image.getHeight());
			((Graphics2D) cpy.getGraphics()).drawImage(image, xform, null);
			image = cpy;
		}
		
		super.newVideoFrame(image);
	}

}
