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

import java.util.ArrayList;
import java.util.List;
import com.tmetrics.util.ListUtil;

/**
 * Tester for the regression model
 * 
 * Each instance of this class tests one specific regression model with a
 * specific set of test data. Use it to evaluate the performance of a regression
 * model that was previously trained on different data. If performance drops
 * significantly, this is a sign of overfitting.
 * 
 * The class features a main method to print performance measures, a comparison
 * function to compare regression performance to that of the
 * DictionarySentimentClassifier, and an export-to-csv function.
 * 
 * With multiple instances of this class, you can, for example, compare
 * different models, e.g. models that use different feature matrices, with the
 * same set of test data, or compare the same model using different test data,
 * e.g. evaluate a model trained on Obama tweets by comparing performance on
 * Obama test tweets with performance on Berlusconi test tweets and much more!
 * 
 * @author Björn, Erwin
 * 
 */
public class RegressionTester {

	// model to test
	private RegressionModel model;

	// test data - to use in regression model
	private SentimentSourceData sourceData;

	// sentiment estimates and errors for test tweets
	private List<Float> estimates;
	private List<Float> errors;

	// statistics to evaluate the regression error
	private float mse;
	private float meanError;
	private float[] meanErrorBySentiment;
	/**
	 * store the number of positive labeled tweets in test data, negative
	 * tweets, and neutral tweets. [0]: positive [1]: negative [2]: neutral
	 */
	private int[] positiveNegativeNeutralCount = new int[3];
	private int[] positiveNegativeNeutralTrainingCount = new int[3];

	/**
	 * This constructor instantiates the class using a model
	 * 
	 * @param model
	 *            Regression Model which we want to test
	 * @param percentageTraining
	 *            how many tweets should be used to test. Maybe should be the
	 *            same value which was used to train the model. We calculate in
	 *            this method from this value to 100% when we determine the
	 *            number of test tweets.
	 */
	public RegressionTester(RegressionModel model, float percentageTraining)
			throws Exception {
		this.model = model;
		this.sourceData = new SentimentSourceData(model.getSearchTermId(),
				model.getModelOfLanguage(), model.getUsedDatabase());

		this.estimates = new ArrayList<Float>();

		// read test data from the database, excluding those which were used for
		// training
		this.sourceData.readDataFromDB(percentageTraining, true);

		// estimate sentiment for test data using the model, and compare
		// estimates with actual human labels
		estimateSentimentForTestData();
	}

	/**
	 * Calculates the Regression Estimations of the Test Data with the current
	 * test model. Furthermore, it stores the error statistics.
	 */
	private void estimateSentimentForTestData() {
		// walk through the test tweets and estimate the sentiment for each of
		// them
		for (LabeledTweetContainer testTweet : this.sourceData.getTweets()) {
			this.estimates.add(model.determineSentiment(testTweet
					.getTweetText()));
		}

		// compare estimates with actual human labels
		errors = ListUtil.subtract(this.estimates, this.sourceData.getLabels());
		meanErrorBySentiment = new float[3];

		countPositiveNegativeNeutralGroupsAndErrors();

		meanErrorBySentiment[0] = meanErrorBySentiment[0]
				/ positiveNegativeNeutralCount[0];
		meanErrorBySentiment[1] = meanErrorBySentiment[1]
				/ positiveNegativeNeutralCount[1];
		meanErrorBySentiment[2] = meanErrorBySentiment[2]
				/ positiveNegativeNeutralCount[2];

		// save some useful statistics
		mse = ListUtil.meanSquared(errors);
		meanError = ListUtil.meanAbsolute(errors);

	}

	/**
	 * Compare estimates with those of the DictionarySentimentClassifier.
	 * 
	 * To be used only after estimateSentimentForTestData().
	 * 
	 * Be careful, method assumes data is in English, since the dict. is in
	 * english.
	 * 
	 * @return mean absolute error
	 */
	private float compareWithDictionaryEstimates() {
		DictionarySentimentClassifier dict = new DictionarySentimentClassifier();
		List<Float> dictionaryEstimates = new ArrayList<Float>();
		float estimate;
		for (String tweetText : this.sourceData.getTweetTexts()) {
			estimate = dict.determineSentiment(tweetText, "en");
			dictionaryEstimates.add(estimate);
		}
		return ListUtil.meanAbsolute(ListUtil.subtract(dictionaryEstimates,
				this.sourceData.getLabels()));
	}

