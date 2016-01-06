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
package com.daemon;

import org.joda.time.DateTime;

import com.daemon.database.SearchTerm;

/**
 * A class for storing a search term and associated meta data.
 * @author Torsten
 */
public class SearchTermMetaData {
	private SearchTerm _searchTerm = null;
	private DateTime _newestTweetDate = null;
	private DateTime _oldestTweetDate = null;
	
	// Number of new found tweets
	private int _tweetCount = 0;
	
	// Indicates for a search term to whether it should be searched for or not (not to
	// be confused with isActive(). This variable here is just used for a session.
	private boolean _filtered = false;
	
	private int _fetchedCount = 0;
	
	/**
	 * Creates a new search term meta data object.
	 * @param term The term that is associated with the meta data.
	 */
	public SearchTermMetaData(SearchTerm term) {
		_searchTerm = term;
	}
	
	/**
	 * Returns the search term.
	 * @return The search term.
	 */
	public SearchTerm getSearchTerm() {
		return _searchTerm;
	}
	
	/**
	 * Returns the newest tweet date.
	 * @return The newest tweet date.
	 */
	public DateTime getNewestTweetDate() {
		return _newestTweetDate;
	}
	
	/**
	 * Sets the newest tweet date.
	 * @param tweetDate The newest tweet date.
	 */
	public void setNewestTweetDate(DateTime tweetDate) {
		_newestTweetDate = tweetDate;
	}
	
	/**
	 * Returns the oldest tweet date.
	 * @return The oldest tweet date.
	 */
	public DateTime getOldestTweetDate() {
		return _oldestTweetDate;
	}

	/**
	 * Sets the oldest tweet date.
	 * @param tweetDate The oldest tweet date.
	 */
	public void setOldestTweetDate(DateTime tweetDate) {
		_oldestTweetDate = tweetDate;
	}
	
	/**
	 * Returns the number of new found tweets.
	 * @return The number of new found tweets.
	 */
	public int getTweetCount() {
		return _tweetCount;
	}

	/**
	 * Sets the number of new found tweets.
	 * @param tweetCount The number of new found tweets.
	 */
	public void setTweetCount(int tweetCount) {
		_tweetCount = tweetCount;
	}
	
	/**
	 * Returns whether the search term is filtered or not.
	 * @return Returns whether the search term is filtered or not.
	 */
	public boolean isFiltered() {
		return _filtered;
	}
	
	/**
	 * Sets whether the search term is filtered or not.
	 * @param filtered Sets whether the search term is filtered or not.
	 */
	public void setFiltered(boolean filtered) {
		_filtered = filtered;
	}
	
	/**
	 * Sets the number of times this search term has been fetched.
	 * @param fetchedCount The number of times this search term has been fetched.
	 */
	public void setFetchedCount(int fetchedCount) {
		_fetchedCount = fetchedCount;
	}
	
	/**
	 * Returns the number of times this search term has been fetched.
	 * @return The number of times this search term has been fetched.
	 */
	public int getFetchedCount() {
		return _fetchedCount;
	}
}
