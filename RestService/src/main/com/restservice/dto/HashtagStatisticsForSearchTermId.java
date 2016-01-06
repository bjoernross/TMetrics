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
/**
 * 
 */
package com.restservice.dto;

import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Wladimir Haffner
 * 
 * Class to storing information for
 * hashtags were found in tweets 
 * for a given search term. 
 *
 */
public class HashtagStatisticsForSearchTermId {
	@JsonProperty("search_term_id")
	private Long               searchTermId;
	@JsonProperty("hashtag_ids")
	private ArrayList<String>  hashtagIds;
	@JsonProperty("hashtag_texts")
	private ArrayList<String>  hashtagTexts;
	@JsonProperty("counts")
	private ArrayList<Integer> counts;
	
	/**
	 * Constructs the DTO
	 * 
	 * @param searchTerm Search term as text
	 * @param searchTermId Search term id
	 * @param tweetIds Tweet ids
	 * @param hashtagIds Hashtag ids
	 * @param hashtagTexts Text of the hashtags
	 * @param counts Amount of the occurrence of the hashtag
	 */
	public HashtagStatisticsForSearchTermId(
			String            searchTerm,
			Long              searchTermId,
			ArrayList<String> hashtagIds, 
			ArrayList<String> hashtagTexts, 
			ArrayList<Integer> counts) {
		this.searchTermId = searchTermId;
		this.hashtagIds    = hashtagIds;
		this.hashtagTexts  = hashtagTexts;
		this.counts        = counts;
	}
	
	/**
	 * Empty constructor for this class
	 */
	public HashtagStatisticsForSearchTermId() {
		
	}

	/**
	 * @return the search term id
	 */
	public Long getSearchTermId() {
		return searchTermId;
	}

	/**
	 * @param searchTermId the search term id for search item
	 */
	public void setSearchTermId(Long searchTermId) {
		this.searchTermId = searchTermId;
	}

	/**
	 * @return the hashtag ids
	 */
	public ArrayList<String> getHashtagIds() {
		return hashtagIds;
	}

	/**
	 * @param hashtagIds the hashtag ids
	 */
	public void setHashtagIds(ArrayList<String> hashtagIds) {
		this.hashtagIds = hashtagIds;
	}

	/**
	 * @return the hashtag text
	 */
	public ArrayList<String> getHashtagTexts() {
		return hashtagTexts;
	}

	/**
	 * @param hashtagTexts the hashtag texts to set
	 */
	public void setHashtagTexts(ArrayList<String> hashtagTexts) {
		this.hashtagTexts = hashtagTexts;
	}

	/**
	 * @return the counts
	 */
	public ArrayList<Integer> getCounts() {
		return counts;
	}

	/**
	 * @param counts the counts to set
	 */
	public void setCounts(ArrayList<Integer> counts) {
		this.counts = counts;
	}
}
