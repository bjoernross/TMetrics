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


import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import com.dataGrouping.clustering.HierarchicalClusteringAlgorithm;

/**
 * Implementation of an agglomerativ clustering algorithm.
 * 
 * @author Wladimir
 *
 */
public class WladimirotivesClustering implements HierarchicalClusteringAlgorithm {
	//Attributes
	private double[][] distances;
	
	private int countOfClusters;
	private int maxCountOfClusters;
	
	//Methods


	/**
	 * Constructor for the simple Algorithm
	 * @param distances Distancematrix which represent the distances between elements
	 */
	public WladimirotivesClustering(double[][] distances, int maxCountOfClusters) {
		this.distances = distances;
		this.maxCountOfClusters = maxCountOfClusters;
	}
	
	/**
	 * Empty Constructor for the simple Algorithm
	 */
	public WladimirotivesClustering() {
		this.distances = null;
	}
	
	public int[] getClusterMemberships() {
		//check whether the input is valid
		if (distances == null) {
			throw new IllegalArgumentException("Distance matrix is null.");
		}
		if (distances.length == 0 || distances[0].length == 0) {
			throw new IllegalArgumentException("One of the matrix dimensions is zero.");
		}
		if (!(distances.length == distances[0].length)) {
			throw new IllegalArgumentException("Input musst be a square matrix.");
		}
		
		//allocate memory for result array
		int[] result = new int[this.distances.length];
		
		//This set is needed to keep track of remaining clusters. The list contains the heads of the linked list of the appropriated cluster.
		HashSet<Integer> clusterlist = new HashSet<Integer>(this.distances.length);
		
		//create clusters which at the beginning consist only from a single point
		Cluster[] clusters = new Cluster[this.distances.length];
		for (int i = 0; i < this.distances.length; i++) {
			clusters[i] = new Cluster(i);
			clusterlist.add(i);
		}
		
		//create object for representing the cluster distances
		Clusterdistance[] distances = new Clusterdistance[(this.distances.length*this.distances.length - this.distances.length)/2];
		int distInd = 0;
		for (int i = 0; i < this.distances.length; i++) {
			for (int j = i + 1; j < this.distances.length; j++) {
				distances[distInd] = new Clusterdistance(this.distances[i][j], i, j);
				distInd++;
			}
		}
		
		//create heap in order to get the actual shortest distance between two clusters
		DistanceHeap heap = new DistanceHeap(distances);
	
		//at the beginning the count of clusters is equals to the number of points because each point is a single cluster
		int currentNumberOfClusters = this.distances.length;
		Clusterdistance minDist = null;

		
		while (currentNumberOfClusters > maxCountOfClusters && (minDist = heap.extractMin()) != null) {
			//remove temporary both clusters (their heads) from the cluster list because we have to merge them.
			clusterlist.remove(minDist.getIndizes().getI());
			clusterlist.remove(minDist.getIndizes().getJ());
			
			//merge both clusters belonging to the actual minimal distance. 
			clusters[minDist.getIndizes().getI()].append(clusters[minDist.getIndizes().getJ()]);
			
			//here we have to compare the distances from both clusters, belonging to the minimal distance, to the other clusters
			//and decide according complete or single linkage which of them we want to keep.
			for (Integer index : clusterlist) {
				//get the two distances to compare
				Clusterdistance d1 = heap.getDistanceForIndices(minDist.getIndizes().getI(), index);
				Clusterdistance d2 = heap.getDistanceForIndices(minDist.getIndizes().getJ(), index);
				
				//According the COMPLETE LINKAGE we take the greater distance.
				if (d1.getDist() > d2.getDist()) {
					heap.remove(d2);
					
					//after removing the shorter distance we wand to adapt the larger distance to new indices 
					if (d1.getIndizes().getI() != index) {
						heap.changeIndicesOf(d1, clusters[d1.getIndizes().getI()].head.pointInd, index);						
					}
					else {						
						heap.changeIndicesOf(d1, clusters[d1.getIndizes().getJ()].head.pointInd, index);						
					}
				}
				else {
					heap.remove(d1);
					
					//after removing the shorter distance we wand to adapt the larger distance to new indices 
					if (d2.getIndizes().getI() != index) {
						heap.changeIndicesOf(d2, clusters[d2.getIndizes().getI()].head.pointInd, index);						
					}
					else {						
						heap.changeIndicesOf(d2, clusters[d2.getIndizes().getJ()].head.pointInd, index);						
					}
				}
			}
			
			//after each iteration the number of cluster is decreased by one
			currentNumberOfClusters--;
			
			//add the new cluster to the cluster list
			clusterlist.add(clusters[minDist.getIndizes().getI()].head.pointInd);
		}

		int momCluster = 0;
		//iterate through the clusters and label the points with the cluster number
		for (int i = 0; i < clusters.length; i++) {
			//in this case we have found the head of a new cluster. So we have to label its points.
			if (clusters[i].head == clusters[i]) {
				//here we iterate through the cluster by using the next link an label the points
				Cluster mom = clusters[i];
				result[mom.pointInd] = momCluster;
				while (mom.next != mom) {
					mom = mom.next;
					result[mom.pointInd] = momCluster;
				}
				//each time we find a new cluster we increase the actual label
				momCluster++;
			}
		}
		
		//save the number of clusters in the member variable
		countOfClusters = momCluster;

		return result;
	}

	
	public int getCountOfClusters() {
		return countOfClusters;
	}

