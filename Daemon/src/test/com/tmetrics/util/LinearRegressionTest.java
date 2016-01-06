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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Class to test the linear Regression. We compare an test case - calculated in R with our Regression Output.
 * @author Erwin
 *
 */
public class LinearRegressionTest {

	// Test the complete regression procedure and compare the results with a
	// regression which we have done in R ;)
	@Test
	public final void testCompleteRegression() {

		int columns = 2;
		int rows = 6;
		SparseMatrix matrix = new SparseMatrix(columns);
		// add constant column
		for (int i = 0; i < rows; i++) {
			matrix.set(i, 0, (float) (1));
		}
		// add x1 column
		float[] input = new float[] { 20,16,15,16,13,10 };
		for(int i=0; i < input.length; i++)
			matrix.set(i, 1, input[i]);
		// add y = labels column
		float[] y = new float[] {0,3,7,4,6,10};
		List<Float> labels = new ArrayList<Float>();
		for(int i = 0; i< y.length; i++){
			labels.add(y[i]);
		}

//		System.out.println(matrix);
//		System.out.println(labels);
		
		LinearRegression linReg = new LinearRegression(matrix,labels,(float)0.008);

		// Show Results
//		System.out.println(linReg.getParameters());
//		System.out.println(linReg.getErrors());
		
		// Compare Results
		// Estimated Parameters
		// Output of R: 19.7321429  -0.9821429 
		assertEquals((float)19.7321429,linReg.getParameters().get(0),0.8);
		assertEquals((float)-0.9821429,linReg.getParameters().get(1),0.5);
		
		// MSE
		// Output of R: 0.9970238
		float mse = ListUtil.meanSquared(linReg.getErrors());
		assertEquals((float)0.9970238,mse,0.2);
		
	}

}
