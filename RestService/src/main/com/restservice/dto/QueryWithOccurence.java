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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for search terms and their search result count
 * 
 * @author
 */
public class QueryWithOccurence extends Query {

	@JsonProperty("count")
	private int count;

	/**
	 * Constructs the DTO
	 * 
	 * @param id
	 *            search term index
	 * @param string
	 *            search term string
	 * @param count
	 *            search result count
	 */
	public QueryWithOccurence(Long id, String string, int count) {
		super(id, string);
		this.setCount(count);
	}

	/**
	 * Returns the search result count
	 * 
	 * @return search result count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * Sets the search result count
	 * 
	 * @param count
	 *            search result count
	 */
	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public String toString()
	{
		return "QueryWithOccurence [count=" + count + ", getId()=" + getId()
				+ ", getString()=" + getString() + "]";
	}
}
