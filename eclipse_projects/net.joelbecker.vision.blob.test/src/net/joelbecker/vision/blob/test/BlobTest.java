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
package net.joelbecker.vision.blob.test;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.media.CaptureDeviceInfo;
import javax.media.CaptureDeviceManager;
import javax.media.Format;
import javax.media.MediaLocator;
import javax.swing.JFrame;

import net.joelbecker.vision.blob.BlobPublishingCamera;
import net.joelbecker.vision.blob.ui.swing.BlobPublishingCameraPanel;

/**
 * 
 * file:D:\large\video\AVI\CIMG2171_xvid.avi
 * 
 * @author Joel
 *
 */
public class BlobTest  extends JFrame {
	/** Serialize id. */
	private static final long serialVersionUID = 6332743548963362538L;
	
	BlobPublishingCamera camera;
	BlobPublishingCameraPanel cameraPanel;
	
	public BlobTest(String mediaLoc) {
		listDevices();
		camera = new BlobPublishingCamera();
		if(mediaLoc == null) {
			if(!autoOpen()) {
				System.out.println("Could not find a connected camera.");
				System.exit(1);
			}
		} else {
			if (!setURL(mediaLoc)) {
				System.out.println("Could not open " + mediaLoc);
				System.exit(2);
			}
		}
		cameraPanel = new BlobPublishingCameraPanel(camera);
		setContentPane(cameraPanel);
		setSize(800, 600);
		setTitle("Joel Becker's Video Motion Tracker");
		setVisible(true);
	}
	
	
	void start() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				camera.close();
				System.exit(0);
			}			
		});
		cameraPanel.switchFeed(0);
	}
	
	void listDevices() {
		Vector deviceInfos = CaptureDeviceManager.getDeviceList(null);
		if (deviceInfos.isEmpty()) {
			System.out.println("No capture devices found.");
		} else {
			for (Object o : deviceInfos) {
				CaptureDeviceInfo info = (CaptureDeviceInfo) o;
				System.out.println(info.getName() + " :");
				for (Format format : info.getFormats()) {
					System.out.println(String.format("\t%s", format.toString()));
				}
			}
		}
	}
	
	private boolean setURL(String url) {
		MediaLocator ml;

		if ((ml = new MediaLocator(url)) == null) {
			System.err.println("Cannot build media locator from: " + url);
			System.exit(0);
		}

		//FrameAccess fa = new FrameAccess();

		return camera.open(ml);
	}

	private boolean autoOpen() {
		boolean success = false;
		int camNum = 0;
		String mediaLoc;
		do {
			mediaLoc = "vfw://" + camNum;
			System.out.println("Trying video for windows, camera #" + camNum + ": " + mediaLoc);
			success = setURL(mediaLoc);
		} while (!success && (++camNum) < 4);
		return success;
	}
	
	/**
	 * Main program
	 */
	public static void main(String[] args) {
		String url;
		if (args.length > 0) {
			url = args[0];
		} else {
			url = null;
			System.err.println("Usage: java BlobTest <url>");
		}
		
		BlobTest frame = new BlobTest(url);
		frame.start();
	}
}
