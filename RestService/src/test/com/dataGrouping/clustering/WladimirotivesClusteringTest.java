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

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import com.dataGrouping.clustering.WladimirotivesClustering;

/**
 * Test class for testing SimpleAlgorithm class
 * 
 * @author Wladimir
 * 
 */
public class WladimirotivesClusteringTest extends WladimirotivesClustering {

	private double[][] distances = { { 0, 1.1, 1.4, 9.7, 1.2, 1.9 },
			{ 1.1, 0, 1.7, 9.3, 0.8, 1.6 }, { 1.4, 1.7, 0, 4.1, 1.9, 3.0 },
			{ 9.7, 9.3, 4.1, 0, 2.8, 2.6 }, { 1.2, 0.8, 1.9, 2.8, 0, 0.3 },
			{ 1.9, 1.6, 3.0, 2.6, 0.3, 0 } };

	/**
	 * cluster distances will be set here to our standard input matrix
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Test for our standard input matrix.
	 */
	@Test
	public void testStandardInput() {
		WladimirotivesClustering test = new WladimirotivesClustering(distances, 2);
		int[] actuals = test.getClusterMemberships();
		int[] expecteds = { 0, 0, 0, 1, 1, 1 };
		assertArrayEquals(expecteds, actuals);
	}

	/**
	 * Test for unusual input: parameter == null.
	 */
	@Test
	public void testParameterIsNull() {
		try {
			new WladimirotivesClustering(null, 2).getClusterMemberships();
			fail("Should have thrown an exception");
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}

	/**
	 * Test for unusual input: parameter is empty.
	 */
	@Test
	public void testParameterIsEmpty() {
		try {
			new WladimirotivesClustering(new double[][] { {} }, 2).getClusterMemberships();
			fail("Should have thrown an exception");
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}

	/**
	 * Test for unusual input: parameter is not a square matrix.
	 */
	@Test
	public void testParameterIsSquareMatrix() {
		try {
			new WladimirotivesClustering(new double[][] { { 0, 1 } }, 2)
					.getClusterMemberships();
			fail("Should have thrown an exception");
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		try {
			new WladimirotivesClustering(new double[][] { { 0 }, { 1 } }, 2)
					.getClusterMemberships();
			fail("Should have thrown an exception");
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		try {
			new WladimirotivesClustering(new double[][] { { 0, 1 }, { 1, 0 } }, 2)
					.getClusterMemberships();
		} catch (IllegalArgumentException e) {
			fail("Shouldn't have thrown an exception");
		}
	}

	/**
	 * Test for inner class PairOfIndizes
	 */
	@Test
	public void testInnerClassPairOfIndizes() {
		WladimirotivesClustering.PairOfIndizes a = new WladimirotivesClustering.PairOfIndizes(1,
				2);
		WladimirotivesClustering.PairOfIndizes b = new WladimirotivesClustering.PairOfIndizes(2,
				1);
		assertTrue(a.hashCode() == b.hashCode());
		assertTrue(a.equals(b));
	}

	/**
	 * Test for inner class Clusterdistance
	 */
	@Test
	public void testInnerClassClusterdistance() {
		WladimirotivesClustering.Clusterdistance a = new WladimirotivesClustering.Clusterdistance(
				1.0, 1, 2);
		WladimirotivesClustering.Clusterdistance b = new WladimirotivesClustering.Clusterdistance(
				2.0, 2, 1);
		WladimirotivesClustering.Clusterdistance c = new WladimirotivesClustering.Clusterdistance(
				1.0, 2, 1);

		WladimirotivesClustering.Clusterdistance aNegativ = new WladimirotivesClustering.Clusterdistance(
				-1.0, 1, 2);
		WladimirotivesClustering.Clusterdistance bNegativ = new WladimirotivesClustering.Clusterdistance(
				-2.0, 2, 1);
		WladimirotivesClustering.Clusterdistance cNegativ = new WladimirotivesClustering.Clusterdistance(
				-1.0, 2, 1);
		assertTrue(a.hashCode() == b.hashCode());
		assertTrue(a.equals(b));

		assertTrue(a.compareTo(b) < 0);
		assertTrue(b.compareTo(a) > 0);
		assertTrue(a.compareTo(a) == 0);
		assertTrue(a.compareTo(c) == 0);

		assertTrue(aNegativ.compareTo(b) < 0);
		assertTrue(aNegativ.compareTo(bNegativ) > 0);
		assertTrue(bNegativ.compareTo(aNegativ) < 0);
		assertTrue(aNegativ.compareTo(cNegativ) == 0);
	}
	
	/**
	 * Test for inner class DistanceHeap
	 */
	@Test
	public void testInnerClassDistanceHeap() {
		WladimirotivesClustering.Clusterdistance ab = new WladimirotivesClustering.Clusterdistance(
				1.0, 1, 2);
		WladimirotivesClustering.Clusterdistance bc = new WladimirotivesClustering.Clusterdistance(
				2.0, 2, 3);
		WladimirotivesClustering.Clusterdistance ac = new WladimirotivesClustering.Clusterdistance(
				3.0, 1, 3);

		WladimirotivesClustering.DistanceHeap distHeap = new WladimirotivesClustering.DistanceHeap(new WladimirotivesClustering.Clusterdistance[]{ac, bc, ab});
		
		assertTrue(distHeap.getMin().getIndizes().equals(ab.getIndizes()));
		
		assertTrue(distHeap.getDistanceForIndices(1, 3).getIndizes().equals(ac.getIndizes()));
		assertTrue(distHeap.getDistanceForIndices(1, 2).getIndizes().equals(ab.getIndizes()));
		assertTrue(distHeap.getDistanceForIndices(2, 3).getIndizes().equals(bc.getIndizes()));
		
		assertTrue(distHeap.isHeap());
		assertTrue(distHeap.isHeap(0));
		assertTrue(distHeap.isHeap(1));
		assertTrue(distHeap.isHeap(2));

		assertTrue(distHeap.extractMin().equals(ab));
		assertTrue(distHeap.remove(new WladimirotivesClustering.Clusterdistance(5, 1, 2)) == null);
		
		assertTrue(distHeap.remove(new WladimirotivesClustering.Clusterdistance(5, 1, 3)).equals(ac));
		
		distHeap.changeIndicesOf(new WladimirotivesClustering.Clusterdistance(5, 2, 3), 5, 6);
		assertTrue(distHeap.getDistanceForIndices(6, 5).equals(new WladimirotivesClustering.Clusterdistance(5, 5, 6)));
	}
}
