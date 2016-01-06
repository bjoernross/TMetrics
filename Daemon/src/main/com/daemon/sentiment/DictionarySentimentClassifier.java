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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.ibatis.io.Resources;

import com.tmetrics.dto.DictionarySentimentDetails;
import com.tmetrics.logging.LogManager;
import com.tmetrics.logging.Logger;

/**
 * Sentiment classifier that uses a dictionary to evaluate a text
 * 
 * @author Björn
 */
public class DictionarySentimentClassifier implements SentimentClassifier,
		Serializable {

	// necessary to serialize the classifier
	private static final long serialVersionUID = 1L;

	// positive and negative words in the dictionary, loaded from a file
	private Set<String> positiveWords;
	private Set<String> negativeWords;

	// when scanning a text for words, these characters are used as delimiters
	private static final String TOKENIZER_DELIM = " \t\n\r\f,.:;?![]()*\"";

	// the number of positive/negative words in the last text that was analysed
	// by determineSentiment
	private int lastTextPosCount = 0;
	private int lastTextNegCount = 0;

	// logger to log error messages
	private transient Logger _logger = LogManager.getLogger("logs/Master.log");

	/**
	 * Set up the classifier by getting positive and negative words from a
	 * dictionary. May take some time so call only once!
	 * 
	 * The dictionary used is LiuDictionary, its two files positive-words.txt
	 * and negative-words.txt can be found in the Resources folder.
	 */
	public DictionarySentimentClassifier() {
		// set up the dictionary
		new LiuDictionary();
	}

	/**
	 * Get the number of positive words in the last text that
	 * determineSentiment() was used on
	 * 
	 * @return
	 */
	int getLastTextPosCount() {
		return lastTextPosCount;
	}

	/**
	 * Get the number of positive words in the last text that
	 * determineSentiment() was used on
	 * 
	 * @return
	 */
	int getLastTextNegCount() {
		return lastTextNegCount;
	}

	/**
	 * Determines the sentiment of the given text using the opinion lexicon
	 * loaded when constructing the classifier
	 * 
	 * @param tweetText
	 *            Text to be analysed
	 * @param language
	 *            Language the text is in
	 * @return Returns a number between -1 and 1 where 1 means maximum positive,
	 *         -1 means maximum negative and 0 means neutral sentiment; returns
	 *         null if sentiment could not be determined (e.g. because there is
	 *         no classifier for the given language)
	 */
	@Override
	public Float determineSentiment(String tweetText, String language) {

		// only determine sentiment for English-language tweets
		if (!language.equals("en")) {
			return null;
		}

		// convert the string to lower case
		tweetText = tweetText.toLowerCase();

		// tokenize it
		StringTokenizer tokenizer = new StringTokenizer(tweetText,
				TOKENIZER_DELIM);

		// initialize a few counters to keep track of the number of positive and
		// negative words in the tweetText
		int posCount = 0;
		int negCount = 0;

		// current token
		String token;

		// walk through the tokens
		while (tokenizer.hasMoreTokens()) {
			token = tokenizer.nextToken();

			// for each token, determine if it is in the list of
			// positive/negative words and if so, increment the appropriate
			// counter
			if (positiveWords.contains(token)) {
				posCount++;
			}

			if (negativeWords.contains(token)) {
				negCount++;
			}
		}

		// save posCount and negCount to member variables so they can be
		// accessed until the next text is classified
		this.lastTextPosCount = posCount;
		this.lastTextNegCount = negCount;

		// if there are no sentiment words, sentiment is defined as 0
		if (posCount == 0 && negCount == 0) {
			return (float) 0;
		}

		// else, sentiment is (p - n) / (p + n)
		float sentiment = ((float) posCount - (float) negCount)
				/ ((float) posCount + (float) negCount);
		return sentiment;
	}

	/**
	 * Determines the sentiment of a given text using the opinion lexicon loaded
	 * when constructing the classifier, and returns information on which words
	 * were found in the dictionary
	 * 
	 * The sentiment and method of determining it are exactly the same as for
	 * determineSentiment, but more information is returned, enclosed in the
	 * return type DictionarySentimentDetails.
	 * 
	 * @param tweetText
	 *            Text to be analysed
	 * @return A DTO containing the dictionary words present in the text, and
	 *         overall sentiment
	 */
	public DictionarySentimentDetails determineSentimentDetails(String tweetText) {

		// convert the string to lower case
		tweetText = tweetText.toLowerCase();

		// tokenize it
		StringTokenizer tokenizer = new StringTokenizer(tweetText,
				TOKENIZER_DELIM);

		// initialize a few counters to keep track of the number of positive and
		// negative words in the tweetText
		int posCount = 0;
		int negCount = 0;

		// current token
		String token;

		// create a DTO that will hold the results
		DictionarySentimentDetails dto = new DictionarySentimentDetails();

		// walk through the tokens
		while (tokenizer.hasMoreTokens()) {
			token = tokenizer.nextToken();

			// for each token, determine if it is in the list of
			// positive/negative words and if so, increment the appropriate
			// counter
			if (positiveWords.contains(token)) {
				dto.addPositiveWord(token);
				posCount++;
			}

			if (negativeWords.contains(token)) {
				dto.addNegativeWord(token);
				negCount++;
			}
		}

		// resulting sentiment
		float sentiment;
		
		if (posCount == 0 && negCount == 0) {
			// if there are no sentiment words, sentiment is defined as 0
			sentiment = (float) 0;
		} else {
			// else, sentiment is (p - n) / (p + n)
			sentiment = ((float) posCount - (float) negCount)
					/ ((float) posCount + (float) negCount);

		}

		dto.setSentiment(sentiment);
		return dto;
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
	 * Liu English-language sentiment dictionary for use with
	 * DictionarySentimentClassifier
	 * 
	 * @author Björn
	 */
	private class LiuDictionary {

		/**
		 * Set up the dictionary by reading it from files into the member
		 * variables. Should be called only once to minimize File I/O
		 * 
		 */
		LiuDictionary() {
			try {
				positiveWords = readDictionaryFromFile(Resources
						.getResourceAsStream("positive-words.txt"));
				negativeWords = readDictionaryFromFile(Resources
						.getResourceAsStream("negative-words.txt"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Reads a dictionary from a file. Specific to the dictionary format.
		 * 
		 * @param File
		 *            to be read. Must be in the correct format
		 * @return Set of dictionary words
		 */
		private Set<String> readDictionaryFromFile(InputStream is) {
			Set<String> words = new HashSet<String>(10000);
			try {
				// open the file
				BufferedReader br = new BufferedReader(
						new InputStreamReader(is));
				// Scanner sc = new Scanner(file);
				String currentLine;

				while ((currentLine = br.readLine()) != null) {

					// skip comments
					if (currentLine.startsWith(";"))
						continue;

					// skip empty lines
					if (currentLine.equals(""))
						continue;

					// skip lines that begin with a space
					if (currentLine.startsWith(";"))
						continue;

					// add to word list
					words.add(currentLine);

				}

				br.close();
			} catch (IOException e) {
				_logger.log("Cannot find sentiment dictionary");
				_logger.log(e.getMessage());
			}
			return words;
		}

	}

}
