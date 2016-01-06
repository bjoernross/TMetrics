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
import com.tmetrics.dto.SentimentFeatures;

public class TweetBasic {

	@JsonProperty("id")
	private String id = null;
	@JsonProperty("coordinate_longitude")
	private Float coordinateLongitude = null;
	@JsonProperty("coordinate_latitude")
	private Float coordinateLatitude = null;
	@JsonProperty("created_at")
	private String createdAt = null;
	@JsonProperty("text")
	private String text = null;
	@JsonProperty("lang")
	private Language lang;
	@JsonProperty("retweet_count")
	private Integer retweetCount = null;
	@JsonProperty("sentiment")
	private Sentiment sentiment = null;
	@JsonProperty("sentimentFeatures")
	private SentimentFeatures features = null;
	
	public TweetBasic(String id, Float coordinateLongitude, Float coordinateLatitude, 
			String createdAt, String text, Language lang,
			Integer retweetCount, Sentiment sentiment) {
		this.id = id;
		this.coordinateLongitude = coordinateLongitude;
		this.coordinateLatitude = coordinateLatitude;
		this.createdAt = createdAt;
		this.text = text;
		this.lang = lang;
		this.retweetCount = retweetCount;
		this.sentiment = sentiment;
	}
	
	public TweetBasic() {
		
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Float getCoordinateLongitude() {
		return coordinateLongitude;
	}
	public void setCoordinateLongitude(Float coordinateLongitude) {
		this.coordinateLongitude = coordinateLongitude;
	}
	public Float getCoordinateLatitude() {
		return coordinateLatitude;
	}
	public void setCoordinateLatitude(Float coordinateLatitude) {
		this.coordinateLatitude = coordinateLatitude;
	}
	public String getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public Language getLang() {
		return lang;
	}
	public void setLang(Language lang) {
		this.lang = lang;
	}
	public Integer getRetweetCount() {
		return retweetCount;
	}
	public void setRetweetCount(Integer retweetCount) {
		this.retweetCount = retweetCount;
	}
	public Sentiment getSentiment() {
		return sentiment;
	}
	public void setSentiment(Sentiment sentiment) {
		this.sentiment = sentiment;
	}
	
	public SentimentFeatures getSentimentFeatures() {
		return features;
	}
	
	public void setSentimentFeatures(SentimentFeatures features) {
		this.features = features;
	}
	
}