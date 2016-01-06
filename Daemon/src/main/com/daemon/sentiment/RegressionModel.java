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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.tmetrics.dto.DictionarySentimentDetails;
import com.tmetrics.dto.Ngram;
import com.tmetrics.dto.SentimentFeatures;
import com.tmetrics.logging.LogManager;
import com.tmetrics.logging.Logger;
import com.tmetrics.util.LinearRegression;
import com.tmetrics.util.ListUtil;

/**
 * Represents a regression model, based on specified training data.
 * 
 * When initialized, the model is immediately trained using gradient descent.
 * The training set is chosen according to the constructor parameters.
 * 
 * From then on, the model can be used to determine the sentiment of a given
 * tweet. It also supplies other relevant information, such as the training set,
 * feature matrix, estimated parameters, errors, and estimated sentiment for the
 * training set.
 * 
 * @author Björn, Erwin
 */
public class RegressionModel implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	// the training data - to use for regression
	private SentimentSourceData sourceData;

	// regression object - do the regression
	private LinearRegression linearRegression;

	// feature matrix
	private FeatureMatrix featureMatrix;

	// estimated parameters
	private List<Float> parameters;

	// percentage of the available tweets, used for training the regression
	// model.
	private final float percentageTraining;

	private long createdAt = 0;

	private transient Logger _logger = LogManager.getLogger("logs/Master.log");

	/**
	 * Estimated Accuracy of Model (using MAE) by using test data
	 */
	private Float estimatedMAE = null;

	/**
	 * Constructor to initialize a Regression Model
	 * 
	 * @param features
	 *            which will be extracted from tweets to training the model.
	 * @param language
	 *            of tweets used for training the model.
	 * @param searchTermId
	 *            the search term id of tweets used for training the model.
	 * @param percentageTraining
	 *            percentage of the available tweets, used for training the
	 *            regression model.
	 * @param database
	 *            determines which database should be used. "Local" for the
	 *            production database, use "RestTest" for the test rest
	 *            database, "DaemonTest" for the test daemon database and
	 *            "Remote" if you are coming from RegressionTester.
	 * @throws Exception
	 */
	public RegressionModel(Features features, String language,
			Long searchTermId, float percentageTraining, String database)
			throws Exception {

		// Create the object variables, needed to calculate / represent the
		// regression model
		parameters = new ArrayList<Float>();

		// Create the Data Object - delivering the data to do the regression
		this.sourceData = new SentimentSourceData(searchTermId, language,
				database);

		this.percentageTraining = percentageTraining;
		// Calculate the regression model

		// 1. Get Training Data
		System.out.println("Begin to fetch tweets from database " + database);
		sourceData.readDataFromDB(this.percentageTraining, false);
		// 2. Create Design Matrix
		System.out.println("Begin to create Feature matrix");
		this.featureMatrix = new FeatureMatrix(features, sourceData);
		// 3. Solve the regression with - gradient descent -
		System.out.println("Begin to do regression");
		this.linearRegression = new LinearRegression(
				this.featureMatrix.getFeatureMatrixAsSparseMatrix(),
				this.sourceData.getLabels());
		this.parameters = this.linearRegression.getParameters();

		this.createdAt = System.currentTimeMillis();
		// Finished!

		// some useful statistics
		/*
		 * System.out.println("Feature names: " + getFeatureNamesOrdered());
		 * System.out.println("Parameters: " + parameters);
		 */
	}

	public long getCreatedAt() {
		return createdAt;
	}

	/**
	 * Get the source data of the regression model.
	 * 
	 * Use this method if you want to get the training tweets of this regression
	 * model.
	 * 
	 * @return
	 */
	SentimentSourceData getSourceData() {
		return this.sourceData;
	}

	/**
	 * Get the feature matrix (aka design matrix)
	 * 
	 * @return feature matrix
	 */
	FeatureMatrix getFeatureMatrix() {
		return featureMatrix;
	}

	/**
	 * Get estimated parameters
	 * 
	 * @return estimated parameters
	 */
	List<Float> getParameters() {
		return parameters;
	}

	public Float getEstimatedMAE() {
		return estimatedMAE;
	}

	public void setEstimatedMAE(Float estimatedMAE) {
		this.estimatedMAE = estimatedMAE;
	}

	/**
	 * Language of Regression model
	 * 
	 * @return the language of model for which the model is trained.
	 */
	public String getModelOfLanguage() {
		return this.sourceData.getLanguage();
	}

	/**
	 * Search term id for which regression model is trained. Can be null.
	 * 
	 * @return
	 */
	public Long getSearchTermId() {
		return this.sourceData.getSearchTermId();
	}

	/**
	 * Get the database name which was used to get tweets.
	 * 
	 * @return "DaemonTest", "RestTest", "Server" or "Local"
	 */
	public String getUsedDatabase() {
		return this.sourceData.getUsedDatabase();
	}

	/**
	 * Determine the sentiment of a given tweet using the parameters
	 * 
	 * @param tweetText
	 * @return
	 */
	public float determineSentiment(String tweetText) {
		// create feature vector by extracting features from tweet
		List<Float> featureVector = this.featureMatrix.createFeatureVector(
				tweetText, false).getRowAsList(0);

		// estimate sentiment by multiplying feature vector with parameters
		float estimate = ListUtil.scalarProduct(parameters, featureVector);

		return estimate;
	}

	/**
	 * Gets all training tweets that influenced a feature
	 * 
	 * @param featureText
	 * @return
	 */
	public List<LabeledTweetContainer> determineImportantTrainingTweets(
			String featureText) {
		// featureText = "$unigram$love" oder "$bigram$I love" oder oder...

		// get column
		int matrix_j = this.featureMatrix.getFeatureNames().get(featureText);

		List<Float> featureColumn = this.featureMatrix
				.getFeatureMatrixAsSparseMatrix().getColumnAsList(matrix_j);
		List<String> tweetTexts = sourceData.getTweetTexts();
		List<Float> tweetLabels = sourceData.getLabels();

		List<LabeledTweetContainer> importantTrainingTweets = new ArrayList<LabeledTweetContainer>();

		// get all rows for which column does not equal 0, and add the
		// corresponding training tweet text to the list
		for (int i = 0; i < featureColumn.size(); i++) {
			if (Math.abs(featureColumn.get(i)) > 0.001) {
				importantTrainingTweets.add(new LabeledTweetContainer(
						tweetTexts.get(i), tweetLabels.get(i)));
			}
		}

		return importantTrainingTweets;
	}

	/**
	 * Return features influencing the sentiments of a given tweet
	 * 
	 * @param tweetText
	 * @return
	 */
	public SentimentFeatures determineSentimentDetails(String tweetText) {
		List<Float> featureVector = this.featureMatrix.createFeatureVector(
				tweetText, false).getRowAsList(0);
		List<String> featureNames = this.featureMatrix.getFeatureNamesOrdered();
		List<Float> featureParameters = getParameters();

		SentimentFeatures features = new SentimentFeatures();

		String name;
		Float value, parameter;

		for (int i = 0; i < featureVector.size(); i++) {
			value = featureVector.get(i);
			name = featureNames.get(i);
			parameter = featureParameters.get(i);

			if (value != 0) {
				if (name.equals("$tmetrics$dictionary_sentiment")) {
					DictionarySentimentDetails dict = this.featureMatrix
							.getDictionaryClassifier()
							.determineSentimentDetails(tweetText);
					dict.setSentimentParam(parameter);
					dict.setPosCountParam(featureParameters.get(i + 1));
					dict.setNegCountParam(featureParameters.get(i + 2));
					features.setWordDictionaryDetails(dict);
				} else if (name.equals("$tmetrics$emoticon_sentiment")) {
					DictionarySentimentDetails dict = this.featureMatrix
							.getEmoticonClassifier().determineSentimentDetails(
									tweetText);
					dict.setSentimentParam(parameter);
					dict.setPosCountParam(featureParameters.get(i + 1));
					dict.setNegCountParam(featureParameters.get(i + 2));
					features.setEmoticonDictionaryDetails(dict);
				} else if (name.equals("$tmetrics$regression_constant")) {
					features.addOther("regression_constant", parameter);
				} else if (name.startsWith("$unigram$")) {
					// shortName = name.replace("$unigram$", "");
					// unigrams.add(new Ngram(shortName,
					// tweetText.toLowerCase().indexOf(shortName), parameter));
					features.addUnigram(name.replace("$unigram$", ""),
							parameter);
				} else if (name.startsWith("$bigram$")) {
					// shortName = name.replace("$bigram$", "");
					// bigrams.add(new Ngram(shortName,
					// tweetText.toLowerCase().indexOf(shortName), parameter));
					features.addBigram(name.replace("$bigram$", ""), parameter);
				} else if (name.startsWith("$trigram$")) {
					// shortName = name.replace("$trigram$", "");
					// trigrams.add(new Ngram(shortName,
					// tweetText.toLowerCase().indexOf(shortName), parameter));
					features.addTrigram(name.replace("$trigram$", ""),
							parameter);
				} else if (name.startsWith("$fourgram$")) {
					// shortName = name.replace("$fourgram$", "");
					// fourgrams.add(new Ngram(shortName,
					// tweetText.toLowerCase().indexOf(shortName), parameter));
					features.addFourgram(name.replace("$fourgram$", ""),
							parameter);
				}
			}
		}

		/*
		 * System.out.println("Testing sentiment details.");
		 * 
		 * System.out.println("Tweet text:\n" + tweetText);
		 * 
		 * System.out.println("Before sorting:");
		 * System.out.println(unigrams.toString());
		 * System.out.println(bigrams.toString());
		 * System.out.println(trigrams.toString());
		 * System.out.println(fourgrams.toString());
		 * 
		 * Collections.sort(unigrams); Collections.sort(bigrams);
		 * Collections.sort(trigrams); Collections.sort(fourgrams);
		 * 
		 * System.out.println("After sorting:");
		 * System.out.println(unigrams.toString());
		 * System.out.println(bigrams.toString());
		 * System.out.println(trigrams.toString());
		 * System.out.println(fourgrams.toString());
		 * 
		 * System.out.println("");
		 * 
		 * for (Ngram unigram : unigrams)
		 * features.addUnigram(unigram.getString(), unigram.getParameter()); for
		 * (Ngram bigram : bigrams) features.addBigram(bigram.getString(),
		 * bigram.getParameter()); for (Ngram trigram : trigrams)
		 * features.addTrigram(trigram.getString(), trigram.getParameter()); for
		 * (Ngram fourgram : fourgrams)
		 * features.addFourgram(fourgram.getString(), fourgram.getParameter());
		 */

		return features;
	}

	/**
	 * Serialize the object
	 * 
	 * @throws IOException
	 */
	public void serialize(String filename) throws IOException {
		OutputStream fos = new FileOutputStream(filename);
		ObjectOutputStream o = new ObjectOutputStream(fos);
		o.writeObject(this);
		o.close();
	}

	/**
	 * Read the object, restoring it to its previous state and acquiring a new
	 * instance of Logger (which is transient and therefore not serialized)
	 * 
	 * @param ois
	 * @throws IOException
	 */
	private void readObject(ObjectInputStream ois) throws IOException {
		try {
			ois.defaultReadObject();
			_logger = LogManager.getLogger("logs/Master.log");
		} catch (ClassNotFoundException e) {
			throw new IOException("Class not found.");
		}
	}

	/**
	 * Exports the RegressionModel, the parameters and labels to a TSV-file.
	 * Is an experimental feature.
	 */
	public void exportModelToTSV() {
		StringBuilder sb;

		try {
			// I. Write Design Matrix
			FileWriter tw = new FileWriter(System.getProperty("user.home")
					+ "/exportedRegressionModel_Matrix.txt");

			// Write Design Matrix with column names (=featureNames)
			// First: Column Names
			/*
			 * sb = new StringBuilder(); for (String name :
			 * this.getFeatureNamesOrdered()) { sb.append(name + "\t"); }
			 * sb.append("\n"); tw.write(sb.toString());
			 */

			// Second: Table
			// Tokenize the featureMatrix.toString result with \n, so that we
			// can write each Matrix line in a separate row of the target file.
			StringTokenizer tokenizer = new StringTokenizer(featureMatrix
					.getFeatureMatrixAsSparseMatrix().toString(), "\n");
			String token;
			while (tokenizer.hasMoreTokens()) {
				token = tokenizer.nextToken();
				tw.write(token + "\n");
			}
			// Close File
			tw.close();

			// II. Write labels
			tw = new FileWriter(System.getProperty("user.home")
					+ "/exportedRegressionModel_Labels.txt");

			sb = new StringBuilder();
			for (Float label : this.sourceData.getLabels()) {
				sb.append(label + "\t");
			}
			tw.write(sb.toString() + "\n");
			tw.close();

			// III. Write Parameters Vector
			tw = new FileWriter(System.getProperty("user.home")
					+ "/exportedRegressionModel_EstimatedParameters.txt");

			sb = new StringBuilder();
			for (Float para : this.parameters) {
				sb.append(para + "\t");
			}
			tw.write(sb.toString() + "\n");
			tw.close();

			System.out.println("Length FeatureNames"
					+ featureMatrix.getFeatureNames().size());
			System.out.println("Length RowOfMatrix"
					+ featureMatrix.getFeatureMatrixAsSparseMatrix().ncol());

		} catch (IOException e) {
			_logger.logStackTrace(e);
		}

	}

	/**
	 * To do some fast tests ;)
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			RegressionModel model = new RegressionModel(new Features()
					.useUnigrams(true).useBigrams(true).useTrigrams(true)
					.use4Grams(true).useDictionary(true).useEmoticons(true)
					.usePOSTagger(true).useNegations(true), "en", null,
					(float) 0.66, "Local");

			model.serialize("regression_model.ser");
			// System.out.println(model.addElongatedWordsFeature("good",
			// new SparseMatrix()));
			FileInputStream fis = new FileInputStream("regression_model.ser");
			ObjectInputStream o = new ObjectInputStream(fis);
			RegressionModel model2 = (RegressionModel) o.readObject();
			float sentiment = model2
					.determineSentiment("The NSA spying on my phone is shocking!");
			System.out.println(sentiment);
			o.close();
			fis.close();
		} catch (Exception e) {
			System.out.println("Failed.");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

}
