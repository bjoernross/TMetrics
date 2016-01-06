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

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author Torsten
 */
public class MapUtilTest {
	private static Map<Integer, List<String>> _testMap = null;

	private static final Integer ONE = new Integer(1);
	private static final Integer TWO = new Integer(2);
	private static final Integer THREE = new Integer(3);
	
	@BeforeClass
	public static void prepare() {
		_testMap = new HashMap<Integer, List<String>>();

		List<String> list1 = new LinkedList<String>();
		list1.add("a1");
		list1.add("a2");
		list1.add("a3");
		list1.add("a4");
		list1.add("a5");
		list1.add("a6");
		list1.add("a7");
		List<String> list2 = new LinkedList<String>();
		list1.add("b1");
		list1.add("b2");
		list1.add("b3");
		list1.add("b4");
		list1.add("b5");
		list1.add("b6");
		list1.add("b7");
		List<String> list3 = new LinkedList<String>();
		list1.add("c1");
		list1.add("c2");
		list1.add("c3");
		list1.add("c4");
		list1.add("c5");
		list1.add("c6");

		_testMap.put(ONE, list1);
		_testMap.put(TWO, list2);
		_testMap.put(THREE, list3);
	}
	
	@Test
	public void testFillupMap() {
		Map<Integer, List<String>> testMap = new HashMap<Integer, List<String>>();

		List<String> list1 = new LinkedList<String>();
		list1.add("t1");
		list1.add("t2");
		list1.add("t3");
		List<String> list2 = new LinkedList<String>();
		list1.add("s1");

		testMap.put(ONE, list1);
		testMap.put(TWO, list2);
		
		MapUtil.fillupMap(testMap, _testMap);
		
		
	}
}
