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
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.daemon.sentiment.LabeledTweetContainer;
import com.daemon.sentiment.SentimentSourceData;
import com.dataGrouping.clustering.HierarchicalClusteringAlgorithm;
import com.dataGrouping.clustering.WladimirotivesClustering;
import com.dataGrouping.multiDimensionalScaling.ModifiedSmacofScaling;
import com.dataGrouping.similarityMeasure.QuantitativeDissimilarityMeasure;
import com.restservice.dto.DataGroupingResult;
import com.tmetrics.exceptions.NotDataFoundException;

/**
 * Responsible for managing the clustering of tweets based on their hashtags. As
 * a matter of fact, we group hashtags, classifiy each tweet to such a hashtag
 * cluster and return the hashtag clusters with its tweets.
 * 
 * @author eq
 * 
 */
public class TweetClusterMaster {

	// the cluster feature matrix: the attributes of hashtags
	private ClusterFeatureMatrix clusterFeatureMatrix;

	// the distance matrix of hashtags, based on feature matrix
	private double[][] distanceMatrix;

	// the cluster memberships of hashtags
	private int[] clusterResult;
	private int numberOfClusters;

	/**
	 * Identifies hashtags in sourceData and clusters the hashtags. For doing
	 * so, it creates for each hashtag a virtual document. This is a collection
	 * of all tweets which has the hashtag. However, it removes all hashtags
	 * that have less than "MinTweets" tweets. Then, it extracts the features of
	 * each hashtag based on this virtual document.
	 * 
	 * @param sourceData
	 *            the tweets which will be used to find the hashtag groups.
	 * @param numberOfClusters
	 *            determines the maximum number of clusters that will be
	 *            created.
	 * @param MinTweets
	 *            the number of tweets that each virtual document should have.
	 *            Hashtags that have less than this number will be removed.
	 * @throws NotDataFoundException 
	 */
	public void createModel(SentimentSourceData sourceData,
			int numberOfClusters, int MinTweets) throws NotDataFoundException {

		// Stage I: Prepare Hashtag-Clustering
		this.clusterFeatureMatrix = new ClusterFeatureMatrix(sourceData);
		this.clusterFeatureMatrix.createVirtualDocuments();

		// clean the result from hashtags that has only a few tweets.
		this.clusterFeatureMatrix.considerOnlyHashtagsWith(MinTweets);

		// calculate attributes of hashtags = their word count.
		this.clusterFeatureMatrix.createClusterFeatureMatrix();
		
		// normalize data / rows
		this.clusterFeatureMatrix.normalizeRows();

		// calculate distance matrix
		this.distanceMatrix = QuantitativeDissimilarityMeasure
				.getDissimilaritySparseMatrix(this.clusterFeatureMatrix
						.getFeatureMatrix());
		// SimilarityMeasureTest.printDoubleMatrix(this.distanceMatrix);

		// if no data were found during analysis, throw exception
		if (this.distanceMatrix.length == 0) {
			throw new NotDataFoundException(
					"No hashtags found or hashtags that have less than "
							+ MinTweets + " tweets");
		}

		// Stage II: Clustering Hashtags
		HierarchicalClusteringAlgorithm clusterer = new WladimirotivesClustering(
				this.distanceMatrix, numberOfClusters);
		// Alternatively:
		// HierarchicalClusteringAlgorithm clusterer = new
		// SingleLinkageClustering(binaryDissimilarityMatrix);

		this.clusterResult = clusterer.getClusterMemberships();
		this.numberOfClusters = clusterer.getCountOfClusters();

	}

	/**
	 * Returns the created virtual documents. In fact, this method returns a map
	 * where the key is the hashtag, and the value a list of all tweets which
	 * forms the virtual document.
	 * 
	 * @return
	 */
	public Map<String, List<String>> getHashtagsAndTheirVirtualDocument() {
		return this.clusterFeatureMatrix.getVirtualDocuments();
	}

	/**
	 * Returns a integer array, representing the cluster number of each hashtag.
	 * The order of hashtags is represented by the array number and is the same
	 * as in the key set of getHashtagsAndTheirVirtualDocument.
	 * 
	 * @return
	 */
	public int[] getHashtagsClusterMemberships() {
		return this.clusterResult;
	}

