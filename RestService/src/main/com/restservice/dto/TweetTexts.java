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

public class TweetTexts
{
	@JsonProperty("text")
	private String text = null;
	@JsonProperty("count")
	private Long count = null;
	
	public TweetTexts(Long id, String searchTerm, String text, Long count)
	{
		this.text = text;
		this.count = count;
	}
	
	public TweetTexts()
	{
		super();
	}
	
	public String getText()
	{
		return text;
	}
	
	public void setText(String _text)
	{
		text = _text;
	}
	
	public Long getCount()
	{
		return count;
	}
	
	public void setCount(Long _count)
	{
		count = _count;
	}
	
	
	
}
