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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import twitter4j.TwitterException;

import com.daemon.database.SearchTerm;
import com.daemon.database.Transactor;
import com.daemon.sentiment.RegressionSentimentClassifier;
import com.tmetrics.logging.LogManager;
import com.tmetrics.logging.Logger;
import com.tmetrics.util.ListUtil;
import com.tmetrics.util.Localization;
import com.tmetrics.util.ResettableTimer;

/**
 * A master to spawn, direct and kill minions.
 * The master is responsible for monitoring the search term distribution
 * among his minions. If work is not getting done, the master will spawn
 * a new minion to be assigned to that work. If a minion finishes, it
 * puts the found tweets into a bag that will be emptied, if necessary.
 * @author Torsten, Jens, Björn
 */
public class Master implements Observer {
	
	private Transactor _transactor = null;
	
	// Contains the id of a search term
	private List<Integer> _searchTermInUse = null;
	
	private TwitterProfile[] _twitterProfiles = null;
	
	private TwitterProfile _limitedMinionProfile = null;
	
	private List<String> _twitterProfileFilenames = new LinkedList<String>();
	
	private Lock _mutex = new ReentrantLock(true);
	
	private List<Package> _bagOfPackages = new LinkedList<Package>();

	private ResettableTimer _treasurerTimer = null;
	
	private Logger _logger = LogManager.getLogger("logs/Master.log");
	
	private DaemonProperties _props = null;
	
	private List<SearchTerm> _localSearchTerms = new LinkedList<>();
	
	// Used because only one treasurer is allowed to enter the treasury at once
	// (exception: one royal treasurer can interrupt a normal treasurer and enter
	// the treasury, too)
	private static Treasury _treasury;
	
	private static ExecutorService _minionPool = null;
	
	private RegressionSentimentClassifier sentimentClassifier = new RegressionSentimentClassifier();

	public Master() throws ClassNotFoundException, IOException {
		_props = new DaemonProperties("daemon.properties");
		
		_treasury = new Treasury(this);
		
		_transactor = new Transactor();
		
		_searchTermInUse = new LinkedList<Integer>();

		// get files in current directory
		File[] files = new File(".").listFiles();
		
		for (File file : files) {
			String fileName = file.getName();
			if (file.isFile() && fileName.startsWith("profile") && fileName.endsWith(".properties")) {
				_twitterProfileFilenames.add(fileName);
			}
		}
		
		// Should we spawn the limited minion, if there is a new search term?
		if (!_props.spawnLimitedMinion) {
			System.out.println(Localization.now() + " Master will never spawn a limited minion.");
		}
		
		System.out.println(Localization.now() + " Master uses " + _twitterProfileFilenames.size() + " Twitter profile(s).");
		
		if (_twitterProfileFilenames.size() == 0) {
			String message = "Cannot load any profile files (profileX.properties) from folder "
					+ new File(".").getCanonicalPath()
					+ ". No data will be fetched from Twitter.";
			_logger.log(message);
			System.out.println(Localization.now() + " " + message + " Consult log for more information.");
		}

		// Create Twitter profiles from files (if any)
		_twitterProfiles = new TwitterProfile[_twitterProfileFilenames.size()];
		for (int i = 0; i < _twitterProfileFilenames.size(); ++i) {
			_twitterProfiles[i] = TwitterProfile
					.fromFilename(_twitterProfileFilenames.get(i), _props);
		}

		// Only load limited minion profile if necessary
		if (_props.spawnLimitedMinion) {
			try {
				_limitedMinionProfile = TwitterProfile.fromFilename("LimitedMinion.properties", _props);
			} catch (IllegalArgumentException e) {
				String message = "Cannot load "
						+ new File(".").getCanonicalPath()
						+ "/LimitedMinion.properties. New search terms might not be fetched for immediately.";
				_logger.log(message);
				System.out.println(message + " Consult log for more information.");
			}		
		}
		
		// Create thread-pool for minions
		if (_twitterProfiles.length > 0)
			_minionPool = Executors.newFixedThreadPool(_twitterProfiles.length);
		
		// Set the timer for the treasurer
		_treasurerTimer = new ResettableTimer(new SummonTreasurerTask(_mutex, _treasury, _bagOfPackages), _props.maxBagNotEmptiedDuration.getMillis());
	}
	
