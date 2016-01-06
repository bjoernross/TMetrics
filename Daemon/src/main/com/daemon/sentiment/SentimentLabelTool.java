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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import com.tmetrics.logging.LogManager;
import com.tmetrics.logging.Logger;

/**
 * The sentiment label tool
 * 
 * An independent program that you can use to provide tweets with sentiment
 * labels using the command-line instead of the PHPMyAdmin web interface.
 * 
 * Users are presented random unlabeled tweets in batches, and asked to sort
 * them into one of five categories. Once a batch is completed, the labels are
 * written to the database and a new batch is fetched.
 * 
 * @author Björn
 * 
 */
public class SentimentLabelTool {
	// scanner to read user input
	private Scanner scanner = new Scanner(System.in);

	// database stuff
	Connection connection;
	private PreparedStatement prepStatementGetNewBatch = null;
	private PreparedStatement prepStatementSendBatch = null;

	// tweets and ids in the current batch; read from DB
	private List<Long> retrievedTweetIDs = new ArrayList<Long>();
	private List<String> retrievedTweetTexts = new ArrayList<String>();

	// user labels for the current batch; provided by the user
	private List<Long> labeledTweetIDs = new ArrayList<Long>();
	private List<Float> labeledTweetLabels = new ArrayList<Float>();

	// number of tweets to label before sending results to the database
	// actual batch size may be lower if fewer than 10 unlabeled tweets remain
	private static final int BATCH_SIZE_GET = 1000;
	private static final int BATCH_SIZE_SEND = 10;

	// language for random tweets
	private String language = "en";

	private static final String DATABASE_PROPERTIES_PATH_LOCAL = System
			.getProperty("user.home") + "/database.properties";

	private static final String DATABASE_PROPERTIES_PATH_SERVER = System
			.getProperty("user.home") + "/database-server.properties";

	private String database_properties_path;

	private Logger _logger = LogManager.getLogger("logs/Sentiment.log");

	/**
	 * Run the sentiment label tool
	 * 
	 * This method prints a welcome message to the user and asks for a few
	 * settings in order to connect to the database. Once the connection is
	 * established, it enters a loop where a (potentially large) batch of tweets
	 * is fetched from the database, the user is asked to provide a label (i.e.
	 * sentiment estimate) and every once in a while, the labels are sent to the
	 * database in (potentially smaller) batches.
	 * 
	 * @param args
	 *            Are ignored
	 */
	public static void main(String[] args) {
		SentimentLabelTool tool = new SentimentLabelTool();
		String last_input;

		tool.printWelcome();

		tool.askForDatabase();

		tool.askForLanguage();

		tool.connect();

		while (true) {
			System.out
					.println("Fetching tweets from database... note that this may take a while.");

			// get BATCH_SIZE_GET tweets from Twitter, load them into memory
			tool.getNewBatch();

			// point to the first tweet in the received batch
			int iteratorRetrievedBatch = 0;

			// get new tweets once we've gone through the received batch
			while (iteratorRetrievedBatch < tool.retrievedTweetIDs.size()) {

				// send once BATCH_SIZE_SEND tweets are labeled, or if there are
				// no more tweets in the received batch
				while (iteratorRetrievedBatch < tool.retrievedTweetIDs.size()
						&& tool.labeledTweetIDs.size() < BATCH_SIZE_SEND) {
					// present the user with a tweet
					System.out.println(tool.retrievedTweetTexts
							.get(iteratorRetrievedBatch));

					// read their input
					last_input = tool.scanner.nextLine();

					// process it
					tool.processInput(
							tool.retrievedTweetIDs.get(iteratorRetrievedBatch),
							last_input);

					iteratorRetrievedBatch++;
				}

				System.out.println("Thank you! Sending tweets to database...");
				tool.sendBatchToDB();
			}
		}
	}

	/**
	 * Print a welcome message to the user
	 */
	private void printWelcome() {
		String welcome = "Welcome to the tmetrics sentiment labeling tool!\n"
				+ "You will now be given batches of 10 tweets each.\n"
				+ "Please rate them into either of the categories: -1, -0.5, 0, 0.5, 1\n"
				+ "where -1 is maximum negative, and 1 is maximum positive sentiment.\n"
				+ "For convenience, you can use the letters c, v, b, n, and m on your keyboard.\n"
				+ "c means -1, and m means 1.\n"
				+ "Enter q at any time to quit. But if you quit during a batch, the batch will be lost!\n"
				+ "Have fun!" + "\n";
		System.out.println(welcome);
	}

