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

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for search term meta information
 * 
 * @author
 */
public class QueryMetadata {

	@JsonProperty("query")
	private Query query = null;
	@JsonProperty("count")
	private int occurence;
	@JsonProperty("oldest_tweet")
	private Date oldestTweet = null;
	@JsonProperty("newest_tweet")
	private Date newestTweet = null;
	@JsonProperty("language")
	private String language = null;
	

	/**
	 * Constructs a meta information DTO
	 * 
	 * @param query
	 *            search term DTO
	 * @param occurence
	 *            count
	 * @param oldestTweet
	 *            oldest date
	 * @param newestTweet
	 *            newest date
	 */
	public QueryMetadata(Query query, int occurence, Date oldestTweet,
			Date newestTweet) {
		super();
		this.query = query;
		this.occurence = occurence;
		this.oldestTweet = oldestTweet;
		this.newestTweet = newestTweet;
	}

	public QueryMetadata() {}

	/**
	 * Returns the search term DTO
	 * 
	 * @return search term DTO
	 */
	public Query getQuery() {
		return query;
	}

	/**
	 * Sets the search term DTO
	 * 
	 * @param query
	 *            search term DTO
	 */
	public void setQuery(Query query) {
		this.query = query;
	}

	/**
	 * Returns the count
	 * 
	 * @return count
	 */
	public int getOccurence() {
		return occurence;
	}

	/**
	 * Sets the count
	 * 
	 * @param occurence
	 *            count
	 */
	public void setOccurence(int occurence) {
		this.occurence = occurence;
	}

	/**
	 * Returns the oldest date
	 * 
	 * @return oldest date
	 */
	public Date getOldestTweet() {
		return oldestTweet;
	}

	/**
	 * Sets the oldest date
	 * 
	 * @param oldestTweet
	 *            oldest date
	 */
	public void setOldestTweet(Date oldestTweet) {
		this.oldestTweet = oldestTweet;
	}

	/**
	 * Returns the newest date
	 * 
	 * @return newest date
	 */
	public Date getNewestTweet() {
		return newestTweet;
	}

	/**
	 * Sets the newest date
	 * 
	 * @param newestTweet
	 *            newest date
	 */
	public void setNewestTweet(Date newestTweet) {
		this.newestTweet = newestTweet;
	}
	
	public String getLanguage()
	{
		return language;
	}
	
	public void setLanguage(String _language)
	{
		language = _language;
	}

	@Override
	public String toString() 
	{
		return "QueryMetadata [query=" + query + ", occurence=" + occurence
				+ ", oldestTweet=" + oldestTweet + ", newestTweet="
				+ newestTweet + "]";
	}
}
