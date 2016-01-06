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
package com.daemon;

import java.util.List;

import com.daemon.database.SearchTerm;

/**
 * A structure used to export relevant minion data to the master.
 * @author Torsten
 */
public class MinionData {
	public MessageType messageType = MessageType.UNKNOWN;
	
	public List<SearchTerm> searchTerms = null;
	
	public List<Package> tweetPackages = null;
	
	/**
	 * Creates a new object with the given parameters.
	 * @param messageType The type of the message.
	 * @param searchTerms The search terms used by the minion.
	 * @param tweetPackages The packages containing the fetched tweets.
	 */
	public MinionData(MessageType messageType, List<SearchTerm> searchTerms, List<Package> tweetPackages) {
		this.messageType = messageType;
		this.searchTerms = searchTerms;
		this.tweetPackages = tweetPackages;
	}
}
