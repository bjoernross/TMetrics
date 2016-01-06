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
package com.dataGrouping.clustering;

/**
 * Defines what a class needs to do to be a Clustering Algorithm. Do not forget
 * the constructor that takes a distance matrix and calculates the clusters.
 * 
 * @author Erwin
 * 
 */
public interface HierarchicalClusteringAlgorithm {
	
	/**
	 * Returns a Integer-Array of the cluster membership of the elements. The
	 * Algorithm propose a number of clusters.
	 * 
	 * @return @return an array, where the array-position represents the
	 *         element-id, the value its cluster number.
	 */
	public int[] getClusterMemberships();

	/**
	 * Returns the number of clusters (after clustering).
	 * 
	 * @return an integer, representing the total amount of clusters.
	 */
	public int getCountOfClusters();

}
