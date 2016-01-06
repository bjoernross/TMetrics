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


/**
 * Super class for all classes which implement a Multi Dimensional Scaling algorithm
 * @author Wladimir Haffner
 *
 */
public abstract class MultiDimensionalScalingAlgorithm {
	//Attributes
	protected double[][] dissimilarityMatrix;
	private double[][] result;
	
	//Methods
	/**
	 * Constructor for MDS algorithm.
	 * @param dissimilarityMatrix Matrix which contains dissimilarities of
	 * samples
	 */
	MultiDimensionalScalingAlgorithm(double[][] dissimilarityMatrix) {
		if (dissimilarityMatrix == null) {
			throw new IllegalArgumentException("Dissimilarity matrix is null.");
		}
		if (dissimilarityMatrix.length == 0 || dissimilarityMatrix[0].length == 0) {
			throw new IllegalArgumentException("One of the matrix dimensions is zero.");
		}
		if (dissimilarityMatrix.length != dissimilarityMatrix[0].length) {
			throw new IllegalArgumentException("Input musst be a square matrix.");
		}
		this.setDissimilarityMatrix(dissimilarityMatrix);
	}
	
	/**
	 * Method starts the calculation of the appropriated algorithm
	 * implemented by a subclass and return it or return the result 
	 * if already implemented
	 * @return result of the MDS calculation
	 */
	public double[][] getMDS() {
		if (result != null) {
			return result;
		}
		else {
			return calcMDS();
		}
	}
	
	/**
	 * Method calculates the MDS. This method must be implemented by an appropriated
	 * algorithm.
	 * @return result of the calculated MDS 
	 */
	protected abstract double[][] calcMDS();
	

	/**
	 * Returns the dissimilarity matrix.
	 * @return Dissimilarity matrix
	 */
	public final double[][] getDissimilarityMatrix() {
		return dissimilarityMatrix;
	}

	/**
	 * Set the dissimilarity matrix for the MDS calculation to
	 * dissimilarityMatrix and discard the result calculated before.
	 * @param dissimilarityMatrix New dissimilarity matrix
	 */
	public void setDissimilarityMatrix(double[][] dissimilarityMatrix) {
		result = null;
		this.dissimilarityMatrix = dissimilarityMatrix;
	}
	
	/**
	 * Compute dissimilarities(distances) from resulr of the MDS 
	 * @return computed dissimilarities
	 */
	public double[][] calcDissimilaritiesFromMDS() {
		//Compute simple MDS first
		double[][] resultMDS = getMDS();
		
		//result for dissimilarities
		double[][] newDissimilarities = new double[resultMDS.length][resultMDS.length];
		
		//calc distances from point to point
		for (int i = 0; i < resultMDS.length; ++i) {
			for (int j = i + 1; j < resultMDS.length; ++j) {
				double distance = Math.sqrt((resultMDS[i][0] - resultMDS[j][0])*(resultMDS[i][0] - resultMDS[j][0])+(resultMDS[i][1] - resultMDS[j][1])*(resultMDS[i][1] - resultMDS[j][1]));
				newDissimilarities[i][j] = distance;
				newDissimilarities[j][i] = distance;
			}
		}
		
		//return new dissimilarities
		return newDissimilarities;
	}
}
