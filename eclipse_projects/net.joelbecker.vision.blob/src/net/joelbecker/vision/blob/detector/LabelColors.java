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
package net.joelbecker.vision.blob.detector;

import java.awt.Color;

public class LabelColors {
	static public final byte[] REDS;
	static public final byte[] GREENS;
	static public final byte[] BLUES;
	static public final Color[] COLORS;
	
	static {
		REDS = new byte[256];
		GREENS = new byte[256];
		BLUES = new byte[256];
		COLORS = new Color[] {
				Color.black, 	// 0 == NOT A LABEL
				Color.red,		// 1
				Color.blue,		// 2
				Color.yellow,	// 3
				Color.green,	// 4
				Color.cyan,		// 5
				Color.magenta,	// 6
				Color.orange	// 7
		};
		int i;
		for (i = 0; i < COLORS.length; i++) {
			REDS[i] = (byte) COLORS[i].getRed();
			GREENS[i] = (byte) COLORS[i].getGreen();
			BLUES[i] = (byte) COLORS[i].getBlue();
		}
		for(; i < 256; i++) {
			REDS[i] = GREENS[i] = BLUES[i] = (byte) 255;
		}
	}
}
