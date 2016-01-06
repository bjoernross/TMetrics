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
package com.restservice.serviceLogic;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.joda.time.LocalDateTime;

import com.daemon.sentiment.FeatureMatrix;
import com.daemon.sentiment.Features;
import com.daemon.sentiment.LabeledTweetContainer;
import com.daemon.sentiment.RegressionModel;
import com.daemon.sentiment.RegressionSentimentClassifier;
import com.daemon.sentiment.SentimentSourceData;
import com.dataGrouping.clustering.HierarchicalClusteringAlgorithm;
import com.dataGrouping.clustering.WladimirotivesClustering;
import com.dataGrouping.multiDimensionalScaling.ModifiedSmacofScaling;
import com.dataGrouping.similarityMeasure.BinarySimilarityMeasure;
import com.dataGrouping.tweetClusterAnalysis.TweetClusterMaster;
import com.news.PeaksUtil;
import com.news.TopNewsFetcherThread;
import com.restservice.database.Transactor;
import com.restservice.dto.CountAndNewsPerHour;
import com.restservice.dto.CountPeaksNewsAndDate;
import com.restservice.dto.DataGroupingResult;
import com.restservice.dto.Envelope;
import com.restservice.dto.HashtagStatisticsForSearchTermId;
import com.restservice.dto.LanguageCount;
import com.restservice.dto.News;
import com.restservice.dto.NewsItem;
import com.restservice.dto.SearchTermsPerQueryPerDate;
import com.restservice.dto.SentimentData;
import com.restservice.dto.SentimentPerQueryPerDate;
import com.restservice.dto.Tweet;
import com.restservice.dto.TweetBasic;
import com.restservice.dto.TweetWithUser;
import com.tmetrics.dto.SentimentFeatures;
import com.tmetrics.exceptions.NotDataFoundException;

/**
 * Service logic handling search result related requests
 * 
 * @author
 */
public class ResultLogic {

	private Transactor transactor;

	// the features to group the tweets (clustering)
	private Features clusterFeatures = new Features().useUnigrams(true)
			.useBigrams(false).useTrigrams(false).use4Grams(false)
			.useDictionary(false).useEmoticons(false).usePOSTagger(false)
			.useNegations(false);

	// standard constructor to associate a database transactor with the ResultLogic
	public ResultLogic() {
		transactor = new Transactor();
	}

	// constructor that specifies the location of a properties file to establish a connection with a specific database
	public ResultLogic(String propertiesPath) {
		transactor = new Transactor(propertiesPath);
	}

	// classifier object to get access to the sentiment models created in the Daemon module
	private static RegressionSentimentClassifier regressionSentimentClassifier = new RegressionSentimentClassifier();