	public void setCountOfClusters(int countOfClusters) {
		this.countOfClusters = countOfClusters;
	}
	
	//Help classes
	/**
	 * This class holds information about one cluster. One cluster is a linked list of his elements.
	 * @author Wladimir
	 *
	 */
	public class Cluster {
		public int pointInd;
		
		public Cluster head = null;
		public Cluster tail = null;
		public Cluster next = null;
		
		/**
		 * Append the given cluster to this one
		 * @param bHead Head of the cluster to append
		 * @return This cluster
		 */
		public Cluster append(Cluster bHead) {
			this.tail.next = bHead;
			this.tail = bHead.tail;
			bHead.head = this;
			return this;
		}
		
		/**
		 * Constructor for the Cluster. At the beginning the Cluster consist from a single point,
		 * which is the point with given index 
		 * @param index Index of the point which the cluster represents.
		 */
		public Cluster(int index) {
			pointInd = index;
			head = this;
			tail = this;
			next = this;
		}
	}
	
	/**
	 * Holds a pair of indexes for the appropriated clusters.
	 * The hashCode() and equals(Object obj) methods are overriden in such way that
	 * following holds: (x, y) = (y, x)
	 * @author Wladimir
	 *
	 */
	protected class PairOfIndizes {
		public void setI(int i) {
			this.i = i;
		}

		public void setJ(int j) {
			this.j = j;
		}

		public PairOfIndizes(int i, int j) {
			this.i = i;
			this.j = j;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + i + j;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PairOfIndizes other = (PairOfIndizes) obj;
			if (i != other.i)
				return (i == other.j && j == other.i);
			if (j != other.j)
				return false;
			return true;
		}
		
		public int getI() {
			return i;
		}
		
		public int getJ() {
			return j;
		}
		
		private int i;
		private int j;
	}
	
	/**
	 * Class for represent cluster distances. This class
	 * store the distance for two clusters and the indices of 
	 * the appropriated clusters. For ordering purpose Comparabele
	 * interface is implemented whereby two Clusterdistances are
	 * compared by their distances stored within. 
	 * 
	 * In order to find a Clusterdistance in a HashMap the equals() 
	 * method is overriden such that two Clusterdistances are equals
	 * if and only if the indices of the objects are equals. That means
	 * Clusterdistance a is equals to Clusterdistance b when a.indices == b.indices.
	 * The order of indexes is neglected.
	 * @author Wladimir
	 *
	 */
	protected class Clusterdistance implements Comparable<Clusterdistance> {
		//Attributes
		private Double dist;
		private PairOfIndizes indizes;
		public int indexInHeap;
		
		//Methods
		public Clusterdistance(double dist, int i, int j) {
			this.dist = new Double(dist);
			this.indizes = new PairOfIndizes(i, j);
			indexInHeap = -1;
		}

		public void setDistance(int newDist) {
			dist = new Double(newDist);
		}
		
		@Override
		public int compareTo(Clusterdistance o) {
			return dist.compareTo(o.dist);
		}
		
		public PairOfIndizes getIndizes() {
			return indizes;
		}

