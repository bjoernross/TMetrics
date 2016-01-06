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

public class TwitterUserBasic {

	@JsonProperty("id")
	String id;
	@JsonProperty("name")
	String name;
	@JsonProperty("screen_name")
	String screenName; 
	
	public TwitterUserBasic(String id, String name, String screenName) {
		this.id = id;
		this.name = name;
		this.screenName = screenName;
	}
	
	public TwitterUserBasic() {
		
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}
	
	@Override
	public String toString() {
		return "TwitterUserBasic [userId=" + id + ", name=" + name
				+ ", screenName=" + screenName + "]";
	}
}
