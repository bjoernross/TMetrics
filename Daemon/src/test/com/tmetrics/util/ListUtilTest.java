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

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class ListUtilTest {

	@Test
	public final void testSubtract() {
		List<Float> a = Arrays.asList((float) 1, (float) 2, (float) 3, (float) 4);
		List<Float> b = Arrays.asList((float) 1, (float) 1, (float) 1, (float) 1);
		List<Float> c = ListUtil.subtract(a, b);
		assertEquals(0, c.get(0), 0.01);
		assertEquals(1, c.get(1), 0.01);
		assertEquals(2, c.get(2), 0.01);
		assertEquals(3, c.get(3), 0.01);
	}

	@Test
	public final void testMultiply() {
		List<Float> a = Arrays.asList((float) 1, (float) 2, (float) 3, (float) 4);
		float m = 2;
		List<Float> r = ListUtil.multiply(a, m);
		assertEquals(2, r.get(0), 0.01);
		assertEquals(4, r.get(1), 0.01);
		assertEquals(6, r.get(2), 0.01);
		assertEquals(8, r.get(3), 0.01);
	}

	@Test
	public final void testScalarProduct() {
		List<Float> a = Arrays.asList((float) 1, (float) 2, (float) 3, (float) 4);
		List<Float> b = Arrays.asList((float) 1, (float) 2, (float) 3, (float) 4);
		float r = ListUtil.scalarProduct(a, b);
		assertEquals(30, r, 0.01);
	}

	@Test
	public final void testCreateNullList() {
		List<Float> a = ListUtil.createNullList(4);
		
		assertNull(a.get(0));
		assertNull(a.get(1));
		assertNull(a.get(2));
		assertNull(a.get(3));
		
		assertEquals(4, a.size());
		
		// product of null list and numbers should be zero
		List<Float> b = Arrays.asList((float) 1, (float) 2, (float) 3, (float) 4);
		float r = ListUtil.scalarProduct(a, b);
		assertEquals(0, r, 0.01);
	}

	@Test
	public final void testMeanSquared() {
		List<Float> a = Arrays.asList((float) 1, (float) 2, (float) 3, (float) 4);
		assertEquals(7.5, ListUtil.meanSquared(a), 0.01);
		
		List<Float> b = Arrays.asList((float) 1, (float) -2, (float) 3, (float) -4);
		assertEquals(7.5, ListUtil.meanSquared(b), 0.01);
	}

	@Test
	public final void testMeanAbsolute() {
		List<Float> a = Arrays.asList((float) 1, (float) 2, (float) 3, (float) 4);
		assertEquals(2.5, ListUtil.meanAbsolute(a), 0.01);
		
		List<Float> b = Arrays.asList((float) 1, (float) -2, (float) 3, (float) -4);
		assertEquals(2.5, ListUtil.meanAbsolute(b), 0.01);
	}

}
