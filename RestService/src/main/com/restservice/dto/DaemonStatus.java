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

public class DaemonStatus {
	
	@JsonProperty("total_count")
	public Integer totalCount = 0;
	@JsonProperty("active_count")
	public Integer activeCount = 0;
	@JsonProperty("search_terms")
	public ArrayList<SearchTermStatus> searchTerms = new ArrayList<>();
	
	public DaemonStatus(Integer totalCount, Integer activeCount,
			ArrayList<SearchTermStatus> searchTerms) {
		this.totalCount = totalCount;
		this.activeCount = activeCount;
		this.searchTerms = searchTerms;
	} 
	
	public DaemonStatus() {
		
	}

	public Integer getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}

	public Integer getActiveCount() {
		return activeCount;
	}

	public void setActiveCount(Integer activeCount) {
		this.activeCount = activeCount;
	}

	public ArrayList<SearchTermStatus> getSearchTerms() {
		return searchTerms;
	}

	public void setSearchTerms(ArrayList<SearchTermStatus> searchTerms) {
		this.searchTerms = searchTerms;
	}
	
}
