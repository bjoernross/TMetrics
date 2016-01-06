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

public class Tweet extends TweetBasic {

	@JsonProperty("user_id")
	private String userId = null;
	@JsonProperty("retweet_id")
	private String retweetId = null;
	@JsonProperty("reply_id")
	private String replyId = null;
	@JsonProperty("source")
	private String source = null;


	public Tweet(String id, Float coordinateLongitude, Float coordinateLatitude, 
			String createdAt, String text, Language lang,
			Integer retweetCount, Sentiment sentiment,
			String userId, String retweetId, String replyId, String source) {
		super(id, coordinateLongitude, coordinateLatitude, createdAt, text, lang, retweetCount, sentiment);
		this.userId = userId;
		this.retweetId = retweetId;
		this.replyId = replyId;
		this.source = source;
	}
	
	public Tweet() {
		super();
	}


	public String getUserId() {
		return userId;
	}


	public void setUserId(String userId) {
		this.userId = userId;
	}


	public String getRetweetId() {
		return retweetId;
	}


	public void setRetweetId(String retweetId) {
		this.retweetId = retweetId;
	}


	public String getReplyId() {
		return replyId;
	}


	public void setReplyId(String replyId) {
		this.replyId = replyId;
	}


	public String getSource() {
		return source;
	}


	public void setSource(String source) {
		this.source = source;
	}



}
