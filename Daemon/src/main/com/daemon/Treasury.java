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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The treasury. A thread-safe class for administrating and coordinating
 * (royal) treasurer.
 * @author Torsten, Jens
 */
public class Treasury {
	// Used because only one treasurer is allowed to enter the royal treasury at once
	private Lock _treasurerMutex = new ReentrantLock(true);

	// Used because only one high priority treasurer is allowed to enter the royal treasury at once
	private Lock _hpTreasurerMutex = new ReentrantLock(true);
	
	// Used by treasurers, so high priority treasurers know when they have to wait
	private Lock _isWorking = new ReentrantLock(true);

	private Queue<Treasurer> _normalTreasurers = new ConcurrentLinkedQueue<Treasurer>();
	
	private Queue<RoyalTreasurer> _hpTreasurers = new ConcurrentLinkedQueue<RoyalTreasurer>();
	
	private Master _owner;
	
	/**
	 * Creates a new treasury.
	 * @param owner The owner of the treasury.
	 */
	public Treasury(Master owner) {
		assert (owner != null);
		
		_owner = owner;
	}
	
	/**
	 * A treasurer wants to enter the treasury.
	 * If he cannot enter the treasury, he waits outside.
	 * @param t The treasurer that enters the treasury.
	 */
	public void treasurerEnter(Treasurer t) {
		// Important note: We first add a new treasurer, so that the treasury already
		// knows that there are more treasurers waiting. Just then we lock the mutex.
		
		// This is not the same lock-and-use-chain as in treasurerLeave (unlock-and-use). But because
		// we are using a queue and not a stack, this should hopefully be no issue.
		_normalTreasurers.add(t);
		
		_treasurerMutex.lock();
	}
	
	/**
	 * The treasurer that was working within the vaults of the
	 * treasury leaves. A waiting treasurer may now enter the treasury.
	 */
	public void treasurerLeave() {
		// Important note: We first remove the current treasurer and then unlock to prevent
		// that waiting treasurers instant relock the mutex. Because then, the current treasurer
		// that is still the first in the queue(!) would be used instead of the waiting treasurer.
		
		// This is not the same lock-and-use-chain as in treasurerEnter (use-and-lock). But because
		// we are using a queue and not a stack, this should hopefully be no issue.
		_normalTreasurers.poll();
		
		_treasurerMutex.unlock();
	}

	/**
	 * A treasurer wants to enter the treasury.
	 * If he cannot enter the treasury, he waits outside.
	 * @param t The royal treasurer that enters the treasury.
	 */
	public void royalTreasurerEnter(RoyalTreasurer t) {
		_hpTreasurerMutex.lock();
		_hpTreasurers.add(t);
	}
	
	/**
	 * The royal treasurer that was working within the vaults of the
	 * treasury leaves. A waiting treasurer may now enter the treasury.
	 */
	public void royalTreasurerLeave() {
		_hpTreasurers.poll();
		_hpTreasurerMutex.unlock();
	}
	
	/**
	 * Checks whether there is a royal treasurer waiting to enter the
	 * treasury.
	 * @return True, if a royal treasurer is waiting, false otherwise.
	 */
	public synchronized boolean isRoyalTreasurerWaiting() {
		return _hpTreasurers.size() > 0;
	}
	
	/**
	 * Returns the number of treasurers waiting / working in the treasury.
	 * @return The number of treasurers waiting / working in the treasury.
	 */
	public synchronized int countTreasuresWaiting() {
		return _normalTreasurers.size();
	}
	
	/**
	 * Someone is working within the vaults of the treasury.
	 */
	public void working() {
		_isWorking.lock();
	}
	
	/**
	 * The person working within the royal treasury is done with his work.
	 */
	public void workDone() {
		_isWorking.unlock();
	}
	
	/**
	 * Notifies a sleeping treasurer that he can restart working. The
	 * treasurer is worken up.
	 */
	public synchronized void notifyTreasurer() {
		if (_normalTreasurers.peek() != null) {
			synchronized (_normalTreasurers.peek()) {
				_normalTreasurers.peek().notifyAll();
			}
		}
	}
	
	/**
	 * Returns the owner of the treasury.
	 * @return The owner of the treasury.
	 */
	public Master getOwner() {
		return _owner;
	}
}
