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

import org.junit.Ignore;
import org.junit.Test;

public class SparseMatrixTest {

	@Ignore
	public final void testSet() {
		// this is implicitly tested with the other test methods
	}
	
	@Test
	public final void testToString() {
		SparseMatrix sm = new SparseMatrix();
		
		sm.set(0, 0, 1);
		assertEquals("1.0\t\n", sm.toString());
		
		sm.set(1, 1, 2);
		assertEquals("1.0\t0\t\n0\t2.0\t\n", sm.toString());
	}
	
	@Test
	public final void testNrow() {
		SparseMatrix sm = new SparseMatrix();
		sm.set(0, 0, 1);
		assertEquals(1, sm.nrow());
		
		sm.set(1, 1, 2);
		assertEquals(2,sm.nrow());
		
		// we can leave out rows; they will be automatically created will all elements initialized as 0
		sm.set(3, 2, 3);
		assertEquals(4,sm.nrow());
		
		sm.set(1, 3, 4);
		assertEquals(4,sm.nrow());
	}

	@Test
	public final void testNcol() {
		SparseMatrix sm = new SparseMatrix();
		sm.set(0, 0, 1);
		assertEquals(1, sm.ncol());
		
		sm.set(1, 1, 2);
		assertEquals(2,sm.ncol());
		
		// we can also leave out columns; they will be automatically created will all elements initialized as 0
		sm.set(1, 3, 4);
		assertEquals(4,sm.ncol());
		
		sm.set(3, 2, 3);
		assertEquals(4,sm.ncol());
	}

	@Test
	public final void testMultiply() {
		SparseMatrix sm = new SparseMatrix();
		sm.set(0, 0, 1);
		sm.set(1, 1, 2);
		sm.set(3, 2, 3);
		sm.set(1, 3, 4);
		
		List<Float> a = Arrays.asList((float) 1, (float) 2, (float) 3, (float) 4);
		
		List<Float> result = sm.multiply(a);
		
		assertEquals(1, result.get(0), 0.01);
		assertEquals(20, result.get(1), 0.01);
		assertEquals(0, result.get(2), 0.01);
		assertEquals(9, result.get(3), 0.01);
	}

	@Test
	public final void testQuiringProduct() {
		SparseMatrix sm = new SparseMatrix();
		sm.set(0, 0, 1);
		sm.set(1, 1, 2);
		sm.set(3, 2, 3);
		sm.set(1, 3, 4);
		
		List<Float> a = Arrays.asList((float) 1, (float) 2, (float) 3, (float) 4);
		
		List<Float> result = sm.quiringProduct(a);
		
		assertEquals(1, result.get(0), 0.01);
		assertEquals(4, result.get(1), 0.01);
		assertEquals(12, result.get(2), 0.01);
		assertEquals(8, result.get(3), 0.01);
	}

	@Test
	public final void testConcatenateVertically() {
		SparseMatrix sm1 = new SparseMatrix();
		sm1.set(0, 0, 1);
		
		SparseMatrix sm2 = new SparseMatrix();
		sm2.set(0, 0, 1);
		
		sm1.concatenateVertically(sm2);
		
		assertEquals(2, sm1.nrow(), 0.01);
		assertEquals(1, sm2.ncol(), 0.01);
		assertEquals("1.0\t\n1.0\t\n", sm1.toString());
	}

	@Test
	public final void testToArray() {
		SparseMatrix sm = new SparseMatrix();
		sm.set(0, 0, 1);
		sm.set(1, 1, 2);
		sm.set(3, 2, 3);
		sm.set(1, 3, 4);
		
		float[][] array = new float[][]{
				{1, 0, 0, 0},
				{0, 2, 0, 4},
				{0, 0, 0, 0},
				{0, 0, 3, 0}
		};
		
		float[][] sm_array = sm.toArray();
		boolean equals = Arrays.deepEquals(sm_array, array);
		
		assertTrue(equals);
	}

	@Test
	public final void testGetColumnAsList() {
		SparseMatrix sm = new SparseMatrix();
		sm.set(0, 0, 1);
		sm.set(1, 1, 2);
		sm.set(3, 2, 3);
		sm.set(1, 3, 4);
		
		List<Float> list = sm.getColumnAsList(0);
		
		assertEquals(1, list.get(0), 0.01);
		assertEquals(0, list.get(1), 0.01);
		assertEquals(0, list.get(2), 0.01);
		assertEquals(0, list.get(3), 0.01);
		
		list = sm.getColumnAsList(1);
		
		assertEquals(0, list.get(0), 0.01);
		assertEquals(2, list.get(1), 0.01);
		assertEquals(0, list.get(2), 0.01);
		assertEquals(0, list.get(3), 0.01);
	}
	
	@Test
	public final void testGetRowAsList() {
		SparseMatrix sm = new SparseMatrix();
		sm.set(0, 0, 1);
		sm.set(1, 1, 2);
		sm.set(3, 2, 3);
		sm.set(1, 3, 4);
		
		List<Float> list = sm.getRowAsList(0);
		
		assertEquals(1, list.get(0), 0.01);
		assertEquals(0, list.get(1), 0.01);
		assertEquals(0, list.get(2), 0.01);
		assertEquals(0, list.get(3), 0.01);
	}

}