	/**
	 * Request handler for getting the count per hour statistic
	 * Additional logic beyond the transactor database connection to insert additional zeros
	 * for dates where no count (row) has been returned
	 * 
	 * @param id
	 *            search term index
	 * @return envelope containing a status message and a search result count
	 *         per date DTO
	 * @throws SQLException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public Envelope getCountAndNewsPerHour(Long id, String lang)
			throws SQLException, InterruptedException, ExecutionException {

		SearchTermsPerQueryPerDate countsPerDay;
		Envelope env = new Envelope();

		countsPerDay = transactor.getCountPerHour(id, lang);

		// Fill with zeros
		ArrayList<Integer> newCounts = new ArrayList<Integer>();
		ArrayList<LocalDateTime> newDates = new ArrayList<LocalDateTime>();
		if (!countsPerDay.getDates().isEmpty()) {
			ArrayList<LocalDateTime> oldDates = new ArrayList<>();
			for (LocalDateTime curDate : countsPerDay.getDates()) {
				oldDates.add(new LocalDateTime(curDate.getYear(), curDate
						.getMonthOfYear(), curDate.getDayOfMonth(), curDate
						.getHourOfDay(), 0));
			}

			newDates.add(oldDates.get(0));
			newCounts.add(countsPerDay.getCounts().get(0));
			for (int i = 1; i < oldDates.size(); i++) {
				if (!oldDates.get(i - 1).plusHours(1).equals(oldDates.get(i))) {
					LocalDateTime startDate = oldDates.get(i - 1);
					LocalDateTime endDate = oldDates.get(i);
					while (!startDate.equals(endDate)) {
						startDate = startDate.plusHours(1);
						if (startDate.equals(endDate)) {
							newDates.add(oldDates.get(i));
							newCounts.add(countsPerDay.getCounts().get(i));
						} else {
							newCounts.add(0);
							newDates.add(startDate);
						}

					}
				} else {
					newDates.add(oldDates.get(i));
					newCounts.add(countsPerDay.getCounts().get(i));
				}
			}
		}

		countsPerDay.setCounts(newCounts);
		countsPerDay.setDates(newDates);
		countsPerDay.updateDateStrings();

		// convert to nice output format
		CountAndNewsPerHour countAndNews = new CountAndNewsPerHour();
		for (Integer index = 0; index < countsPerDay.getCounts().size(); index++) {
			CountPeaksNewsAndDate element = new CountPeaksNewsAndDate();
			element.setRawDate(countsPerDay.getDates().get(index));
			element.setCount(countsPerDay.getCounts().get(index));
			element.setPeak(false);
			countAndNews.getGraph().add(element);
		}
		countAndNews.setQuery(countsPerDay.getQuery());

		// find and marks peaks
		ArrayList<Integer> peakIndices = PeaksUtil.findPeaks24(countAndNews);
		for (Integer peakIndex : peakIndices) {
			countAndNews.getGraph().get(peakIndex).setPeak(true);
		}

		if (peakIndices.size() > 0) {
			// create news fetchers
			HashMap<Integer, Future<ArrayList<NewsItem>>> newsFetchers = new HashMap<Integer, Future<ArrayList<NewsItem>>>();
			ExecutorService executor = Executors.newFixedThreadPool(peakIndices
					.size());
			for (Integer peakIndex : peakIndices) {
				LocalDateTime date = countAndNews.getGraph().get(peakIndex)
						.getRawDate();
				newsFetchers.put(peakIndex, executor
						.submit(new TopNewsFetcherThread(id, date
								.getDayOfMonth(), date.getMonthOfYear(), date
								.getYear())));
			}
			// retrieve news fetchers results
			executor.shutdown();
			java.util.Iterator<Entry<Integer, Future<ArrayList<NewsItem>>>> iterator = newsFetchers
					.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<Integer, Future<ArrayList<NewsItem>>> entry = iterator
						.next();
				ArrayList<NewsItem> result = entry.getValue().get();
				if (result != null) {
					for (NewsItem newsitem : result) {
						countAndNews.getGraph().get(entry.getKey()).getNews()
								.add(newsitem.toShortString());
					}
				}
			}
		}

		env.setData(countAndNews);

		return env;
	}

	/**
	 * Request handler for getting sentiment information
	 * Additional logic to add zeros for dates where no count has been returned
	 * 
	 * @param id
	 *            search term index
	 * @return envelope containing a status message and a sentiment DTO
	 * @throws SQLException
	 */
	public Envelope getSentiments(long id, String lang) throws SQLException {
		Envelope env = new Envelope();

		SentimentData data = transactor.getSentimentData(id, lang);

		env.setData(data);

		return env;
	}