	/**
	 * Finds out which database the user wants to connect to
	 */
	private void askForDatabase() {
		System.out.println("Which server do you want to label tweets on?\n"
				+ "Please enter either l (local) or s (server).");
		String dbInput = scanner.nextLine();
		switch (dbInput) {
		case "s":
		case "server":
			database_properties_path = DATABASE_PROPERTIES_PATH_SERVER;
			break;
		case "l":
		case "local":
			database_properties_path = DATABASE_PROPERTIES_PATH_LOCAL;
			break;
		case "q":
			this.quit(0);
		default:
			System.out.println("Oh dear, that was not what I wanted to hear.");
			// try again
			askForDatabase();
		}
		System.out.println("\n");
	}

	/**
	 * Have the user enter the language that they want the tweets to be in
	 */
	private void askForLanguage() {
		System.out.println("Which language do you want to label tweets for?\n"
				+ "Please enter a valid ISO two-letter language code.");
		language = scanner.nextLine();
		System.out.println("\n");
	}

	/**
	 * Connect to the database and initialize the statements
	 */
	private void connect() {
		Properties props = new Properties();
		FileInputStream fis = null;
		try {
			// get database URL
			fis = new FileInputStream(database_properties_path);
			props.load(fis);
			Class.forName(props.getProperty("javabase.jdbc.driver"));
			String dbUrl = props.getProperty("javabase.jdbc.url")
					+ props.getProperty("database.name") + "?user="
					+ props.getProperty("javabase.jdbc.username")
					+ "&password="
					+ props.getProperty("javabase.jdbc.password");

			// connect
			connection = DriverManager.getConnection(dbUrl);

			// initialize statements
			// note that we do not train on retweets
			String sqlQueryGetNewBatch = "SELECT id,text FROM tweets WHERE is_retweet_of_id IS NULL AND sentiment_human_label IS NULL AND iso_language_code = ? ORDER BY RAND() LIMIT ?";
			String sqlQuerySendBatch = "UPDATE tweets SET sentiment_human_label = ? WHERE id = ?";
			prepStatementGetNewBatch = connection
					.prepareStatement(sqlQueryGetNewBatch);
			prepStatementSendBatch = connection
					.prepareStatement(sqlQuerySendBatch);

		} catch (IOException e) {
			_logger.log("Cannot open database properties file.");
			_logger.logStackTrace(e);
			System.out
					.println("An error occurred. Please consult logs/Sentiment.log for further details.");
			// without a DB connection, further executing the program does not
			// make any sense
			System.exit(-1);
		} catch (ClassNotFoundException e) {
			_logger.log("Cannot find database JDBC driver.");
			_logger.logStackTrace(e);
			System.out
					.println("An error occurred. Please consult logs/Sentiment.log for further details.");
			// without a DB connection, further executing the program does not
			// make any sense
			System.exit(-1);
		} catch (SQLException e) {
			_logger.logStackTrace(e);
			System.out
					.println("An error occurred. Please consult logs/Sentiment.log for further details.");
			// without a DB connection, further executing the program does not
			// make any sense
			System.exit(-1);
		} finally {
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
	 * Process user input, i. e. find out what number they entered, and add it
	 * to the label list. Lets them skip the tweet or quit the app
	 * 
	 * @param tweetID
	 *            ID of the tweet the input was for
	 * @param last_input
	 *            Whatever the user just entered
	 */
	private void processInput(long tweetID, String last_input) {
		Float last_input_float;
		// does the user want to skip?
		if (last_input.equals("s")) {
			labeledTweetLabels.add(null);
			labeledTweetIDs.add(null);
			return;
		}
		// does the user want to quit?
		if (last_input.equals("q")) {
			// pack your things and run
			System.out.println("Goodbye...");
			this.quit(0);
		}
		// try to convert input to a float
		try {
			// what number did the user enter?
			// note that if they didn't enter a number, this will trigger the
			// catch block
			last_input_float = Float.parseFloat(last_input);
			// can the number the user entered be resolved to a category?
			if (Math.abs(last_input_float - 1) < 0.01) {
				labeledTweetLabels.add((float) 1);
				labeledTweetIDs.add(tweetID);
			} else if (Math.abs(last_input_float - 0.5) < 0.01) {
				labeledTweetLabels.add((float) 0.5);
				labeledTweetIDs.add(tweetID);
			} else if (Math.abs(last_input_float) < 0.01) {
				labeledTweetLabels.add((float) 0);
				labeledTweetIDs.add(tweetID);
			} else if (Math.abs(last_input_float + 0.5) < 0.01) {
				labeledTweetLabels.add((float) -0.5);
				labeledTweetIDs.add(tweetID);
			} else if (Math.abs(last_input_float + 1) < 0.01) {
				labeledTweetLabels.add((float) -1);
				labeledTweetIDs.add(tweetID);
			} else {
				// user entered another number
				System.out
						.println("You entered the number "
								+ last_input_float
								+ ". Please only use the categories -1, -0.5, 0, 0.5, and 1.");
				// try again
				last_input = scanner.nextLine();
				processInput(tweetID, last_input);
			}

		} catch (Exception e) {
			// oops! user entered a string. define keyboard shortcuts
			// typing -0.5 takes a lot longer than typing c
			switch (last_input) {
			case "m":
				labeledTweetLabels.add((float) 1);
				labeledTweetIDs.add(tweetID);
				break;
			case "n":
				labeledTweetLabels.add((float) 0.5);
				labeledTweetIDs.add(tweetID);
				break;
			case "b":
				labeledTweetLabels.add((float) 0);
				labeledTweetIDs.add(tweetID);
				break;
			case "v":
				labeledTweetLabels.add((float) -0.5);
				labeledTweetIDs.add(tweetID);
				break;
			case "c":
				labeledTweetLabels.add((float) -1);
				labeledTweetIDs.add(tweetID);
				break;
			default:
				// unrecognized string
				System.out
						.println("I'm pretty sure that was not a number. Please try again!");
				// try again
				last_input = scanner.nextLine();
				processInput(tweetID, last_input);
				break;
			}
		}
	}

	/**
	 * Get a new batch of tweets from the database
	 */
	private void getNewBatch() {
		ResultSet results;

		try {
			// get 10 random tweets
			// a possible direction for future development: active learning,
			// i.e. choosing the training examples so that they maximize some
			// sort of measure (human labour is costly :) )
			prepStatementGetNewBatch.setString(1, language);
			prepStatementGetNewBatch.setInt(2, BATCH_SIZE_GET);
			prepStatementGetNewBatch.execute();
			results = prepStatementGetNewBatch.getResultSet();

			// find out if resultSet is empty
			if (!results.isBeforeFirst()) {
				System.out
						.println("Wow! It seems like there are no more tweets for you to label. Congratulations! (Or, maybe, your database is empty.)");
				this.quit(0);
			}

			retrievedTweetIDs = new ArrayList<Long>();
			retrievedTweetTexts = new ArrayList<String>();

			// save the results into object members
			while (results.next()) {
				retrievedTweetIDs.add(results.getLong(1));
				retrievedTweetTexts.add(results.getString(2));
			}
			results.close();

		} catch (SQLException e) {
			_logger.logStackTrace(e);
			System.out
					.println("An error occurred. Please consult logs/Sentiment.log for further details.");
			// without a DB connection, further executing the program does not
			// make any sense
			this.quit(-1);
		}
	}

	/**
	 * Send the current batch to the database
	 */
	private void sendBatchToDB() {
		try {
			for (int i = 0; i < labeledTweetIDs.size(); i++) {
				if (labeledTweetLabels.get(i) == null) {
					prepStatementSendBatch.setNull(1, java.sql.Types.NULL);
				} else {
					prepStatementSendBatch.setFloat(1,
							labeledTweetLabels.get(i));
				}
				prepStatementSendBatch.setLong(2, retrievedTweetIDs.get(i));
				prepStatementSendBatch.addBatch();
			}
			prepStatementSendBatch.executeBatch();

			// if we've successfully sent everything to the DB, we can delete it
			// from our memory
			labeledTweetIDs = new ArrayList<Long>();
			labeledTweetLabels = new ArrayList<Float>();

		} catch (SQLException e) {
			_logger.logStackTrace(e);
			System.out
					.println("An error occurred. Please consult logs/Sentiment.log for further details.");
			// without a DB connection, further executing the program does not
			// make any sense
			System.exit(-1);
		}
	}

	/**
	 * Quit the program, closing all statements and the database connection
	 * 
	 * @param status
	 *            Status code passed through to System.exit
	 */
	private void quit(int status) {
		scanner.close();
		try {
			if (prepStatementGetNewBatch != null) {
				prepStatementGetNewBatch.close();
			}
			if (prepStatementSendBatch != null) {
				prepStatementSendBatch.close();
			}
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			_logger.logStackTrace(e);
			System.out
					.println("An error occurred. Please consult logs/Sentiment.log for further details.");
			// exit with a failure status code
			System.exit(-1);
		}
		// everything is okay
		System.exit(status);
	}

}
