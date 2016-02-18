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

import javax.swing.JPanel;

import net.joelbecker.util.pattern.ClassMappingFactory;
import net.joelbecker.video.processing.BackgroundUpdater;
import net.joelbecker.video.processing.PixelizationDialate8Bit;
import net.joelbecker.video.processing.RgbThresholdEffect;
import net.joelbecker.video.processing.RgbVideoEffect;
import net.joelbecker.video.processing.ui.swing.panel.BackgroundUpdaterPanel;
import net.joelbecker.video.processing.ui.swing.panel.PixelizationDialate8BitPanel;
import net.joelbecker.video.processing.ui.swing.panel.RgbThresholdPanel;

/**
 * Singleton that creates JPanel sub-classes for {@link RgbVideoEffect} sub-classes.
 * @author Joel R. Becker
 * <br>8/7/09
 */
public class VideoEffectUIFactory extends ClassMappingFactory<RgbVideoEffect, JPanel> {
	
	/** The singleton instance. */
	private static final VideoEffectUIFactory INSTANCE = new VideoEffectUIFactory();
	
	/** Constructor. */
	private VideoEffectUIFactory() {
		//// Register the ones we know of
		registerClass(RgbThresholdEffect.class, RgbThresholdPanel.class);
		registerClass(BackgroundUpdater.class, BackgroundUpdaterPanel.class);
		registerClass(PixelizationDialate8Bit.class, PixelizationDialate8BitPanel.class);
	}
	
	/**
	 * Returns the singleton instance.
	 * @return The singleton instance.
	 */
	public static VideoEffectUIFactory getInstance() {
		return INSTANCE;
	}
}
