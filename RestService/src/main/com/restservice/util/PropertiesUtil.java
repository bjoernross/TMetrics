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
package com.restservice.util;

/**
 * @author olaf
 * 
 */
public class PropertiesUtil {
	
	/**
	 * (implode) reduces one dimensional array to CommaSeperatedValues-string
	 * @param array object array (one dimensional)
	 * @return CSV string
	 */
	public static String serializeArray1DimToCSV(Object[] array) {
		String result = "";
		for (int i = 0; i < array.length; i++) {
			result += ((i!=0)?",":"") + array[i].toString();
		}
		return result;
	}

	/**
	 * (implode) reduces two dimensional array to CommaSeperatedValues-string
	 * @param array object array (two dimensional)
	 * @return CSV string
	 */
	public static String serializeArray2DimToCSV(Object[][] array) {
		String result = "";
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[i].length; j++) {
				result += ((i!=0 || j!=0)?((j==0)?";":","):"") + array[i][j].toString();
			}
		}
		return result;
	}

	/**
	 * (explode) parses CommaSeperatedValues-string to one dimensional string array
	 * @param s CSV String
	 * @return array string array (one dimensional)
	 */
	public static String[] unserializeCVSToStringArray1Dim(String s) {
		if (s == null || s == "") return new String[0];
		String parts[] = s.split(",");
		return parts;
	}

	/**
	 * (explode) parses CommaSeperatedValues-string to two dimensional string array
	 * @param s CSV String
	 * @return array string array (two dimensional)
	 */
	public static String[][] unserializeCVSToStringArray2Dim(String s) {
		if (s == null || s == "") return new String[0][0];
		String parts[] = s.split(";");
		String result[][] = new String[parts.length][parts[0].split(",").length];
		for (int i = 0; i < parts.length; i++) {
			result[i] = parts[i].split(",");
		}
		return result;
	}
}
