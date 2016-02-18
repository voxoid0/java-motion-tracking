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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import net.joelbecker.video.processing.BackgroundUpdater;
import net.joelbecker.video.processing.ui.swing.VideoEffectUIFactory;

public class BackgroundUpdaterPanel extends JPanel implements ActionListener {
	private BackgroundUpdater effect;
	private JButton btnReset;

	public BackgroundUpdaterPanel(BackgroundUpdater effect) {
		this.effect = effect;
		
		setLayout(new BorderLayout());
		btnReset = new JButton("Recapture Background");
		add(btnReset, BorderLayout.NORTH);
		btnReset.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		effect.resetBackground();
	}
}
