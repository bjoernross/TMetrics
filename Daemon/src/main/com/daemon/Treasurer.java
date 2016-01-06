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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import com.daemon.database.SearchTerm;
import com.daemon.database.Transactor;
import com.tmetrics.logging.LogManager;
import com.tmetrics.logging.Logger;
import com.tmetrics.util.Localization;
import twitter4j.Status;

/**
 * The treasurer is responsible for storing the gathered tweets in the treasury.
 * He has a lesser priority than the royal treasurer and can be interrupted by
 * him.
 * @author Torsten, Jens
 */
public class Treasurer implements Runnable {
	
	protected List<Package> _bag = null;
	
	protected Treasury _treasury = null;
	
	protected Logger _logger = LogManager.getLogger("logs/Treasurer.log");
	
	protected int _maxHeapSize;
	
	protected static boolean _limitedMinionWorking = false;
	
	/**
	 * Creates a new instance of the royal treasurer.
	 * @param treasury The treasury that it used by the treasurer.
	 * @param bag The bag with tweets that the treasurer shall save.
	 */
	public Treasurer(final Treasury treasury, final List<Package> bag) {
		assert(treasury != null);
		assert(bag != null);
		
		_treasury = treasury;
		_bag = bag;
		
		_maxHeapSize = _treasury.getOwner().getDaemonProperties().maxHeapSize;
	}
	
	/**
	 * Executes the treasurer. He takes the bag with valuable tweets and tries to store them under the
	 * specific search term they are associated with. He does so by taking _maxHeapSize many tweets
	 * and storing them at once.
	 */
	@Override
	public void run() {
		Transactor transactor = null;
		
		// We have to enter the treasury no matter what, because we may have
		// some search terms with no tweets, but those terms have to be updated, too :(
		
		// Enter the treasury.
		// If the chamber is already occupied, we have to wait.
		_treasury.treasurerEnter(this);
		try {
			transactor = new Transactor();
			transactor.connect();

			System.out.println(Localization.now() + " Treasurer Entered the Treasury.");
			System.out.println(Localization.now() + " Treasurer We have " + Package.sizeOfAllPackages(_bag) + " tweets for " + Package.countDifferentSearchTerms(_bag) + " search terms in the bag to be stored.");
			
			List<Status> heap = new LinkedList<Status>();
			
			// Sort packages by date
			Collections.sort(_bag, Package.DATETIME_COMPARATOR);
			
			// Iterate over all packages
			for (Package pack : _bag) {
				SearchTerm term = pack.getSearchTerm();
				List<Status> tweets = pack.getTweets();
				
				System.out.println(Localization.now() + " Treasurer Storing " + tweets.size() + " tweets under '" + term.getTerm() + "' in steps of " + _maxHeapSize + ".");
				
				for (Status tweet : tweets) {
					heap.add(tweet);
					
					// is there a high priority treasurer waiting?
					while (_treasury.isRoyalTreasurerWaiting()) {
						System.out.println(Localization.now() + " Treasurer Interrupted by Royal Treasurer.");
						// go to sleep
						synchronized (this) {
							wait();	
						}
						System.out.println(Localization.now() + " Treasurer Awoken by Royal Treasurer.");
					}
					
					// Check if the stack is full and should be flushed into the
					// treasury
					if (heap.size() >= _maxHeapSize) {
						// no high priority treasurer is waiting so start working
						_treasury.working();						
						try {
							transactor.saveAllTweets(heap, term, _treasury.getOwner().getRegressionSentimentClassifier());
							heap.clear();	
						}
						finally {
							// stop working, in case of a high priority treasurer
							_treasury.workDone();
						}
					}
				}
				
				if (heap.size() > 0) {
					// no high priority treasurer is waiting so start working
					_treasury.working();
					try {
						// Flush the rest of the heap
						// and clear it afterwards(!)
						transactor.saveAllTweets(heap, term, _treasury.getOwner().getRegressionSentimentClassifier());
						heap.clear();
					}
					finally {
						// work is done.
						_treasury.workDone();
					}
				}

				// Update the search term to the database
				transactor.updateSearchTerm(term);
			}
			
			// Reset the treasurer timer as the current batch is finished and we do not need to start
			// a new treasurer immediately (this is more a memory fix than a "logic enhancement").
			_treasury.getOwner().getTreasurerTimer().reset();
			
			System.out.println(Localization.now() + " Treasurer Finished.");
		}
 		catch (Exception ex) {
			_logger.logStackTrace(ex);
			
			System.err.println(Localization.now() + " Treasurer An error occured. Consult Treasurer.log for further information.");
		}
		finally {
			if (transactor != null)
				transactor.close();
			
			// Leave the treasury
			_treasury.treasurerLeave();
		}
	}
}
