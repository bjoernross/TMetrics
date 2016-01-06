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
package com.dataGrouping.multiDimensionalScaling;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ModifiedSmacofScalingTest {

	private double[][] input1;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		double[][] tmp = {
				{0, 5, 3, 4},
				{5, 0, 2, 2},
				{3, 2, 0, 1},
				{4, 2, 1, 0}
		};
		input1 = new double[4][4];
		for (int i = 0; i < tmp.length; ++i) {
			for (int j = 0; j < tmp[0].length; ++j) {
				input1[i][j] = tmp[i][j]; 
			}
		}
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		ModifiedSmacofScaling sc = new ModifiedSmacofScaling(input1, new int[]{1, 1, 0, 0}, 2);
		
		double[][] actuals = sc.getMDS();
		
		double[][] expecteds = {
				{-1.2735363569533777, -2.66624048046241}, 
				{-0.022655461141955602, 2.1145959185814265}, 
				{0.16458978949154285, 0.0725191600650576}, 
				{1.1316020286037904, 0.4791254018159255}};
		
		for (int i = 0; i < actuals.length; ++i) {
			for (int j = 0; j < actuals[0].length; ++j) {
				assertEquals(expecteds[i][j], actuals[i][j], 0.01);
			}
		}
	}
	
	/**
	 * Test for unusual input: parameter == null.
	 */
	@Test
	public void testParameterIsNull() {
		try {
			double[][] m = null;
			new ModifiedSmacofScaling(m, new int[]{0}, 1);
			fail("Should have thrown an exception");
		}
		catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}
	
	/**
	 * Test for unusual input: parameter is empty.
	 */
	@Test
	public void testParameterIsEmpty() {
		try {
			new ModifiedSmacofScaling(new double[][] { {} }, new int[]{0}, 1);
			fail("Should have thrown an exception");
		}
		catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}
	
	/**
	 * Test for unusual input: parameter is not a square matrix.
	 */
	@Test
	public void testParameterIsSquareMatrix() {
		try {
			new ModifiedSmacofScaling(new double[][] { {0, 1} }, new int[]{0}, 1);
			fail("Should have thrown an exception");
		}
		catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		try {
			new ModifiedSmacofScaling(new double[][] { {0}, {1}}, new int[]{0}, 1);
			fail("Should have thrown an exception");
		}
		catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		try {
			new ModifiedSmacofScaling(new double[][] { {0, 1}, {1, 0}}, new int[]{0}, 1);
		}
		catch (IllegalArgumentException e) {
			fail("Shouldn't have thrown an exception");
		}
	}
}
