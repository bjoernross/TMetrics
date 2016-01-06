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
package com.dataGrouping.tweetClusterAnalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.daemon.sentiment.SentimentSourceData;
import com.tmetrics.util.ListUtil;
import com.tmetrics.util.SparseMatrix;

/**
 * This feature matrix represents the extracted features to group hashtags. The
 * rows are the hashtags and the columns are the features, here the word
 * occurences in the virtual documents. A virtual document is a collection of
 * all tweets which has a specific hashtag.
 * 
 * @author eq
 * 
 */
public class ClusterFeatureMatrix {

	/**
	 * HashMap, the key is the hashtag-word and the value is a list of all
	 * tweets which has the hashtag.
	 */
	private Map<String, List<String>> hashtagTweets;

	// Source Data, the tweets and their id
	private SentimentSourceData sourceData;

	// (hashtags will only have letters, numbers, underscores in them!)
	private static final String HASHTAG_PATTERN = "#([A-Za-z0-9_]+)";

	// characters to use as delimiters when tokenizing tweets
	private static final String TOKENIZER_DELIM = " \t\n\r\f,.:;?![]()*\"";

	// regular expression to match URLs
	private static final String URL_PATTERN = "(https?:\\/\\/)([\\da-z.-]+).([a-z.]{2,6})([\\/\\w.-]*)*\\/?";

	// the feature matrix
	private SparseMatrix featureMatrix;

	// names of the features and their position (column) in the feature matrix
	private Map<String, Integer> featureNames;

	/**
	 * Constructor. Initializes only the variables. Afterwards using the
	 * constructor, call the method "createVirtualDocuments" and then
	 * "createClusterFeatureMatrix".
	 * 
	 * @param sourceData
	 *            the tweets which will be used to create the virtual documents
	 *            and their attributes.
	 */
	public ClusterFeatureMatrix(SentimentSourceData sourceData) {
		// this.hashtagTweets = new HashMap<String, List<String>>();
		this.hashtagTweets = new LinkedHashMap<String, List<String>>();
		this.sourceData = sourceData;

		this.featureMatrix = new SparseMatrix();
		this.featureNames = new HashMap<String, Integer>();
	}

	/**
	 * Returns the feature matrix.
	 * 
	 * @return
	 */
	public SparseMatrix getFeatureMatrix() {
		return this.featureMatrix;
	}

	/**
	 * Get feature names as a list, in the correct order.
	 * 
	 * @return a list of feature names in the right column order of the design
	 *         (feature) matrix
	 */
	public List<String> getFeatureNamesOrdered() {
		List<String> featureNamesOrdered = ListUtil.createNullList(featureNames
				.size());

		// Sort in O(n) ;)
		for (String column : featureNames.keySet()) {
			int number = featureNames.get(column);
			featureNamesOrdered.set(number, column);
		}
		return featureNamesOrdered;
	}

	/**
	 * Returns the created virtual documents. In fact, this method returns a map
	 * where the key is the hashtag, and the value a list of all tweets which
	 * forms the virtual document.
	 * 
	 * @return
	 */
	public Map<String, List<String>> getVirtualDocuments() {
		return this.hashtagTweets;
	}

	/**
	 * Creates the virtual documents. A virtual document is a concatenation of
	 * all tweet textes with the same hashtag. In this step, only tweets are
	 * considered that has at least one hashtag.
	 */
	public void createVirtualDocuments() {

		Pattern pattern = Pattern.compile(HASHTAG_PATTERN);
		Matcher matcher;
		String hashtag;

		// Run through all tweets
		for (String tweetText : this.sourceData.getTweetTexts()) {

			// find all hashtags
			matcher = pattern.matcher(tweetText);
			// .find() returns true if a new matching subsequence was found.
			while (matcher.find()) {
				// get the hashtag String
				hashtag = matcher.group();
				hashtag = hashtag.toLowerCase();

				// and put the tweet-text into each hashtag-category:

				if (this.hashtagTweets.containsKey(hashtag)) {
					// if hashmap already contains hashtag, add tweet-text to
					// list.
					this.hashtagTweets.get(hashtag).add(tweetText);

				} else {
					// if hashtag is new, create a new key,value.
					List<String> list = new ArrayList<String>();
					list.add(tweetText);
					this.hashtagTweets.put(hashtag, list);
				}

			}

		}
	}