	/**
	 * Short: Returns the hashtag clusters (based on all tweets that has the
	 * respective hashtags.
	 * 
	 * @return
	 */
	public DataGroupingResult returnHastagClusters() {

		// I. Preprocessing (already done)
		// II.a Clustering (already done)
		// II.b MDS
		ModifiedSmacofScaling sc = new ModifiedSmacofScaling(
				this.distanceMatrix, this.clusterResult, this.numberOfClusters);
		double[][] mds = sc.getMDS();

		int counter = 0;

		// III. Return
		ArrayList<String> hashtags = new ArrayList<String>();
		for (String s : this.clusterFeatureMatrix.getVirtualDocuments()
				.keySet()) {
			hashtags.add(s);
			counter += this.clusterFeatureMatrix.getVirtualDocuments().get(s)
					.size();
		}

		DataGroupingResult dataGroupingResult = new DataGroupingResult(
				this.clusterResult, mds, hashtags);

		// Some test statistics (to debug)

		// System.out.println("Count of tweets:" + counter);
		// int k = 0;
		// for (String s : this.clusterFeatureMatrix.getVirtualDocuments()
		// .keySet()) {
		// System.out.println("Hashtag [Cluster:" + this.clusterResult[k] + "]"
		// + s + ": "
		// + this.clusterFeatureMatrix.getVirtualDocuments().get(s));
		// System.out.println("*****************************");
		// k++;
		// }

		// System.out.println("///////////////////////////");
		// System.out.println(this.clusterFeatureMatrix.getFeatureNamesOrdered());

		// System.out.println("///////////////////////////");
		// System.out.println(this.clusterFeatureMatrix);
		// System.out.println(this.clusterFeatureMatrix.getFeatureMatrix().getRowAsList(4));

		// System.out.println("///////////////////////////");
		// QuantitativeDissimilarityMeasure.showDisMatrix(hashtags,
		// this.distanceMatrix);

		return dataGroupingResult;
	}

	/**
	 * Short: Returns the tweet clusters. In fact: Based on the hashtag
	 * clusters, this method identifies the nearest hashtag cluster of each
	 * tweet. By doing so, it groups the tweets in clusters.
	 * 
	 * @param sourceData
	 *            the tweets which should be grouped.
	 */
	public DataGroupingResult determineClusterMembershipOfTweets(
			SentimentSourceData sourceData) {

		// I. Preprocessing

		// cluster numbers of tweet textes
		int numberOfTweets = sourceData.getTweets().size();
		int[] clusterTweetMembership = new int[numberOfTweets];
		double[][] dissimilarityTweetMatrix = new double[numberOfTweets][numberOfTweets];

		// Get tweet objects from source Data
		List<LabeledTweetContainer> tweets = sourceData.getTweets();

		// variables for loop:
		String tweetText;
		long tweetId;
		int hashtagRowNumber;

		// / II.a "Clustering" of tweets
		// Run through all tweets
		int i = 0;
		for (LabeledTweetContainer tweet : tweets) {
			tweetText = tweet.getTweetText();
			tweetId = tweet.getid();

			// get nearest hashtag and therefore the cluster number
			hashtagRowNumber = this.clusterFeatureMatrix
					.findNearestHashtagOfTweet(tweetText);
			clusterTweetMembership[i] = this.clusterResult[hashtagRowNumber];
			// System.out.println("." + clusterTweetMembership[i]);

			// increment tweet counter
			i++;
		}

		// II.b MultiDimensionalScaling for tweet textes

		// create the dissimilarity matrix
		for (int j = 0; j < numberOfTweets; j++) {
			for (int k = 0; k < numberOfTweets; k++) {
				dissimilarityTweetMatrix[j][k] = this.distanceMatrix[clusterTweetMembership[j]][clusterTweetMembership[k]];
			}
		}
		// SimilarityMeasureTest.printDoubleMatrix(dissimilarityTweetMatrix);
		// MDS:
		ModifiedSmacofScaling sc = new ModifiedSmacofScaling(
				dissimilarityTweetMatrix, clusterTweetMembership,
				this.numberOfClusters);
		double[][] mds = sc.getMDS();

		// III. Return
		DataGroupingResult dataGroupingResult = new DataGroupingResult(
				clusterTweetMembership, mds,
				(ArrayList) sourceData.getTweetIds());

		return dataGroupingResult;
	}

}
