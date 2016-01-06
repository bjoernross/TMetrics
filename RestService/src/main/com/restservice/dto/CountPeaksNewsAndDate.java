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

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CountPeaksNewsAndDate {

	@JsonProperty("count")
	private Integer count;
	@JsonIgnore
	private LocalDateTime rawDate;
	@JsonProperty("date")
	private String dateString;
	@JsonProperty("peak")
	private Boolean peak;
	@JsonProperty("news")
	private ArrayList<String> news = new ArrayList<String>();

	public CountPeaksNewsAndDate() {
	}

	/**
	 * @return the count
	 */
	public Integer getCount() {
		return count;
	}

	/**
	 * @param count
	 *            the count to set
	 */
	public void setCount(Integer count) {
		this.count = count;
	}

	/**
	 * @return the dates
	 */
	public LocalDateTime getRawDate() {
		return rawDate;
	}

	/**
	 * @param dates
	 *            the dates to set
	 */
	public void setRawDate(LocalDateTime rawDate) {
		this.rawDate = rawDate;
		this.dateString = rawDate.toString();
	}

	/**
	 * @return the dateString
	 */
	public String getDateString() {
		return dateString;
	}

	/**
	 * @param dateString
	 *            the dateString to set
	 */
	public void setDateString(String dateString) {
		this.dateString = dateString;
	}

	/**
	 * @return the peak
	 */
	public Boolean getPeak() {
		return peak;
	}

	/**
	 * @param peak
	 *            the peak to set
	 */
	public void setPeak(Boolean peak) {
		this.peak = peak;
	}

	/**
	 * @return the news
	 */
	public ArrayList<String> getNews() {
		return news;
	}

	/**
	 * @param news
	 *            the news to set
	 */
	public void setNews(ArrayList<String> news) {
		this.news = news;
	}
}
