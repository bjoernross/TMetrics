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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tmetrics.dto.SentimentFeatures;
import com.tmetrics.logging.LogManager;
import com.tmetrics.logging.Logger;

/**
 * Sentiment classifier that uses a regression model.
 * 
 * @author Björn, Erwin
 */
public class RegressionSentimentClassifier implements SentimentClassifier {

	static private Logger _logger = LogManager.getLogger("logs/Master.log");

	/**
	 * Map that holds the regression models, indexed by two-letter ISO language
	 * codes (e.g. "de", "en")
	 */
	private Map<String, RegressionModel> models;

	/**
	 * Constructor to initialize the Classifier
	 * 
	 * Reads regression models from files in user's home directory. If there are
	 * no model files on disk, they will be created (and saved to disk) by
	 * RegressionSentimentUpdater, which can be called explicitly after
	 * RegressionSentimentClassifier is constructed
	 */
	public RegressionSentimentClassifier() {
		this.models = new HashMap<String, RegressionModel>();

		// try reading the models from hard disk
		this.readModelsFromFiles();
	}

	/**
	 * Try reading regression models from user's hard disk. Fails silently if no
	 * models are present
	 */
	private void readModelsFromFiles() {

		RegressionModel model;
		File[] files = new File(System.getProperty("user.home")).listFiles();

		ObjectInputStream ois = null;
		FileInputStream fis = null;

		// Iterate over all files in user's home directory and look for
		// regression models:
		for (File file : files) {
			if (file.isFile() && file.getName().startsWith("regression_model")) {

				try {
					// Read model from hard disk
					fis = new FileInputStream(System.getProperty("user.home")
							+ "/" + file.getName());
					ois = new ObjectInputStream(fis);
					model = (RegressionModel) ois.readObject();

					// depending on models language, put it into right position
					// of models map.
					this.models.put(model.getModelOfLanguage(), model);

					ois.close();
				} catch (IOException | ClassNotFoundException e) {
					_logger.log("Could not load regression model from hard disk for "
							+ file.getAbsolutePath());
					_logger.logStackTrace(e);
				} catch (ClassCastException ec) {
					String message = "ClassCastException when trying to read model from file "
							+ file.getAbsolutePath()
							+ ". Most likely your file is based on an outdated version of this class. Please delete the file and try again. Consult the logs for more details.";
					System.out.println("WARNING: " + message);
					_logger.log(message);
					_logger.logStackTrace(ec);
				}
			}
		}
		try {
			if (ois != null) {
				ois.close();
			}
			if (fis != null) {
				fis.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Start a separate thread which continually makes sure regression models
	 * are up-to-date
	 */
	public void startRegressionSentimentUpdater() {
		Thread updater = new Thread(new RegressionSentimentUpdater(models));
		updater.start();
	}

	@Override
	/**
	 * Get the sentiment of a given tweet
	 */
	public Float determineSentiment(String tweetText, String language) {
		if (this.models.containsKey(language)) {
			return this.models.get(language).determineSentiment(tweetText);
		} else {
			return null;
		}
	}

	/**
	 * Get the sentiment of a given tweet as well as detailed information on how
	 * the estimate was calculated (i.e. all relevant features and parameter
	 * values)
	 * 
	 * @param tweetText
	 *            Text to be analyzed
	 * @param language
	 *            Language the text is assumed to be in
	 * @return DTO containing detailed information on the tweet's sentiment
	 */
	public SentimentFeatures determineSentimentDetails(String tweetText,
			String language) {
		if (this.models.containsKey(language)) {
			return this.models.get(language).determineSentimentDetails(
					tweetText);
		} else {
			return null;
		}
	}

	/**
	 * Given a feature (e.g. a unigram), get the training tweets that have the
	 * feature
	 * 
	 * @param feature
	 *            A feature, e.g. a unigram, bigram, trigram, or fourgram
	 * @param language
	 *            Language the original text is assumed to be in (tells us in
	 *            which model to look for the feature)
	 * @return A list of tweets that were used as training data and that have
	 *         the feature
	 */

	public List<LabeledTweetContainer> determineImportantTrainingTweets(
			String feature, String language) {
		if (this.models.containsKey(language)) {
			return this.models.get(language).determineImportantTrainingTweets(
					feature);
		} else {
			return null;
		}
	}

	/**
	 * Get the map of regression models
	 * 
	 * @return Map of regression models, indexed by two-letter ISO language code
	 */
	public Map<String, RegressionModel> getModels() {
		return models;
	}

}
