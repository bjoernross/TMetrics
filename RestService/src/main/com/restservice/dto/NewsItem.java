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
 * 
 */

/**
 * @author olaf
 * 
 */
public class NewsItem {
	@JsonProperty("provider")
	private String provider = "";
	@JsonProperty("title")
	private String title = "";
	@JsonProperty("url")
	private String url = "";
	@JsonProperty("rating")
	private Double rating = .0;
	@JsonProperty("text")
	private String text = "";

	/**
	 * @return the rating
	 */
	public Double getRating() {
		return rating;
	}

	/**
	 * @param rating
	 *            the rating to set
	 */
	public void setRating(Double rating) {
		this.rating = rating;
	}

	/**
	 * @return the provider
	 */
	public String getProvider() {
		return provider;
	}

	/**
	 * @param provider
	 *            the provider to set
	 */
	public void setProvider(String provider) {
		this.provider = provider;
	}

	public NewsItem() {
	};

	public NewsItem(String provider) {
		this.provider = provider;
	};

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url
	 *            the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text
	 *            the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}

	public String toString() {
		return ((rating != .0) ? (rating + " ") : "") + "[" + this.provider
				+ "] " + this.title + ": " + this.text + "(" + this.url + ")";
	}

	public String toShortString() {
		return "[" + this.provider + "] " + this.title;
	}

	@Override
	public boolean equals(Object other) {
		if (other != null && other instanceof NewsItem)
			return false;
		NewsItem otherNews = (NewsItem) other;
		return (this.title != null && this.provider != null && this.url != null)
				&& (otherNews.getTitle() != null
						&& otherNews.getProvider() != null && otherNews
						.getUrl() != null)
				&& (this.title.equals(otherNews.getTitle())
						&& this.provider.equals(otherNews.getProvider()) && this.url
							.equals(otherNews.getUrl()));
	}
}