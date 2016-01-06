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

import java.util.Random;
import org.ejml.simple.SimpleMatrix;

/**
 * This class implements Smacof Scaling Algorithm from "Modern Multidimensional Scaling Theory and Applications" p. 191.
 * Unlike normal Smacof Algorithm implementation in SmacofScaling class, here was another initialisation routine used,
 * which takes the affinity of an entity to a cluster and the count of clusters in account.
 * 
 * 
 * @author Wladimir
 */
public class ModifiedSmacofScaling extends MultiDimensionalScalingAlgorithm  {
	
	private int[] indToClusterNum;
	private int countOfClusters;
	
	/**
	 * Constructor for ModifiedSmacofScaling
	 * @param dissimilarityMatrix Dissimilarity matrix contains dissimilarities of the entities
	 * @param indToClusterNum Array contains the number of cluster to which the entity with index i belong
	 * @param countOfClusters Total count of clusters in indToClusterNum. 
	 * Note this is not the length of indToClusterNum array but rather the count of distinct values.
	 */
	public ModifiedSmacofScaling (double[][] dissimilarityMatrix, int[] indToClusterNum, int countOfClusters) {
		super(dissimilarityMatrix);
		this.indToClusterNum = indToClusterNum;
		this.countOfClusters = countOfClusters;
	}

	/**
	 * Alternative initialization method which produce coordinates such that
	 * the points are arranged around the cluster centers and the cluster centers are arranged 
	 * equidistant along the x-axis.
	 * @return Start configuration as described above
	 */
	private SimpleMatrix computeStartConfigForClustersInRow() {
		SimpleMatrix result = new SimpleMatrix(indToClusterNum.length, 2);
		
		Random randomGenerator = new Random(1);
		for (int i = 0; i < indToClusterNum.length; i++) {
			double posx = (double)indToClusterNum[i] + randomGenerator.nextDouble() - 0.5;
			double posy = randomGenerator.nextDouble() - 0.5;
			result.set(i, 0, posx);
			result.set(i, 1, posy);
		}
		
		return result;
	}
	
	/**
	 * Calculate the start configuration for the given clusters. Using this method you obtain a point cloud
	 * in which the points are placed near their cluster center. The cluster center is placed on the border of 
	 * a sphere such that the angle between the line from the  center of the sphere to the cluster center of the x-axis
	 * id equals to (clusterNumber * 360/countOfClusters). Note that first clusterNumber is zero.
	 * @return Start configuration as described above
	 */
	private SimpleMatrix computeStartConfigForClustersInCircle() {
		SimpleMatrix result = new SimpleMatrix(indToClusterNum.length, 2);
		
		Random randomGenerator = new Random(1);
		for (int i = 0; i < indToClusterNum.length; i++) {
			double angle = randomGenerator.nextDouble()*2.0*Math.PI;
			double r = randomGenerator.nextDouble()*Math.sin(2.0*Math.PI/(double)countOfClusters)/(2.0*2);
			
			double clusterAngle = ((double)indToClusterNum[i]*2)/((double)countOfClusters*2)*2.0*Math.PI;

			
			double posx = r*Math.cos(angle) + Math.cos(clusterAngle);
			double posy = r*Math.sin(angle) + Math.sin(clusterAngle);
			result.set(i, 0, posx);
			result.set(i, 1, posy);
		}
		
		return result;
	}
	
	@Override
	protected double[][] calcMDS() {
		//setup algorithmus parameters
		double epsilon = 0.0000001;
		int maxIter = 10;
		
		//Step 1. Start configuration is result from classical scaling
		SimpleMatrix coordinatesX = computeStartConfigForClustersInCircle();
		SimpleMatrix dissimilarityMatrix = new SimpleMatrix(this.dissimilarityMatrix);
		
		//Step 2. Compute Stress. At the beginning holds stress_pre=stress
		double stress_pre = computeStressL2(coordinatesX, dissimilarityMatrix);
		double stress = stress_pre;
		
		//Step 3. Iterate until convergence criterion or maxIter is reached
		int k = 0; //Iteration counter
		while ((k == 0 && maxIter > 0) || (((stress_pre - stress) > epsilon) && (k < maxIter))) {
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
