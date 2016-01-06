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
package com.dataGrouping.similarityMeasure;

import com.tmetrics.util.SparseMatrix;

/**
 * Defines similarity / dissimilarity measure of binary attributes. The input is
 * a matrix of elements and their binary attributes, the output either their
 * similarity or their dissimilarity.
 * 
 * @author Wladimir, Erwin
 * 
 */
public class BinarySimilarityMeasure {

	/**
	 * Calculates the S Coefficient similarity for the given input of elements
	 * and their attributes
	 * 
	 * @return Similarity matrix of the S Coefficient.
	 */
	static public double[][] getSCoefficientSimilarity(double[][] input) {
		if (input == null || input.length == 0 || input[0].length == 0) {
			throw new RuntimeException(
					"Invalid input: one of the dimensions is zero or the input is null.");
		}

		double[][] resultDistance = new double[input.length][input.length];

		int n_01sum = 0;
		int n_10sum = 0;
		int n_11sum = 0;
		double tmpDenominator = 0.0;

		for (int i = 0; i < input.length; ++i) {
			for (int j = i; j >= 0; --j) {
				n_01sum = 0;
				n_10sum = 0;
				n_11sum = 0;

				for (int k = 0; k < input[0].length; ++k) {
					if (input[i][k] == 0 && input[j][k] == 1) {// calculate //
																// n_01
						n_01sum++;
					} else if (input[i][k] == 1 && input[j][k] == 0) {// calculate
																		// n_10
						n_10sum++;
					} else if (input[i][k] == 1 && input[j][k] == 1) {// calculate
																		// n_11
						n_11sum++;
					}
				}
				// calculate (n11)/(n01+n10+n11)
				tmpDenominator = n_01sum + n_10sum + n_11sum;
				if (tmpDenominator != 0.0) {
					resultDistance[i][j] = ((double) n_11sum)
							/ (double) tmpDenominator;
					resultDistance[j][i] = ((double) n_11sum)
							/ (double) tmpDenominator;
				} else {
					resultDistance[i][j] = 0.0;
					resultDistance[j][i] = 0.0;
				}

			}
		}

		return resultDistance;
	}

	/**
	 * Calculates the M Coefficient similarity for the given input of elements
	 * and their attributes
	 * 
	 * @return Similarity matrix of the M Coefficient.
	 */
	static public double[][] getMCoefficientSimilarity(double[][] input) {
		if (input == null || input.length == 0 || input[0].length == 0) {
			throw new RuntimeException(
					"Invalid input: one of the dimensions is zero or the input is null.");
		}

		double[][] resultDistance = new double[input.length][input.length];

		int n_00sum = 0;
		int n_11sum = 0;
		double tmpDenominator = input[0].length; // p

		for (int i = 0; i < input.length; ++i) {
			for (int j = i; j >= 0; --j) {
				n_00sum = 0;
				n_11sum = 0;

				for (int k = 0; k < input[0].length; ++k) {
					if (input[i][k] == 0 && input[j][k] == 0) {// calculate n_00
						n_00sum++;
					} else if (input[i][k] == 1 && input[j][k] == 1) {// calculate
																		// n_11

						n_11sum++;
					}
				}
				// calculate (n_00 + n_11)/p
				if (tmpDenominator != 0.0) {
					resultDistance[i][j] = ((double) (n_00sum + n_11sum))
							/ (double) tmpDenominator;
					resultDistance[j][i] = ((double) (n_00sum + n_11sum))
							/ (double) tmpDenominator;
				} else {
					resultDistance[i][j] = 0.0;
					resultDistance[j][i] = 0.0;
				}

			}
		}

		return resultDistance;
	}

	/**
	 * Calculates the Dice Coefficient similarity for the given input of
	 * elements and their attributes
	 * 
	 * @return Similarity matrix of the M Coefficient.
	 */
	static public double[][] getDiceCoefficientSimilarity(double[][] input) {
		if (input == null || input.length == 0 || input[0].length == 0) {
			throw new RuntimeException(
					"Invalid input: one of the dimensions is zero or the input is null.");
		}

		double[][] resultDistance = new double[input.length][input.length];

		int n_01sum = 0;
		int n_10sum = 0;
		int n_11sum = 0;
		double tmpDenominator = input[0].length; // p

		for (int i = 0; i < input.length; ++i) {
			for (int j = i; j >= 0; --j) {
				n_01sum = 0;
				n_10sum = 0;
				n_11sum = 0;

				for (int k = 0; k < input[0].length; ++k) {
					if (input[i][k] == 0 && input[j][k] == 1) {// calculate
																// n_01
						n_01sum++;
					} else if (input[i][k] == 1 && input[j][k] == 0) {// calculate
																		// n_10
						n_10sum++;
					} else if (input[i][k] == 1 && input[j][k] == 1) {// calculate
																		// n_11
						n_11sum++;
					}
				}
				tmpDenominator = n_01sum + n_10sum + 2 * n_11sum;
				// calculate 2*n_11/(n_01+n_10+2*n_11)
				if (tmpDenominator != 0.0) {
					resultDistance[i][j] = ((double) (2 * n_11sum))
							/ (double) tmpDenominator;
					resultDistance[j][i] = ((double) (2 * n_11sum))
							/ (double) tmpDenominator;
				} else {
					resultDistance[i][j] = 0.0;
					resultDistance[j][i] = 0.0;
				}

			}
		}

		return resultDistance;
	}

