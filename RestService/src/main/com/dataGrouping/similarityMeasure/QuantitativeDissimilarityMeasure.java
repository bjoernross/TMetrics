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

import java.util.List;

import com.tmetrics.util.SparseMatrix;

/**
 * Dissimilarity Measurement: This class defines the dissimilarity measure of
 * quantitative attributes. The input is a sparse matrix where the rows are the
 * objects and the columns are the attributes.
 * 
 * @author eq
 * 
 */
public class QuantitativeDissimilarityMeasure {

	/**
	 * Returns a Dissimilarity Matrix for quantitative attributes using
	 * euclidian distances when a sparse matrix (of com.tmetrics.util package)
	 * is given.
	 * 
	 * @param sparseMatrix
	 *            The input is a sparse matrix where the rows are the objects
	 *            and the columns are the attributes.
	 * @return a dissimilarity matrix.
	 */
	public static double[][] getDissimilaritySparseMatrix(
			SparseMatrix sparseMatrix) {

		int sparseMatrixRows = sparseMatrix.nrow();
		int sparseMatrixColumns = sparseMatrix.ncol();
		float[][] matrix = sparseMatrix.toArray();
		double[][] resultMatrix = new double[sparseMatrixRows][sparseMatrixRows];

		// variables to store matching between a tweet-pair:
		double L2sum = 0;

		// A. run through the tweets / rows
		for (int i = 0; i < sparseMatrixRows; i++) {

			// B. run through the tweets / rows until the superior row is
			// reached.
			for (int j = 0; j <= i; j++) {

				// C. (i,j) rows represent a tweet pair:

				for (int k = 0; k < sparseMatrixColumns; k++) {

					L2sum += Math.pow((matrix[i][k] - matrix[j][k]), 2);

				}
				L2sum = Math.sqrt(L2sum);
				resultMatrix[i][j] = resultMatrix[j][i] = L2sum;

				// C3. reset coefficients
				L2sum = 0;

			}

		}

		return resultMatrix;
	}
	
	/**
	 * Print the dissimilarity matrix with the column and row names.
	 * @param rowNames the row (and the column) names
	 * @param disMatrix the values of the matrix
	 */
	public static void showDisMatrix(List<String> rowNames, double[][] disMatrix) {
		
		StringBuilder sb = new StringBuilder();
		
		// a) create colums
		sb.append("--- \t");
		for(String s : rowNames)
			sb.append(s + "\t");
		sb.append("\n");
		// b) create rows (including row names)
		
		for(int i = 0; i < disMatrix.length; i++) {
			sb.append(rowNames.get(i) + "\t");
			for(int j = 0; j < disMatrix[0].length ; j++) {
				sb.append(disMatrix[i][j] + "\t");
			}
			sb.append("\n");
		}
	
		System.out.println(sb.toString());
		
	}

}
