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
 * 
 * @author olaf
 *
 */
public class CountAndNewsPerHour {

	@JsonProperty("search_term")
	public Query query = null;
	@JsonProperty("graph")
	private ArrayList<CountPeaksNewsAndDate> graph = new ArrayList<CountPeaksNewsAndDate>();

	/**
	 * @return the news
	 */
	public ArrayList<CountPeaksNewsAndDate> getGraph() {
		return graph;
	}

	/**
	 * @param news the news to set
	 */
	public void setGraph(ArrayList<CountPeaksNewsAndDate> graph) {
		this.graph = graph;
	}

	/**
	 * @return the query
	 */
	public Query getQuery() {
		return query;
	}

	/**
	 * @param query the query to set
	 */
	public void setQuery(Query query) {
		this.query = query;
	}
}
