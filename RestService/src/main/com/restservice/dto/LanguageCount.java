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

public class LanguageCount
{
	@JsonProperty("iso_code")
	private String isoCode = null;
	@JsonProperty("count")
	private Integer count = null;
	
	public String getIsoCode()
	{
		return isoCode;
	}
	
	public void setIsoCode(String _isoCode)
	{
		isoCode = _isoCode;
	}
	
	public int getCount()
	{
		return count;
	}
	
	public void setCount(int _count)
	{
		count = _count;
	}
}