	/**
	 * Request handler for getting the count per hour statistic
	 * 
	 * @param id
	 *            search term index
	 * @param lang
	 *            iso language code of the language (all languages are selected
	 *            if this parameter is null)
	 * 
	 * @return envelope containing a status message and the number of
	 *         positive/negative tweets per hour
	 * @throws SQLException
	 */
	public Envelope getSentimentPerHour(Long id, String lang)
			throws SQLException {

		SentimentPerQueryPerDate data;
		Envelope env = new Envelope();

		// TODO: Filling these zeros shouldn't been done three times (twice
		// here and once in getCountPerHour). Fix it! See ticket #86
		data = transactor.getSentimentPerHour(id, lang);

		ArrayList<LocalDateTime> oldDatesPositive = new ArrayList<>();
		ArrayList<Integer> newCountsPositive = new ArrayList<Integer>();
		ArrayList<LocalDateTime> newDatesPositive = new ArrayList<LocalDateTime>();
		ArrayList<LocalDateTime> oldDatesNegative = new ArrayList<>();
		ArrayList<Integer> newCountsNegative = new ArrayList<Integer>();
		ArrayList<LocalDateTime> newDatesNegative = new ArrayList<LocalDateTime>();

		// Reset minutes, seconds and miliseconds to 0
		if (!data.getPositiveCounts().getDates().isEmpty()) {
			for (LocalDateTime curDate : data.getPositiveCounts().getDates()) {
				oldDatesPositive.add(new LocalDateTime(curDate.getYear(),
						curDate.getMonthOfYear(), curDate.getDayOfMonth(),
						curDate.getHourOfDay(), 0));
			}
		}
		if (!data.getNegativeCounts().getDates().isEmpty()) {
			for (LocalDateTime curDate : data.getNegativeCounts().getDates()) {
				oldDatesNegative.add(new LocalDateTime(curDate.getYear(),
						curDate.getMonthOfYear(), curDate.getDayOfMonth(),
						curDate.getHourOfDay(), 0));
			}
		}

		// Get first date from both (positive or negative) and fill the
		// other one with leading zero counts
		if (!oldDatesPositive.isEmpty() && !oldDatesNegative.isEmpty()) {
			// The first positive date is earlier than the first negative
			// date
			if (oldDatesPositive.get(0).compareTo(oldDatesNegative.get(0)) == -1) {
				LocalDateTime curDate = oldDatesPositive.get(0);
				while (!curDate.equals(oldDatesNegative.get(0))) {
					newCountsNegative.add(0);
					newDatesNegative.add(curDate);
					curDate = curDate.plusHours(1);
				}
			}
			// The first negative date is earlier than the first positive
			// date
			else if (oldDatesPositive.get(0).compareTo(oldDatesNegative.get(0)) == 1) {
				LocalDateTime curDate = oldDatesNegative.get(0);
				while (!curDate.equals(oldDatesPositive.get(0))) {
					newCountsPositive.add(0);
					newDatesPositive.add(curDate);
					curDate = curDate.plusHours(1);
				}
			}
		}

		// Fill hours that have 0 counts for positive tweets
		if (!oldDatesPositive.isEmpty()) {
			newDatesPositive.add(oldDatesPositive.get(0));
			newCountsPositive.add(data.getPositiveCounts().getCounts().get(0));
			for (int i = 1; i < oldDatesPositive.size(); i++) {
				if (!oldDatesPositive.get(i - 1).plusHours(1)
						.equals(oldDatesPositive.get(i))) {
					LocalDateTime startDate = oldDatesPositive.get(i - 1);
					LocalDateTime endDate = oldDatesPositive.get(i);
					while (!startDate.equals(endDate)) {
						startDate = startDate.plusHours(1);
						if (startDate.equals(endDate)) {
							newDatesPositive.add(oldDatesPositive.get(i));
							newCountsPositive.add(data.getPositiveCounts()
									.getCounts().get(i));
						} else {
							newCountsPositive.add(0);
							newDatesPositive.add(startDate);
						}

					}
				} else {
					newDatesPositive.add(oldDatesPositive.get(i));
					newCountsPositive.add(data.getPositiveCounts().getCounts()
							.get(i));
				}
			}
		}

		// Fill hours that have 0 counts for negative tweets
		if (!oldDatesNegative.isEmpty()) {
			newDatesNegative.add(oldDatesNegative.get(0));
			newCountsNegative.add(data.getNegativeCounts().getCounts().get(0));
			for (int i = 1; i < oldDatesNegative.size(); i++) {
				if (!oldDatesNegative.get(i - 1).plusHours(1)
						.equals(oldDatesNegative.get(i))) {
					LocalDateTime startDate = oldDatesNegative.get(i - 1);
					LocalDateTime endDate = oldDatesNegative.get(i);
					while (!startDate.equals(endDate)) {
						startDate = startDate.plusHours(1);
						if (startDate.equals(endDate)) {
							newDatesNegative.add(oldDatesNegative.get(i));
							newCountsNegative.add(data.getNegativeCounts()
									.getCounts().get(i));
						} else {
							newCountsNegative.add(0);
							newDatesNegative.add(startDate);
						}

					}
				} else {
					newDatesNegative.add(oldDatesNegative.get(i));
					newCountsNegative.add(data.getNegativeCounts().getCounts()
							.get(i));
				}
			}
		}

		// Fill negative with zeros when only positive exists
		if (!newDatesPositive.isEmpty() && newDatesNegative.isEmpty()) {
			for (LocalDateTime curDate : newDatesPositive) {
				newCountsNegative.add(0);
				newDatesNegative.add(curDate);
			}
		}
		// Fill positive with zeros when only negative exists
		else if (newDatesPositive.isEmpty() && !newDatesNegative.isEmpty()) {
			for (LocalDateTime curDate : newDatesNegative) {
				newCountsPositive.add(0);
				newDatesPositive.add(curDate);
			}
		}

		// Get last date from both (positive or negative) and fill the other
		// one with trailing zero counts
		if (!newDatesPositive.isEmpty() && !newDatesNegative.isEmpty()) {
			// The last positive date is later than the last negative date
			if (newDatesPositive.get(newDatesPositive.size() - 1).compareTo(
					newDatesNegative.get(newDatesNegative.size() - 1)) == -1) {
				LocalDateTime curDate = newDatesPositive.get(newDatesPositive
						.size() - 1);
				while (!curDate.equals(newDatesNegative.get(newDatesNegative
						.size() - 1))) {
					newCountsNegative.add(0);
					newDatesNegative.add(curDate);
					curDate = curDate.plusHours(1);
				}
			}
			// The last negative date is later than the last positive date
			else if (newDatesPositive.get(newDatesPositive.size() - 1)
					.compareTo(
							newDatesNegative.get(newDatesNegative.size() - 1)) == 1) {
				LocalDateTime curDate = newDatesNegative.get(newDatesNegative
						.size() - 1);
				while (!curDate.equals(newDatesPositive.get(newDatesPositive
						.size() - 1))) {
					newCountsPositive.add(0);
					newDatesPositive.add(curDate);
					curDate = curDate.plusHours(1);
				}
			}
		}

		data.getPositiveCounts().setCounts(newCountsPositive);
		data.getPositiveCounts().setDates(newDatesPositive);
		data.getPositiveCounts().updateDateStrings();

		data.getNegativeCounts().setCounts(newCountsNegative);
		data.getNegativeCounts().setDates(newDatesNegative);
		data.getNegativeCounts().updateDateStrings();

		env.setData(data);

		return env;
	}

