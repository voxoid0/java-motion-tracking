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
package net.joelbecker.util.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
	private StringUtil() {}
	
	/**
	 * Returns the index into the given string where the first non-letter is found.
	 * @param str
	 * @return the index into the given string where the first non-letter is found. If
	 * no non-letters were found, the index will equal the length of the string.
	 */
	public static int indexOfFirstNonLetter(String str, int startIndex) {
		if (str == null) {
			throw new NullPointerException();
		}
		int index = startIndex;
		while (index < str.length() && Character.isLetter(str.charAt(index)))
				index++;
		return index;
	}
	
	/**
	 * Returns the index into the given string where the first non-letter is found.
	 * @param str
	 * @return the index into the given string where the first non-letter is found. If
	 * no non-letters were found, the index will equal the length of the string.
	 */
	public static int indexOfFirstLetter(String str, int startIndex) {
		Pattern patt = Pattern.compile("[a-zA-Z]");
		Matcher matcher = patt.matcher(str);
		if (matcher.find(startIndex)) {
			return matcher.start();
		} else {
			return str.length();
		}
	}
	
	public static String removeWhitespace(String str) {
		str.replaceAll("\\s", "");
		return str;
	}
}
