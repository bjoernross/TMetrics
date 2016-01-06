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
 * Represents an envelope, the communication Object that is returned as JSON.
 * 
 * @author
 */
public class Envelope {

	@JsonProperty("error_codes")
	private String errorCodes = null;
	@JsonProperty("stacktrace")
	private String stacktrace = null;
	@JsonProperty("data")
	private Object data = null;

	/**
	 * Returns the envelope's error code.
	 * 
	 * @return String errorCode
	 */
	public String getErrorCodes() {
		return errorCodes;
	}

	/**
	 * Sets the envelope's error code.
	 * 
	 * @param errorCodes
	 *            String
	 */
	public void setErrorCodes(String errorCodes) {
		this.errorCodes = errorCodes;
	}

	/**
	 * Returns the envelope's data.
	 * 
	 * @return Object data
	 */
	public Object getData() {
		return data;
	}

	/**
	 * Sets the envelope's data.
	 * 
	 * @param data
	 *            Object
	 */
	public void setData(Object data) {
		this.data = data;
	}

	@Override
	public String toString()
	{
		return "Envelope [errorCodes=" + errorCodes + ", stacktrace=" + stacktrace
				+ ", data=" + data + "]";
	}

	public String getStacktrace() {
		return stacktrace;
	}

	public void setStacktrace(String stacktrace) {
		this.stacktrace = stacktrace;
	}
	
	public void setStacktrace(StackTraceElement[] stacktrace)
	{
		String message = "";
		
		for (StackTraceElement element : stacktrace)
		{
			message += element.toString() + "\n";
		}
		
		this.stacktrace = message;
	}

}