	/**
	 * Request handler for getting statistics for a given search term id
	 * 
	 * @param id
	 *            Id of the search term
	 * @return Statistics for a given search term id
	 * @throws SQLException
	 */
	public Envelope getHashtagStatisticsForSearchTermId(Long id, String lang,
			Long limit) throws SQLException {
		Envelope env = new Envelope();

		HashtagStatisticsForSearchTermId data = transactor
				.getHashtagStatisticsForSearchTermId(id, lang, limit);
		env.setData(data);

		return env;
	}

	/**
	 * Request handler for getting tweets from a specified search term
	 * Optional: specific hash tag
	 * Optional: specific sentiment upper threshold
	 * Optional: specific sentiment lower threshold
	 * Optional: specific earliest start date
	 * Optional: specific latest end date
	 * @param id
	 *            search term index
	 * @return envelope containing a status message and a search term tweets DTO
	 * @throws SQLException
	 */
	public Envelope getTweetsForSearchTerm(long id, float sentTop,
			float sentBottom, String start, String end, String lang,
			Long hashTagId, int limit) throws SQLException {
		Envelope env = new Envelope();

		File[] files = new File(System.getProperty("user.home")).listFiles();

		// check whether the regression model files have been updated
		for (File file : files) {
			if (file.isFile() && file.getName().startsWith("regression_model_")) {
				String key = file.getName().substring(17, 19);
				RegressionModel model = regressionSentimentClassifier
						.getModels().get(key);
				
				if (model == null)
					System.out.println("Model for key " + key + " is null");
				
				if (Math.abs(model.getCreatedAt() - file.lastModified()) > 1000) {
					System.out.println("new file detected with key: " + key);
					regressionSentimentClassifier = null;
					regressionSentimentClassifier = new RegressionSentimentClassifier();

				}
			}
		}

		// get data from database
		ArrayList<TweetWithUser> tweets = transactor.getSearchTermTweets(id,
				sentTop, sentBottom, start, end, lang, hashTagId, limit);

		// use sentiment classifier to determinent sentiment details for the fetched tweets
		for (TweetWithUser usertweet : tweets) {
			TweetBasic tweet = usertweet.getTweet();
			String text = tweet.getText();
			String language = tweet.getLang().getIsoCode();
			SentimentFeatures sf = regressionSentimentClassifier
					.determineSentimentDetails(text, language);
			tweet.setSentimentFeatures(sf);
		}

		env.setData(tweets);

		return env;
	}

