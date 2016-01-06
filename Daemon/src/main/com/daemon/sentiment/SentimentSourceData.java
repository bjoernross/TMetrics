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
package com.daemon.sentiment;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.tmetrics.exceptions.NotDataFoundException;
import com.tmetrics.logging.LogManager;
import com.tmetrics.logging.Logger;

/**
 * Represents the data set - usable for the regression and regression model
 * tests. At first, create this object, then decide where and which data you
 * want to use, e.g. if you want to load training data from database or load
 * from CSV. If you want to load data from database, the default value is the
 * local production database. If you want to use another database, e.g. the
 * Rest-Test-DB, set the right database after the constructor.
 * 
 * @author Erwin, Björn
 * 
 */
public class SentimentSourceData implements Serializable {

	private static final long serialVersionUID = 1L;

	private String properties_file_path;
	// Store the used database more readable than properties_file_path, e.g.
	// "DaemonTest", "RestTest", "Server".
	private String usedDatabase;

	// training / test tweets
	private List<LabeledTweetContainer> tweets;

	static private Logger _logger = LogManager.getLogger("logs/Master.log");

	// search term training data is restricted to
	private final Long searchTermId;
	// language of tweets
	private final String language;

	/**
	 * Constructor of the SentimentSourceData object
	 * 
	 * Use the parameters to specify what data you want to load.
	 * 
	 * @param language
	 *            Restricts results to a specific language.
	 * 
	 * @param id
	 *            Restricts results to tweets found for a specify search term
	 *            id. Can be null.
	 * @param database
	 *            Choose the right database. Per default, SentimentSourceData
	 *            chooses the local production database. Note that on the server
	 *            the local database is the server database. If you are testing,
	 *            you can choose the Daemon Test or Rest Test Database. If you
	 *            are coming from RegressionTest and you want to test the server
	 *            database, use "Remote" to connect to the global server
	 *            database (especially its tweet table). These changes applies
	 *            to all subsequent calls of readTrainingDataFromDB and
	 *            readTestDataFromDB. All in all, use "DaemonTest", "RestTest",
	 *            "Remote" or "Local".
	 * 
	 */
	public SentimentSourceData(Long id, String language, String database) {
		this.tweets = new ArrayList<LabeledTweetContainer>();
		this.searchTermId = id;
		this.language = language;
		this.chooseDatabase(database);
	}

	/**
	 * Choose the right database
	 * 
	 * You can use either "DaemonTest", "RestTest", "Remote" or "Local".
	 * 
	 * Explanation: Per default, SentimentSourceData chooses the local
	 * production database. Note that on the server the local database is the
	 * server database. If you are testing, you can choose the Daemon Test or
	 * Rest Test Database. If you are coming from RegressionTest and you want to
	 * test the server database, use "Remote" to connect to the global server
	 * database. These changes applies to all subsequent calls of
	 * readTrainingDataFromDB and readTestDataFromDB.
	 * 
	 * @param input
	 *            Can be "DaemonTest", "RestTest", "Remote". Each other input
	 *            will result in using the default local database ;)
	 */
	private void chooseDatabase(String input) {
		switch (input) {
		case "DaemonTest":
			properties_file_path = "src/test/resources/database.properties";
			usedDatabase = input;
			break;

		case "RestTest":
			properties_file_path = System.getProperty("user.home")
					+ "/database_test.properties";
			usedDatabase = input;
			break;
		case "Remote":
			properties_file_path = System.getProperty("user.home")
					+ "/database-server.properties";
			usedDatabase = input;
			break;
		default:
			properties_file_path = System.getProperty("user.home")
					+ "/database.properties";
			usedDatabase = "Local";
		}
	}

	/**
	 * Get the tweets used for training
	 * 
	 * @return training set tweets
	 */
	public List<LabeledTweetContainer> getTweets() {
		return this.tweets;
	}

	/**
	 * Returns an List of the used Labels - usable in the regressionModel
	 * 
	 * @return List of Labels
	 */
	List<Float> getLabels() {

		List<Float> labelList = new ArrayList<Float>();
		for (LabeledTweetContainer tweet : tweets) {
			labelList.add(tweet.getLabel());
		}
		return labelList;
	}

	/**
	 * Returns an List of the tweet texts
	 * 
	 * @return List of tweet texts
	 */
	public List<String> getTweetTexts() {
		List<String> tweetTextList = new ArrayList<String>();
		for (LabeledTweetContainer tweet : tweets) {
			tweetTextList.add(tweet.getTweetText());
		}
		return tweetTextList;
	}

