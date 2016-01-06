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
package com.daemon;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.TimeZone;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import com.daemon.database.SearchTerm;
import com.tmetrics.logging.LogManager;
import com.tmetrics.logging.Logger;
import com.tmetrics.util.Localization;
import com.tmetrics.util.MapUtil;

public class Minion extends Observable {
	
	private int _numRequests = 0;
	
	private Twitter _twitter = null;
	
	private Master _master = null;
	
	private TwitterProfile _twitterProfile = null;
	
	private List<SearchTerm> _searchTerms = null;
	
	private List<SearchTerm> _shortTerms = null;
	
	private List<SearchTerm> _longTerms  = null;
	
	private String _logFilename = null;
	
	private Logger _logger = null;
	
	private int _limitPerSearchTerm;
	
	private DaemonProperties _props = null;

	/**
	 * Creates a new minion instance.
	 * @param twitterProfileFilename The filename of the Twitter profile to be used by this minion.
	 * @param shortTerms The list of short terms. DO NOT alter this list.
	 * @param longTerms The list of long terms. DO NOT alter this list.
	 */
	public Minion(Master master, TwitterProfile twitterProfile, List<SearchTerm> searchTerms, List<SearchTerm> shortTerms, List<SearchTerm> longTerms) {
		this(master, twitterProfile, searchTerms, shortTerms, longTerms, master.getDaemonProperties().unlimitedRequestsPerSearchTerm);
	}
	
	/**
	 * Creates a new minion instance.
	 * @param twitterProfileFilename The filename of the Twitter profile to be used by this minion.
	 * @param shortTerms The list of short terms. DO NOT alter this list.
	 * @param longTerms The list of long terms. DO NOT alter this list.
	 */
	public Minion(Master master, TwitterProfile twitterProfile, List<SearchTerm> searchTerms, List<SearchTerm> shortTerms, List<SearchTerm> longTerms, int limitPerSearchTerm) {
		assert(master != null);
		assert(searchTerms != null);
		assert(shortTerms != null);
		assert(longTerms != null);
		assert(shortTerms.size() + longTerms.size() >= searchTerms.size());
		
		_master = master;
		
		_twitterProfile = twitterProfile;
		
		_searchTerms = searchTerms;
		
		_shortTerms = shortTerms;
		_longTerms  = longTerms;
		
		_logFilename = "logs/Minions.log";
		
		_limitPerSearchTerm = limitPerSearchTerm;
		
		_props = master.getDaemonProperties();
	}
	
