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
package com.news;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.restservice.database.Transactor;
import com.restservice.dto.NewsItem;
import com.restservice.dto.Query;
import com.restservice.dto.TweetTexts;

public class TopNewsFetcherThread implements Callable<ArrayList<NewsItem>> {

	public static Transactor transactor = null;
	private Long searchtermId;
	private String keyword;
	private Integer day;
	private Integer month;
	private Integer year;
	private ExecutorService feedfetcherexecutor;
	ArrayList<Future<ArrayList<NewsItem>>> feedfetcherresults;

	public TopNewsFetcherThread() {
		if (transactor == null) {
			transactor = new Transactor();
		}
	}

	public TopNewsFetcherThread(Long searchtermId, Integer day, Integer month,
			Integer year) {
		this();
		this.searchtermId = searchtermId;
		this.day = day;
		this.month = month;
		this.year = year;
	}

	@Override
	public ArrayList<NewsItem> call() {
		try {
			// get searchterm id from database
			synchronized (transactor) {
				Query query = transactor.getSearchTerms(this.searchtermId);
				this.keyword = query.getString();
			}
			// build urls
			String[][] providerUrls = NewsUtil.getProviderURLs(keyword, day,
					month, year);
			// start feed fetching
			startFeedFetchers(providerUrls);
			// get tweetstexts from database
			String tweetsText = "";
			synchronized (transactor) {
				String datetime = this.year + "-" + this.month + "-" + this.day
						+ " 00:00:00";
				TweetTexts aggregatedTweets = transactor.getTagCloud(
						this.searchtermId, null, (long) 100, datetime, false);
				tweetsText = aggregatedTweets.getText();
			}
			// count tweetstext words
			HashMap<String, Double> ratingTextCounts = NewsUtil
					.listWordProportions(tweetsText);
			// retrieve feedfetcher results
			ArrayList<NewsItem> feeds = getFeedFetchersResult();
			// rate feeds
			feeds = rateFeeds(feeds, ratingTextCounts);
			// filter feeds for top results
			feeds = NewsUtil.filterNewsByRating(feeds);
			return feeds;
		} catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public ArrayList<NewsItem> rateFeeds(ArrayList<NewsItem> feedItems,
			HashMap<String, Double> originaltextCounts) {
		for (NewsItem feedItem : feedItems) {
			feedItem.setRating(NewsUtil.rateText(feedItem.toString(),
					originaltextCounts));
		}
		return feedItems;
	}

	public void startFeedFetchers(String[][] providerUrls) {
		// start rss feed fetching threads
		this.feedfetcherexecutor = Executors
				.newFixedThreadPool(providerUrls.length);
		this.feedfetcherresults = new ArrayList<Future<ArrayList<NewsItem>>>();
		for (String[] providerUrl : providerUrls) {
			this.feedfetcherresults.add(this.feedfetcherexecutor
					.submit(new FeedFetcherThread(providerUrl[0],
							providerUrl[1])));
		}
	}

	private ArrayList<NewsItem> getFeedFetchersResult()
			throws InterruptedException, ExecutionException {
		// collect results and delete duplicates
		feedfetcherexecutor.shutdown();
		ArrayList<NewsItem> accumulatedFeeds = new ArrayList<NewsItem>();
		for (Future<ArrayList<NewsItem>> thread : this.feedfetcherresults) {
			ArrayList<NewsItem> result = thread.get();
			if (result != null) {
				// System.out.println("AAA" + result.size());
				accumulatedFeeds.addAll(result);
			}
		}
		return accumulatedFeeds;
	}
}
