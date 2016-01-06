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
package com.tmetrics.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This DTO holds detailed information on how the sentiment of a text was
 * determined, using a dictionary.
 * 
 * @author Daniel, Björn
 * 
 */
public class DictionarySentimentDetails {

	// words from the text found in the dictionary's positive word list; the
	// number of words is a feature in regression
	@JsonProperty("positive_words")
	private List<String> positiveWords = new ArrayList<String>();

	// words from the text found in the dictionary's negative word list; the
	// number of words is a feature in regression
	@JsonProperty("negative_words")
	private List<String> negativeWords = new ArrayList<String>();

	// overall text sentiment as determined by the dictionary; is a feature in
	// regression
	@JsonProperty("sentiment")
	private float sentiment = 0.0f;

	// regression parameter for the feature "number of positive words"
	@JsonProperty("pos_count_param")
	private float posCountParam = 0.0f;

	// regression parameter for the feature "number of negative words"
	@JsonProperty("neg_count_param")
	private float negCountParam = 0.0f;

	// regression parameter for the feature "overall text sentiment"
	@JsonProperty("sentiment_param")
	private float sentimentParam = 0.0f;

	// getters and setters for DTO members
	public void addPositiveWord(String word) {
		positiveWords.add(word);
	}

	public void addNegativeWord(String word) {
		negativeWords.add(word);
	}

	public void setSentiment(float fNewValue) {
		sentiment = fNewValue;
	}

	public List<String> getPositiveWords() {
		return positiveWords;
	}

	public List<String> getNegativeWords() {
		return negativeWords;
	}

	public float getSentiment() {
		return sentiment;
	}

	public float getPosCountParam() {
		return posCountParam;
	}

	public void setPosCountParam(float posCountParam) {
		this.posCountParam = posCountParam;
	}

	public float getNegCountParam() {
		return negCountParam;
	}

	public void setNegCountParam(float negCountParam) {
		this.negCountParam = negCountParam;
	}

	public float getSentimentParam() {
		return sentimentParam;
	}

	public void setSentimentParam(float sentimentParam) {
		this.sentimentParam = sentimentParam;
	}

}
