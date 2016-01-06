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

public class SmacofScalingTest {

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
		SmacofScaling sc = new SmacofScaling(input1);
		
		double[][] actuals = sc.getMDS();
		
		double[][] expecteds = {
				{2.948457783096623, -0.24072658098595073},
				{-1.9881926309545466, -0.741394108037325}, 
				{-0.11682760615291307, 0.11396136279693972}, 
				{-0.843437545989163, 0.8681593262263361} 
		};
		
		
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
			new SmacofScaling(m);
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
			new SmacofScaling(new double[][] { {} });
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
			new SmacofScaling(new double[][] { {0, 1} });
			fail("Should have thrown an exception");
		}
		catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		try {
			new SmacofScaling(new double[][] { {0}, {1}});
			fail("Should have thrown an exception");
		}
		catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		try {
			new SmacofScaling(new double[][] { {0, 1}, {1, 0}});
		}
		catch (IllegalArgumentException e) {
			fail("Shouldn't have thrown an exception");
		}
	}

}
