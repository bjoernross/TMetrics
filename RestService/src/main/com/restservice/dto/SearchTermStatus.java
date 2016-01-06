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

import org.joda.time.Duration;
import org.joda.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tmetrics.util.Localization;

public class SearchTermStatus {
	
	@JsonProperty("id")
	public Integer id = null;
	@JsonProperty("name")
	public String name = "";
	@JsonProperty("priority")
	public Integer priority = 0;
	@JsonProperty("active")
	public Boolean active = false;
	@JsonProperty("created_at")
	public String createdAt = "";
	@JsonProperty("time_last_fetched")
	public String timeLastFetched = "";
	@JsonProperty("interval_length")
	public String intervalLength = "";
	@JsonProperty("in_iteration")
	public Boolean inIteration = false;
	
	public SearchTermStatus(String name, Integer priority, Boolean active, String createdAt, String timeLastFetched) {
		this.name = name;
		this.priority = priority;
		this.active = active;
		this.createdAt = createdAt;
		this.timeLastFetched = timeLastFetched;
	}
	
	public SearchTermStatus() {
		
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Boolean getInIteration() {
		return inIteration;
	}

	public void setInIteration(Boolean inIteration) {
		this.inIteration = inIteration;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getTimeLastFetched() {
		return timeLastFetched;
	}

	public void setTimeLastFetched(String timeLastFetched) {
		this.timeLastFetched = timeLastFetched;
	}

	public String getIntervalLength() {
		return intervalLength;
	}

	public void setIntervalLength(String intervalLength) {
		this.intervalLength = intervalLength;
	}
	
}
