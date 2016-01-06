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

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.daemon.database.SearchTerm;

import twitter4j.Status;

/**
 * Used to store Tweets associated with a specific search term object.
 * This class is necessary for the consistency of local and remote
 * search terms.
 * @author Torsten
 */
public class Package {
	private List<Status> _tweets = new LinkedList<Status>();
	
	private SearchTerm _searchTerm = null;
	
	private DateTime _date = null;

	// A comparator for sorting dates ascending
	public static final Comparator<? super Package> DATETIME_COMPARATOR = new Comparator<Package>() {
		@Override
		public int compare(Package pack1, Package pack2) {
			long diff = pack1.getDate().getMillis() - pack2.getDate().getMillis();
			
			if (diff < 0)
				return -1;
			else if (diff == 0)
				return 0;
			else
				return 1;
		}
	};
	
	/**
	 * Creates a new instance of this class.
	 * @param tweets The Tweets to be contained.
	 * @param searchTerm The search term to be associated.
	 * @param date The date of this package.
	 */
	public Package(List<Status> tweets, SearchTerm searchTerm, DateTime date) {
		assert(tweets != null);
		assert(searchTerm != null);
		assert(date != null);
		
		_tweets = tweets;
		_searchTerm = searchTerm;
		_date = date;
	}

	/**
	 * Returns the stored Tweets.
	 * @return The stored Tweets.
	 */
	public List<Status> getTweets() {
		return _tweets;
	}

	/**
	 * Returns the associated search term.
	 * @return The associated search term.
	 */
	public SearchTerm getSearchTerm() {
		return _searchTerm;
	}
	
	/**
	 * Returns the size of the package.
	 * @return The size of the package.
	 */
	public int size() {
		return _tweets.size();
	}

	/**
	 * Returns the date of the package.
	 * @return The date of the package.
	 */
	public DateTime getDate() {
		return _date;
	}
	
	/**
	 * Counts the size of all packages in the list and returns the added value.
	 * @param packages List of packages to be count.
	 * @return The size of all packages together.
	 */
	public static int sizeOfAllPackages(List<Package> packages) {
		int size = 0;
		
		for (Package pack : packages) {
			size += pack.size();
		}
		
		return size;
	}

	/**
	 * Counts the number of different search terms in the list of packages.
	 * @param packages 
	 * @return The number of different search terms in the list of packages.
	 */
	public static int countDifferentSearchTerms(List<Package> packages) {
		Map<SearchTerm, Integer> countMap = new HashMap<SearchTerm, Integer>();
		
		for (Package pack : packages) {
			if (countMap.get(pack.getSearchTerm()) == null) {
				countMap.put(pack.getSearchTerm(), 0);
			}
			
			// For each occurrence of a search terms, increase its count by 1
			countMap.put(pack.getSearchTerm(), countMap.get(pack.getSearchTerm()) + 1);
		}
		
		// Number of entries is the number of different search terms
		return countMap.entrySet().size();
	}
}
