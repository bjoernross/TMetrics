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
 * DTO for sentiment information
 * 
 * @author
 */
public class SentimentData {

	@JsonProperty("id")
	private Long id = null;
	@JsonProperty("positive")
	private Long positive = null;
	@JsonProperty("neutral")
	private Long neutral = null;
	@JsonProperty("negative")
	private Long negative = null;

	/**
	 * Constructs the sentiment information DTO
	 * 
	 * @param _id
	 *            search term index
	 * @param _positive
	 * @param _neutral
	 * @param _negative
	 */
	public SentimentData(Long _id, Long _positive, Long _neutral, Long _negative) {
		id = _id;
		positive = _positive;
		neutral = _neutral;
		negative = _negative;
	}

	/**
	 * Returns the search term index
	 * 
	 * @return search term index
	 */
	public Long getID() {
		return id;
	}

	/**
	 * Sets the search term index
	 * 
	 * @param _id
	 *            search term index
	 */
	public void setID(Long _id) {
		id = _id;
	}

	/**
	 * Returns the count of positive tweets
	 * 
	 * @return count of positive tweets
	 */
	public Long getPositive() {
		return positive;
	}

	/**
	 * Sets the count of positive tweets
	 * 
	 * @param _positive
	 *            count of positive tweets
	 */
	public void setPositive(Long _positive) {
		positive = _positive;
	}

	/**
	 * Returns the count of neutral tweets
	 * 
	 * @return count of neutral tweets
	 */
	public Long getNeutral() {
		return neutral;
	}

	/**
	 * Sets the count of neutral tweets
	 * 
	 * @param _neutral
	 *            count of neutral tweets
	 */
	public void setNeutral(Long _neutral) {
		neutral = _neutral;
	}

	/**
	 * Returns the count of negative tweets
	 * 
	 * @return count of negative tweets
	 */
	public Long getNegative() {
		return negative;
	}

	/**
	 * Sets the count of negative tweets
	 * 
	 * @param _negative
	 *            count of negative tweets
	 */
	public void setNegative(Long _negative) {
		negative = _negative;
	}

	@Override
	public String toString()
	{
		return "SentimentData [id=" + id + ", positive=" + positive
				+ ", neutral=" + neutral + ", negative=" + negative + "]";
	}
}