	public void run() {
		// Connect to database
		_transactor.connect();
		System.out.println(Localization.now() + " Connected to database.");
		
		// start sentiment regression updater
		sentimentClassifier.startRegressionSentimentUpdater();
		
		// Start the timer for the treasurer
		_treasurerTimer.start();
		
		// Indicates whether the master should sleep a while or not
		boolean shouldSleep = false;
		
		while (true) {
			try {
				if (shouldSleep) {
					Thread.sleep(_props.sleepDuration);
					shouldSleep = false;
				}
			}
			catch (InterruptedException ex) {
				_logger.log(Localization.now() + " Waiting interrupted.");
			}
			
			// Clear the rate limit of a Twitter profile if applicable
			DateTime now = new DateTime();
			for (TwitterProfile profile : _twitterProfiles) {
				// If the profile is already a fresh one, skip it
				if (profile.getLastUsed() == null) {
					continue;
				}
				
				// Reset rate limit and last start if cooldown is up
				if (now.minus(profile.getLastUsed().getMillis()).getMillis() >= _props.profileResetTime.getMillis()) {
					profile.setLastUsed(null);
					profile.setUsedRateLimit(0);
				}
			}
			
			// Short terms are those terms, that will hopefully only fetch once, while
			// long terms are those that we expect to fetch multiply times until
			// the rate limit is used up
			List<SearchTerm> shortTerms = new LinkedList<SearchTerm>();
			List<SearchTerm> longTerms  = new LinkedList<SearchTerm>();
			
			// Lock the access to get only actualized search terms
			_mutex.lock();
			
			try {
				// Get search terms
				List<SearchTerm> searchTerms = _transactor.getSearchTerms(true);
				_localSearchTerms = makeSearchTermsConsistent(_localSearchTerms, searchTerms, true);
			}
			catch (Exception ex) {
				_logger.log("An error occured while trying fetch the search terms. Consult Master.log for further information.");
				_logger.logStackTrace(ex);
			}
			finally {
				_mutex.unlock();
			}
			
			// Extract new search terms and start a special minion for those
			List<SearchTerm> newSearchTerms = getNewSearchTerms(_localSearchTerms);
			
			if (_props.spawnLimitedMinion && newSearchTerms.size() > 0) {
				startLimitedMinion(newSearchTerms);
			}
			
			// Fill short and long term list
			fillShortAndLongList(_localSearchTerms, shortTerms, longTerms, _props);
			
			// Sort the lists
			sortListByDateTime(shortTerms);
			sortListByDateTime(longTerms);
			
			// Now lock the mutex again
			_mutex.lock();
			
			// Do we need to spawn a new minion?
			// We do so, if we have any search term that is currently not in use AND
			// there are no too many treasurers working / waiting in the treasury.
			if ((shortTerms.size() > 0 || longTerms.size() > 0) &&
				(_searchTermInUse.size() < searchableSearchTerms(_localSearchTerms).size()) &&
				_treasury.countTreasuresWaiting() < _props.maxTreasurers) {
				// Yes, so let's check, if we actually can spawn a new minion
					
				// Search for a valid profile and spawn a new minion using that
				// profile.
				// A profile is valid if its rate limit is not exceeded AND the
				// profile is not in use, yet
				TwitterProfile validProfile = null;
				for (int i = 0; (i < _twitterProfiles.length) && validProfile == null; ++i) {
					if (!_twitterProfiles[i].isInUse() &&
						_twitterProfiles[i].getUsedRateLimit() < _props.maxRateLimit) {
						validProfile = _twitterProfiles[i];
					}
				}
				
				// If there is no valid profile currently, start new iteration
				if (validProfile == null) {
					// No valid profile found, so we go sleepy sleepy
					shouldSleep = true;
					
					// Release the lock
					_mutex.unlock();
					
					continue;
				}
				
				// This profile will be used and cannot be used by another minion
				validProfile.setIsInUse(true);
				
				// We now have a valid profile. Before spawning a new minion, we have to pick
				// the correct amount of short and long terms.
				List<SearchTerm> minionShortTerms = new LinkedList<SearchTerm>();
				List<SearchTerm> minionLongTerms  = new LinkedList<SearchTerm>();
				
				// Short terms
				for (int i = 0, j = 0; j < _props.maxShortTermsCount && i < shortTerms.size(); ++i) {
					// If the current short term is already in use by another minion,
					// skip it
					try {
						if (!ListUtil.contains(_searchTermInUse, shortTerms.get(i).getId())) {
							minionShortTerms.add(shortTerms.get(i));
							_searchTermInUse.add(shortTerms.get(i).getId());
							++j;
						}
					}
					catch (Exception ex) {}
				}
				
				// Long terms
				for (int i = 0, j = 0; j < _props.maxLongTermsCount && i < longTerms.size(); ++i) {
					// If the current long term is already in use by another minion,
					// skip it
					try {
						if (!ListUtil.contains(_searchTermInUse, longTerms.get(i).getId())) {
							minionLongTerms.add(longTerms.get(i));
							_searchTermInUse.add(longTerms.get(i).getId());
							++j;
						}
					}
					catch (Exception ex) {}
				}

				// Create the list of search terms used by the minion to be spawned
				List<SearchTerm> minionSearchTerms = new LinkedList<SearchTerm>();
				for (SearchTerm shortTerm : minionShortTerms) {
					minionSearchTerms.add(shortTerm);
				}
				for (SearchTerm longTerm : minionLongTerms) {
					minionSearchTerms.add(longTerm);
				}
				
				// Fill up the long terms as that has not been done, yet
				minionLongTerms = fillUpList(minionLongTerms);
				
				// If this a reseted profile, set last used
				if (validProfile.getLastUsed() == null) {
					validProfile.setLastUsed(new DateTime());
				}
				
				// Actualize the profile's rate limit
				try {
					int rateLimit = validProfile.getSearchRateLimit();
					
					// Set the profile's rate limit and subtract the rate limit buffer, because Twitter
					// returns the actual rate limit without the buffer
					validProfile.setUsedRateLimit(_props.maxRateLimit - rateLimit);
					
					// Start the minion in a new thread
					System.out.println(Localization.now() + " Master spawns a new minion.");
	
					// Note: _minionPool might be null, if we have no profiles at all. BUT in that case
					// we would have exited the if-case long before this line, so there cannot not be thrown
					// a NullPointerException.
					_minionPool.execute(new MinionThread(this, validProfile, minionSearchTerms, minionShortTerms, minionLongTerms));
				}
				catch (TwitterException ex) {
					// If there is something wrong with twitter, we are unable to do anything about it
					LogManager.getLogger("logs/Minions.log").logStackTrace(ex, validProfile.getScreenName());
					
					System.err.println(Localization.DATEANDTIME_FORMATTER.format(new Date()) + " " + validProfile.getScreenName() + " Error during communicating with Twitter. Consult 'logs/Minions.log' for further information.");
				}
			}
			else {
				// We have no search terms to be searched left, so go to sleep
				shouldSleep = true;
			}
			
			// Unlock as we are done updating
			_mutex.unlock();
		}
	}
	