	/**
	 * Request handler for getting a single tweet
	 * 
	 * @param id
	 *            tweet index
	 * @return envelope containing a status message and a tweet DTO
	 * @throws SQLException
	 */
	public Envelope getTweet(String id) throws SQLException {
		Envelope env = new Envelope();

		Tweet tweet = transactor.getTweet(id);
		
		// no additional check for update necessary because it is always
		// called together with getTweetsForSearchTerm()

		// use sentiment classifier to determine sentiment details for the fetched tweet
		if (tweet != null) {
			String text = tweet.getText();
			String language = tweet.getLang().getIsoCode();
			SentimentFeatures sf = regressionSentimentClassifier
					.determineSentimentDetails(text, language);
			tweet.setSentimentFeatures(sf);
		}

		env.setData(tweet);

		return env;
	}

	public Envelope getUser(String id) throws SQLException {
		Envelope env = new Envelope();

		env.setData(transactor.getUser(id));

		return env;
	}

	public Envelope getTagCloud(Long id, String lang, Long count)
			throws SQLException {
		Envelope env = new Envelope();

		env.setData(transactor.getTagCloud(id, lang, count));

		return env;
	}

	/**
	 * Newer Request handler for getting the data groups of a given search term
	 * id, language and limit (number of tweets). In contrast to previous
	 * approach, we group hashtags, classifiy each tweet to such a hashtag
	 * cluster and return the hashtag clusters with its tweets.
	 * 
	 * @param id
	 *            search term index
	 * @param lang
	 *            language of tweets that will be grouped together.
	 * @param limit
	 *            number of tweets that will be grouped together.
	 * @param database
	 *            the database which should be used for data grouping (internal
	 *            value, no user input)
	 * 
	 * @return envelope containing the data groups
	 * @throws Exception
	 */
	public Envelope getDataGroups(Long id, String lang, int limit,
			String database) throws Exception {
		Envelope env = new Envelope();

		limit = 15000;
		
		try {
			// a) get data
			SentimentSourceData sourceData;
			if (database != null && database.equals("RestTest")) {
				sourceData = new SentimentSourceData(id, lang, "RestTest");
			} else {
				sourceData = new SentimentSourceData(id, lang, "Local");
			}

			sourceData.readClusterDataFromDB(limit);

			// b) do clustering
			TweetClusterMaster clusterMaster = new TweetClusterMaster();
			clusterMaster.createModel(sourceData, 12, 30);

			//		DataGroupingResult result = clusterMaster
			//				.determineClusterMembershipOfTweets(sourceData);
			DataGroupingResult result = clusterMaster.returnHastagClusters();
			// c) prepare return result
			env.setData(result);
		}
		catch (IllegalArgumentException e) {
			env.setData(null);
		}
		catch (NotDataFoundException e) {
			env.setData(null);
		}

		return env;

	}

