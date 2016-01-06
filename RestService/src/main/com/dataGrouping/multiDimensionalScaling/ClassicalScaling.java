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

import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.factory.EigenDecomposition;
import org.ejml.simple.SimpleMatrix;

/**
 * This class implements Classical Scaling Algorithm from "Modern Multidimensional Scaling Theory and Applications"
 * @author Wladimir
 *
 */
public class ClassicalScaling extends MultiDimensionalScalingAlgorithm {
	//Methods
	
	/**
	 * Constructor for MDS algorithm.
	 * @param dissimilarityMatrix Matrix which contains dissimilarities of
	 * samples
	 */
	public ClassicalScaling(double[][] dissimilarityMatrix) {
		super(dissimilarityMatrix);
	}

	@Override
	protected double[][] calcMDS() {
		//Step 1. Compute the matrix of squared dissimilarities
		//They are needed because from them we can calculate the matrix XX', where X is the coordinate matrix.
		double[][] squaredValues = new double[dissimilarityMatrix.length][dissimilarityMatrix.length];
		for (int i = 0; i < dissimilarityMatrix.length; ++i) {
			for (int j = 0; j < dissimilarityMatrix.length; ++j) {
				double value = dissimilarityMatrix[i][j];
				squaredValues[i][j] = value*value;
			}
		}
		
		SimpleMatrix squareddissimilarityMatrix = new SimpleMatrix(squaredValues);
		squaredValues = null;
		
		//Step 2. Apply double centering to squareddissimilarityMatrix
		//the result of this operation is the matrix XX'
		double[][] onesDividedByN = new double[dissimilarityMatrix.length][dissimilarityMatrix.length];
		for (int i = 0; i < dissimilarityMatrix.length; ++i) {
			for (int j = 0; j < dissimilarityMatrix.length; ++j) {
				onesDividedByN[i][j] = 1.0/dissimilarityMatrix.length;
			}
		}
		SimpleMatrix centeringMatrix = SimpleMatrix.identity(dissimilarityMatrix.length).minus(new SimpleMatrix(onesDividedByN));
		onesDividedByN = null; 
		SimpleMatrix delta = centeringMatrix.mult(squareddissimilarityMatrix).mult(centeringMatrix).scale(-0.5);
		
		//Step 3. Compute the eigendecomposition
		//this step decompose the centered matrix from above which is actually XX' into eigenvalues and the appropriated eigenvectors
		//in the end we obtain XX'=QVQ'=(QV^(1/2))*(QV^(1/2))'
		EigenDecomposition<DenseMatrix64F> evd =  DecompositionFactory.eig(delta.numRows(), true);
		evd.decompose(delta.getMatrix());
		

		double[] eigVal12 = {Double.MIN_VALUE, Double.MIN_VALUE};
		int eigVal1Ind = 0;
		int eigVal2Ind = 0;

		//search for the largest eigenvalue
		for (int i = 0; i < evd.getNumberOfEigenvalues(); i++) {
			double eigVal = evd.getEigenvalue(i).real;
			if (eigVal > eigVal12[0]) {
				eigVal12[0] = eigVal;
				eigVal1Ind = i;
			}
		}
		
		//search for the second largest
		for (int i = 0; i < evd.getNumberOfEigenvalues(); i++) {
			double eigVal = evd.getEigenvalue(i).real;
			if (eigVal > eigVal12[1] && i != eigVal1Ind) {
				eigVal12[1] = eigVal;
				eigVal2Ind = i;
			}
		}
		
		SimpleMatrix V = SimpleMatrix.wrap(evd.getEigenVector(eigVal1Ind)).combine(0, 1, SimpleMatrix.wrap(evd.getEigenVector(eigVal2Ind)));
		
		//compute the resultig coordinates of the points from the largest and the second largest eigenvalue and the appropriated eigenvectors 
		//because of the step above we obtain the coordinate matrix X by calculate X=(QV^(1/2))
		//and convert them into ordinary array
		SimpleMatrix result = V.mult(SimpleMatrix.diag(Math.sqrt(eigVal12[0]), Math.sqrt(eigVal12[1])));
		double[][] resArray = new double[result.numRows()][result.numCols()];
		for (int i = 0; i < result.numRows(); i++) {
			for (int j = 0; j < result.numCols(); j++) {
				resArray[i][j] = result.get(i, j);
			}
		}
		return resArray;
	}
}
