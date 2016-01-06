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
package com.dataGrouping.tweetClusterAnalysisTest;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.daemon.sentiment.SentimentSourceData;
import com.daemon.sentiment.LabeledTweetContainer;
import com.dataGrouping.tweetClusterAnalysis.ClusterFeatureMatrix;
import com.tmetrics.util.SparseMatrix;

public class ClusterFeatureMatrixTest {

	SentimentSourceData sourceData;
	List<LabeledTweetContainer> tweetList;

	/**
	 * Prepares a fictitious source Data scenario to test hashtag clustering
	 */
	@Before
	public void setUp() {
		this.sourceData = new SentimentSourceData((long) 42, "en", "local");

		String tweet1 = "#Erwin PC TV Aloha awesome yet";
		String tweet2 = "#Erwin Dot TV Aloha love word";
		String tweet3 = "#Bjoern TV Aloha Aloha german";
		String tweet4 = "#Tina feature text english cool";
		String tweet5 = "#Tina feature woo so aweso woo";

		tweetList = new ArrayList<LabeledTweetContainer>();
		tweetList.add(new LabeledTweetContainer((long) 2000, tweet1, 1));
		tweetList.add(new LabeledTweetContainer((long) 2001, tweet2, 1));
		tweetList.add(new LabeledTweetContainer((long) 2002, tweet3, 1));
		tweetList.add(new LabeledTweetContainer((long) 2003, tweet4, 1));
		tweetList.add(new LabeledTweetContainer((long) 2004, tweet5, 1));

		this.sourceData.setTweets(tweetList);

	}

	@Test
	public void testCreatedFeatureMatrix() {
		ClusterFeatureMatrix matrix = new ClusterFeatureMatrix(this.sourceData);
		matrix.createVirtualDocuments();
		matrix.createClusterFeatureMatrix();

		// System.out.println(matrix.getFeatureMatrix());
		// System.out.println(matrix.getFeatureNamesOrdered());

		// Expected:
		Float[] expRowArray = new Float[] { (float) 0, (float) 0, (float) 1.0,
				(float) 2.0, (float) 0, (float) 0, (float) 0, (float) 0,
				(float) 0, (float) 1.0, (float) 1.0, (float) 0, (float) 0,
				(float) 0, (float) 0, (float) 0, (float) 0, (float) 0,
				(float) 0 };

		SparseMatrix actualMatrix = matrix.getFeatureMatrix();
		List<Float> actualRow = actualMatrix.getRowAsList(1);

		// Compare a row in feature matrix
		for (int i = 0; i < actualRow.size(); i++) {
			assertEquals(expRowArray[i], actualRow.get(i), 0.0001);
		}

		// Compare some feature names
		List<String> actualNames = matrix.getFeatureNamesOrdered();
		assertEquals("pc", actualNames.get(1));
		assertEquals("tv", actualNames.get(2));
		assertEquals("aloha", actualNames.get(3));
		assertEquals("german", actualNames.get(10));
		assertEquals("aweso", actualNames.get(18));

	}

	@Test
	public void testfindNearestHashtagOfTweet() {
		ClusterFeatureMatrix matrix = new ClusterFeatureMatrix(this.sourceData);
		matrix.createVirtualDocuments();
		matrix.createClusterFeatureMatrix();
		
		String tweet1 = "#Erwin PC TV Aloha awesome yet";
		String tweet2 = "#Erwin Dot TV Aloha changed something";
		
		assertEquals(0,matrix.findNearestHashtagOfTweet(tweet1));
		assertEquals(1,matrix.findNearestHashtagOfTweet(tweet2));
	}
}
