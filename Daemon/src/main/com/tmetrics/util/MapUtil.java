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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Helper class to provide methods for lists.
 * 
 * @author Torsten
 */
public class MapUtil {
	/**
	 * Adds the list items of filler to the map toBeFilled. Note: The search terms in filler do have
	 * to be already existing in toBeFilled.
	 * @param <T> Type of the content in the list.
	 * @param <U> Type of the key.
	 * @param toBeFilled The map to be filled.
	 * @param filler The map containing the list items to fill.
	 */
	public static <T, U> void fillupMap(Map<U, List<T>> toBeFilled, Map<U, List<T>> filler) {
		for (Map.Entry<U, List<T>> entry : filler.entrySet()) {
			// If the key does not already exist in toBeFilled, create it
			if (toBeFilled.get(entry.getKey()) == null) {
				toBeFilled.put(entry.getKey(), new LinkedList<T>());
			}
			
			// Fill it up!
			toBeFilled.get(entry.getKey()).addAll(entry.getValue());
		}
	}
	
	/**
	 * Clones the given map (flat).
	 * @param <T> Type of the content in the list.
	 * @param <U> Type of the key.
	 * @param toBeCloned The map to be cloned.
	 * @return A cloned copy of the map.
	 */
	public static <T, U> Map<U, List<T>> flatClone(Map<U, List<T>> toBeCloned) {
		Map<U, List<T>> cloned = new HashMap<U, List<T>>();
		
		fillupMap(cloned, toBeCloned);
		
		return cloned;
	}
	
	/**
	 * Count the number of items contained within ALL lists in the map.
	 * @param <T> Type of the content in the list.
	 * @param <U> Type of the key.
	 * @param map The map containing the list items to be counted.
	 * @return The number of list items.
	 */
	public static <T, U> int countMapSize(Map<U, List<T>> map) {
		int num = 0;
		
		for (Map.Entry<U, List<T>> entry : map.entrySet()) {
			num += entry.getValue().size();
		}
		
		return num;
	}
}
