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
package net.joelbecker.video.processing.ui.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import net.joelbecker.video.processing.RgbVideoEffect;
import net.joelbecker.video.processing.VideoFrameListener;

public class VideoPanel extends JPanel
		implements VideoFrameListener {

	/** The current video input. */
	RgbVideoEffect curEffect;
	
	/**
	 * Sets the video input for this VideoPanel.
	 * @param effect The {@link RgbVideoEffect} to display, or null to display none.
	 */
	public void setInput(RgbVideoEffect effect) {
		if (curEffect != null) {
			curEffect.removeVideoFrameListener(this);
		}
		if (effect == null) {
			getGraphics().setColor(Color.BLUE);
			getGraphics().fillRect(0, 0, getWidth(), getHeight());
		} else {
			// if video width > 0 {
			effect.addVideoFrameListener(this);
			curEffect = effect;
		}
	}
	

	/** {@inheritDoc} */
	@Override
	public void newVideoFrame(BufferedImage image) {
		Graphics gr = getGraphics();
		if (gr != null) {
			int pw = getWidth();
			int ph = getHeight();
			int iw = image.getWidth();
			int ih = image.getHeight();
			int x, y;
			if(ph != 0 && ih != 0) {
				float pratio = (float)pw / (float)ph;
				float iratio = (float)iw / (float)ih;
				
				//// If panel is proportionally wider than image, scale according to height
				if (pratio > iratio) {
					iw = iw * ph / ih; 
					ih = ph;
					x = (pw - iw) / 2;
					y = 0;
				} else {
					ih = ih * pw / iw;
					iw = pw;
					y = (ph - ih) / 2;
					x = 0;
				}
				gr.drawImage(image, x, y, iw, ih, null);
			}
		}
	}

}
