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

import java.io.Serializable;

import com.tmetrics.dto.DictionarySentimentDetails;

/**
 * Sentiment classifier that uses emoticons to evaluate texts
 * 
 * @authors Erwin, Björn
 */
public class EmoticonSentimentClassifier implements SentimentClassifier,
		Serializable {

	// necessary to serialize the classifier
	private static final long serialVersionUID = 1L;

	/**
	 * List of positive emoticons
	 */
	private final String[] positiveEmoticons = { ":)", ":-)", ":d", ":-d",
			":-p", ":p" };

	/**
	 * List of negative emoticons
	 */
	private final String[] negativeEmoticons = { ":(", ":-(" };

	// the number of positive/negative emoticons in the last text that was
	// analysed by determineSentiment
	private int lastTextPosCount = 0;
	private int lastTextNegCount = 0;

	/**
	 * Get the number of positive emoticons in the last text that
	 * determineSentiment() was used on
	 * 
	 * @return
	 */
	int getLastTextPosCount() {
		return lastTextPosCount;
	}

	/**
	 * Get the number of positive emoticons in the last text that
	 * determineSentiment() was used on
	 * 
	 * @return
	 */
	int getLastTextNegCount() {
		return lastTextNegCount;
	}

	/**
	 * Determines the sentiment of the given text using an emoticon dictionary.
	 * 
	 * @param tweetText
	 *            Text to be analysed
	 * @param language
	 *            Language the text is in. Ignored. Emoticons are assumed to be
	 *            language-independent.
	 * @return Returns a number betweet -1 and 1 where 1 means maximum positive,
	 *         -1 means maximum negative and 0 means neutral sentiment
	 */
	@Override
	public Float determineSentiment(String tweetText, String language) {

		// initialize a few counters to keep track of the number of positive and
		// negative emoticons in the tweetText
		int posCount = 0;
		int negCount = 0;

		// convert the string to lower case
		tweetText = tweetText.toLowerCase();

		// for each positive emoticon, check if it is in the text
		for (String posEmoticon : positiveEmoticons) {
			if (tweetText.contains(posEmoticon)) {
				posCount++;
			}
		}

		// for each negative emoticon, check if it is in the text
		for (String negEmoticon : negativeEmoticons) {
			if (tweetText.contains(negEmoticon)) {
				negCount++;
			}
		}

		// save posCount and negCount to member variables so they can be
		// accessed until the next text is classified
		this.lastTextPosCount = posCount;
		this.lastTextNegCount = negCount;

		// if there are no emoticons, sentiment is defined as 0
		if (posCount == 0 && negCount == 0) {
			return (float) 0;
		}

		// else, sentiment is (p - n) / (p + n)
		float sentiment = ((float) posCount - (float) negCount)
				/ ((float) posCount + (float) negCount);
		return sentiment;
	}

	/**
	 * Determines the sentiment of a given text using the hard-coded emoticon
	 * dictionary, and returns information on which emoticons were found.
	 * 
	 * The sentiment and method of determining it are exactly the same as for
	 * determineSentiment, but more information is returned, enclosed in the
	 * return type DictionarySentimentDetails.
	 * 
	 * @param tweetText
	 *            Text to be analysed
	 * @return A DTO containing the emoticons present in the text, and overall
	 *         sentiment
	 */
	public DictionarySentimentDetails determineSentimentDetails(String tweetText) {

		// initialize a few counters to keep track of the number of positive and
		// negative emoticons in the tweetText
		int posCount = 0;
		int negCount = 0;

		// create a DTO that will hold the results
		DictionarySentimentDetails dto = new DictionarySentimentDetails();

		// convert the string to lower case
		tweetText = tweetText.toLowerCase();

		// for each positive emoticon, check if it is in the text
		for (String posEmoticon : positiveEmoticons) {
			if (tweetText.contains(posEmoticon)) {
				posCount++;
				dto.addPositiveWord(posEmoticon);
			}
		}

		// for each negative emoticon, check if it is in the text
		for (String negEmoticon : negativeEmoticons) {
			if (tweetText.contains(negEmoticon)) {
				negCount++;
				dto.addNegativeWord(negEmoticon);
			}
		}

		// save posCount and negCount to member variables so they can be
		// accessed until the next text is classified
		this.lastTextPosCount = posCount;
		this.lastTextNegCount = negCount;

		float sentiment;

		// if there are no emoticons, sentiment is defined as 0
		if (posCount == 0 && negCount == 0) {
			sentiment = (float) 0;
		}

		// else, sentiment is (p - n) / (p + n)
		sentiment = ((float) posCount - (float) negCount)
				/ ((float) posCount + (float) negCount);
		dto.setSentiment(sentiment);

		return dto;
	}

}