	/**
	 * Returns a String List of tweet ids (in database used as long). Used by
	 * clustering analysis.
	 * 
	 * @return List of tweet ids
	 */
	public List<String> getTweetIds() {
		List<String> tweetIdList = new ArrayList<String>();
		for (LabeledTweetContainer tweet : tweets) {
			tweetIdList.add(String.valueOf(tweet.getid()));
		}
		return tweetIdList;
	}

	/**
	 * Get the search term used to get the tweets.
	 * 
	 * @return
	 */
	public Long getSearchTermId() {
		return searchTermId;
	}

	/**
	 * Get the language of the tweets stored in this object.
	 * 
	 * @return
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * Get the database name which was used to get tweets. Can be "DaemonTest",
	 * "RestTest", "Remote" or "Local".
	 * 
	 * @return
	 */
	public String getUsedDatabase() {
		return this.usedDatabase;
	}

	/**
	 * Set the tweets of Source Data. By doing so, we can set our own tweets and
	 * do e.g. some tests.
	 * 
	 * @param tweets
	 *            a List of LabeledTweetContainers representing the tweets.
	 */
	public void setTweets(List<LabeledTweetContainer> tweets) {
		this.tweets = tweets;
	}

	/**
	 * Get tweets from the database that have human labels
	 * 
	 * @param percentageOfData
	 *            Percentage of labeled tweets to be fetched (or omitted, if
	 *            invert = true)
	 * @param invert
	 *            If set to false, percentageOfData is understood to specify the
	 *            portion of tweets that are to be included in the results. Use
	 *            this to get training data. If set to true, the results are
	 *            inverted, i. e. all other tweets are returned. Use this to get
	 *            test data.
	 * @return List of container objects that stores tweets and labels. Null in
	 *         case of a failure
	 * @throws Exception
	 */
	public void readDataFromDB(float percentageOfData, boolean invert)
			throws Exception {

		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet results = null;
		FileInputStream fis = null;

		try {
			// connect to the database
			Properties props = new Properties();
			fis = new FileInputStream(this.properties_file_path);
			props.load(fis);
			Class.forName(props.getProperty("javabase.jdbc.driver"));
			String dbUrl = props.getProperty("javabase.jdbc.url")
					+ props.getProperty("database.name") + "?user="
					+ props.getProperty("javabase.jdbc.username")
					+ "&password="
					+ props.getProperty("javabase.jdbc.password");
			connection = DriverManager.getConnection(dbUrl);

			// get tweets labeled by humans
			// @Nice-To-Have: Make from 2 calls 1 call - DB Performance!

			// build the SQL query based on the parameters:
			StringBuilder sqlBuilder = new StringBuilder(300);

			sqlBuilder.append("SELECT COUNT(t.id) FROM tweets AS t");

			if (searchTermId != null) {
				sqlBuilder.append(" JOIN tweets_has_search_terms AS thst"
						+ " ON t.id = thst.tweets_id"
						// + " JOIN search_terms AS s"
						// + " ON s.id = thst.search_terms_id"
						// + " WHERE s.term = ?" + " AND");
						+ " WHERE thst.search_terms_id = ?" + " AND");
			} else {
				sqlBuilder.append(" WHERE");
			}

			sqlBuilder.append(" t.sentiment_human_label IS NOT NULL");

			if (language != null) {
				sqlBuilder.append(" AND t.iso_language_code = ?");
			}

			sqlBuilder.append(" ORDER BY t.id");

			// prepare statement
			statement = connection.prepareStatement(sqlBuilder.toString());

			// set parameters to their values
			int i = 1;

			if (searchTermId != null) {
				statement.setLong(i++, searchTermId);
			}

			if (language != null) {
				statement.setString(i++, language);
			}

			// execute statement and get number of tweets
			statement.execute();
			results = statement.getResultSet();
			results.first();
			int count = results.getInt(1);
			results.close();
			statement.close();

			// calculate number of training examples
			int numberOfTrainingExamples = (int) (count * percentageOfData);

			// if we could not find data, throw Exception:
			if (invert == false && numberOfTrainingExamples == 0)
				throw new NotDataFoundException(
						"No training data for sentiment regression. Found "
								+ count + " labeled tweets, using "
								+ (percentageOfData * 100)
								+ "% for training leaves us with 0.");

			if (invert == true && numberOfTrainingExamples == count) {
				throw new NotDataFoundException(
						"No test data for sentiment regression. Found "
								+ count
								+ " labeled tweets, using "
								+ (percentageOfData * 100)
								+ "% for training tweets leaves none for testing.");
			}

			// modify SQL query to get the actual tweets:

			// in SELECT COUNT(t.id), replace "COUNT(t.id)" to get columns we
			// need
			sqlBuilder.replace(7, 18, "t.text, t.sentiment_human_label");

			if (invert == false) {
				sqlBuilder.append(" LIMIT ?");
			} else {
				sqlBuilder.append(" LIMIT ?, 18446744073709551615");
			}

			// prepare statement
			statement = connection.prepareStatement(sqlBuilder.toString());

			// set parameter values
			i = 1;
			if (searchTermId != null) {
				statement.setLong(i++, searchTermId);
			}
			if (language != null) {
				statement.setString(i++, language);
			}
			statement.setInt(i++, numberOfTrainingExamples);

			// execute statement and get tweets
			statement.execute();
			results = statement.getResultSet();

			// iterate over each tweet and store it in the container and store
			// this in the list
			while (results.next()) {
				tweets.add(new LabeledTweetContainer(results.getString(1),
						results.getFloat(2)));
			}

		} catch (IOException e) {
			_logger.log("Cannot open database properties file.");
			_logger.logStackTrace(e);
			throw e;
		} catch (ClassNotFoundException e) {
			_logger.log("Cannot find database JDBC driver.");
			_logger.logStackTrace(e);
			throw e;
		} catch (SQLException e) {
			_logger.logStackTrace(e);
			throw e;
		} finally {
			// close everything
			if (results != null) {
				results.close();
			}

			if (statement != null) {
				statement.close();
			}

			if (connection != null) {
				connection.close();
			}

			if (fis != null) {
				fis.close();
			}
		}

	}

