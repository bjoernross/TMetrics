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

public class TweetWithUser {

	@JsonProperty("tweet")
	private TweetBasic tweet = null;
	@JsonProperty("user")
	private TwitterUserBasic user = null;	
	
	public TweetWithUser(TweetBasic tweet, TwitterUserBasic user) {
		this.tweet = tweet;
		this.user = user;
	}
	
	public TweetWithUser() {
		
	}

	public TweetBasic getTweet() {
		return tweet;
	}

	public void setTweet(TweetBasic tweet) {
		this.tweet = tweet;
	}

	public TwitterUserBasic getUser() {
		return user;
	}

	public void setUser(TwitterUserBasic user) {
		this.user = user;
	}
		
}
