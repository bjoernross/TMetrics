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
import org.junit.Before;
import org.junit.Test;

public class ClassicalScalingTest {

	private double[][] input1;
	private double[][] input2;
	
	@Before
	public void setUp() throws Exception {
		//Example DISSIMILARITIES
		double[][] input1 = {        // input dissimilarity matrix
			    {0.00,2.04,1.92,2.35,2.06,2.12,2.27,2.34,2.57,2.43,1.90,2.41},
			    {2.04,0.00,2.10,2.00,2.23,2.04,2.38,2.36,2.23,2.36,2.57,2.34},
			    {1.92,2.10,0.00,1.95,2.21,2.23,2.32,2.46,1.87,1.88,2.41,1.97},
			    {2.35,2.00,1.95,0.00,2.05,1.78,2.08,2.27,2.14,2.14,2.38,2.17},
			    {2.06,2.23,2.21,2.05,0.00,2.35,2.23,2.18,2.30,1.98,1.74,2.06},
			    {2.12,2.04,2.23,1.78,2.35,0.00,2.21,2.12,2.21,2.12,2.17,2.23},
			    {2.27,2.38,2.32,2.08,2.23,2.21,0.00,2.04,2.44,2.19,1.74,2.13},
			    {2.34,2.36,2.46,2.27,2.18,2.12,2.04,0.00,2.19,2.09,1.71,2.17},
			    {2.57,2.23,1.87,2.14,2.30,2.21,2.44,2.19,0.00,1.81,2.53,1.98},
			    {2.43,2.36,1.88,2.14,1.98,2.12,2.19,2.09,1.81,0.00,2.00,1.52},
			    {1.90,2.57,2.41,2.38,1.74,2.17,1.74,1.71,2.53,2.00,0.00,2.33},
			    {2.41,2.34,1.97,2.17,2.06,2.23,2.13,2.17,1.98,1.52,2.33,0.00}
		        };
		
		this.input1 = new double[12][12];
		for (int i = 0; i < this.input1.length; ++i) {
			for (int j = 0; j < this.input1.length; ++j) {
				this.input1[i][j] = input1[i][j];
			}
		}
		
		double[][] input2 = {
				{   0, 4.05, 8.25, 5.57},
				{4.05,    0, 2.54, 2.69},
				{8.25, 2.54,    0, 2.11},
				{5.57, 2.69, 2.11,    0}
				};		
		this.input2 = new double[4][4];
		for (int i = 0; i < this.input2.length; ++i) {
			for (int j = 0; j < this.input2.length; ++j) {
				this.input2[i][j] = input2[i][j];
			}
		}
	}
	
	@Test
	public void testInput2() {
		ClassicalScaling test = new ClassicalScaling(input2);
		
		double[][] actuals =  test.calcMDS();
		double[][] expecteds = {
				{-4.62, -0.07},
				{-0.09,  1.11},
				{+3.63,  0.34},
				{+1.08, -1.38}
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
			new ClassicalScaling(m);
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
			new ClassicalScaling(new double[][] { {} });
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
			new ClassicalScaling(new double[][] { {0, 1} });
			fail("Should have thrown an exception");
		}
		catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		try {
			new ClassicalScaling(new double[][] { {0}, {1}});
			fail("Should have thrown an exception");
		}
		catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		try {
			new ClassicalScaling(new double[][] { {0, 1}, {1, 0}});
		}
		catch (IllegalArgumentException e) {
			fail("Shouldn't have thrown an exception");
		}
	}

}
