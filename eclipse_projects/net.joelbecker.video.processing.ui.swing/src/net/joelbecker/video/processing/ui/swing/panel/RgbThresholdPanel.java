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
package net.joelbecker.video.processing.ui.swing.panel;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.joelbecker.video.processing.RgbThresholdEffect;
import net.joelbecker.video.processing.ui.swing.VideoEffectUIFactory;

/**
 * JPanel for {@link RgbThresholdEffect}.
 * @author Joel R. Becker
 *
 */
public class RgbThresholdPanel extends JPanel implements ChangeListener {
	private RgbThresholdEffect effect;
	private JSlider sldThreshold;
	private JLabel lblThreshold;
	
	public RgbThresholdPanel(RgbThresholdEffect effect) {
		this.effect = effect;
		setLayout(new BorderLayout());
		sldThreshold = new JSlider(0, 255);
		add(sldThreshold, BorderLayout.CENTER);
		sldThreshold.addChangeListener(this);
		lblThreshold = new JLabel();
		
		update();
	}

	public void update() {
		sldThreshold.setValue(effect.getThreshold());
		lblThreshold.setText(Character.toString(effect.getThreshold()));
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if(e.getSource() == sldThreshold) {
			effect.setThreshold((char) sldThreshold.getValue());
			update();
		}
	}
}
