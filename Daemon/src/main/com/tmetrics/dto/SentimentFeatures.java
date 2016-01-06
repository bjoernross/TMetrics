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
 * A JSON-annotated DTO that holds detailed information on how the sentiment of
 * a tweet was estimated, i.e. features and their parameters.
 * 
 * Words and emoticons found in dictionaries are represented by an object of the
 * type DictionarySentimentDetails.
 * 
 * N-Grams are represented by a list of Ngram objects (one list for unigrams,
 * one for bigrams, one for trigrams, one for fourgrams), which are essentially
 * string-integer pairs.
 * 
 * There is also a list of Ngram objects where other string-integer pairs can be
 * stored, e.g. the regression constant.
 * 
 * @author Björn, Daniel
 * 
 */
public class SentimentFeatures {

	// a word dictionary: positive and negative words found in the text, etc.
	@JsonProperty("words")
	private DictionarySentimentDetails words = null;

	// an emoticon dictionary: positive and negative emoticons found in the
	// text, etc.
	@JsonProperty("emoticons")
	private DictionarySentimentDetails emoticons = null;

	// unigrams, e.g. "I", "love", "my", and "dog"
	@JsonProperty("unigrams")
	private List<Ngram> unigrams = new ArrayList<Ngram>();

	// bigrams, e.g. "I love", "love my" and "my dog"
	@JsonProperty("bigrams")
	private List<Ngram> bigrams = new ArrayList<Ngram>();

	// trigrams, e.g. "I love my" and "love my dog"
	@JsonProperty("trigrams")
	private List<Ngram> trigrams = new ArrayList<Ngram>();

	// fourgrams, e.g. "I love my dog"
	@JsonProperty("fourgrams")
	private List<Ngram> fourgrams = new ArrayList<Ngram>();

	// other features, e.g. the regression constant
	@JsonProperty("others")
	private List<Ngram> others = new ArrayList<Ngram>();

	public void setWordDictionaryDetails(
			DictionarySentimentDetails wordDictionaryDetails) {
		words = wordDictionaryDetails;
	}

	public DictionarySentimentDetails getWordDictionaryDetails() {
		return words;
	}

	public void setEmoticonDictionaryDetails(
			DictionarySentimentDetails emoticonDictionaryDetails) {
		emoticons = emoticonDictionaryDetails;
	}

	public DictionarySentimentDetails getEmoticonDictionaryDetails() {
		return emoticons;
	}

	public void addUnigram(String key, Float value) {
		unigrams.add(new Ngram(key, value));
	}

	public void addBigram(String key, Float value) {
		bigrams.add(new Ngram(key, value));
	}

	public void addTrigram(String key, Float value) {
		trigrams.add(new Ngram(key, value));
	}

	public void addFourgram(String key, Float value) {
		fourgrams.add(new Ngram(key, value));
	}

	public DictionarySentimentDetails getWords() {
		return words;
	}

	public DictionarySentimentDetails getEmoticons() {
		return emoticons;
	}

	public List<Ngram> getUnigrams() {
		return unigrams;
	}

	public List<Ngram> getBigrams() {
		return bigrams;
	}

	public List<Ngram> getTrigrams() {
		return trigrams;
	}

	public List<Ngram> getFourgrams() {
		return fourgrams;
	}

	public void addOther(String key, Float value) {
		others.add(new Ngram(key, value));
	}

	public List<Ngram> getOthers() {
		return others;
	}

}
