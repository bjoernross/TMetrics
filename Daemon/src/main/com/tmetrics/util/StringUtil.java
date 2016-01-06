/*******************************************************************************
 * This file is part of Tmetrics.
 *
 * Tmetrics is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Tmetrics is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Tmetrics. If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.tmetrics.util;

/**
 * A utility class offering functionality for string operations.
 * @author Torsten
 */
public class StringUtil {
	
	/**
	 * Escapes unicode charaters to their ASCII readable unicode code.
	 * @param str The string to be escaped.
	 * @return Returns the escaped string.
	 */
	public static String escapeNonAscii(String str) {
		StringBuilder retStr = new StringBuilder();
		for(int i=0; i<str.length(); i++) {
		    int cp = Character.codePointAt(str, i);
		    int charCount = Character.charCount(cp);
		    if (charCount > 1) {
		    	i += charCount - 1;
				if (i >= str.length()) {
					throw new IllegalArgumentException("truncated unexpectedly");
				}
		    }

		    if (cp < 128) {
		    	retStr.appendCodePoint(cp);
		    } else {
		    	retStr.append(String.format("\\u%x", cp));
		    }
		}
		return retStr.toString();
	}
}