	/**
	 * Calculates the Russel Rao Coefficient similarity for the given input of
	 * elements and their attributes
	 * 
	 * @return Similarity matrix of the Russel Rao Coefficient.
	 */
	static public double[][] getRusselRaoCoefficientSimilarity(double[][] input) {
		if (input == null || input.length == 0 || input[0].length == 0) {
			throw new RuntimeException(
					"Invalid input: one of the dimensions is zero or the input is null.");
		}

		double[][] resultDistance = new double[input.length][input.length];

		int n_11sum = 0;
		double tmpDenominator = input[0].length; // p

		for (int i = 0; i < input.length; ++i) {
			for (int j = i; j >= 0; --j) {
				n_11sum = 0;

				for (int k = 0; k < input[0].length; ++k) {
					if (input[i][k] == 1 && input[j][k] == 1) {// calculate
																// n_11
						n_11sum++;
					}
				}
				// calculate n_11/p
				if (tmpDenominator != 0.0) {
					resultDistance[i][j] = ((double) (n_11sum))
							/ (double) tmpDenominator;
					resultDistance[j][i] = ((double) (n_11sum))
							/ (double) tmpDenominator;
				} else {
					resultDistance[i][j] = 0.0;
					resultDistance[j][i] = 0.0;
				}

			}
		}

		return resultDistance;
	}

	/**
	 * Convert similarities to distances according the formula d(i,j) =
	 * sqrt(c_ii + c_jj - 2*c_ij). Where c_ii, c_jj and c_ij are similarities
	 * between appropriated objects.
	 * 
	 * @param input
	 *            similarities
	 * @return distances
	 */
	static public double[][] similarityToDistances(double[][] input) {
		if (input == null) {
			throw new IllegalArgumentException("Dissimilarity matrix is null.");
		}
		if (input.length == 0 || input[0].length == 0) {
			throw new IllegalArgumentException(
					"One of the matrix dimensions is zero.");
		}
		if (input.length != input[0].length) {
			throw new IllegalArgumentException(
					"Input musst be a square matrix.");
		}

		double[][] result = new double[input.length][input[0].length];

		for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < result[0].length; j++) {
				double c_ii = input[i][i];
				double c_jj = input[j][j];
				double c_ij = input[i][j];
				double entry = Math.sqrt(c_ii + c_jj - 2 * c_ij);
				result[i][j] = entry;
				result[j][i] = entry;
			}
		}

		return result;
	}

	/**
	 * Returns a Dissimilarity Matrix when a sparse matrix (of com.tmetrics.util
	 * package) is given.
	 * 
	 * @param sparseMatrix
	 *            Sparse Matrix representing the objects in rows and their
	 *            attributes in columns.
	 * @param similarityMeasure
	 *            - "Dice" or "Jaccard" are supported.
	 * @return a dissimilarity matrix.
	 */
	public static double[][] getDissimilaritySparseMatrix(
			SparseMatrix sparseMatrix, String similarityMeasure) {

		int sparseMatrixRows = sparseMatrix.nrow();
		int sparseMatrixColumns = sparseMatrix.ncol();
		float[][] matrix = sparseMatrix.toArray();
		double[][] resultMatrix = new double[sparseMatrixRows][sparseMatrixRows];

		// variables to store each binary matching between a tweet-pair:
		int n_01sum = 0;
		int n_10sum = 0;
		int n_11sum = 0;

		// A. run through the tweets / rows
		for (int i = 0; i < sparseMatrixRows; i++) {

			// B. run through the tweets / rows until the superior row is
			// reached.
			for (int j = 0; j <= i; j++) {

				// C. (i,j) rows represent a tweet pair:

				// C1. if two columns of a tweet pair matches: n_xx++;
				for (int k = 0; k < sparseMatrixColumns; k++) {
					if (matrix[i][k] == 1 && matrix[j][k] == 1)
						n_11sum++;
					if (matrix[i][k] == 0 && matrix[j][k] == 1)
						n_01sum++;
					if (matrix[i][k] == 1 && matrix[j][k] == 0)
						n_10sum++;
				}
				// C2. calculate similarity between a tweet pair
				switch (similarityMeasure) {
				case "Dice":
					resultMatrix[i][j] = resultMatrix[j][i] = 1 - ((double) 2
							* n_11sum / (double) (2 * n_11sum + n_10sum + n_01sum));
					break;
				case "Jaccard":
					resultMatrix[i][j] = resultMatrix[j][i] = 1 - ((double) n_11sum / (double) (n_11sum
							+ n_10sum + n_01sum));
					break;
				}

				// C3. reset coefficients
				n_11sum = 0;
				n_10sum = 0;
				n_01sum = 0;

			}

		}

		return resultMatrix;
	}
}
