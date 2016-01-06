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
package com.tmetrics.logging;

import java.util.HashMap;
import java.util.Map;

/**
 * Used for getting a (new) logger.
 * @author Torsten
 */
public class LogManager {
	private static Map<String, Logger> _loggerMap = new HashMap<String, Logger>();

	/**
	 * Returns the logging with the given name.
	 * @param filename The logger to be received.
	 * @return The logging with the given name.
	 */
	public static Logger getLogger(String filename) {
		if (!_loggerMap.containsKey(filename)) {
			_loggerMap.put(filename, new Logger(filename));
		}
		return _loggerMap.get(filename);
	}
	
	/**
	 * Returns the logging with the given name.
	 * @param filename The logger to be received.
	 * @param maxEntries The number of maximal entries to be stored. Note: Entry means line.
	 * @param truncEntries The number of entries / lines to be truncated from the front of
	 * the file.
	 * @return The logging with the given name.
	 */
	public static Logger getLogger(String filename, int maxEntries, int truncEntries) {
		if (!_loggerMap.containsKey(filename)) {
			_loggerMap.put(filename, new Logger(filename, maxEntries, truncEntries));
		}
		return _loggerMap.get(filename);
	}
}
