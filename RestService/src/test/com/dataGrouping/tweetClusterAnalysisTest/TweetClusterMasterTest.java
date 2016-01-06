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

import com.daemon.sentiment.LabeledTweetContainer;
import com.daemon.sentiment.SentimentSourceData;
import com.dataGrouping.tweetClusterAnalysis.TweetClusterMaster;
import com.restservice.dto.DataGroupingResult;
import com.restservice.dto.DataGroupingResult.Series;
import com.tmetrics.exceptions.NotDataFoundException;

public class TweetClusterMasterTest {

	SentimentSourceData sourceData;
	List<LabeledTweetContainer> tweetList;
	int numberOfClusters;

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

		// we recognize that tweet1-3 belongts to one group, 4-5 to another...
		this.numberOfClusters = 2;

	}

	@Test
	public void testClusteringByTweetClusterMaster() {

		// a) init
		TweetClusterMaster master = new TweetClusterMaster();
		try {
			master.createModel(this.sourceData, this.numberOfClusters, 1);
		} catch (NotDataFoundException e) {
			e.printStackTrace();
			fail("No hashtags found to test clustering of hashtags");
		}

		// b) compare cluster result of hashtags
		int[] actClusters = master.getHashtagsClusterMemberships();
		int[] expClusters = new int[] { 0, 0, 1 };
		for (int i = 0; i < actClusters.length; i++) {
			// System.out.println(actClusters[i]);
			assertEquals(expClusters[i], actClusters[i]);
		}

		// c) compare identified hashtag groups
		String[] expHashtags = new String[] { "#erwin", "#bjoern", "#tina" };

		int i = 0;
		for (String s : master.getHashtagsAndTheirVirtualDocument().keySet()) {
			assertEquals(expHashtags[i], s);
			i++;
		}

	}
}
