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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import net.joelbecker.video.processing.RgbVideoEffect;
import net.joelbecker.video.processing.ui.swing.VideoEffectUIFactory;
import net.joelbecker.video.processing.ui.swing.VideoPanel;
import net.joelbecker.vision.blob.BlobPublishingCamera;
import net.joelbecker.vision.blob.detector.FourNeighborBlobDetector;

public class BlobPublishingCameraPanel extends JPanel {
	
	/** Serialization id. */
	private static final long serialVersionUID = 1789835098650851831L;

	/** The {@link BlobPublishingCamera} that this JPanel interacts with. */
	private BlobPublishingCamera camera;
	
	/** Index of the effect chain element being watched. */ 
	private int curFeedIndex;
	
	//private JComboBox cmbEffect;
	private JRadioButton btnVideoInput[];
	
	private BlobVideoPanel pnlVideo;

	public BlobPublishingCameraPanel(BlobPublishingCamera camera) {
		if (camera == null) {
			throw new NullPointerException("camera");
		}
		if (camera.getProcessingChain() == null) {
			throw new IllegalStateException("Camera must already be opened before UI may be created.");
		}
		
		// Register our effects' UIs
		VideoEffectUIFactory.getInstance().registerClass(
				FourNeighborBlobDetector.class, FourNeighborBlobDetectorPanel.class);

		//
		this.camera = camera;
		createUI();
	}
	
	private void createUI() {
		// Display the visual & control component if there's one.
		setLayout(new BorderLayout());

		//////// Left
		JPanel pnlLeft = new JPanel();
		pnlLeft.setLayout(new BorderLayout());
		
		pnlVideo = new BlobVideoPanel(camera);
		pnlLeft.add("Center", pnlVideo);
		
		JCheckBox chkFlip = new JCheckBox("Flip video vertically");
		chkFlip.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				pnlVideo.setFlipVideo(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		pnlLeft.add("South", chkFlip);
		
		add("Center", pnlLeft);
		
		//////// Right
		JPanel pnlRight = new JPanel();
		GridBagLayout gbLayout = new GridBagLayout();
		pnlRight.setLayout(gbLayout);

		ButtonGroup btngrpInput = new ButtonGroup();
		btnVideoInput = new JRadioButton[camera.getProcessingChain().length];
		JPanel pnlInput = new JPanel();
		pnlInput.setBorder(new TitledBorder(new EtchedBorder(), "Video View"));
		GridLayout inputLayout = new GridLayout(0, 1); //camera.getProcessingChain().length);
		pnlInput.setLayout(inputLayout);
		for (int i = 0; i < camera.getProcessingChain().length; i++) {
			btnVideoInput[i] = new JRadioButton(Integer.toString(i+1) + " " +
					camera.getProcessingChain()[i].getName());
			btngrpInput.add(btnVideoInput[i]);
			pnlInput.add(btnVideoInput[i]);

			final int index = i;
			btnVideoInput[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					//if (btnVideoInput[index])
					switchFeed(index); //effectIndex);
				}
			});
		}

		GridBagConstraints constr = new GridBagConstraints();
		constr.gridwidth = 1;
		constr.gridheight = 1;
		constr.fill = GridBagConstraints.BOTH;
		constr.anchor = GridBagConstraints.NORTH;
		constr.gridwidth = GridBagConstraints.REMAINDER; // end row
		gbLayout.setConstraints(pnlInput, constr);
		pnlRight.add(pnlInput);
		//add(pnlInput, "South");
		
		//// Processing Chain UI
		//pnlRight.setLayout(new B)
		int i = 0;
		for (RgbVideoEffect effect : camera.getProcessingChain()) {
			
			//effect.createUI(pnl);
			JPanel ui = VideoEffectUIFactory.getInstance().provideFor(effect);
			if (ui != null) {
				JPanel pnl = new JPanel();
				pnl.setBorder(new TitledBorder(new EtchedBorder(), effect.getName()));
				pnl.add(ui);
				
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridwidth = 1;
				gbc.gridheight = 1;
				gbc.fill = GridBagConstraints.BOTH;
				gbc.anchor = GridBagConstraints.NORTH;
				gbc.gridwidth = GridBagConstraints.REMAINDER; // end row
				gbLayout.setConstraints(pnl, gbc);

				pnlRight.add(pnl);
				pnl.addMouseListener(new EffectPanelMouseListener(i));				
			}
			
			i++;
		}
		add("West", pnlRight);
	}

	public void addNotify() {
		super.addNotify();
		//pack();
		this.getParent().validate();
	}

	/*
	@Override
	public void itemStateChanged(ItemEvent e) {
		if(e.getSource() == cmbEffect) {
			int effectIndex = Integer.parseInt(
					((String)e.getItem()).substring(0, 2).trim()) - 1;
			if(e.getStateChange() == ItemEvent.DESELECTED) {
				//camera.getProcessingChain()[effectIndex].removeVideoFrameListener(this);
			} else if(e.getStateChange() == ItemEvent.SELECTED) {
				switchFeed(effectIndex);
			}
		}
	}*/

	/**
	 * Switches the video input to an effect in the camera's processing chain.
	 * @param index Index into the processing chain.
	 */
	public void switchFeed(int index) {
		if(camera.getBlobManager().getVideoSize().width > 0) {
			pnlVideo.setInput(camera.getProcessingChain()[index]);
			curFeedIndex = index;
		}
	}
	
	/**
	 * Listens to mouse events.
	 */
	private class EffectPanelMouseListener implements MouseListener {
		private int effectIndex;
		public EffectPanelMouseListener(int effectIndex) {
			this.effectIndex = effectIndex;	
		}
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() > 1) {
				switchFeed(effectIndex);
			}
		}
		public void mouseEntered(MouseEvent e) {
		}
		public void mouseExited(MouseEvent e) {
		}
		public void mousePressed(MouseEvent e) {
		}
		public void mouseReleased(MouseEvent e) {
		}		
	}
}
