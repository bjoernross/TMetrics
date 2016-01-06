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
import java.util.concurrent.locks.Lock;
import com.tmetrics.util.ListUtil;

/**
 * A task for summoning the treasurer to store tweets.
 * 
 * @author Torsten
 */
public class SummonTreasurerTask implements Runnable {

	private Lock _mutex = null;
	
	private Treasury _treasury = null;
	
	private List<Package> _bag = null;
	
	/**
	 * Creates a new treasurer task.
	 * @param mutex Mutex that the master also uses.
	 * @param bag The bag of tweets to be stored.
	 */
	public SummonTreasurerTask(Lock mutex, Treasury treasury, List<Package> bag) {
		assert(mutex != null);
		assert(treasury != null);
		assert(bag != null);
		
		_mutex = mutex;
		_treasury = treasury;
		_bag = bag;
	}

	/**
	 * Executes the treasurer. He takes the bag with valuable tweets and tries to store them under the
	 * specific search term they are associated with. He does so by taking all tweets within the bag
	 * to the treasury.
	 */
	@Override
	public void run() {
		// Check if there are any tweets in the bag. If not, do nothing
		if (Package.sizeOfAllPackages(_bag) == 0) {
			return;
		}
		
		// Lock the access to the bag because the master might be working with it
		_mutex.lock();
		
		List<Package> copyOfBag = ListUtil.flatClone(_bag);
		
		new Thread(new Treasurer(_treasury, copyOfBag)).start();
		
		// Finally clear the bag as the content is now in the greedy hands of
		// the royal treasurer
		_bag.clear();

		// Unlock as we are done updating
		_mutex.unlock();
	}
}
