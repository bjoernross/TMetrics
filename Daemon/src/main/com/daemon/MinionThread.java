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
 * The thread used to spawn a new minion.
 * @author Torsten
 */
public class MinionThread implements Runnable {

	private List<SearchTerm> _searchTerms = null;
	
	private List<SearchTerm> _shortTerms = null;
	
	private List<SearchTerm> _longTerms = null;
	
	private TwitterProfile _twitterProfile = null;
	
	private int _limitPerSearchTerm;
	
	private Master _master = null;
	
	/**
	 * Creates a new thread object.
	 * @param master The master of the minion to be spawned.
	 * @param twitterProfile The Twitter profile to be used by the minion.
	 * @param searchTerms The search terms to be used by the minion (no duplicates).
	 * @param shortTerms The short term to be used by the minion (may contain duplicates).
	 * @param longTerm The long term to be used by the minion (may contain duplicates).
	 */
	public MinionThread(Master master, TwitterProfile twitterProfile, List<SearchTerm> searchTerms, List<SearchTerm> shortTerms, List<SearchTerm> longTerm) {
		this(master, twitterProfile, searchTerms, shortTerms, longTerm, master.getDaemonProperties().unlimitedRequestsPerSearchTerm);
	}
	/**
	 * Creates a new thread object.
	 * @param master The master of the minion to be spawned.
	 * @param twitterProfile The Twitter profile to be used by the minion.
	 * @param searchTerms The search terms to be used by the minion (no duplicates).
	 * @param shortTerms The short term to be used by the minion (may contain duplicates).
	 * @param longTerm The long term to be used by the minion (may contain duplicates).
	 * @param limitPerSearchTerm The amount of fetches for each search term.
	 */
	public MinionThread(Master master, TwitterProfile twitterProfile, List<SearchTerm> searchTerms, List<SearchTerm> shortTerms, List<SearchTerm> longTerms, int limitPerSearchTerm) {
		_master = master;
		_twitterProfile = twitterProfile;
		_searchTerms = searchTerms;
		_shortTerms  = shortTerms;
		_longTerms   = longTerms;
		_limitPerSearchTerm = limitPerSearchTerm;
	}
	
	@Override
	public void run() {
		Minion d = new Minion(_master, _twitterProfile, _searchTerms, _shortTerms, _longTerms, _limitPerSearchTerm);
		d.run();
	}
}
