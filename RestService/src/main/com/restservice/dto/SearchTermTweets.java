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
package com.restservice.dto;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for tweets from a specified search term
 * 
 * @author olaf
 * 
 */
public class SearchTermTweets {

	@JsonProperty("id")
	private Long id = null;
	@JsonProperty("tweets")
	private ArrayList<String> tweetIds = new ArrayList<String>();

	/**
	 * Constructs the DTO
	 * 
	 * @param id
	 *            search term index
	 * @param tweets
	 *            search term tweets
	 */
	public SearchTermTweets(Long id, ArrayList<String> tweetIds) {
		this.id = id;
		this.tweetIds = tweetIds;
	}

	public SearchTermTweets() {
	}

	/**
	 * Returns the search term index
	 * 
	 * @return search term index
	 */
	public Long getId() {
		return this.id;
	}

	/**
	 * Sets the search term index
	 * 
	 * @param id
	 *            search term index
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Returns the search term tweets
	 * 
	 * @return search term tweets ids
	 */
	public ArrayList<String> getTweetIds() {
		return this.tweetIds;
	}

	/**
	 * Sets the search term tweets
	 * 
	 * @param tweets
	 *            search term tweets ids
	 */
	public void setTweetIds(ArrayList<String> tweetIds) {
		this.tweetIds = tweetIds;
	}

}
