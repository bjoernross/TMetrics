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

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.dataGrouping.similarityMeasure.BinarySimilarityMeasure;
import com.tmetrics.util.SparseMatrix;

public class SimilarityMeasureTest {

	double[][] tweetHasHashtagArray;

	@Before
	public void setUp() throws Exception {
		tweetHasHashtagArray = new double[3][3];

		// 1 1 1
		// 0 1 0
		// 1 0 1
		int i = 0;
		tweetHasHashtagArray[i][0] = 1;
		tweetHasHashtagArray[i][1] = 1;
		tweetHasHashtagArray[i][2] = 1;
		i++;
		tweetHasHashtagArray[i][0] = 0;
		tweetHasHashtagArray[i][1] = 1;
		tweetHasHashtagArray[i][2] = 0;
		i++;
		tweetHasHashtagArray[i][0] = 1;
		tweetHasHashtagArray[i][1] = 0;
		tweetHasHashtagArray[i][2] = 1;
		i++;
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSCoefficientSimilarity() {
		double[][] result = BinarySimilarityMeasure
				.getSCoefficientSimilarity(tweetHasHashtagArray);

		int i = 0;
		assertEquals(result[i][0], 1.0, 1e-8);
		assertEquals(result[i][1], 1.0 / 3.0, 1e-8);
		assertEquals(result[i][2], 2.0 / 3.0, 1e-8);
		i++;
		assertEquals(result[i][0], 1.0 / 3.0, 1e-8);
		assertEquals(result[i][1], 1.0, 1e-8);
		assertEquals(result[i][2], 0.0, 1e-8);
		i++;
		assertEquals(result[i][0], 2.0 / 3.0, 1e-8);
		assertEquals(result[i][1], 0.0, 1e-8);
		assertEquals(result[i][2], 1.0, 1e-8);
		i++;

	}

	@Test
	public void testMCoefficientSimilarity() {
		double[][] result = BinarySimilarityMeasure
				.getMCoefficientSimilarity(tweetHasHashtagArray);

		int i = 0;
		assertEquals(result[i][0], 1.0, 1e-8);
		assertEquals(result[i][1], 1.0 / 3.0, 1e-8);
		assertEquals(result[i][2], 2.0 / 3.0, 1e-8);
		i++;
		assertEquals(result[i][0], 1.0 / 3.0, 1e-8);
		assertEquals(result[i][1], 1.0, 1e-8);
		assertEquals(result[i][2], 0.0, 1e-8);
		i++;
		assertEquals(result[i][0], 2.0 / 3.0, 1e-8);
		assertEquals(result[i][1], 0.0, 1e-8);
		assertEquals(result[i][2], 1.0, 1e-8);
		i++;
	}

	@Test
	public void testDiceCoefficientSimilarity() {
		double[][] result = BinarySimilarityMeasure
				.getDiceCoefficientSimilarity(tweetHasHashtagArray);

		int i = 0;
		assertEquals(result[i][0], 1.0, 1e-8);
		assertEquals(result[i][1], 2.0 / 4.0, 1e-8);
		assertEquals(result[i][2], 4.0 / 5.0, 1e-8);
		i++;
		assertEquals(result[i][0], 2.0 / 4.0, 1e-8);
		assertEquals(result[i][1], 1.0, 1e-8);
		assertEquals(result[i][2], 0.0, 1e-8);
		i++;
		assertEquals(result[i][0], 4.0 / 5.0, 1e-8);
		assertEquals(result[i][1], 0.0, 1e-8);
		assertEquals(result[i][2], 1.0, 1e-8);
	}

	@Test
	public void testRusselRaoCoefficientSimilarity() {
		double[][] result = BinarySimilarityMeasure
				.getRusselRaoCoefficientSimilarity(tweetHasHashtagArray);

		int i = 0;
		assertEquals(result[i][0], 1.0, 1e-8);
		assertEquals(result[i][1], 1.0 / 3.0, 1e-8);
		assertEquals(result[i][2], 2.0 / 3.0, 1e-8);
		i++;
		assertEquals(result[i][0], 1.0 / 3.0, 1e-8);
		assertEquals(result[i][1], 1.0 / 3.0, 1e-8);
		assertEquals(result[i][2], 0.0, 1e-8);
		i++;
		assertEquals(result[i][0], 2.0 / 3.0, 1e-8);
		assertEquals(result[i][1], 0.0, 1e-8);
		assertEquals(result[i][2], 2.0 / 3.0, 1e-8);
	}

	@Test
	public void testSimilarityToDistances() {
		double[][] result = BinarySimilarityMeasure
				.similarityToDistances(BinarySimilarityMeasure
						.getRusselRaoCoefficientSimilarity(tweetHasHashtagArray));

		int i = 0;
		assertEquals(result[i][0], Math.sqrt(0.0), 1e-8);
		assertEquals(result[i][1],
				Math.sqrt(1.0 / 3.0 + 1.0 - 2.0 * 1.0 / 3.0), 1e-8);
		assertEquals(result[i][2],
				Math.sqrt(2.0 / 3.0 + 1.0 - 2.0 * 2.0 / 3.0), 1e-8);
		i++;
		assertEquals(result[i][0],
				Math.sqrt(1.0 / 3.0 + 1.0 - 2.0 * 1.0 / 3.0), 1e-8);
		assertEquals(result[i][1], Math.sqrt(0.0), 1e-8);
		assertEquals(result[i][2],
				Math.sqrt(2.0 / 3.0 + 1.0 / 3.0 - 2.0 * 0.0), 1e-8);
		i++;
		assertEquals(result[i][0],
				Math.sqrt(2.0 / 3.0 + 1.0 - 2.0 * 2.0 / 3.0), 1e-8);
		assertEquals(result[i][1],
				Math.sqrt(2.0 / 3.0 + 1.0 / 3.0 - 2.0 * 0.0), 1e-8);
		assertEquals(result[i][2], Math.sqrt(0.0), 1e-8);
	}

	@Test
	public void testgetDissimilaritySparseMatrix() {

		// a) create the sparse matrix
		SparseMatrix matrix = new SparseMatrix();
		// 1 1 1
		// 0 1 0
		// 1 0 1
		matrix.set(0, 0, 1);
		matrix.set(0, 1, 1);
		matrix.set(0, 2, 1);
		matrix.set(1, 1, 1);
		matrix.set(2, 0, 1);
		matrix.set(2, 2, 1);

		// b) test the different similarity measurements
		
		// b.1) Dice
		// Expected:
		// 0 0.5 1/5
		// 0.5 0 1
		// 1/5 1 0
		double[][] expectedDice = { { 0, 0.5, 0.2 }, { 0.5, 0, 1 },
				{ 0.2, 1, 0 } };
		double[][] actualDice = BinarySimilarityMeasure
				.getDissimilaritySparseMatrix(matrix, "Dice");
		testIfTwoDoubleMatricesEquals(expectedDice, actualDice, 0.05);
		// printDoubleMatrix(actualDice);

		// b.2) Jaccard
		// Expected:
		// 0 2/3 1/3
		// 2/3 0 1
		// 1/3 1 0
		double[][] expectedJaccard = { { 0, 0.666, 0.333 }, { 0.666, 0, 1 },
				{ 0.333, 1, 0 } };
		double[][] actualJaccard = BinarySimilarityMeasure
				.getDissimilaritySparseMatrix(matrix, "Jaccard");
		testIfTwoDoubleMatricesEquals(expectedJaccard, actualJaccard, 0.05);
//		printDoubleMatrix(actualJaccard);

	}

	@Ignore
	public static void printDoubleMatrix(double[][] matrix) {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				System.out.println("Pos: [" + i + "][" + j + "]:"
						+ matrix[i][j]);
			}
		}
	}

	@Ignore
	public static void testIfTwoDoubleMatricesEquals(double[][] expected,
			double[][] actuals, double epsilon) {
		for (int i = 0; i < expected.length; i++) {
			for (int j = 0; j < expected[0].length; j++) {
				assertEquals(expected[i][j], actuals[i][j], epsilon);
			}
		}
	}

}
