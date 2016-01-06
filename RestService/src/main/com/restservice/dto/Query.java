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
 * DTO for search terms
 * 
 * @author
 */
public class Query {

	@JsonProperty("id")
	private Long id = null;
	@JsonProperty("String")
	private String string = null;

	/**
	 * Constructs a new search term DTO
	 * 
	 * @param id
	 *            id
	 * @param string
	 *            search term string
	 */
	public Query(Long id, String string) {
		super();
		this.id = id;
		this.string = string;
	}

	public Query() {}

	/**
	 * Returns the id
	 * 
	 * @return id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Sets the id
	 * 
	 * @param id
	 *            id
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Returns the search term string
	 * 
	 * @return search term string
	 */
	public String getString() {
		return string;
	}

	/**
	 * Sets the search term string
	 * 
	 * @param string
	 *            search term string
	 */
	public void setString(String string) {
		this.string = string;
	}

	@Override
	public String toString()
	{
		return "Query [id=" + id + ", string=" + string + "]";
	}
}
