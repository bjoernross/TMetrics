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
import org.junit.Test;
import com.dataGrouping.clustering.SingleLinkageClustering;

public class SingleLinkageClusteringTest {

	@Test
	public final void test() {

		// double matrix[][] = {
		// {0, 3.1, 2.4, 4.7, 1.2, 2.9},
		// {0, 0, 1.7, 2.3, 0.8, 3.6},
		// {0, 0, 0, 4.1, 1.9, 3.0},
		// {0, 0, 0, 0, 2.8, 2.6},
		// {0, 0, 0, 0, 0, 0.3},
		// {0, 0, 0, 0, 0, 0}
		// };

		// double matrix[][] = { { 0, 3.1, 2.4, 4.7, 1.2, 2.9 },
		// { 3.1, 0, 1.7, 2.3, 0.8, 3.6 }, { 2.4, 1.7, 0, 4.1, 1.9, 3.0 },
		// { 4.7, 2.3, 4.1, 0, 2.8, 2.6 }, { 1.2, 0.8, 1.9, 2.8, 0, 0.3 },
		// { 2.9, 3.6, 3.0, 2.6, 0.3, 0 } };

		double matrix[][] = { { 0, 0, 0, 0, 0, 0 }, { 3.1, 0, 0, 0, 0, 0 },
				{ 2.4, 1.7, 0, 0, 0, 0 }, { 4.7, 2.3, 4.1, 0, 0, 0 },
				{ 1.2, 0.8, 1.9, 2.8, 0, 0 }, { 2.9, 3.6, 3.0, 2.6, 0.3, 0 } };

		SingleLinkageClustering test = new SingleLinkageClustering(matrix);
		int[] actuals = test.getClusterMemberships();
		int[] expecteds = { 0, 1, 2, 3, 4, 4 };

		assertArrayEquals(expecteds, actuals);
		assertEquals(5, test.getCountOfClusters());

		// System.out.println(test.getCountOfClusters());
		// test.printClusterMembership();

	}

}
