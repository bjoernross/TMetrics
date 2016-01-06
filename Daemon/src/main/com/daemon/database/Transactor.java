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
package com.daemon.database;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.joda.time.DateTime;
import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.User;
import twitter4j.UserMentionEntity;
import com.daemon.sentiment.RegressionSentimentClassifier;
import com.tmetrics.logging.LogManager;
import com.tmetrics.logging.Logger;
import com.tmetrics.util.Localization;
import com.tmetrics.util.StringUtil;

/**
 * Used to communicate with the Database and making requests for Twitter.
 * 
 * @author Daniel, Tobias, Torsten, Erwin, Björn, Jens
 */
public class Transactor {

	// The path from which the database properties are read
	public static String DATABASE_PROPERTY_PATH = System.getProperty("user.home") + "/database.properties";
	
	private Logger _logger = LogManager.getLogger("logs/Master.log");
	
	private Connection connect = null;
	private ResultSet resultSet = null;

	private PreparedStatement prepStatementGetSearchTerms = null;
	private PreparedStatement prepStatementUpdateSearchTerms = null;
	private PreparedStatement prepStatementTweet = null;
	private PreparedStatement prepStatementUser = null;
	private PreparedStatement prepStatementMentions = null;
	private PreparedStatement prepStatementTweetWithTerm = null;
	private PreparedStatement prepStatementHashtag = null;
	private PreparedStatement prepStatementTweetHasHashtag = null;
	private PreparedStatement prepStatementInsertSearchTerm = null;

	private String dbUrl = "";

