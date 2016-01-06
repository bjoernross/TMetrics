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

import org.joda.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for search result counts per date
 * 
 * @author
 */
public class SearchTermsPerQueryPerDate {

	@JsonProperty("search_term")
	public Query query = null;
	@JsonProperty("counts")
	public ArrayList<Integer> counts = new ArrayList<Integer>();
	@JsonIgnore
	public ArrayList<LocalDateTime> dates = new ArrayList<LocalDateTime>();
	@JsonProperty("dates")
	public ArrayList<String> dateStrings = new ArrayList<String>();

	/**
	 * Constructs the DTO
	 * 
	 * @param query
	 *            search term DTO
	 * @param counts
	 *            search result count list
	 * @param dates
	 *            search result date list
	 */

	public SearchTermsPerQueryPerDate(Query query, ArrayList<Integer> counts,
			ArrayList<LocalDateTime> dates) {
		this.query = query;
		this.counts = counts;
		this.dates = dates;
	}

	public SearchTermsPerQueryPerDate() {

	}

	/**
	 * Retuns the search term DTO
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
	 * Returns the search result count list
	 * 
	 * @return search result count list
	 */
	public ArrayList<Integer> getCounts() {
		return counts;
	}

	/**
	 * Sets the search result count list
	 * 
	 * @param counts
	 *            search result count list
	 */
	public void setCounts(ArrayList<Integer> counts) {
		this.counts = counts;
	}

	/**
	 * Returns the search result date list
	 * 
	 * @return search result date list
	 */
	public ArrayList<LocalDateTime> getDates() {
		return dates;
	}

	/**
	 * Sets the search result date list
	 * 
	 * @param dates
	 *            search result date list
	 */
	public void setDates(ArrayList<LocalDateTime> dates) {
		this.dates = dates;
	}

	/**
	 * Returns the search result date string list
	 * 
	 * @return search result date string list
	 */
	public ArrayList<String> getDateStrings() {
		return dateStrings;
	}

	/**
	 * Sets the search result date string list
	 * 
	 * @param dateStrings
	 *            search result date string list
	 */
	public void setDateStrings(ArrayList<String> dateStrings) {
		this.dateStrings = dateStrings;
	}

	/**
	 * Fills the DateStrings attribute with information from the Dates attribute
	 */
	public void updateDateStrings() {
		for (int i = 0; i < this.dates.size(); i++) {
			this.dateStrings.add(this.dates.get(i).toString());
		}
	}
}
