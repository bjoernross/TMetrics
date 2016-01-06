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

import com.dataGrouping.clustering.minimumSpanningTree.AdaptedKruskalMST;
import com.dataGrouping.clustering.minimumSpanningTree.Bag;
import com.dataGrouping.clustering.minimumSpanningTree.Edge;
import com.dataGrouping.clustering.minimumSpanningTree.EdgeWeightedGraph;

/**
 * Single Linkage Clustering Algorithm, using an adapted version of the Kruskal
 * Algorithm.
 * 
 * @author Erwin
 * 
 */
public class SingleLinkageClustering implements HierarchicalClusteringAlgorithm {

	// private HierarchicalClusterTree clusterTree;
	private AdaptedKruskalMST mst;

	// Each position represents an element - its value represents its cluster
	// number
	private int[] clusterMembership;
	
	// Number of clusters after clustering
	private int numberOfClusters;

	/**
	 * Constructor to calculate the clusters of a given distance matrix with the
	 * Single Linkage approach.
	 * 
	 * @param distanceMatrix distance matrix
	 */
	public SingleLinkageClustering(double[][] distanceMatrix) {

		// Create a weighted, undirected graph (without loops and parallel
		// edges) that represents the distance Matrix
		EdgeWeightedGraph G = new EdgeWeightedGraph(distanceMatrix);

		// Perform Kruskal Algorithm, adapted to work as Single Linkage
		// Clustering.
		mst = new AdaptedKruskalMST(G);

		// Get the data structure that represents the tree of calculated
		// clusters
		// clusterTree = mst.getHierarchicalClusterTree();

		// Create an graph from the mst - as the mst only stores the edges
		EdgeWeightedGraph graph = new EdgeWeightedGraph(G.V());
		for (Edge e : mst.edges()) {
			graph.addEdge(e);
		}
		// calculate the connectivity parts of the graph, with 'Outer DFS'
		clusterMembership = outerDFSForClustering(graph);

	}

	private int[] outerDFSForClustering(EdgeWeightedGraph graph) {

		// Each position represents an element - its value represents its
		// cluster number
		int[] elementsClusters = new int[graph.V()];

		// Store the current cluster number = connectivity parts of the graph
		this.numberOfClusters = 0;

		// get the adjacency list of graph
		Bag<Edge>[] adj = graph.getAdj();

		// Perform a outer DFS over the graph
		boolean[] visitedVertices = new boolean[graph.V()];

		for (int v = 0; v < graph.V(); v++) {

			if (visitedVertices[v] == false) {
				DFS(v, elementsClusters, this.numberOfClusters, adj, visitedVertices);
				this.numberOfClusters++;
			}

		}

		return elementsClusters;

	}

	private void DFS(int vertex, int[] elementsClusters, int clusters,
			Bag<Edge>[] adj, boolean[] visitedVertices) {

		// Flag the current vertex as visited
		visitedVertices[vertex] = true;
		// Set the cluster number of the current vertex
		elementsClusters[vertex] = clusters;

		// Run through the edges of the current vertex
		for (Edge e : adj[vertex]) {
			if (!visitedVertices[e.other(vertex)]) {
				DFS(e.other(vertex), elementsClusters, clusters, adj,
						visitedVertices);
			}
		}

	}

	public int[] getClusterMemberships() {

		return this.clusterMembership;
	}
	
	
	public int getCountOfClusters(){
		return this.numberOfClusters;
	}

	/**
	 * Prints the cluster membership of each element
	 */
	public void printClusterMembership() {

		for (int vertex = 0; vertex < this.clusterMembership.length; vertex++) {
			System.out.println("Element: " + (vertex) + " belongs to Cluster: "
					+ (this.clusterMembership[vertex]));
		}

	}

}
