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
import twitter4j.Status;
import com.daemon.database.SearchTerm;
import com.daemon.database.Transactor;
import com.tmetrics.util.Localization;

/**
 * The royal treasurer is responsible for storing the gathered tweets in the
 * treasury.
 * He has a higher priority than the normal treasurer and
 * can interrupt the normal treasurer if necessary.
 * @author Torsten, Jens
 */
public class RoyalTreasurer extends Treasurer {
	
	/**
	 * Creates a new instance of the royal treasurer.
	 * @param treasury The treasury that it used by the treasurer.
	 * @param bag The bag with tweets that the treasurer shall save.
	 */
	public RoyalTreasurer(final Treasury treasury, final List<Package> bag) {
		super(treasury, bag);
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
		
		// Enter the royal treasury.
		// If the chamber is already occupied by another royal treasurer, we have to wait.
		_treasury.royalTreasurerEnter(this);
		_treasury.working();
		try {
			transactor = new Transactor();
			transactor.connect();

			System.out.println(Localization.now() + " Royal Treasurer Entered the Royal Treasury.");
			System.out.println(Localization.now() + " Royal Treasurer We have " + Package.sizeOfAllPackages(_bag) + " tweets for " + Package.countDifferentSearchTerms(_bag) + " search terms in the bag to be stored.");
			
			List<Status> heap = new LinkedList<Status>();

			// Sort packages by date
			Collections.sort(_bag, Package.DATETIME_COMPARATOR);
			
			// Iterate over all search terms
			for (Package pack : _bag) {
				SearchTerm term = pack.getSearchTerm();
				List<Status> tweets = pack.getTweets();
				
				System.out.println(Localization.now() + " Royal Treasurer Storing " + tweets.size() + " tweets under '" + term.getTerm() + "' in steps of " + _maxHeapSize + ".");
				
				for (Status tweet : tweets) {
					heap.add(tweet);
					
					// Check if the stack is full and should be flushed into the
					// royal treasury
					if (heap.size() >= _maxHeapSize) {
						transactor.saveAllTweets(heap, term, _treasury.getOwner().getRegressionSentimentClassifier());
						heap.clear();
					}
				}
				
				if (heap.size() > 0) {
					// Flush the rest of the heap
					// and clear it afterwards(!)
					System.out.println(Localization.now() + " Royal Treasurer " + heap.size() + " tweets left in the bag. Storing them under '" + term.getTerm() + "' at once.");
					transactor.saveAllTweets(heap, term, _treasury.getOwner().getRegressionSentimentClassifier());
					heap.clear();
				}

				// Update the search term to the database
				transactor.updateSearchTerm(term);
			}
			
			System.out.println(Localization.now() + " Royal Treasurer Finished.");
		}
 		catch (Exception ex) {
			_logger.logStackTrace(ex);
			
			System.err.println(Localization.now() + " Royal Treasurer An error occured. Consult Treasurer.log for further information.");
		}
		finally {
			if (transactor != null)
				transactor.close();
			_treasury.workDone();
			// We are done. Leave the treasury and inform the normal treasurers (if any)
			_treasury.royalTreasurerLeave();
			
			_treasury.notifyTreasurer();
		}
	}
}