	/**
	 * Read tweets from database (until the limit). Used by clustering analysis,
	 * since this method can get tweets with no labeled sentiment value (not
	 * required for clustering). While doing so, it excludes all retweets.
	 * 
	 * @param limit
	 *            how many tweets will be stored
	 * @throws Exception
	 *             if error occurs while working with the database.
	 */
	public void readClusterDataFromDB(int limit) throws Exception {
		try {
			// connect to the database
			Properties props = new Properties();
			Connection connect;

			props.load(new FileInputStream(this.properties_file_path));
			Class.forName(props.getProperty("javabase.jdbc.driver"));
			String dbUrl = props.getProperty("javabase.jdbc.url")
					+ props.getProperty("database.name") + "?user="
					+ props.getProperty("javabase.jdbc.username")
					+ "&password="
					+ props.getProperty("javabase.jdbc.password");
			connect = DriverManager.getConnection(dbUrl);

			ResultSet results;

			// Now we use a StringBuilder to build the SQL-Command, since
			// we have to cases:
			// a) no search term id // search term id
			// b) Language / no language
			StringBuilder sqlBuilder = new StringBuilder(300);

			sqlBuilder
					.append("SELECT id, t.text, sentiment_human_label FROM tweets AS t");
			if (searchTermId != null) {
				// if search term id is not null, limit tweets on this term id.
				sqlBuilder.append(" JOIN tweets_has_search_terms AS thst"
						+ " ON t.id = thst.tweets_id"
						+ " WHERE thst.search_terms_id = ?"
						+ " AND t.is_retweet_of_id IS NULL");
			} else {
				sqlBuilder.append(" WHERE t.is_retweet_of_id IS NULL");
			}

			
			
			if (language != null) {
				// if language is not null, search all tweets with this lang.
//				if (searchTermId != null) {
//					sqlBuilder.append(" AND t.iso_language_code = ?");
//				} else {
//					sqlBuilder.append(" WHERE t.iso_language_code = ?");
//				}
				sqlBuilder.append(" AND t.iso_language_code = ?");
			}
			
			sqlBuilder.append(" LIMIT ?");

			PreparedStatement stat = connect.prepareStatement(sqlBuilder
					.toString());

			// set values dependent on the two possible cases:
			int i = 1;

			if (searchTermId != null) {
				stat.setLong(i, this.searchTermId);
				i++;
			}
			if (language != null) {
				stat.setString(i, language);
				i++;
			}
			stat.setInt(i, limit);

			// Now execute...
			stat.execute();
			results = stat.getResultSet();

			// iterate over each tweet and store it in the container and store
			// this in the list
			while (results.next()) {
				tweets.add(new LabeledTweetContainer(results.getLong(1),
						results.getString(2), results.getFloat(3)));
			}

			// if no tweets were found, throw NotDataFoundException
			if (tweets.size() == 0) {
				throw new NotDataFoundException(
						"Could not find data in database");
			}

			// close everything database-related
			results.close();
			stat.close();
			connect.close();

		} catch (IOException e) {
			_logger.log("Cannot open database properties file.");
			_logger.logStackTrace(e);
			throw e;
		} catch (ClassNotFoundException e) {
			_logger.log("Cannot find database JDBC driver.");
			_logger.logStackTrace(e);
			throw e;
		} catch (SQLException e) {
			e.printStackTrace();
			_logger.log("SQL Exception.");
			_logger.logStackTrace(e);
			throw e;
		}

	}

}
