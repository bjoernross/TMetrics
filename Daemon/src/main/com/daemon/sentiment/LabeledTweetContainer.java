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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A tweet with an associated (human-provided) label
 * 
 * Comes with JSON annotations so it can be used as a JSON DTO
 * 
 * @author Björn, Erwin
 */
public class LabeledTweetContainer implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonProperty("text")
	public String tweetText;

	@JsonProperty("label")
	public float label;

	// tweet id
	@JsonIgnore
	private long id = -1;

	/**
	 * Create tweet container with tweet text, label and id information. Used by
	 * Sentiment Analysis.
	 * 
	 * @param tweetText
	 * @param label
	 */
	public LabeledTweetContainer(String tweetText, float label) {
		this.tweetText = tweetText;
		this.label = label;
	}

	/**
	 * Create tweet container with id, tweet text and label information. Used by
	 * Clustering Analysis.
	 * 
	 * @param id
	 * @param tweetText
	 * @param label
	 */
	public LabeledTweetContainer(long id, String tweetText, float label) {
		this.id = id;
		this.tweetText = tweetText;
		this.label = label;
	}

	/**
	 * Get tweet text
	 * 
	 * @return Tweet text
	 */
	@JsonIgnore
	public String getTweetText() {
		return tweetText;
	}

	/**
	 * Get human-provided label
	 * 
	 * @return label
	 */
	@JsonIgnore
	public float getLabel() {
		return label;
	}

	/**
	 * Get id of tweet
	 * 
	 * @return id. If value is -1, the container has no id information.
	 */
	@JsonIgnore
	public long getid() {
		return id;
	}
}