	public static void main(String[] args) {
		// Set the time zone for the daemon to UTC
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		DateTimeZone.setDefault(DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC")));

		System.out.println(Localization.now() + " Master started.");
		
		Master m = null;
		try {
			// Start the master
			m = new Master();
			m.run();
		}
		catch (Exception ex) {
			if (m != null) {
				m.getLogger().logStackTrace(ex);
				
				System.err.println(Localization.now() + " An error occured. Consult Master.log for further information.");
			} else {
				System.err.println(Localization.now() + " An error occured.");
				
				// Print stack trace to error stream, because there is no log file, yet.
				ex.printStackTrace();
			}
		}
	}
	
	/**
	 * Returns the Transactor object for every other process to use.
	 * @return The Transactor object for every other process to use.
	 */
	public Transactor getTransactor() {
		return _transactor;
	}
	
	/**
	 * Returns the logging object for this master.
	 * @return The logging object for this master.
	 */
	public Logger getLogger() {
		return _logger;
	}

	/**
	 * Called by a minion to inform the master about the minion's
	 * business. Currently only used to notify about the exit of a minion
	 * and the type of the existing minion.
	 * @param o The minion object that called this method.
	 * @param arg Data sent by the minion for the master to handle. The type
	 * of the object is MinionData.
	 */
	@Override
	public synchronized void update(Observable o, Object arg) {
		// Lock the access to the _searchTermInUse because the list
		// my be updated in the run-method as well at the same time OR
		// the treasurer may be active
		_mutex.lock();
		
		try {
			MinionData md = (MinionData)arg;
			Minion m = (Minion)o;
			
			switch (md.messageType) {
			case MINION_FINISHED:
				updateSearchTermsInUse(md.searchTerms);
				
				// Put received packages in the bag
				for (Package pack : md.tweetPackages) {
					// Do not check for empty bags as otherwise a fresh
					// search term would not be updated in to database
					// (consult Torsten or Sebastian for further questions)
					_bagOfPackages.add(pack);
				}
				// clear Miniondata afterwards as we have everything we need in our bag
				md.searchTerms.clear();
				md.tweetPackages.clear();
				
				// Check if the bag of tweets is overflowing
				if (Package.sizeOfAllPackages(_bagOfPackages) >= _props.maxBagSize) {
					List<Package> copyOfBag = ListUtil.flatClone(_bagOfPackages);
					
					// Summon the treasurer, so he can do his business
					new Thread(new Treasurer(_treasury, copyOfBag)).start();
					
					// Finally clear the bag as the content is now in the greedy hands of
					// the treasurer
					_bagOfPackages.clear();
					
					// Reset the treasurer timer as the bag was already emptied.
					_treasurerTimer.reset();
				}
				
				// Twitter profile is no longer in use
				m.getTwitterProfile().setIsInUse(false);
				
				break;
			case LIMITEDMINION_FINISHED:
				updateSearchTermsInUse(md.searchTerms);
				
				// Summon a high priority treasurer, so he can do his business
				new Thread(new RoyalTreasurer(_treasury, md.tweetPackages)).start();
				
				break;
			default:
				break;
			}
		}
		finally {
		// Unlock as we are done updating
		_mutex.unlock();
		}
	}
	
	/**
	 * Updates the list of search terms that are in use according to the
	 * search terms argument.
	 * @param searchTerms The search terms that are no longer in use.
	 */
	private void updateSearchTermsInUse(List<SearchTerm> searchTerms) {
		// Remove used search terms as those are not available again
		// as: copy _searchTermInUse without the used search terms of the minion
		List<Integer> newSearchTermInUseList = new LinkedList<Integer>();
		for (Integer termId : _searchTermInUse) {
			boolean valid = true;
			
			for (int i = 0; i < searchTerms.size() && valid; ++i) {
				try {
					if (termId == searchTerms.get(i).getId()) {
						valid = false;
					}
				}
				catch (Exception ex) {}
			}
			
			if (valid && !newSearchTermInUseList.contains(termId)) {
				newSearchTermInUseList.add(termId);
			}
		}
		
		_searchTermInUse = newSearchTermInUseList;
	}
	
	/**
	 * Returns only new search terms (that is those that have no old start AND whose
	 * last fetched tweet id is NULL).
	 * @param searchTerms The search terms to be searched.
	 * @return Returns only new search terms.
	 */
	public List<SearchTerm> getNewSearchTerms(List<SearchTerm> searchTerms) {
		List<SearchTerm> newSearchTerms = new LinkedList<SearchTerm>();
		
		for (SearchTerm term : searchTerms) {
			try {
				// If the term is new and it is not already in use, add it
				// as a new search term
				if (term.isNew() && !_searchTermInUse.contains(term.getId())) {
					newSearchTerms.add(term);
				}
			}
			catch (Exception _) {}
		}
		
		return newSearchTerms;
	}
	
	/**
	 * Starts the limited minion for immediate search term fetching.
	 * @param newSearchTerms The search terms to be fetched immediately.
	 */
	public void startLimitedMinion(List<SearchTerm> newSearchTerms) {
		// Lock the mutex because we need to alter _searchTermInUse
		_mutex.lock();
		
		for (int i = 0; i < newSearchTerms.size(); ++i) {
			// This search term is now in use
			try {
				_searchTermInUse.add(newSearchTerms.get(i).getId());
			}
			catch (Exception ex) {}
		}
		
		// Spawn the limited Minion (he has no short terms, only long terms)
		System.out.println(Localization.now() + " Master spawns minion for new search terms.");
		new Thread(new MinionThread(this, _limitedMinionProfile, newSearchTerms, new LinkedList<SearchTerm>(), newSearchTerms, _props.limitSearchTerms)).start();
		
		_mutex.unlock();
	}
	
	/**
	 * Returns a new local version of search terms that is consistent with the search terms stored.
	 * in the database.
	 * @param localSearchTerms The current local search terms to be made consistent.
	 * @param remoteSearchTerms The search terms freshly fetched from the database.
	 * @param removeInactive If this flag is set, inactive local search terms will be removed instead
	 * of kept.
	 * @return A new local version of search terms that is consistent with the search terms stored.
	 */
	public static List<SearchTerm> makeSearchTermsConsistent(List<SearchTerm> localSearchTerms, List<SearchTerm> remoteSearchTerms, boolean removeInactive) {
		List<SearchTerm> newLocalSearchTerms = new LinkedList<SearchTerm>();
		
		// Iterate through all remote search terms and adjust the local terms
		// That means, we keep the local term if it was already present OR
		// we add the remote term, because it is to be added.
		for (SearchTerm term : remoteSearchTerms) {
			int index = localSearchTerms.indexOf(term);
			
			// Does local terms already contain this term?
			if (index != -1) {
				// Yes, so get that local term
				SearchTerm localTerm = localSearchTerms.get(index);
				
				// Change priority if it has changed remotely
				if (term.getPriority() != localTerm.getPriority()) {
					localTerm.setPriority(term.getPriority());
				}
				
				// Add term no matter what, but reset active field.
				// If the remote term is inactive, but the local term is
				// active, we have to mark the local term as inactive so
				// it gets written to the database at a later point
				// If the remote term is active, nothing will change for
				// the local term.
				localTerm.setActive(term.isActive());
				newLocalSearchTerms.add(localTerm);
			}
			else {
				// The term was not contained locally, so add it for sure
				// if it is active
				if (term.isActive()) {
					newLocalSearchTerms.add(term);
				}
			}
		}
		
		// Iterate over all local terms and check, if they should be removed
		// (because the remote term is marked as inactive and may or may not
		// be in the remote search terms list)
		for (SearchTerm localTerm : localSearchTerms) {
			int index = remoteSearchTerms.indexOf(localTerm);
			int indexNewLocal = newLocalSearchTerms.indexOf(localTerm);
			SearchTerm newLocalSearchTerm = null;
			
			// Is the local term already contained in the new list?
			if (indexNewLocal == -1) {
				// No, so add it
				newLocalSearchTerm = localTerm;
				newLocalSearchTerms.add(newLocalSearchTerm);
			}
			else {
				// Yes, so set the reference to it
				newLocalSearchTerm = newLocalSearchTerms.get(indexNewLocal);
			}
			
			// Does remote terms contain this local term?
			if (index == -1) {
				// The term was not contained remotely, so set the local term
				// to inactive
				newLocalSearchTerm.setActive(false);
			}
		}
		
		// Should we purge the new local search terms?
		if (removeInactive) {
			List<SearchTerm> newNewLocalSearchTerms = new LinkedList<SearchTerm>();
			
			// Iterate through all new search terms and use only the active ones
			for (SearchTerm localTerm : newLocalSearchTerms) {
				if (localTerm.isActive()) {
					newNewLocalSearchTerms.add(localTerm);
				}
			}
			
			newLocalSearchTerms = newNewLocalSearchTerms;
		}
		
		return newLocalSearchTerms;
	}
	
	/**
	 * Fills the short and long term lists.
	 * @param searchTerms The search terms to be divided into the two lists.
	 * @param shortTerms The list containing the short terms. This list will be filled, so it
	 * should be empty at the beginning.
	 * @param longTerms The list containing the long terms. This list will be filled, so it
	 * should be empty at the beginning.
	 * @param props The properties file that is used by the daemon.
	 */
	public static void fillShortAndLongList(List<SearchTerm> searchTerms, List<SearchTerm> shortTerms, List<SearchTerm> longTerms, DaemonProperties props) {
		for (SearchTerm term : searchTerms) {
			// First check, if the search term should be searched for
			if (!term.isSearchable(false))
				continue;
			
			// If the interval length of the term is set to / below the default interval length,
			// we have a term that needs many more requests, so it is considered to be a "long term".
			// Also if there is no old start (= very first iteration) we consider it long term
			// as there is very much data to be gathered first
			if (term.getIntervalLength().minus(props.defaultIntervalLength.getMillis()).getMillis() <= 0 || term.getOldStart() == null) {
				longTerms.add(term);
			}
			else {
				// Otherwise it is considered a "short term" that only needs very few reqeusts
				shortTerms.add(term);
			}
		}
	}
	
	/**
	 * Sorts the given terms by datetime in-place.
	 * @param terms The terms to be sorted.
	 */
	public static void sortListByDateTime(List<SearchTerm> terms) {
		Collections.sort(terms, new Comparator<SearchTerm>() {
			@Override
			public int compare(SearchTerm term1, SearchTerm term2) {
				// The minus is used because we want a descending order
				return (int)-term1.getWhenCreated().minus(term2.getWhenCreated().getMillis()).getMillis();
			}
		});
	}
	
	/**
	 * Returns only those search terms that are searchable (i. e. the interval length
	 * is reached).
	 * @param searchTerms The list of search terms to be checked.
	 * @return Only those search terms that are searchable.
	 */
	private List<SearchTerm> searchableSearchTerms(List<SearchTerm> searchTerms) {
		List<SearchTerm> terms = new LinkedList<SearchTerm>();
		
		for (SearchTerm term : searchTerms) {
			if (term.isSearchable(false)) {
				terms.add(term);
			}
		}
		
		return terms;
	}
	
	/**
	 * Creates a copy of terms and adds search terms at the end of the copy according to their
	 * expiration date and expiration factor.
	 * @param terms The list of search terms to be processed.
	 * @return Returns the processed list as a copy.
	 */
	public List<SearchTerm> fillUpList(List<SearchTerm> terms) {
		List<SearchTerm> filledList = new LinkedList<SearchTerm>();

		// Copy all terms into the list
		for (SearchTerm term : terms) {
			filledList.add(term);
		}
		
		DateTime now = new DateTime();
		
		// Add terms at the end of the list according to the expiration factor
		for (SearchTerm term : terms) {
			int i;
			boolean isYounger = true;
			
			// Test for expiration beginning with the oldest date and moving to the youngest
			for (i = _props.expirationDurations.length - 1; i >= 0 && isYounger; i--) {
				if (now.minus(term.getWhenCreated().getMillis()).getMillis() >= _props.expirationDurations[i].getMillis()) {
					isYounger = false;
				}
			}
			
			// If isYounger is still true, that means that the search term is younger than
			// _expirationDurations[0] (minimal expiration date), so we have to decrease i one
			// more time, because the for loop does not account for that.
			if (isYounger) {
				i--;
			}
		
			// Add terms at the end of the list according to the factor
			// We add 1 to i, because the upper for loop decreases i always by 1 too much (because
			// we do not break, but wait for the condition to fail.
			// We also add 1, because the array _expirationFactor has one more entry
			// than the _expirationDurations array. So we add 2 to i in total.
			for (int j = 0; j < _props.expirationFactor[i + 2] - 1; j++) {
				filledList.add(term);
			}
		}
		
		return filledList;
	}
	
	/**
	 * Returns the daemon properties used for this master.
	 * @return The daemon properties used for this master.
	 */
	public DaemonProperties getDaemonProperties() {
		return _props;
	}

	/**
	 * Returns the regression sentiment classifier.
	 * @return The regression sentiment classifier.
	 */
	public RegressionSentimentClassifier getRegressionSentimentClassifier() {
		return sentimentClassifier;
	}
	
	/**
	 * Returns the timer object for the treasurer.
	 * @return The timer object for the treasurer.
	 */
	public ResettableTimer getTreasurerTimer() {
		return _treasurerTimer;
	}
}