	private void countPositiveNegativeNeutralGroupsAndErrors() {
		for (int i = 0; i < this.sourceData.getLabels().size(); i++) {
			float label = this.sourceData.getLabels().get(i);
			if (label > 0.33) {
				positiveNegativeNeutralCount[0]++;
				meanErrorBySentiment[0] += Math.abs(this.estimates.get(i)
						- this.sourceData.getLabels().get(i));
			} else if (label < (-0.33)) {
				positiveNegativeNeutralCount[1]++;
				meanErrorBySentiment[1] += Math.abs(this.estimates.get(i)
						- this.sourceData.getLabels().get(i));
			} else {
				positiveNegativeNeutralCount[2]++;
				meanErrorBySentiment[2] += Math.abs(this.estimates.get(i)
						- this.sourceData.getLabels().get(i));
			}
		}

		for (int i = 0; i < this.model.getSourceData().getLabels().size(); i++) {
			float label = this.model.getSourceData().getLabels().get(i);
			if (label > 0.33) {
				positiveNegativeNeutralTrainingCount[0]++;
			} else if (label < (-0.33)) {
				positiveNegativeNeutralTrainingCount[1]++;
			} else {
				positiveNegativeNeutralTrainingCount[2]++;
			}
		}
	}

	/**
	 * Show test statistics, i.e. the MAE or MSE of training data with the given
	 * regression model
	 * 
	 * @param verbose
	 *            true, if more details should be shown, e.g. the labels,
	 *            errors, estimates, feature matrix. Should not be used if
	 *            number of tweets is high.
	 */
	public void showTestStatistics(boolean verbose) {

		// TODO
		// PRINT DETAILS ABOUT MODEL, USED FEATURES AND ...

		// print result, i. e. error
		System.out.println("Number of test tweets: "
				+ this.sourceData.getTweets().size());
		System.out.println("Mean squared error for test data: " + this.mse);
		System.out.println("Mean absolute error for test data: "
				+ this.meanError);

		if (this.model.getModelOfLanguage().equals("en")) {
			System.out.println("Dictionary MAE: "
					+ this.compareWithDictionaryEstimates());
		}

		System.out.println("Number of positive labeled training tweets: "
				+ positiveNegativeNeutralTrainingCount[0]);
		System.out.println("Number of negative labeled training tweets: "
				+ positiveNegativeNeutralTrainingCount[1]);
		System.out.println("Number of neutral labeled training tweets: "
				+ positiveNegativeNeutralTrainingCount[2]);

		System.out.println("Number of positive labeled test tweets: "
				+ positiveNegativeNeutralCount[0]);
		System.out.println("Number of negative labeled test tweets: "
				+ positiveNegativeNeutralCount[1]);
		System.out.println("Number of neutral labeled test tweets: "
				+ positiveNegativeNeutralCount[2]);

		System.out.println("Error for positive labeled test tweets: "
				+ meanErrorBySentiment[0]);
		System.out.println("Error for negative labeled test tweets: "
				+ meanErrorBySentiment[1]);
		System.out.println("Error of neutral labeled test tweets: "
				+ meanErrorBySentiment[2]);

		// show more details:
		if (verbose) {

			System.out.println("Estimates for test data: " + this.estimates);
			System.out.println("Labels for test data: "
					+ this.sourceData.getLabels());
			System.out.println("Errors for test data: " + this.errors);

			// System.out.println("Training tweets "
			// + this.model.getSourceData().getTweetTexts());
			// System.out
			// .println("Test tweets " + this.sourceData.getTweetTexts());
			// System.out.println("Feature names:"
			// + this.model.getFeatureMatrix().getFeatureNamesOrdered());
			System.out.println("Parameters:" + this.model.getParameters());
			// System.out.println("Matrix\n"
			// + this.model.getFeatureMatrix()
			// .getFeatureMatrixAsSparseMatrix());
		}
		// this.model.exportModelToFile();
	}

	/**
	 * The main method will create a regressio model and tester, as well as
	 * print some useful indicators of performance
	 * 
	 * @param args
	 *            are ignored
	 */
	public static void main(String[] args) {
		try {
			// initiate a regression model and tester
			RegressionModel model = new RegressionModel(new Features()
					.useUnigrams(false).useBigrams(false).useTrigrams(false)
					.use4Grams(false).useDictionary(true).useEmoticons(false)
					.usePOSTagger(false).useNegations(false), "de", null,
					(float) 0.66, "Remote");

			RegressionTester tester = new RegressionTester(model, (float) 0.66);
			tester.showTestStatistics(true);

		} catch (Exception e) {
			System.out
					.println("Failed building the regression model or tester.");
			System.out.println(e.getMessage());
			e.printStackTrace();

		}

	}

}
