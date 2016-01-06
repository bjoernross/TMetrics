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
package com.tmetrics.exceptions;

/**
 * Not data found exception. For example, usable if we want to get data from
 * database (for a given search term), but there is no data.
 * 
 * @author eq
 * 
 */
public class NotDataFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	public NotDataFoundException(String msg) {
		super(msg);
	}

}