	/**
	 * Creates a transactor object and tries to load the database properties from
	 * the file specified in DATABASE_PROPERTY_PATH
	 * @throws IOException Thrown, if the database properties file cannot be loaded.
	 * @throws ClassNotFoundException Thrown, if the JDBC driver cannot be loaded.
	 */
	public Transactor() throws IOException, ClassNotFoundException {
		FileInputStream fis = null;
		
		try {

			Properties props = new Properties();
			fis = new FileInputStream(DATABASE_PROPERTY_PATH);
			props.load(fis);
			Class.forName(props.getProperty("javabase.jdbc.driver"));
			dbUrl = props.getProperty("javabase.jdbc.url")
					+ props.getProperty("database.name") + "?user="
					+ props.getProperty("javabase.jdbc.username")
					+ "&password="
					+ props.getProperty("javabase.jdbc.password")
					+ "&useLegacyDatetimeCode=false"
					+ "&serverTimezone=UTC";

		} catch (IOException e) {
			_logger.log("Cannot open database properties file.");
		} catch (ClassNotFoundException e) {
			_logger.log("Cannot find database JDBC driver.");
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
		
	}
	
	/**
	 * Returns the URL for the DB.
	 * @return Returns the URL for the DB.
	 */
	public String getDbUrl() {
		return dbUrl;
	}

	/**
	 * Creates a database connection for the Transactor object and all prepared
	 * statements usable to save a tweet.
	 */
	public void connect() {
		// SQL queries
		String sqlQueryGetSearchTerms = "SELECT * FROM search_terms";
		
		// the fields are expected to be separated by "," while between the last field and the where clause there
		// must not(!) be a ","
		String sqlQueryUpdateSearchTerms = "UPDATE search_terms SET current_start=?, old_start=?, interval_length=?, time_last_fetched=?, last_fetched_tweet_id=?, last_fetched_tweet_count=?, when_created=? WHERE id=?";
		String sqlQueryTweet = "insert into tweets (id, coordinates_longitude, coordinates_latitude, "
				+ "users_id, is_reply_to_status_id, is_retweet_of_id, created_at, source, text, "
				+ "iso_language_code, retweet_count,sentiment) values (?,?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE retweet_count = VALUES(retweet_count)";
		String sqlQueryUser = "insert into users (id, name, screen_name, profile_image_url, created_at, location, url, lang, followers_count, verified, time_zone, description, statuses_count, friends_count) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE name = VALUES(name), screen_name = VALUES(screen_name), profile_image_url = VALUES(profile_image_url), location = VALUES(location), lang = VALUES(lang), followers_count = VALUES(followers_count), verified = VALUES(verified), time_zone = VALUES(time_zone), description = VALUES(description), statuses_count = VALUES(statuses_count), friends_count = VALUES(friends_count);";
		String sqlQueryMentions = "INSERT INTO mentions (tweets_id, users_id) values (?, ?) ON DUPLICATE KEY UPDATE tweets_id = VALUES(tweets_id), users_id = VALUES(users_id);";
		String sqlQueryTweetWithTerm = 
				"INSERT INTO tweets_has_search_terms"
				+ " (tweets_id, search_terms_id, iso_language_code, sentiment, created_at, is_retweet_of_id, retweet_count)"
				+ " VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE tweets_id = VALUES(tweets_id),"
				+ " search_terms_id = VALUES(search_terms_id), iso_language_code = VALUES(iso_language_code),"
				+ " sentiment = VALUES(sentiment), created_at = VALUES(created_at),"
				+ " is_retweet_of_id =VALUES(is_retweet_of_id), retweet_count = VALUES(retweet_count) ;";
		String sqlQueryHashtag = "INSERT INTO hashtags (text) VALUES (LOWER(?)) ON DUPLICATE KEY UPDATE id = id;";
		String sqlQueryTweetHasHashtag = "INSERT INTO tweets_has_hashtags (tweets_id, hashtags_id) VALUES (?, (SELECT id FROM hashtags WHERE text = LOWER(?))) ON DUPLICATE KEY UPDATE tweets_id = VALUES(tweets_id), hashtags_id = VALUES(hashtags_id);";
		// insert is only used for testing
		String sqlInsertSearchTerm= "INSERT INTO search_terms (term, active, current_start, old_start, interval_length, time_last_fetched, last_fetched_tweet_id, last_fetched_tweet_count, priority, when_created)"
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";


		try {
			// establish connection
			connect = DriverManager.getConnection(dbUrl);
			// turn off autocommit mode so database transactions are only
			// committed once commit(), which we do in saveAllTweets()
			connect.setAutoCommit(false);
			
			// prepare statements
			prepStatementGetSearchTerms = connect
					.prepareStatement(sqlQueryGetSearchTerms);
			prepStatementUpdateSearchTerms = connect
					.prepareStatement(sqlQueryUpdateSearchTerms);
			prepStatementTweet = connect.prepareStatement(sqlQueryTweet);
			prepStatementUser = connect.prepareStatement(sqlQueryUser);
			prepStatementMentions = connect.prepareStatement(sqlQueryMentions);
			prepStatementTweetWithTerm = connect
					.prepareStatement(sqlQueryTweetWithTerm);
			prepStatementHashtag = connect.prepareStatement(sqlQueryHashtag);
			prepStatementTweetHasHashtag = connect
					.prepareStatement(sqlQueryTweetHasHashtag);
			// we only need to insert for testing purposes.
			prepStatementInsertSearchTerm = connect
					.prepareStatement(sqlInsertSearchTerm);
			
		} catch (Exception e) {
			_logger.log(e.getMessage());
		}
	}

	/**
	 * Retrieves current search terms saved in the DB and returns them
	 * in a list.
	 * 
	 * @param intelligentChoice
	 *            Indicates whether only active search terms should be returned
	 *            or not. Also, if a search term is active, but the difference
	 *            between now and the old_start is less than the interval_length,
	 *            that search term will not be considered active, unless the
	 *            search term is in an iteration.
	 * @return List containing search term entries.
	 */
	public List<SearchTerm> getSearchTerms(boolean intelligentChoice) {
		try {
			resultSet = prepStatementGetSearchTerms.executeQuery();
			connect.commit();
			// Get list of search terms and pack them into a Java list
			List<SearchTerm> searchTermList = new LinkedList<SearchTerm>();
			SearchTerm term;
			
			while (resultSet.next()) {
				// create a search term object from the result set element
				term = new SearchTerm(resultSet);
				
				// Should we use intelligent choice mode?
				if (intelligentChoice) {
					if (term.isActive()) {
						
						// If the term is searchable, search for it
						// (and also update the current start)
						if (term.isSearchable(true)) {
							searchTermList.add(term);
						}
					}
				}
				else {
					searchTermList.add(term);
				}
			}
			return searchTermList;
		} catch (Exception e) {
			_logger.log("Cannot retrieve search terms from database:");
			_logger.logStackTrace(e);
			return null;
		} finally {
			// close the result set if it's still open
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException e) {
					_logger.log(e.getMessage());
				}
			}
		}
	}
	
	/**
	 * Updates only the for the daemon relevant data from a search term.
	 * 
	 * @param term Search term to be updated
	 */
	public synchronized void updateSearchTerm(SearchTerm term) {
		try {
			// current_start
			prepStatementUpdateSearchTerms.setTimestamp(1, new Timestamp(term.getCurrentStart().getMillis()), Localization.UTC);
			
			// old_start
			if (term.getOldStart() == null) {
				prepStatementUpdateSearchTerms.setNull(2, java.sql.Types.NULL);
			}
			else {
				prepStatementUpdateSearchTerms.setTimestamp(2, new Timestamp(term.getOldStart().getMillis()), Localization.UTC);
			}
			
			// interval_length
			prepStatementUpdateSearchTerms.setString(3, Localization.printDuration(term.getIntervalLength()));

			// time_last_fetched
			if (term.getTimeLastFetched() == null) {
				prepStatementUpdateSearchTerms.setNull(4, java.sql.Types.NULL);
			}
			else {
				prepStatementUpdateSearchTerms.setTimestamp(4, new Timestamp(term.getTimeLastFetched().getMillis()), Localization.UTC);
			}
			
			//  last_fetched_tweet_id
			if (term.getLastFetchedTweetId() == null) {
				prepStatementUpdateSearchTerms.setNull(5, java.sql.Types.NULL);
			}
			else {
				prepStatementUpdateSearchTerms.setLong(5, term.getLastFetchedTweetId());
			}
			
			// last_fetched_tweet_count
			if (term.getLastFetchedTweetCount() == null)
				prepStatementUpdateSearchTerms.setNull(6, java.sql.Types.NULL);
			else
				prepStatementUpdateSearchTerms.setInt(6, term.getLastFetchedTweetCount());
			
			// when_created
				prepStatementUpdateSearchTerms.setTimestamp(7, new Timestamp(term.getWhenCreated().getMillis()), Localization.UTC);
			
			// id
			try {
				prepStatementUpdateSearchTerms.setInt(8, term.getId());
			}
			catch (Exception e) {
				// This exception is impossible at this point. The field id is unset only for search terms
				// that are not yet in the database but this function is only executed for search terms already
				// in there
			}
			
			// Execute the command
			prepStatementUpdateSearchTerms.execute();
			connect.commit();
			
			prepStatementUpdateSearchTerms.clearParameters();
		}
		catch (SQLException e) {
			_logger.log(e.getMessage());
		}
	}

	/**
	 * Saves a given tweet in the DB if that tweet is not already saved. Only saves
	 * the tweet no other information!
	 * 
	 * @param tweet The tweet to be saved.
	 * @throws SQLException
	 */
	private void saveTweet(Status tweet, RegressionSentimentClassifier sentimentClassifier) throws SQLException {
		// for reweet, save the original tweet first
		if (tweet.getRetweetedStatus() != null) {
			saveAllTransactionSafe(tweet.getRetweetedStatus(), null, sentimentClassifier);
		}
		// then, save the current tweet

		// 1: Set Tweet ID
		prepStatementTweet.setLong(1, tweet.getId());

		// 2 / 3: Set GeoLocation
		if (tweet.getGeoLocation() != null) {
			prepStatementTweet.setFloat(2, (float) tweet.getGeoLocation()
					.getLatitude());
			prepStatementTweet.setFloat(3, (float) tweet.getGeoLocation()
					.getLongitude());
		} else {
			prepStatementTweet.setNull(2, java.sql.Types.NULL);
			prepStatementTweet.setNull(3, java.sql.Types.NULL);
		}

		// 4: Set User ID
		prepStatementTweet.setLong(4, tweet.getUser().getId());

		// 5: Set Reply-Tweet ID
		if (tweet.getInReplyToStatusId() == -1) {
			prepStatementTweet.setNull(5, java.sql.Types.NULL);
		} else {
			prepStatementTweet.setLong(5, tweet.getInReplyToStatusId());
		}

		// 6: Set Retweet-ID
		if (tweet.getRetweetedStatus() != null) {
			prepStatementTweet.setLong(6, tweet.getRetweetedStatus().getId());
		} else {
			prepStatementTweet.setNull(6, java.sql.Types.NULL);
		}

		// 7: Set Creation Date of Tweet
		java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(tweet
				.getCreatedAt().getTime());
		prepStatementTweet.setTimestamp(7, sqlTimestamp);

		// 8-11: Other attributes:
		prepStatementTweet.setString(8, tweet.getSource());
		prepStatementTweet.setString(9, tweet.getText());
		prepStatementTweet.setString(10, tweet.getIsoLanguageCode());
		prepStatementTweet.setInt(11, tweet.getRetweetCount());

		// 12: Sentiment
		Float sentiment = sentimentClassifier.determineSentiment(tweet.getText(), tweet.getIsoLanguageCode());
		if (sentiment == null) {
			prepStatementTweet.setNull(12, java.sql.Types.NULL);
		} else {
			prepStatementTweet.setFloat(12, sentimentClassifier.determineSentiment(tweet.getText(), tweet.getIsoLanguageCode()));
		}

		// execute statement
		prepStatementTweet.addBatch();
	}

	/**
	 * Saves the tweet and makes all necessary alterations in the DB - can be
	 * called by saveTweet (in a recursion)!
	 * 
	 * @param tweet The tweet with related information for this tweet to be saved.
	 * @param term The term the tweet is associated with. Nullable.
	 * @throws SQLException
	 */
	public void saveAllTransactionSafe(Status tweet, SearchTerm term, RegressionSentimentClassifier sentimentClassifier)
			throws SQLException {
		saveUser(tweet.getUser());
		saveTweet(tweet,  sentimentClassifier);
		saveUserMentions(tweet);

		// in the case of a retweet: does not have a Search Term.
		if (term != null) {
			saveTweetWithTerm(tweet, term, sentimentClassifier);
		}
		saveTweetsHashtags(tweet);
	}

	/**
	 * Saves multiple tweets and makes all necessary alterations in the DB.
	 * 
	 * @param tweet	The tweet with related information for this tweet to be saved.
	 * @param term	The term the tweet is associated with.
	 */
	public void saveAllTweets(List<Status> tweets, SearchTerm term, RegressionSentimentClassifier sentimentClassifier) {
		Status currentTweet = null;
		try {

			for (Status tweet : tweets) {
				currentTweet = tweet;
				// Save the tweet information
				saveAllTransactionSafe(tweet, term, sentimentClassifier);
			}
			
			// Save all batches
			prepStatementUser.executeBatch();
			prepStatementTweet.executeBatch();
			prepStatementMentions.executeBatch();
			prepStatementTweetWithTerm.executeBatch();
			prepStatementHashtag.executeBatch();
			prepStatementTweetHasHashtag.executeBatch();
			
			connect.commit();
		}
		catch (Exception e)
		{
			_logger.log("Cannot save data: " + e.getMessage());
			if (connect != null){
				try
				{
					_logger.log("Transaction is being rolled back for tweet-id:" + currentTweet.getId() + " and term-id: " + term.getId());
					connect.rollback();	
				}
				catch(SQLException excep)
				{
					_logger.log("Could not rollback the transaction:");
					_logger.logStackTrace(excep);
				}
				catch(Exception excep)
				{
					_logger.log("Could not rollback the transaction, because the search term has no id, yet.");
					_logger.logStackTrace(excep);
				}
			}
			_logger.log(getTweetErrorString(currentTweet));
			_logger.log(getUserErrorString(currentTweet.getUser()));
			_logger.log("Stack trace for the failed saving of data.");
			_logger.logStackTrace(e);
		}
	}
	
	/**
	 * Creates an error message with user information for further analysis.
	 * @param user The user whose information is to be represented.
	 * @return The error message.
	 */
	private String getUserErrorString(User user) {
		StringBuffer sb = new StringBuffer();
		
		sb.append("User information:\n");
		sb.append("  id: " + user.getId() + "\n");
		sb.append("  screen name: " + user.getScreenName() + "\n");
		sb.append("  name: " + user.getName() + "\n");
		sb.append("  description: " + user.getDescription() + "\n");
		sb.append("    length: " + user.getDescription().length() + "\n");
		sb.append("   elength: " + StringUtil.escapeNonAscii(user.getDescription()).length() + "\n");
		sb.append("  language: " + user.getLang() + "\n");
		sb.append("  location: " + user.getLocation());
		
		return sb.toString();
	}
	
 	/**
	 * Creates an error message with tweet information for further analysis.
	 * @param tweet The tweet whose information is to be represented.
	 * @return The error message.
 	 **/
	private String getTweetErrorString(Status tweet) {
		StringBuffer sb = new StringBuffer();
 
		sb.append("Tweet information:\n");
		sb.append("  id: " + tweet.getId() + "\n");
		sb.append("  text: " + tweet.getText() + "\n");
		sb.append("    length: " + tweet.getText().length() + "\n");
		sb.append("   elength: " + StringUtil.escapeNonAscii(tweet.getText()).length() + "\n");
		sb.append("  iso lang. code: " + tweet.getIsoLanguageCode());
		if (tweet.isRetweet())
			sb.append("\n  retweet of id: " + tweet.getRetweetedStatus().getId());
		
		return sb.toString();
	}

	/**
	 * Adds only the given User to the SQL.Batch which will be saved
	 *  in the DB by savedAllTweetsTransactionsafe  
	 * 
	 * @param user The user to be saved.
	 * @throws SQLException
	 */
	private void saveUser(User user) throws SQLException {
		prepStatementUser.setLong(1, user.getId());
		prepStatementUser.setString(2, user.getName());
		prepStatementUser.setString(3, user.getScreenName());
		prepStatementUser.setString(4, user.getProfileImageURL());
		prepStatementUser.setTimestamp(5, new java.sql.Timestamp(user
				.getCreatedAt().getTime()));
		prepStatementUser.setString(6, user.getLocation());
		prepStatementUser.setString(7, user.getURL());
		prepStatementUser.setString(8, user.getLang());
		prepStatementUser.setInt(9, user.getFollowersCount());
		prepStatementUser.setBoolean(10, user.isVerified());
		prepStatementUser.setString(11, user.getTimeZone());
		prepStatementUser.setString(12, user.getDescription());
		prepStatementUser.setInt(13, user.getStatusesCount());
		prepStatementUser.setInt(14, user.getFriendsCount());

		prepStatementUser.addBatch();
	}

	/**
	 * Adds the user mentions for a given tweet to the SQL Batch saved
	 * to the DB by saveAllTweetsTransactionSave.
	 * 
	 * @param tweet	The tweet which contains mentions of users 
	 * @throws SQLException
	 */
	private void saveUserMentions(Status tweet) throws SQLException {
		// Save tweet id in prepared statements
		prepStatementMentions.setLong(1, tweet.getId());

		// 1. Get User Mentions in given tweet
		UserMentionEntity[] userMentionEntities = tweet
				.getUserMentionEntities();

		// 2. Create for each user Mention an db entry, if necessary
		for (UserMentionEntity userMentionEntity : userMentionEntities) {
			prepStatementMentions.setLong(2, userMentionEntity.getId());
			prepStatementMentions.addBatch();
		}
	}

	/**
	 * Adds the relation between the given tweet and the search term-id (DB
	 * specific) the tweet was found with to the SQL Batched executed by 
	 * the saveAllTransactionSafe Function
	 * 
	 * @param tweet  The tweet related to the search term-id.
	 * @param term   The search term related to the tweet.
	 * @throws SQLException
	 */
	private void saveTweetWithTerm(Status tweet, SearchTerm term, RegressionSentimentClassifier sentimentClassifier)
			throws SQLException {
		try {
			prepStatementTweetWithTerm.setLong(1, tweet.getId()); // tweet_id
			prepStatementTweetWithTerm.setInt(2, term.getId()); // search_term_id
			prepStatementTweetWithTerm.setString(3, tweet.getIsoLanguageCode());  // iso__language_code
			// sentiment
			Float sentiment = sentimentClassifier.determineSentiment(tweet.getText(), tweet.getIsoLanguageCode());
			if (sentiment == null) {
				prepStatementTweetWithTerm.setNull(4, java.sql.Types.NULL);
			} else {
				prepStatementTweetWithTerm.setFloat(4, sentimentClassifier.determineSentiment(tweet.getText(), tweet.getIsoLanguageCode()));
			}
			
			// created_at
			java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(tweet
					.getCreatedAt().getTime());
			prepStatementTweetWithTerm.setTimestamp(5, sqlTimestamp); 
			// is_retweet_of_id
			if (tweet.getRetweetedStatus() != null) {
				prepStatementTweetWithTerm.setLong(6, tweet.getRetweetedStatus().getId());
			} else {
				prepStatementTweetWithTerm.setNull(6, java.sql.Types.NULL);
			}
			// retweet_count
			prepStatementTweetWithTerm.setInt(7, tweet.getRetweetCount());

			prepStatementTweetWithTerm.addBatch();
		} catch (Exception e) {
			// Doesn't make sense. we wouldn't find the tweet for the
			// search term if the search term was not in the database
			throw new SQLException(
					"Cannot save tweet with search term. The term has no Id (it was not inserted, yet).");
		}
	}

	/**
	 * Adds for each Hashtag in a given tweet an SQL Batch Entry in the 
	 * table 'hashtags' and also for the relation between tweet and hashtag in
	 * 'tweets_has_hashtags' the storing of the batch takes place in the
	 * saveAllTransactionSafe.
	 * 
	 * @param tweet The tweet whose hashtags shall be saved.
	 * @throws SQLException
	 */
	private void saveTweetsHashtags(Status tweet) throws SQLException {
		// get Hashtags in given tweet
		HashtagEntity[] htEntities = tweet.getHashtagEntities();

		for (HashtagEntity htEntity : htEntities) {
			// save hashtag
			saveHashtag(htEntity);

			// save relation
			saveTweetHashtagRelation(tweet, htEntity);
		}
	}

	/**
	 * Adds the hashtag to the SQL Batch, if not already in the DB. Returns its id.
	 * This method is called by saveTweetsHashtags.
	 * 
	 * @param htEntity	An Hashtag-Entity to be saved.
	 * @throws SQLException
	 */
	private void saveHashtag(HashtagEntity htEntity) throws SQLException {
		prepStatementHashtag.setString(1, htEntity.getText());
		prepStatementHashtag.addBatch();
	}

	/**
	 * Adds the relation between tweet and hashtag to the SQL Batch. This method is called by
	 * saveTweetsHashtags().
	 * 
	 * @param tweet	The tweet related to the search term-id.
	 * @param htEntity	The hashtag object related to the tweet.
	 * @throws SQLException
	 */
	private void saveTweetHashtagRelation(Status tweet, HashtagEntity htEntity)
			throws SQLException {
		prepStatementTweetHasHashtag.setLong(1, tweet.getId());
		prepStatementTweetHasHashtag.setString(2, htEntity.getText());
		prepStatementTweetHasHashtag.addBatch();
	}
	
	/**
	 * Inserts the given search term into the database, should only be used for tests.
	 * 
	 * @param term The term to be inserted into the database.
	 * @throws SQLException Thrown when there is a problem during insertion.
	 */
	public void insertSearchTerm(SearchTerm term) throws SQLException {
		try
		{
			
			// term
			prepStatementInsertSearchTerm.setString(1, term.getTerm());
			//active
			prepStatementInsertSearchTerm.setInt(2, 1);
			// current_start
			prepStatementInsertSearchTerm.setTimestamp(3, new Timestamp(term.getCurrentStart().getMillis()), Localization.UTC);
			// old_start
			if (term.getOldStart() == null) {
				prepStatementInsertSearchTerm.setNull(4, java.sql.Types.NULL);
			}
			else {
				prepStatementInsertSearchTerm.setTimestamp(4, new Timestamp(term.getOldStart().getMillis()), Localization.UTC);
			}
			// interval_length
				prepStatementInsertSearchTerm.setTimestamp(5, new Timestamp(term.getIntervalLength().getMillis()), Localization.UTC);
			// time_last_fetched
			if (term.getTimeLastFetched() == null) {
				prepStatementInsertSearchTerm.setNull(6, java.sql.Types.NULL);
			}
			else {
				prepStatementInsertSearchTerm.setTimestamp(6, new Timestamp(term.getTimeLastFetched().getMillis()), Localization.UTC);
			}
			//  last_fetched_tweet_id
			if (term.getLastFetchedTweetId() == null) {
				prepStatementInsertSearchTerm.setNull(7, java.sql.Types.NULL);
			}
			else {
				prepStatementInsertSearchTerm.setLong(7, term.getLastFetchedTweetId());
			}
			// last_fetched_tweet_count
			if (term.getLastFetchedTweetCount() == null){
				prepStatementInsertSearchTerm.setNull(8,java.sql.Types.NULL);
			} else {
				prepStatementInsertSearchTerm.setInt(8, term.getLastFetchedTweetCount());
			}
			// priority
			if (term.getPriority() == null){
				prepStatementInsertSearchTerm.setInt(9, 0);
			} else {
				prepStatementInsertSearchTerm.setInt(9, term.getPriority());
			}
			// when_created
			if (term.getWhenCreated() == null){
				prepStatementInsertSearchTerm.setTimestamp(10, new Timestamp(new DateTime(2014, 01, 01, 00, 00, 00).getMillis()), Localization.UTC);
			} else {
				prepStatementInsertSearchTerm.setTimestamp(10, new Timestamp(term.getWhenCreated().getMillis()), Localization.UTC);
			}				
			
			//execute everything
			prepStatementInsertSearchTerm.execute();
			connect.commit();
		}
		catch (SQLException e) {
			_logger.log(e.getMessage());
		}
	}
	
	/**
	 * Closes any used resources connected with DB usage. Always call this
	 * method when you are done using DB related functionality.
	 */
	public void close() {
		try {
			if (resultSet != null) {
				resultSet.close();
			}

			if (connect != null) {
				connect.close();
			}

			prepStatementGetSearchTerms.close();
			prepStatementUpdateSearchTerms.close();
			prepStatementUser.close();
			prepStatementMentions.close();
			prepStatementTweetWithTerm.close();
			prepStatementHashtag.close();
			prepStatementTweetHasHashtag.close();
		} catch (Exception e) {
			_logger.log(e.getMessage());
		}
	}
	
	/**
	 * Returns the database connection.
	 * @return Returns the database connection.
	 */
	public Connection getConnection() {
		return connect;
	}
}