	/**
	 * This method runs through the hashtag map and removes each hashtag which
	 * has less than x tweets.
	 * 
	 * @param tweetNumber
	 *            the number of tweets that each hashtag should has to survive.
	 */
	public void considerOnlyHashtagsWith(int tweetNumber) {

		// a) collect all hashtags that will be removed
		List<String> removeHashtags = new ArrayList<String>();
		for (String keyHashtag : this.hashtagTweets.keySet()) {
			if (this.hashtagTweets.get(keyHashtag).size() < tweetNumber) {
				removeHashtags.add(keyHashtag);
			}
		}

		// b) remove them
		for (String keyHashtag : removeHashtags) {
			this.hashtagTweets.remove(keyHashtag);
		}

	}

	/**
	 * Creates the feature matrix, based on the created virtual documents.
	 */
	public void createClusterFeatureMatrix() {

		// row number in featureMatrix
		for (String keyHashtag : this.hashtagTweets.keySet()) {

			List<String> virtualDocument = this.hashtagTweets.get(keyHashtag);
			this.featureMatrix
					.concatenateVertically(createClusterFeatureVector(
							virtualDocument, true));

		}

	}

	/**
	 * Normalizes the word count to prevent that hashtags that have many tweets
	 * are more different to other hashtags only because they have more tweets
	 * and therefore more words. For example, the word 'Kerry' is in 20 of 40 of
	 * #merkel, 'kerry' is in 10 of 20 of #putin; then both hashtags should be
	 * more similiar to each other.
	 */
	public void normalizeRows() {

		float rowSum;

		// run through the rows and get the sum, then calculate wordCount / Sum
		for (int i = 0; i < this.featureMatrix.nrow(); i++) {
			rowSum = this.featureMatrix.rowSum(i);
			this.featureMatrix.divideEachRowValueBy(rowSum, i);
		}

	}

	/**
	 * Creates one row of the feature matrix when one virtual document is given.
	 * 
	 * @param virtualDocument
	 *            a list of tweets that represents the virtual document.
	 * @param addNewFeatures
	 *            Whether new features (e.g. previously unknown words) should be
	 *            added to the model (as during hashtag clustering (first
	 *            stage)) or not (as during tweet clustering (second stage)).
	 */
	private SparseMatrix createClusterFeatureVector(
			List<String> virtualDocument, boolean addNewFeatures) {

		SparseMatrix featureVector;
		if (addNewFeatures) {
			featureVector = new SparseMatrix();
		} else {
			// if no new features should be added, the length of featureVector
			// will be ncol of feature matrix.
			featureVector = new SparseMatrix(this.featureMatrix.ncol());
		}

		for (String tweetText : virtualDocument) {

			// replace URLs
			tweetText = tweetText.replaceAll(URL_PATTERN, " ");

			// convert the string to lower case
			tweetText = tweetText.toLowerCase();

			// tokenize text
			StringTokenizer tokenizer;
			tokenizer = new StringTokenizer(tweetText, TOKENIZER_DELIM);

			String curToken;
			while (tokenizer.hasMoreTokens()) {
				curToken = tokenizer.nextToken();

				featureVector = add_TfIdf_Feature(curToken, featureVector,
						addNewFeatures);

			}

		}

		return featureVector;
	}

	/**
	 * Add the word occurences feature to the feature vector of a specific
	 * token. It ignores whether a word is a hashtag or a normal word, e.g.
	 * "#news and news" should be treated as same words. Furthermore, it removes
	 * apostrophes', e.g. "merkel" and "merkel's" are equally treated.
	 * 
	 * @param token1
	 * @param row
	 * @param addNewFeatures
	 *            Whether new features (e.g. previously unknown words) should be
	 *            added to the model (as during hashtag clustering (first
	 *            stage)) or not (as during tweet clustering (second stage)).
	 */
	private SparseMatrix add_TfIdf_Feature(String token1,
			SparseMatrix featureVector, boolean addNewFeatures) {

		// remove hashtag before word
		if (token1.startsWith("#"))
			token1 = token1.substring(1);
		// remove 's after a word, e.g. merkel's ...
		if (token1.endsWith("'s")) {
			token1 = token1.substring(0, token1.length() - 2);
		}

		int matrix_j;
		float wordCount;
		// check if column exists in featureMatrix
		if (featureNames.containsKey(token1)) {
			// if so, get column number
			matrix_j = featureNames.get(token1);

			// get value from matrix: E.g. we have already merkel in matrix, but
			// we are in a new row, then it is possible that this row does not
			// have the matrix tuple
			// merkel, although this column already exists.
			try {
				wordCount = featureVector.get(0, matrix_j);
			} catch (IndexOutOfBoundsException e) {
				wordCount = 0;
			}
			featureVector.set(0, matrix_j, ++wordCount);

		} else if (addNewFeatures == true) {
			// else, add new token to feature Matrix
			matrix_j = featureNames.size();
			featureNames.put(token1, matrix_j);
			featureVector.set(0, matrix_j, 1);
		}

		return featureVector;
	}