	public void run() {
		// Set the time zone for the minion to UTC
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		DateTimeZone.setDefault(DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC")));
		
		System.out.println(prependInfo("Started."));
		
		// Initialize error log
		_logger = LogManager.getLogger(_logFilename);

		// Minion logic

		// We do not want to work with the search term lists as given, but 
		// want to store a lot of meta data for each search term, so we work
		// with the meta data lists instead. So we need to create and initialize
		// the new lists.
		List<SearchTermMetaData> shortTermMetaData = null;
		List<SearchTermMetaData> longTermMetaData  = null;
		
		// Initialize map holding all tweets
		Map<SearchTerm, List<Status>> allTweetsMap = new HashMap<SearchTerm, List<Status>>();
		for (SearchTerm term : _searchTerms) {
			allTweetsMap.put(term, new LinkedList<Status>());
		}
		
		try
		{
			// Get the twitter object for this profile
			_twitter = _twitterProfile.getTwitterObject();

			// Convert the search terms to meta data search terms
			shortTermMetaData = convertSearchTerms(_shortTerms);
			longTermMetaData  = convertSearchTerms(_longTerms);
			
			// We expect to fetch all tweets with one search for each short search term, so there
			// is no need for a enclosing while loop.
			MapUtil.fillupMap(allTweetsMap, fetchTweetsForSearchTerms(shortTermMetaData));
		
			// We expect the long search terms to run more than once. For every loop iteration
			// each search term in the long list gets one search, except for the newer search terms,
			// which get more search requests per loop iteration. See Minion.fillUpList(...).
			while (countFilteredSearchTerms(longTermMetaData) < longTermMetaData.size() && _twitterProfile.getUsedRateLimit() < _props.maxRateLimit) {
				MapUtil.fillupMap(allTweetsMap, fetchTweetsForSearchTerms(longTermMetaData));
			}
		}
 		catch (TwitterException te)
 		{
			// If there is something wrong with twitter, we are unable to do anything about it
			_logger.logStackTrace(te, _twitterProfile.getScreenName());
			
			System.err.println(prependInfo("Error during communicating with Twitter. Consult " + _logFilename + " for further information."));
 		}
		catch (Exception cnfe) {
			_logger.logStackTrace(cnfe, _twitterProfile.getScreenName());
			
			System.err.println(prependInfo("Cannot load the JDBC driver. Consult " + _logFilename + " for further information."));
		}
		finally {
			int countTweetsTotal = 0;
			
			// Used to count new tweets for the same search term (that can be splitted over many
			// search term meta data objects
			Map<SearchTerm, Integer> searchTermMap = new HashMap<SearchTerm, Integer>();
			
			for (SearchTerm term : _searchTerms) {
				searchTermMap.put(term, 0);
			}
			
			// At the end of the session update each search term's interval length
			// and the count of new fetched tweets
			
			// Short terms
			if (shortTermMetaData != null) {
				for (SearchTermMetaData metaData : shortTermMetaData) {
					updateIntervalLength(metaData);
					metaData.getSearchTerm().setLastFetchedTweetCount(metaData.getTweetCount());
					countTweetsTotal += metaData.getTweetCount();
					
					searchTermMap.put(metaData.getSearchTerm(), searchTermMap.get(metaData.getSearchTerm()) + metaData.getTweetCount());
				}
			}
			
			// Long terms
			if (longTermMetaData != null) {
				for (SearchTermMetaData metaData : longTermMetaData) {
					updateIntervalLength(metaData);
					metaData.getSearchTerm().setLastFetchedTweetCount(metaData.getTweetCount());
					countTweetsTotal += metaData.getTweetCount();
					
					searchTermMap.put(metaData.getSearchTerm(), searchTermMap.get(metaData.getSearchTerm()) + metaData.getTweetCount());
				}
			}
			
			// Output new tweets for search terms
			for (SearchTerm term : _searchTerms) {
				System.out.println(prependInfo("Fetched  " + searchTermMap.get(term) + "  new tweet(s) since last search for term '" + term.getTerm() + "'."));
			}

			// Output for the user
			System.out.println(prependInfo("Fetched  " + countTweetsTotal + "  tweets in total"));
			System.out.println("                     for " + _searchTerms.size() + " search term(s),");
			System.out.println("                     in " + _numRequests + " requests.");
			
			// Inform master about finishing the work
			MessageType messageType = MessageType.MINION_FINISHED;
			
			// If the is a limited minion, the type changes
			if (_limitPerSearchTerm != _props.unlimitedRequestsPerSearchTerm) {
				messageType = MessageType.LIMITEDMINION_FINISHED;
			}

			// Create packages for each search term
			List<Package> tweetPackages = new LinkedList<Package>();
			for (Map.Entry<SearchTerm, List<Status>> entry : allTweetsMap.entrySet()) {
				// The date of the package is now
				tweetPackages.add(new Package(entry.getValue(), new SearchTerm(entry.getKey()), new DateTime()));
			}
			
			_master.update(this, new MinionData(messageType, _searchTerms, tweetPackages));
			// clear Tweets map Afterwards
			allTweetsMap.clear();
			System.out.println(prependInfo("Exited."));
		}
	}

	/**
	 * Tests whether the date of the tweet is older than the given old starting date.
	 * @param tweetDate The date of the tweet to be tested.
	 * @param oldStart The date to be checked against.
	 * @return True, if the tweet date is older than the date of old start.
	 */
	public static boolean tweetIsTooOld(DateTime tweetDate, DateTime oldStart) {
		return tweetDate.minus(oldStart.getMillis()).getMillis() < 0;
	}
	
	/**
	 * Removes all occurrences of the term from the list of search terms.
	 * @param searchTerms The list to be filtered.
	 * @param term The search term to be removed.
	 */
	public static void filterSearchTerms(List<SearchTerm> searchTerms, SearchTerm term) {
		Iterator<SearchTerm> iter = searchTerms.iterator();
		while (iter.hasNext()) {
			SearchTerm current = iter.next();
			
			if (current.equals(term)) {
				iter.remove();
			}
		}
	}

	/**
	 * Counts the number of new tweets compared to the old start date of term in O(log n).
	 * @param term The term containing the old start date.
	 * @param tweets The tweets to be filtered, so we only count the new ones.
	 * @return Returns the number of new tweets contained in the tweets list.
	 */
	public static int countNewTweets(SearchTerm term, List<Status> tweets) {
		return countNewTweetsImpl(term, tweets, 0, tweets.size() - 1);
	}
	
	/**
	 * Counts the number of new tweets from newer to older compared to the old start date
	 * of term in O(log n).
	 * @param term The term containing the old start date.
	 * @param tweets The tweets to be filtered, so we only count the new ones.
	 * @param newer The newer (lower) bound of the tweets list.
	 * @param older The older (upper) bound of the tweets list.
	 * @return Returns the number of new tweets contained in the tweets list.
	 */
	private static int countNewTweetsImpl(SearchTerm term, List<Status> tweets, int newer, int older) {
		// If the boundaries are identical, we only have one tweet left and check,
		// whether it is older or newer
		if (newer == older) {
			if (tweetIsTooOld(new DateTime(tweets.get(newer).getCreatedAt()), term.getOldStart())) {
				// It is an old tweet, so we return (older - 1) + 1, because the older tweet
				// shall not be counted (- 1) but, because the list starts at index 0, we add 1.
				return older;
			}
			else {
				// It is an new tweet, so we return newer + 1, because the list starts at
				// index 0, so we add 1.
				return newer + 1;
			}
		}
		else if (newer == older - 1) {
			if (tweetIsTooOld(new DateTime(tweets.get(newer).getCreatedAt()), term.getOldStart())) {
				// It is an old tweet, so we return (newer - 1) + 1, because the older tweet
				// shall not be counted (- 1) but, because the list starts at index 0, we add 1.
				return newer;
			}
			else {
				if (tweetIsTooOld(new DateTime(tweets.get(older).getCreatedAt()), term.getOldStart())) {
					// It is an old tweet, so we return (older - 1) + 1, because the older tweet
					// shall not be counted (- 1) but, because the list starts at index 0, we add 1.
					return older;
				}
				else {
					// It is an new tweet, so we return older + 1, because the list starts at
					// index 0, so we add 1.
					return older + 1;
				}
			}
		}
		
		int middle = newer + (older - newer) / 2;
		
		// If the middle tweet's date is older that the term's old start date, 
		if (tweetIsTooOld(new DateTime(tweets.get(middle).getCreatedAt()), term.getOldStart())) {
			// we execute a recursive call and jump into the newer branch of the list.
			return countNewTweetsImpl(term, tweets, newer, middle);
		}
		else {
			// we execute a recursive call and jump into the older branch of the list.
			return countNewTweetsImpl(term, tweets, middle, older);
		}
	}
	
	/**
	 * Maps the priority input (-2, -1, 0, 1, 2) to the internal used factor.
	 * @param input The priority to be mapped.
	 * @param props The properties file that is used by the daemon.
	 * @return The internal used factor.
	 * @throws IllegalArgumentException Thrown, if the input value is not within
	 * the allowed number range.
	 */
	private static double mapPriority(int input, DaemonProperties props) throws IllegalArgumentException {
		switch (input) {
		case -2:
			return props.priorityFactors[0];
		case -1:
			return props.priorityFactors[1];
		case 0:
			return props.priorityFactors[2];
		case 1:
			return props.priorityFactors[3];
		case 2:
			return props.priorityFactors[4];
		}
		
		throw new IllegalArgumentException("Priority input must be integer in [-2; 2].");
	}
	
	/**
	 * Counts the number of filtered search terms.
	 * @param searchTermMap The map of search terms to be analyzed.
	 * @return Returns the number of filtered search terms.
	 */
	private static int countFilteredSearchTerms(List<SearchTermMetaData> metaData) {
		int filteredSearchTermsCount = 0;
		
		for (SearchTermMetaData data : metaData) {
			if (data.isFiltered()) {
				filteredSearchTermsCount++;
			}
		}
		
		return filteredSearchTermsCount;
	}
	
	/**
	 * Calculates the interval length for the given search term (and its meta data).
	 * @param metaData The meta data with search term for which the new interval length
	 * should be calculated.
	 * @param logger The log file that is used by the daemon.
	 * @param props The properties file that is used by the daemon.
	 * @param name The name of the Twitter profile that is used.
	 * @return Returns the interval length for the meta data object.
	 */
	public static Duration calculateIntervalLength(SearchTermMetaData metaData, Logger logger, DaemonProperties props, String name) {
		SearchTerm term = metaData.getSearchTerm();
		int count = metaData.getTweetCount();
		
		double priorityFactor = props.priorityFactors[props.defaultPriorityIndex];
		
		// Try setting the priority factor
		try {
			priorityFactor = (int)mapPriority(term.getPriority(), props);
		}
		catch (IllegalArgumentException ex) {
			try {
				if (logger != null) {
					logger.log("Cannot read priority from search term " + term.getTerm() + "(id " + term.getId() + "). Using default value.", name);
				}
			}
			catch (Exception _) {}
		}
		
		// Time difference between newest tweet and oldest acceptable tweet in minutes
		double timeDiff = 0;
		
		// If we have no new tweets, this field is empty
		if (metaData.getNewestTweetDate() != null) {
			timeDiff = metaData.getNewestTweetDate().minus(metaData.getOldestTweetDate().getMillis()).getMillis() / 60000d;
		}
		
		long intervalLengthInMin = 0;
		if (timeDiff != 0) {
			// Tweets per minute
			double tpm = ((double)count) / timeDiff;

			// The formula for the interval length in minutes
			intervalLengthInMin = (int) (priorityFactor * (1 / tpm) * props.throttleFactor * 100);
		}
		else if (count <= 1) {
			// Here: timeDiff == 0
			// This may be a statistical outlier, so we increase the interval length
			// manually and not by formula
			intervalLengthInMin = term.getIntervalLength().getMillis() / 60000 * props.outlierFactor;
		}
		
		// if timeDiff == 0, but count > 1, we have A LOT of tweets, so interval length
		// should be as low as possible (so it is set to 0).
		
		// Make sure the interval length does not fall below the given default value
		Duration intervalLength = new Duration(intervalLengthInMin * 60000L);
		if (intervalLength.minus(props.defaultIntervalLength.getMillis()).getMillis() < 0)
			intervalLength = props.defaultIntervalLength;
		else if (intervalLength.minus(props.maxIntervalLength.getMillis()).getMillis() > 0)
			intervalLength = props.maxIntervalLength;
		
		return intervalLength;
	}
	
	/**
	 * Calculates the interval length for the given search term (and its meta data).
	 * @param metaData The meta data with search term for which the new interval length
	 * should be calculated.
	 * @param props The properties file that is used by the daemon.
	 * @return Returns the interval length for the meta data object.
	 */
	public Duration calculateIntervalLength(SearchTermMetaData metaData, DaemonProperties props) {
		return calculateIntervalLength(metaData, _logger, props, _twitterProfile.getScreenName());
	}
	
	/**
	 * Fetches tweets for the given search terms. Each term is fetched only once.
	 * @param searchTermMetaData The search terms for which tweets should be fetched.
	 * @return Returns a list of all fetched tweets.
	 * @throws TwitterException Thrown if there is a problem with the Twitter service.
	 */
	public Map<SearchTerm, List<Status>> fetchTweetsForSearchTerms(List<SearchTermMetaData> searchTermMetaData) throws TwitterException {
		Map<SearchTerm, List<Status>> allTweetsMap = new HashMap<SearchTerm, List<Status>>();
		
		// Initialize map
		for (SearchTermMetaData searchTerm : searchTermMetaData) {
			allTweetsMap.put(searchTerm.getSearchTerm(), new LinkedList<Status>());
		}
		
		// Iterate over every search term and do some cool stuff
		for (SearchTermMetaData metaData : searchTermMetaData) {
			//System.out.println(prependInfo("MY CURRENT USED RATE LIMIT: " + _twitterProfile.getUsedRateLimit()));
			//System.out.println(prependInfo("MY MAX     USED RATE LIMIT: " + _props.maxRateLimit));
			
			// If the rate limit has reached 0, we stop
			if (_twitterProfile.getUsedRateLimit() >= _props.maxRateLimit)
				return allTweetsMap;
			
			// If we have already fetched enough times for this search times, we do not want
			// to fetch any more
			if (metaData.getFetchedCount() == _limitPerSearchTerm) {
				metaData.setFiltered(true);
			}
			
			// Ignore search terms that have been filtered
			if (metaData.isFiltered())
				continue;
			
			SearchTerm term = metaData.getSearchTerm();
			
			// Update counting stuff.
			// We do this before the actual search because the search can throw an
			// exception, but we have to count nonetheless.
			_twitterProfile.setUsedRateLimit(_twitterProfile.getUsedRateLimit() + 1);
			_numRequests++;
			metaData.setFetchedCount(metaData.getFetchedCount() + 1);
			
			// search for tweets
			QueryResult result = search(term);
			
			List<Status> tweets = result.getTweets();
			
			if (tweets.size() > 1) {
				if (term.getOldStart() != null) {
					if (tweets.get(tweets.size() - 1).getCreatedAt().getTime() < term.getOldStart().getMillis()) {
						// If we enter this case, we are in state 4, but the end constraint was met, because
						// the last fetched tweet date was older than the old start. So we alter the last fetched
						// tweet id to NULL and the old start becomes the current start, so we move from state 4
						// to state 3.

						// Update the number of new tweets for the search term by counting only
						// the new tweets
						int count = countNewTweets(term, tweets);
						metaData.setTweetCount(metaData.getTweetCount() + count);

						term.setOldStart(term.getCurrentStart());
						term.setLastFetchedTweetId(null);
						
						// This term is now finished (for the time being), so we remove it from the list of
						// search terms we still want to process.
						metaData.setFiltered(true);
						
						// If this is the first call for the search term, save the date for the very first tweet
						if (count > 0 && metaData.getNewestTweetDate() == null)
							metaData.setNewestTweetDate(new DateTime(tweets.get(0).getCreatedAt().getTime()));
						
						if (count > 0) {
							// Also save the date of the last acceptable tweet (count - 1, because count starts at 1 [= index 0])
							metaData.setOldestTweetDate(new DateTime(tweets.get(count - 1).getCreatedAt().getTime()));
						}
						
						// We do not have save each tweet individually but can save them in one big transaction
						// (see below), because the transactor will handle duplicate entries.
					}
					else {
						// If we enter this case, we are in state 4 and the end constraint was not met. So we save
						// all tweets in a transaction (see below) and stay in state 4.
						term.setLastFetchedTweetId(tweets.get(tweets.size() - 1).getId());
						
						// Update the number of new tweets for the search term
						metaData.setTweetCount(metaData.getTweetCount() + tweets.size());
						
						// If this is the first call for the search term, save the date for the very first tweet
						if (metaData.getNewestTweetDate() == null) {
							metaData.setNewestTweetDate(new DateTime(tweets.get(0).getCreatedAt().getTime()));
						}
						
						// Update old tweet date
	                    int count = countNewTweets(term, tweets);
						if (count > 0) {
							// Also save the date of the last acceptable tweet (count - 1, because count starts at 1 [= index 0])
							metaData.setOldestTweetDate(new DateTime(tweets.get(count - 1).getCreatedAt().getTime()));
						}
					}
				}
				else {
					// If we enter this case, we are in state 1 or 2 and have found at least 2 tweets.
					// So we have not to take care of anything special, so we enter state 2 next.
					term.setLastFetchedTweetId(tweets.get(tweets.size() - 1).getId());
					
					// Update the number of new tweets for the search term
					metaData.setTweetCount(metaData.getTweetCount() + tweets.size());
                    
                    // If this is the first call for the search term, save the date for the very first tweet
                    if (metaData.getNewestTweetDate() == null) {
                        metaData.setNewestTweetDate(new DateTime(tweets.get(0).getCreatedAt().getTime()));
                    }
                    
                    // Update old tweet date
					// Also save the date of the last acceptable tweet (count - 1, because count starts at 1 [= index 0])
					metaData.setOldestTweetDate(new DateTime(tweets.get(tweets.size() - 1).getCreatedAt().getTime()));
				}

				// We save all tweets in a transaction
				allTweetsMap.get(term).addAll(tweets);
			}
			else {
				// If we enter this case, we are in state 1, 2 (or very unlikely 4) and have not found any
				// new tweets since the last search. So we have to update the old start date and set the
				// last fetched tweet id to NULL, so that we enter state 3.
				term.setOldStart(term.getCurrentStart());
				term.setLastFetchedTweetId(null);
				
				// This term is now finished (for now), so we remove it from the list of
				// search terms we still want to process.
				metaData.setFiltered(true);
			}
			
			term.setTimeLastFetched(new DateTime());
		}
		
		return allTweetsMap;
	}
	
	/**
	 * Convert a list of search terms into a list of search term meta data for better use.
	 * @param searchTerms The list of search terms to be converted.
	 * @return Returns the converted search term meta data list.
	 */
	public List<SearchTermMetaData> convertSearchTerms(List<SearchTerm> searchTerms) {
		List<SearchTermMetaData> searchTermMetaData = new LinkedList<SearchTermMetaData>();
		for (SearchTerm term : searchTerms) {
			searchTermMetaData.add(new SearchTermMetaData(term));
		}
		
		return searchTermMetaData;
	}
	
	/**
	 * Updates the interval length for the given meta data object.
	 * @param metaData The meta data object, whose interval length should be updated.
	 */
	private void updateIntervalLength(SearchTermMetaData metaData) {
		SearchTerm term = metaData.getSearchTerm();
		int count = metaData.getTweetCount();
		term.setLastFetchedTweetCount(count);
		
		// Set the new interval length
		term.setIntervalLength(calculateIntervalLength(metaData, _props));
	}

	/**
	 * Sends a request specified as search term to Twitter and returns the
	 * results Twitter returned.
	 * 
	 * @param term The search term object.
	 * @return The answer of the sent request as QueryResult.
	 * @throws TwitterException Thrown whenever there is a problem querying Twitter
	 * (i. e the Rate Limit was reached).
	 */
	public QueryResult search(SearchTerm term) throws TwitterException {
		Query query = new Query(term.getTerm());
		query.setCount(100);
		query.setResultType(Query.RECENT);
		if (term.getLastFetchedTweetId() == null) {
			// Start a new backwards search fron the current given start
			if (term.getOldStart() == null)
				query.setUntil(Localization.DATETIME_FORMATTER.print(term.getCurrentStart().plusDays(1)));
		}
		else
			// Continue the current search from the last fetched tweet id
			query.setMaxId(term.getLastFetchedTweetId());

		return _twitter.search(query);
	}
	
	/**
	 * Prepends a standard value before the append part.
	 * @param append Text to be appended.
	 * @return The full text.
	 */
	private String prependInfo(String append) {
		return Localization.DATEANDTIME_FORMATTER.format(new Date()) + " " + _twitterProfile.getScreenName() + " " + append;
	}
	
	/**
	 * Returns the associated Twitter profile.
	 * @return The associated Twitter profile.
	 */
	public TwitterProfile getTwitterProfile() {
		return _twitterProfile;
	}
	
	/**
	 * Returns the master of this minion.
	 * @return The master of this minion.
	 */
	public Master getMaster() {
		return _master;
	}
}
