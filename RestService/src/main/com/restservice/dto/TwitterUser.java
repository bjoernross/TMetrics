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

public class TwitterUser extends TwitterUserBasic {
	
	@JsonProperty("profile_image_url")
	private String profileImageUrl = null;
	@JsonProperty("location")
	private String location = null;
	@JsonProperty("url")
	private String url = null; 
	@JsonProperty("lang")
	private String lang = null; 
	@JsonProperty("followers_count")
	private Integer followersCount = null;
	@JsonProperty("verified")
	private Integer verified = null;
	@JsonProperty("time_zone")
	private String timeZone = null;
	@JsonProperty("description")
	private String description = null;
	@JsonProperty("statuses_count")
	private Integer statusesCount = null;
	@JsonProperty("friends_count")
	private Integer friendsCount = null;
	@JsonProperty("created_at")
	private String createdAt = null;
	
	
	public String getProfileImageUrl() {
		return profileImageUrl;
	}
	public void setProfileImageUrl(String profileImageUrl) {
		this.profileImageUrl = profileImageUrl;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getLang() {
		return lang;
	}
	public void setLang(String lang) {
		this.lang = lang;
	}
	public Integer getFollowersCount() {
		return followersCount;
	}
	public void setFollowersCount(Integer followersCount) {
		this.followersCount = followersCount;
	}
	public Integer getVerified() {
		return verified;
	}
	public void setVerified(Integer verified) {
		this.verified = verified;
	}
	public String getTimeZone() {
		return timeZone;
	}
	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Integer getStatusesCount() {
		return statusesCount;
	}
	public void setStatusesCount(Integer statusesCount) {
		this.statusesCount = statusesCount;
	}
	public Integer getFriendsCount() {
		return friendsCount;
	}
	public void setFriendsCount(Integer friendsCount) {
		this.friendsCount = friendsCount;
	}
	public String getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}
	@Override
	public String toString() {
		return "TwitterUser [userId=" + id + ", name=" + name
				+ ", screenName=" + screenName + ", profileImageUrl="
				+ profileImageUrl + ", location=" + location + ", url=" + url
				+ ", lang=" + lang + ", followersCount=" + followersCount
				+ ", verified=" + verified + ", timeZone=" + timeZone
				+ ", description=" + description + ", statusesCount="
				+ statusesCount + ", friendsCount=" + friendsCount + ", created_at=" + createdAt + "]";
	}
	
	public TwitterUser(String id, String name, String screenName,
			String profileImageUrl, String location, String url, String lang,
			Integer followersCount, Integer verified, String timeZone,
			String description, Integer statusesCount, Integer friendsCount, String createdAt) {
		super(id, name, screenName);
		this.profileImageUrl = profileImageUrl;
		this.location = location;
		this.url = url;
		this.lang = lang;
		this.followersCount = followersCount;
		this.verified = verified;
		this.timeZone = timeZone;
		this.description = description;
		this.statusesCount = statusesCount;
		this.friendsCount = friendsCount;
		this.createdAt = createdAt;
	}
	public TwitterUser() {

	}
	
	

}