	/**
	 * Find the nearest hashtag in feature matrix when a tweet text is given. Is
	 * needed to group the tweets into the hashtag clusters (after the hashtag
	 * clustering).
	 * 
	 * @param tweetText
	 * @return the row number of the nearest hashtag in feature matrix for given
	 *         tweetText
	 */
	public int findNearestHashtagOfTweet(String tweetText) {

		// cluster number of tweetText
		int rowOfHashtag = -1;
		float shortestDistance = Integer.MAX_VALUE;

		// get feature vector for given tweet text
		List<String> virtualDocument = new ArrayList<String>();
		virtualDocument.add(tweetText);
		SparseMatrix tweetMatrix = this.createClusterFeatureVector(
				virtualDocument, false);

		if (tweetMatrix.nrow() == 0)
			tweetMatrix.set(0, 0, 0);

		List<Float> tweetList = tweetMatrix.getRowAsList(0);

		// find the nearest hashtag for given tweet text
		List<Float> curRowList;
		float curDistance;
		for (int i = 0; i < this.featureMatrix.nrow(); i++) {
			curRowList = this.featureMatrix.getRowAsList(i);
			curDistance = ListUtil.meanSquared(ListUtil.subtract(tweetList,
					curRowList));
			if (curDistance < shortestDistance) {
				rowOfHashtag = i;
				shortestDistance = curDistance;
			}
		}

		return rowOfHashtag;
	}

	@Override
	/**
	 * Creates a String representation of feature matrix (with column and row names!)
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		String row = "\n";
		String col = "\t";
		float[][] array = this.featureMatrix.toArray();

		ArrayList<String> hashtags = new ArrayList<String>();
		for (String s : this.hashtagTweets.keySet()) {
			hashtags.add(s);
		}

		// Create column names
		sb.append("-" + col);
		for (String s : this.getFeatureNamesOrdered()) {
			sb.append(s + col);
		}
		sb.append(row);

		// Create rows
		for (int i = 0; i < this.featureMatrix.nrow(); i++) {
			// one add. column for the row name!
			for (int j = 0; j < this.featureMatrix.ncol() + 1; j++) {
				if (j == 0)
					sb.append(hashtags.get(i) + col);
				else {
					sb.append(array[i][j - 1] + col);
				}
			}
			sb.append(row);
		}

		return sb.toString();
	}

	// public void showFeaturesForFollowingHashtags(List<String>
	// analyzedHashtags) {
	// String row = "\n";
	// String col = "\t \t \t";
	// List<StringBuilder> builders = new ArrayList<StringBuilder>();
	// StringBuilder sb = new StringBuilder();
	// sb.append("---" + col);
	// builders.add(sb);
	//
	// List<Integer> rows = new ArrayList<Integer>();
	//
	// int i = 0;
	// for (String s : this.hashtagTweets.keySet()) {
	// if (analyzedHashtags.contains(s)) {
	// rows.add(i);
	//
	// sb = new StringBuilder();
	// sb.append(s + col);
	// builders.add(sb);
	// }
	// i++;
	// }
	//
	// float[][] array = this.featureMatrix.toArray();
	//
	// sb = builders.get(0);
	// for (String columnName : this.getFeatureNamesOrdered()) {
	// sb.append(columnName + col);
	// }
	// sb.append(row);
	//
	// int l = 1;
	// for (Integer r : rows) {
	// sb = builders.get(l);
	// for (int j = 0; j < this.featureMatrix.ncol(); j++) {
	// sb.append(array[r][j] + col);
	//
	// }
	// sb.append(row);
	// l++;
	// }
	//
	// String result = new String();
	// for (StringBuilder sbs : builders) {
	// result += sbs.toString();
	// }
	//
	// System.out.println(result);
	//
	// }
}