	/**
	 * Request handler for getting the data groups of a given search term id,
	 * language and limit (number of tweets). This method groups tweets by
	 * measuring the word similarity between each pair of tweet.
	 * 
	 * @param id
	 *            search term index
	 * @param lang
	 *            language of tweets that will be grouped together.
	 * @param limit
	 *            number of tweets that will be grouped together.
	 * @param database
	 *            the database which should be used for data grouping (internal
	 *            value, no user input)
	 * 
	 * @return envelope containing the data groups
	 * @throws Exception
	 */
	public Envelope getDataGroupsAlternative(Long id, String lang, int limit,
			String database) throws Exception {
		Envelope env = new Envelope();

		try {
			// Data Grouping Logic
			// I. Get Data (uses sourceData from Sentiment)
			SentimentSourceData sourceData;
			if (database != null && database.equals("RestTest")) {
				sourceData = new SentimentSourceData(id, lang, "RestTest");
			} else {
				sourceData = new SentimentSourceData(id, lang, "Local");
			}

			sourceData.readClusterDataFromDB(limit);

			FeatureMatrix featureMatrix = new FeatureMatrix(this.clusterFeatures,
					sourceData);

			// II. Similarity Measure
			double[][] binaryDissimilarityMatrix = BinarySimilarityMeasure
					.getDissimilaritySparseMatrix(
							featureMatrix.getFeatureMatrixAsSparseMatrix(),
							"Jaccard");

			// III.a Clustering
			HierarchicalClusteringAlgorithm clusterer = new WladimirotivesClustering(
					binaryDissimilarityMatrix, 10);
			// Alternatively:
			// HierarchicalClusteringAlgorithm clusterer = new
			// SingleLinkageClustering(binaryDissimilarityMatrix);

			int[] clusterResult = clusterer.getClusterMemberships();

			// III.b MultiDimensionalScaling
			ModifiedSmacofScaling sc = new ModifiedSmacofScaling(
					binaryDissimilarityMatrix, clusterResult,
					clusterer.getCountOfClusters());
			double[][] mds = sc.getMDS();

			// IV. Return
			DataGroupingResult dataGroupingResult = new DataGroupingResult(
					clusterResult, mds, (ArrayList) sourceData.getTweetIds());

			env.setData(dataGroupingResult);
		}
		catch (IllegalArgumentException e) {
			env.setData(null);
		}
		catch (NotDataFoundException e) {
			env.setData(null);
		}
		return env;
	}

	public Envelope getLanguages(Long id) throws SQLException {
		Envelope env = new Envelope();

		ArrayList<LanguageCount> data = transactor.getLanguages(id);
		env.setData(data);

		return env;
	}

	public Envelope getImportantTrainingTweets(String feature, String language) {
		Envelope env = new Envelope();

		List<LabeledTweetContainer> data = regressionSentimentClassifier
				.determineImportantTrainingTweets(feature, language);
		env.setData(data);

		return env;
	}

	/**
	 * Request handler for getting news related to a searchterm and day
	 * 
	 * @param id
	 *            search term index
	 * @param lang
	 *            envelope containing a news DTO
	 * @param day
	 * @param month
	 * @param year
	 * @return envelope containing a news DTO
	 * @throws SQLException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public Envelope getRelatedNews(Long id, String lang, Integer day,
			Integer month, Integer year) throws SQLException,
			InterruptedException, ExecutionException {
		Envelope env = new Envelope();

		News news = new News();
		TopNewsFetcherThread newsfetcher = new TopNewsFetcherThread(id, day,
				month, year);
		news.setNews(newsfetcher.call());
		env.setData(news);

		return env;
	}

	/**
	 * Pass along the database query to the ResultService
	 * 
	 */
	public String getTimeLastFetched(Long id) {
		try {
			return transactor.getTimeLastFetched(id);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

}
