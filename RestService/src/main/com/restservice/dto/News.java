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

public class News {

	@JsonProperty("news")
	private ArrayList<NewsItem> news = new ArrayList<NewsItem>();

	public News() {
	}

	public News(ArrayList<NewsItem> news) {
		this.news = news;
	}

	/**
	 * @return the news
	 */
	public ArrayList<NewsItem> getNews() {
		return news;
	}

	/**
	 * @param news
	 *            the news to set
	 */
	public void setNews(ArrayList<NewsItem> news) {
		this.news = news;
	}
}
