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

/*
 * Language holds the ISO Code 639-1 and an user-readable String of this language in english if possible (see com.restservice.util.LanguageDictionary for more info)
 */
public class Language {
	
	@JsonProperty("iso_code")
	private String isoCode = null;
	@JsonProperty("string")
	private String outputString = null;
	
	public Language(String isoCode) {
		this.isoCode = isoCode;
	}
	
	public Language(String isoCode, String outputString) {
		this.isoCode = isoCode;
		this.outputString = outputString;
	}
	
	public String getIsoCode() {
		return this.isoCode;
	}
	public void setIsoCode(String isoCode) {
		this.isoCode = isoCode;
	}
	public String getOutputString() {
		return this.outputString;
	}
	public void setOutputString(String outputString) {
		this.outputString = outputString;
	}

}
