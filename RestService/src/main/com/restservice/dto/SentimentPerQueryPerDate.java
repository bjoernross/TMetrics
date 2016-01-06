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
 * DTO for sentiment counts per date
 */


public class SentimentPerQueryPerDate {

	@JsonProperty("positive_counts")
	public SearchTermsPerQueryPerDate positiveCounts = null;
	@JsonProperty("negative_counts")
	public SearchTermsPerQueryPerDate negativeCounts = null;
	
	public SentimentPerQueryPerDate(SearchTermsPerQueryPerDate positiveCounts, SearchTermsPerQueryPerDate negativeCounts) {
		this.positiveCounts = positiveCounts;
		this.negativeCounts = negativeCounts;
	}
	
	public SentimentPerQueryPerDate() {
		
	}

	public SearchTermsPerQueryPerDate getPositiveCounts() {
		return positiveCounts;
	}

	public void setPositiveCounts(SearchTermsPerQueryPerDate positiveCounts) {
		this.positiveCounts = positiveCounts;
	}

	public SearchTermsPerQueryPerDate getNegativeCounts() {
		return negativeCounts;
	}

	public void setNegativeCounts(SearchTermsPerQueryPerDate negativeCounts) {
		this.negativeCounts = negativeCounts;
	}
	
	
}
