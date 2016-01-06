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
package com.restservice.similarityMeasure.test;

import org.junit.Test;

import com.dataGrouping.similarityMeasure.BinarySimilarityMeasure;
import com.dataGrouping.similarityMeasure.QuantitativeDissimilarityMeasure;
import com.tmetrics.util.SparseMatrix;

public class QuantitativeDissimilarityMeasureTest {

	// distance matrix previously calculated with R's dist-method
	@Test
	public void testDissimilaritySparseMatrix(){
		// a) create the sparse matrix
				SparseMatrix matrix = new SparseMatrix();
				
				matrix.set(0, 0, 3);
				matrix.set(0, 1, 2);
				matrix.set(0, 2, 5);
				matrix.set(0, 3, 1);
				
				matrix.set(1, 0, 4);
				matrix.set(1, 1, 2);
				matrix.set(1, 2, 1);
				matrix.set(1, 3, 2);
				
				matrix.set(2, 0, 5);
				matrix.set(2, 1, 4);
				matrix.set(2, 2, 2);
				matrix.set(2, 3, 1);
				// b) test the different similarity measurements

				double[][] actual = QuantitativeDissimilarityMeasure.getDissimilaritySparseMatrix(matrix);
			
				double[][] expected = { 
					{ 0 , 4.242, 4.123 }, 
					{ 4.242, 0, 2.645  },
					{ 4.123, 2.645, 0  } };
				SimilarityMeasureTest.testIfTwoDoubleMatricesEquals(expected, actual,0.001);
	//			SimilarityMeasureTest.printDoubleMatrix(actual);
	}
	
	
}