		private WladimirotivesClustering getOuterType() {
			return WladimirotivesClustering.this;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + indizes.hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Clusterdistance other = (Clusterdistance) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (indizes == null) {
				if (other.indizes != null)
					return false;
			} else if (!indizes.equals(other.indizes))
				return false;
			return true;
		}

		public Double getDist() {
			return dist;
		}

		public void setDist(Double dist) {
			this.dist = dist;
		}

		public void setIndizes(PairOfIndizes indizes) {
			this.indizes = indizes;
		}
		
		
		
	}
		
	
	/**
	 * Heap for maintain the distances of the clusters.
	 * @author Wladimir
	 *
	 */
	protected class DistanceHeap {
		/**
		 * Construct a heap with the cluster distances from a
		 * @param a cluster distances
		 */
		public DistanceHeap(Clusterdistance[] a) {
			super();
			heapArray = a;
			momSize = a.length;
			indexes = new HashMap<Clusterdistance, Integer>(momSize);
			
			for (int i = 0; i < momSize; i++) {
				indexes.put(heapArray[i], i);
			}
			
			build();
		}
		
		/**
		 * Calculate the index of the left child of the node with index i
		 * @param i Index of the node
		 * @return Index of the left child of the node with index i
		 */
		private int left(int i) {
			return 2*i+1;
		}
		
		/**
		 * Calculate the index of the right child of the node with index i
		 * @param i Index of the node
		 * @return Index of the right child of the node with index i
		 */
		private int right(int i) {
			return 2*i+2;
		}
		
		/**
		 * Calculate the index of the parent of the node with given index.
		 * @param i Index of the node
		 * @return Index of the parent of the node with index i
		 */
		private int parent(int i) {
			return (i-1)/2;
		}
		
		private void heapify(int i) {	
			do {
				int min = i;
				if (left(i) < momSize && heapArray[left(i)].getDist() < heapArray[min].getDist())
					min = left(i);
				if (right(i) < momSize && heapArray[right(i)].getDist() < heapArray[min].getDist())
					min = right(i);
				if (min == i)
					break;
				swap(i, min);
				i = min;
			} while(true);
		}
		  
		/**
		 * This method builds the heap
		 */
		private void build(){
			if (heapArray.length == 0)
				return;
			for (int i = heapArray.length/2-1; i>=0; --i)
				heapify(i);
		}
		  
		    
		/**
		 * This method decrease the key of the node with index i
		 * @param i Index of the node
		 */
		private void decrease(int i) {
//			myAssert(isHeap(0), "decrease");
			while (i>0 && heapArray[i].getDist() < heapArray[parent(i)].getDist()) {
				swap(i, parent(i));
				i = parent(i);
			}

		}
		
		/**
		 * This method swaps two cluster distances
		 * @param i Index of the first distance
		 * @param j Index of the second distance
		 */
		private void swap(int i, int j) {
			Clusterdistance tmp = heapArray[i];
			heapArray[i] = heapArray[j];
			heapArray[i].indexInHeap = i;
			indexes.put(heapArray[i], i);
			
			heapArray[j] = tmp;
			heapArray[j].indexInHeap = j;
			indexes.put(heapArray[j], j);
		}
		 
		/**
		 * This method removes a given element from the heap if the heap contains the element.
		 * @param element Element to remove.
		 * @return The removed Clusterdistance if exists, null otherwise
		 */
		Clusterdistance remove(Clusterdistance element) {
//			myAssert(isHeap(), "remove");
//			if (!myAssert(isHeap(), "remove")) {
//				System.out.println("test");
//			}
//			
//			if (!myAssert(element.indexInHeap<momSize, "remove")) {
//				System.out.println("test");
//			}
//			if (!myAssert(heapArray[element.indexInHeap].indexInHeap == element.indexInHeap, "remove")) {
//				System.out.println("test");
//			}
			
			if (!indexes.containsKey(element)) return null;
			
			int i = indexes.get(element);
			if (i >= momSize) return null;

			int lastIdx = momSize-1;
			swap(i,lastIdx);
			momSize--;

			if ( i != lastIdx ) {
				if ( i == 0) {
					heapify(i);
				} else if (heapArray[i].getDist() < heapArray[parent(i)].getDist()) {
					decrease(i); 
				} else {
					heapify(i);
				}
			}

			return heapArray[lastIdx];
		}
		
