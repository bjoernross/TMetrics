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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;

import com.tmetrics.exceptions.NotDataFoundException;
import com.tmetrics.logging.LogManager;
import com.tmetrics.logging.Logger;

/**
 * RegressionSentimentUpdater is a Runnable (i.e. thread) that regularly checks
 * the database for whether there is new training data for a given set of
 * sentiment regression models. It compares the number of labeled tweets used in
 * the generation of the existing model (if there is one) to the number of
 * currently available tweets. If the difference is large enough, it generates a
 * new model.
 * 
 * Some options can be changed without changing the code, see
 * ~/regression.properties
 * 
 * @author Erwin, Björn
 * 
 */
public class RegressionSentimentUpdater implements Runnable {

	// log exceptions to logs/Sentiment.log
	static private Logger _logger = LogManager.getLogger("logs/Sentiment.log");

	// interval between database checks and possible updates
	private int sleepTime;

	// number of labeled tweets required for a new model to be trained
	private int newModelMinimum;

	// number of labeled tweets required for an existing model to be updated
	private int updateModelMinimum;

	// whether to use bigrams, trigrams, fourgrams as features
	private boolean useBigrams;
	private boolean useTrigrams;
	private boolean useFourgrams;

	// path to database.properties which specifies the database to connect to
	private String database_properties_file_path = System
			.getProperty("user.home") + "/database.properties";

	// path to regression.properties with options that can be changed without
	// changing the code
	static private final String REGRESSION_PROPERTIES_FILE_PATH = System
			.getProperty("user.home") + "/regression.properties";

	// database connections. we need two because JDBC does not support multiple
	// simultaneous statements over one connection if one statement has a
	// streamed result set
	private Connection selectConnection;
	private Connection updateConnection;

	// map of regression models. key is interpreted as a two-letter ISO language
	// code
	private Map<String, RegressionModel> models;

	/**
	 * Constructs a RegressionSentimentUpdater, reading the properties files
	 * 
	 * @param models
	 *            Map of regression models to check against and update
	 */
	public RegressionSentimentUpdater(Map<String, RegressionModel> models) {
		// read properties file regression.properties
		this.readProperties();

		this.models = models;
	}

