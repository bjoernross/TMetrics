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

import org.ejml.simple.SimpleMatrix;

/**
 * This class implements Smacof Scaling Algorithm from "Modern Multidimensional Scaling Theory and Applications" p. 191.
 * 
 * 
 * @author Wladimir
 */
public class SmacofScaling extends MultiDimensionalScalingAlgorithm {

	/**
	 * Constructor for ModifiedSmacofScaling
	 * @param dissimilarityMatrix Dissimilarity matrix contains dissimilarities of the entities
	 */
	public SmacofScaling(double[][] dissimilarityMatrix) {
		super(dissimilarityMatrix);
	}
	
	@Override
	protected double[][] calcMDS() {
		//setup algorithmus parameters
		double epsilon = 0.0000001;
		int maxIter = 35;
		
		//Step 1. Start configuration is result from classical scaling
		SimpleMatrix coordinatesX = new SimpleMatrix(new ClassicalScaling(dissimilarityMatrix).calcMDS());
		SimpleMatrix dissimilarityMatrix = new SimpleMatrix(this.dissimilarityMatrix);
		
		//Step 2. Compute Stress. At the beginning holds stress_pre=stress
		double stress_pre = computeStressL2(coordinatesX, dissimilarityMatrix);
		double stress = stress_pre;
		
		//Step 3. Iterate until convergence criterion or maxIter is reached
		int k = 0; //Iteration counter
		while (k == 0 || (((stress_pre - stress) > epsilon) && (k < maxIter))) {
			//Step 4.
			k++;
			
			//Step 5. Perform Guttman transform. This is the minimization step
			coordinatesX = computeGuttmanTransform(coordinatesX, dissimilarityMatrix);
			
			//Step 6. compute stress
			stress_pre = stress;
			stress = computeStressL2(coordinatesX, dissimilarityMatrix);
		}
		
		//convert the matrix to ordinary array
		double[][] res = new double[coordinatesX.numRows()][coordinatesX.numCols()];
		
		for (int i = 0; i < res.length; i++) {
			for (int j = 0; j < res[0].length; j++) {
				res[i][j] = coordinatesX.get(i, j);
			}
		}
		
		return res;
	}
	
	/**
	 * This method computes the stress error according the formula from "Modern Multidimensional Scaling Theory and Applications"
	 * The result is the sum of squared differences between dissimilarities and the distances of the given points.
	 * @param coordinates Coordinates of the calculated points
	 * @param dissimilarities Given dissimilarities of the entities.
	 * @return Stress error for the given configuration and dissimilarities
	 */
	private double computeStressL2(SimpleMatrix coordinates, SimpleMatrix dissimilarities) {
		double sum = 0.0;
		for (int i = 0; i < coordinates.numRows(); ++i) {
			for (int j = i + 1; j < coordinates.numRows(); ++j) {
				double dist = Math.sqrt((coordinates.get(i, 0) - coordinates.get(j,0))*(coordinates.get(i, 0) - coordinates.get(j,0)) + 
						(coordinates.get(i, 1) - coordinates.get(j,1))*(coordinates.get(i, 1) - coordinates.get(j,1)));
				double difference = dist - dissimilarities.get(i, j);
				sum += difference*difference;
			}
		}
		return sum;
	}
	
	/**
	 * This is the minimization step
	 * @param coordinates Coordinates of the calculated points
	 * @param dissimilarities Given dissimilarities of the entities.
	 * @return New point coordinates such that the stress for new configuration is less than the previous one.
	 */
	private SimpleMatrix computeGuttmanTransform(SimpleMatrix coordinates, SimpleMatrix dissimilarities) {
		//compute the transform matrix according to formula (8.24) from "Modern Multidimensional Scaling Theory and Applications" p. 190
		SimpleMatrix b = new SimpleMatrix(dissimilarities.numRows(), dissimilarities.numCols());
		
		for (int i = 0; i < b.numRows(); ++i) {
			for (int j = i; j < b.numCols(); ++j) {
				if (i != j) {
					double dist = Math.sqrt((coordinates.get(i, 0) - coordinates.get(j,0))*(coordinates.get(i, 0) - coordinates.get(j,0)) + 
							(coordinates.get(i, 1) - coordinates.get(j,1))*(coordinates.get(i, 1) - coordinates.get(j,1)));
					if (dist == 0) {
						b.set(i, j, 0);
						b.set(j, i, 0);
					}
					else {
						double entry = -dissimilarities.get(i, j)/dist;
						b.set(i, j, entry);
						b.set(j, i, entry);
					}
					
				}
			}
		}
		
		for (int i = 0; i < b.numRows(); ++i) {
			double sum = 0.0;
			for (int j = 0; j < b.numCols(); ++j) {				
				if (i != j) {
					sum += b.get(i, j);					
				}
			}
			b.set(i, i, -sum);
		}
		
		//perform Guttman transform
		SimpleMatrix result = b.mult(coordinates).scale(1.0/coordinates.numRows());
		return result;
	}


}
