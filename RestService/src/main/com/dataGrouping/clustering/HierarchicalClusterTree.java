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

import java.util.ArrayList;
import java.util.List;

import com.dataGrouping.clustering.minimumSpanningTree.Edge;

/**
 * Represents the cluster state after each fusion step during clustering. 
 * In doing so, it is possible to create the complete hierarchical structure of the clustering.
 * @author Erwin
 *
 */
public class HierarchicalClusterTree {

	private List<List<Edge>> hierarchyLevel;
	private List<Edge> lastEdgesOnHierachyLevel;
	
	/**
	 * @param hierarchyLevels - Set how many hierarchy levels are expected = how many elements will be clustered - 1
	 */
	public HierarchicalClusterTree(int hierarchyLevels){
		this.hierarchyLevel = new ArrayList<List<Edge>>(hierarchyLevels);
		this.lastEdgesOnHierachyLevel = new ArrayList<Edge>();
	}
	
	public void addClusterPair(int cluster1, int cluster2,double fusionWeight){
		Edge e = new Edge(cluster1,cluster2,fusionWeight);
		
		//List<Edge> edgesOnHierachyLevel = ;
		
	}
	
}