	@Override
	/**
	 * Run the SentimentRegressionUpdater
	 * 
	 * Contains the loop that connects, checks, updates if necessary, and disconnects
	 */
	public void run() {

		// the thread will do this until the daemon is terminated
		while (true) {

			try {

				// connect to the database
				this.connect();

				// check for new data; if there is, update models
				this.updateModels();

				// disconnect from database
				this.disconnect();

				// sleep for the specified amount of time
				Thread.sleep(sleepTime);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

	}

	/**
	 * Read properties file regression.properties, which holds options that can
	 * be changed without changing the code
	 */
	private void readProperties() {
		Properties props = new Properties();
		FileInputStream fis = null;
		try {
			// create FileInputStream from default path
			fis = new FileInputStream(REGRESSION_PROPERTIES_FILE_PATH);

			// load as properties file
			props.load(fis);

			// set options
			sleepTime = Integer.parseInt(props.getProperty("sleep.time")) * 1000;
			newModelMinimum = Integer.parseInt(props
					.getProperty("new.model.minimum"));
			updateModelMinimum = Integer.parseInt(props
					.getProperty("update.model.minimum"));

			useBigrams = Boolean.parseBoolean(props
					.getProperty("update.model.use_bigrams"));
			useTrigrams = Boolean.parseBoolean(props
					.getProperty("update.model.use_trigrams"));
			useFourgrams = Boolean.parseBoolean(props
					.getProperty("update.model.use_fourgrams"));

		} catch (IOException e) {
			// if there is an exception, notify the user and set options to
			// default values
			_logger.log("Cannot load regression.properties file from path "
					+ REGRESSION_PROPERTIES_FILE_PATH
					+ ". Using default values.");
			_logger.logStackTrace(e);

			sleepTime = 86400 * 1000;
			newModelMinimum = 100;
			updateModelMinimum = 1;

			useBigrams = true;
			useTrigrams = true;
			useFourgrams = true;
		} finally {
			// close FileInputStream
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Connect to the database, using the database properties path
	 */
	private void connect() {
		Properties props = new Properties();
		FileInputStream fis = null;

		// connect to database
		try {
			// create FileInputStream from default path
			fis = new FileInputStream(database_properties_file_path);

			// load as properties file
			props.load(fis);

			// JDBC driver
			Class.forName(props.getProperty("javabase.jdbc.driver"));

			// create the database URL by concatenating the relevant option
			// strings
			String dbUrl = props.getProperty("javabase.jdbc.url")
					+ props.getProperty("database.name") + "?user="
					+ props.getProperty("javabase.jdbc.username")
					+ "&password="
					+ props.getProperty("javabase.jdbc.password");
			selectConnection = DriverManager.getConnection(dbUrl);
			updateConnection = DriverManager.getConnection(dbUrl);

			// catch exceptions
		} catch (IOException e) {
			_logger.log("Cannot open database properties file.");
			_logger.logStackTrace(e);
		} catch (ClassNotFoundException e) {
			_logger.log("Cannot find database JDBC driver.");
			_logger.logStackTrace(e);
		} catch (SQLException e) {
			_logger.logStackTrace(e);
		} finally {
			// close FileInputStream
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Disconnect from database
	 */
	private void disconnect() {
		try {
			selectConnection.close();
			updateConnection.close();
		} catch (SQLException e) {
			// oops
		}
	}

	/**
	 * Update regression models if new training data are available (when
	 * hard-working labeler have labeled tweets ;) ).
	 * 
	 * @throws Exception
	 */
	private void updateModels() {

		// connect to the database
		Statement sqlCountStatement = null;
		ResultSet results = null;
		try {
			// get the number of labeled tweets for each language to determine
			// if we can create a new Regression Model or update an existing
			// model.
			String sqlCount = "SELECT iso_language_code, COUNT(*) FROM tweets "
					+ "WHERE sentiment_human_label IS NOT NULL "
					+ "GROUP BY iso_language_code";
			sqlCountStatement = selectConnection.createStatement();
			sqlCountStatement.execute(sqlCount);
			results = sqlCountStatement.getResultSet();

			String language;
			Integer countTweets;
			while (results.next()) {
				language = results.getString(1);
				countTweets = results.getInt(2);

				// (IF there already is a model for the language AND the number
				// of available labeled tweets is higher than the number of
				// tweets that were used in its training plus a minimum
				// threshold), OR (IF there is no model for the language AND the
				// number of available labeled tweets is higher than a minimum
				// threshold)
				if ((this.models.containsKey(language) && countTweets >= this.models
						.get(language).getSourceData().getTweets().size()
						+ updateModelMinimum)
						|| (!this.models.containsKey(language) && countTweets >= newModelMinimum)) {

					// so we're building a model :)
					// time and log everything
					long startTime = System.currentTimeMillis();
					_logger.log("Building regression model for language "
							+ language + "...");

					Features features = new Features();

					// which features to use
					features.useUnigrams(true).useBigrams(useBigrams)
							.useTrigrams(useTrigrams).use4Grams(useFourgrams)
							.useDictionary(true).useEmoticons(true)
							.usePOSTagger(false).useNegations(false);

					// create the regression model
					RegressionModel model = new RegressionModel(features,
							language, null, 1, "Local");

					// more timing and logging
					long stopTime = System.currentTimeMillis();
					long executionTime = stopTime - startTime;
					_logger.log("Built regression model for language "
							+ language + ". Took " + executionTime + "ms.");

					startTime = System.currentTimeMillis();
					_logger.log("Storing regression model for language "
							+ language + "...");

					// Store the model in models (reference shared with
					// RegressionSentimentClassifier) so that the daemon can use
					// it
					this.models.put(language, model);
					// Store the model on HDD by serializing it so that the REST
					// service can load and use it
					model.serialize(System.getProperty("user.home")
							+ "/regression_model_" + language + ".ser");

					// even more timing and logging
					stopTime = System.currentTimeMillis();
					executionTime = stopTime - startTime;
					_logger.log("Stored regression model for language "
							+ language + ". Took " + executionTime + "ms.");

					// if we have a new model, update all tweets in database
					// with that language
					this.updateSentimentForAllDatabaseTweets(language);

				}

			}

			// catch exceptions
		} catch (IOException e) {
			_logger.log("Cannot open database properties file.");
			_logger.logStackTrace(e);
		} catch (SQLException e) {
			_logger.logStackTrace(e);
		} catch (NotDataFoundException e) {
			_logger.log("No training/test data for sentiment regression");
			_logger.logStackTrace(e);
		} catch (Exception e) {
			_logger.log("Unknown error occured while doing sentiment regression");
			_logger.logStackTrace(e);
		} finally {
			// close everything, freeing up used resources, no matter whether it
			// failed or succeeded
			try {
				if (results != null)
					results.close();
				if (sqlCountStatement != null)
					sqlCountStatement.close();
			} catch (SQLException e) {
				// oops
			}
		}

	}

	/**
	 * Updates the sentiment value of all tweets in the database with a specific
	 * language, using the current model
	 * 
	 * To be called after training a new regression model
	 * 
	 * @param language
	 * @throws SQLException
	 */
	private void updateSentimentForAllDatabaseTweets(String language)
			throws SQLException {
		_logger.log("Updating sentiment for all database tweets with language "
				+ language);
		long methodStartTime = System.currentTimeMillis();
		long selectTime = 0;
		long updateTime = 0;
		long estimateTime = 0;
		long tempStartTime = 0;

		// turn off auto-commit mode - 95% performance increase with the batched
		// statements below
		updateConnection.setAutoCommit(false);

		// timing
		tempStartTime = System.currentTimeMillis();

		// get all tweets in database with the specified language
		String sqlSelect = "SELECT id, text FROM tweets WHERE iso_language_code = ?";
		PreparedStatement selectStatement = selectConnection
				.prepareStatement(sqlSelect);
		selectStatement.setString(1, language);
		selectStatement.setFetchSize(Integer.MIN_VALUE);
		selectStatement.execute();
		ResultSet results = selectStatement.getResultSet();

		// timing
		selectTime += System.currentTimeMillis() - tempStartTime;

		// UPDATE statement
		String sqlUpdate = "UPDATE tweets SET sentiment = ? WHERE id = ?";
		PreparedStatement updateStatement = updateConnection
				.prepareStatement(sqlUpdate);
		String sqlUpdate2 = "UPDATE tweets_has_search_terms SET sentiment = ? WHERE tweets_id = ?";
		PreparedStatement updateStatement2 = updateConnection
				.prepareStatement(sqlUpdate2);

		long tweetId;
		String tweetText;
		float sentimentEstimate;
		int batchCount = 0;

		// iterate over tweets in resultSet
		tempStartTime = System.currentTimeMillis();
		while (results.next()) {
			// timing
			selectTime += System.currentTimeMillis() - tempStartTime;

			// get current tweet data
			tweetId = results.getLong(1);
			tweetText = results.getString(2);

			// timing
			tempStartTime = System.currentTimeMillis();

			// get new sentiment value for current tweet
			sentimentEstimate = this.models.get(language).determineSentiment(
					tweetText);

			// timing
			estimateTime += System.currentTimeMillis() - tempStartTime;
			tempStartTime = System.currentTimeMillis();

			// add tweet with new sentiment value to update batch
			updateStatement.setFloat(1, sentimentEstimate);
			updateStatement.setLong(2, tweetId);
			updateStatement.addBatch();
			updateStatement2.setFloat(1, sentimentEstimate);
			updateStatement2.setLong(2, tweetId);
			updateStatement2.addBatch();

			// every 1000 tweets, execute
			if (++batchCount > 1000) {
				updateStatement.executeBatch();
				updateStatement2.executeBatch();
				batchCount = 0;
			}

			// timing
			updateTime += System.currentTimeMillis() - tempStartTime;
			tempStartTime = System.currentTimeMillis();
		}

		// timing
		tempStartTime = System.currentTimeMillis();

		// insert remaining records
		updateStatement.executeBatch();
		updateStatement2.executeBatch();
		updateTime += System.currentTimeMillis() - tempStartTime;

		// commit the transaction
		updateConnection.commit();

		// close resultSet and statements
		results.close();
		selectStatement.close();
		updateStatement.close();
		updateStatement2.close();

		// switch auto-commit mode back on
		updateConnection.setAutoCommit(true);

		// log the timing
		long executionTime = System.currentTimeMillis() - methodStartTime;
		_logger.log("Finished updating sentiment for all database tweets with language "
				+ language
				+ ". Took "
				+ executionTime
				+ "ms in total, of which "
				+ selectTime
				+ "ms were for SELECTs, "
				+ updateTime
				+ "ms for UPDATEs, and "
				+ estimateTime
				+ " for sentiment estimation. (The remaining "
				+ (executionTime - selectTime - updateTime - estimateTime)
				+ "ms were for System.currentTimeMillis :P)");
	}

	/**
	 * Use the test database instead of the production database
	 * 
	 * Applies to all subsequent calls of updateModels
	 */
	public void useTestDB() {
		database_properties_file_path = "src/test/resources/database.properties";
	}

}