		/**
		 * This method remove the first element in the heap and returns it.
		 * The first element in the heap is the Clusterdistance with the smallest key distance. 
		 * @return The element with the smallest distance in the heap
		 */
		Clusterdistance  extractMin() {
//			myAssert(isHeap(), "extractMin");
			if (momSize==0) {
				return null;
			}
			Clusterdistance res = remove(heapArray[0]);
//			myAssert(isHeap(), "extractMin");
			return res;
		}
		
		/**
		 * This method returns the first element of the heap.
		 * The first element in the heap is the Clusterdistance with the smallest key distance. 
		 * @return The element with the smallest distance in the heap
		 */
		public Clusterdistance getMin() {
//			myAssert(isHeap(), "getMin");
//			myAssert(momSize > 0, "getMin");
			return heapArray[0];
		}
		
		/**
		 * Method for check whether the stored array is a heap.
		 * @return true if the stored array is a heap, false otherwise
		 */
		public boolean isHeap() {
			return isHeap(0);
		}
		
		/**
		 * Method for check whether the stored array is a heap from given node (root of the subtree).
		 * @return true if the stored array is a heap from a given index, false otherwise
		 */
		public boolean isHeap(int index) {
			if (index >= momSize) return true;
			boolean l = left(index) < momSize ? heapArray[index].getDist() <= heapArray[left(index)].getDist() : true;
			boolean r = right(index) < momSize ? heapArray[index].getDist() <= heapArray[right(index)].getDist() : true;
			boolean res = l && r;
			if (!res) System.out.println("heap condition failed at index " + index);
			return res && isHeap(left(index)) && isHeap(right(index));
		}
		
		/**
		 * Prints the represented array to the stdout
		 */
		public void prnt() {
			for (int i = 0; i < momSize; i++) {
				System.out.println("dist = " + heapArray[i].getDist());
			}
		}
		
		/**
		 * Assert Method to print message if the assertion fails.
		 * @param b Boolean to check
		 * @param str String to describe the output
		 * @return true if the assertion is true, false otherwise
		 */
		boolean myAssert(boolean b, String str) {
			if (!b) {
				System.out.println("Assertion fail in " + str);
				return false;
			}
			return true;
		}
		
		/**
		 * This method returns the Clusterdistance for two clusters with indices i and j if the distance exists.
		 * @param i Index of the first cluster 
		 * @param j Index of the second cluster 
		 * @return Cluster distance for clusters with indices i and j if such a distance exists, null otherwise
		 */
		public Clusterdistance getDistanceForIndices(int i, int j) {
			Clusterdistance tmp = new Clusterdistance(0, i, j);
			int ind = -1;
			if (indexes.containsKey(tmp) && (ind = indexes.get(tmp))>-1) 
				return heapArray[ind];
			else 
				return null;
		}
		
		/**
		 * This method changes the indices of a given cluster distance in the heap to (newI, newJ)
		 * @param dist Distance in the Heap which should be changed
		 * @param newI New first Index
		 * @param newJ New second Index
		 */
		public void changeIndicesOf(Clusterdistance dist, int newI, int newJ) {
			if (indexes.containsKey(dist)) {
				int i = indexes.get(dist);
				Clusterdistance newD = new Clusterdistance(heapArray[i].getDist(), newI, newJ);
				newD.indexInHeap = i;
				indexes.remove(dist);
				indexes.put(newD, i);
				heapArray[i] = newD;
			}
//			myAssert(isHeap(), "changeIndicesOf");
		}
					  
		  
		private int momSize;
		private Clusterdistance[] heapArray;
		private HashMap<Clusterdistance, Integer> indexes;
	}
	
	public static void main(String[] args) {

		ArrayList<Point> points = new ArrayList<Point>();
		points.add(new Point(10, 10));
		points.add(new Point(20, 10));
		points.add(new Point(30, 10));
		points.add(new Point(10, 20));
		points.add(new Point(20, 20));
		points.add(new Point(30, 20));
		
		points.add(new Point(50, 20));
		points.add(new Point(60, 20));
		points.add(new Point(50, 30));
		points.add(new Point(60, 30));
		points.add(new Point(30, 40));
		

//		ClusterVisualiser.showPoints(new SimpleAlgorithm(points).getClusterMemberships());
	}
}
