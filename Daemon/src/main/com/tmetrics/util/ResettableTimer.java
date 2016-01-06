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
package com.tmetrics.util;

import java.util.Timer;
import java.util.TimerTask;

/**
 * A resettable timer that always repeats its given task.
 * @author Torsten
 */
public class ResettableTimer {
	private Timer _timer = null;
	
	private Runnable _task = null;
	
	private TimerTask _timerTask = null;
	
	private long _period = 0;
	
	/**
	 * Creates a new resettable timer object.
	 * @param task The task to be executed after the timer expires.
	 * @param period The time in milliseconds after which the timer expires.
	 */
	public ResettableTimer(Runnable task, long period) {
		_timer = new Timer();
		_task = task;
		_period = period;
		_timerTask = new TimerTask() { public void run() { _task.run(); } };
	}

	/**
	 * Starts the timer.
	 */
	public void start() {
		_timer.schedule(_timerTask, _period, _period);
	}
	
	/**
	 * Resets the current timer and restarts it.
	 */
	public void reset() {
		_timerTask.cancel();
		_timerTask = new TimerTask() { public void run() { _task.run(); } };
		_timer.schedule(_timerTask, _period, _period);
	}
	
	/**
	 * Stops the timer.
	 */
	public void stop() {
		_timerTask.cancel();
		_timerTask = new TimerTask() { public void run() { _task.run(); } };
	}
}
