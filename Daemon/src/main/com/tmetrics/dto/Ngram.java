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
package com.tmetrics.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A DTO that holds n-gram features and their parameters. Essentially a
 * string-float pair with an integer for the position of the string in the text.
 * 
 * @author Daniel, Björn
 * 
 */
public class Ngram {

	// each feature/n-gram has a string
	@JsonProperty("string")
	private String string = null;

	// and a parameter
	@JsonProperty("parameter")
	private Float parameter = null;

	// and a position in the text
	@JsonIgnore
	private Integer position = null;

	public Ngram(String _string, Float _parameter) {
		string = _string;
		parameter = _parameter;
	}

	public Ngram(String _string, Float _parameter, Integer _position) {
		string = _string;
		parameter = _parameter;
		position = _position;
	}

	public String getString() {
		return string;
	}

	public void setString(String _string) {
		string = _string;
	}

	@JsonIgnore
	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer _position) {
		position = _position;
	}

	public Float getParameter() {
		return parameter;
	}

	@JsonIgnore
	public void setParameter(Float _parameter) {
		parameter = _parameter;
	}

	public int compareTo(Ngram swp) {
		if (getPosition() == swp.getPosition()) {
			return 0;
		}

		return ((getPosition() < swp.getPosition()) ? -1 : 1);
	}

	@Override
	public String toString() {
		return "[" + getString() + ", " + getPosition() + "]";
	}

}
