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

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import net.joelbecker.vision.blob.detector.FourNeighborBlobDetector;

public class FourNeighborBlobDetectorPanel extends JPanel {
	
	private FourNeighborBlobDetector effect;
	//private JSlider sldMinBlobSize;
	//private JSlider sldMaxBlobSize;
	private JTextField txtMinBlobSize;
	private JTextField txtMaxBlobSize;
	
	public FourNeighborBlobDetectorPanel(FourNeighborBlobDetector effect) {
		this.effect = effect;
		
		GridLayout gridLayout = new GridLayout(0, 1);
		setLayout(gridLayout);
		
		JPanel pnl = new JPanel();
		pnl.setBorder(new TitledBorder(new EtchedBorder(), "Min. Blob Size"));
		pnl.setLayout(new BorderLayout());
		txtMinBlobSize = new JTextField(14);
		pnl.add("Center", txtMinBlobSize);
		add(pnl);
		txtMinBlobSize.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent event) {
				try {
					FourNeighborBlobDetectorPanel.this.effect.setMinBlobSize(Integer.parseInt(
							((JTextField) (event.getSource())).getText()));
				} catch (NumberFormatException e) {
					// do nothing
				}
				super.focusLost(event);
			}
		});
		
		JPanel pnl2 = new JPanel();
		pnl2.setBorder(new TitledBorder(new EtchedBorder(), "Max. Blob Size"));
		pnl2.setLayout(new BorderLayout());
		txtMaxBlobSize = new JTextField(14);
		pnl2.add("Center", txtMaxBlobSize);
		add(pnl2);
		txtMaxBlobSize.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent event) {
				try {
					FourNeighborBlobDetectorPanel.this.effect.setMaxBlobSize(Integer.parseInt(
							((JTextField) (event.getSource())).getText()));
				} catch (NumberFormatException e) {
					// do nothing
				}
				super.focusLost(event);
			}
		});
		
		update();
	}
	
	public void update() {
		txtMinBlobSize.setText(Integer.toString(effect.getMinBlobSize()));
		txtMaxBlobSize.setText(Integer.toString(effect.getMaxBlobSize()));
	}
}
