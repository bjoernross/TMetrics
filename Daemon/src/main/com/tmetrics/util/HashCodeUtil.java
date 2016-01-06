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

/**
 * A helper class for generating hash codes.
 * @author Torsten, Jens
 */
public class HashCodeUtil {
	private static final int PRIME_NUMBER = (2 << 16) - 1;
	
	public static final int SEED = 99991;
	
	/**
	 * Generates a start hash code.
	 * @param seed The seed used for the start.
	 * @return A start hash code.
	 */
	private static int first(int seed) {
		return seed * PRIME_NUMBER;
	}
	
	/**
	 * Returns a new hash code depending on the seed and toHash.
	 * @param seed The seed to be used.
	 * @param toHash The integer value to be hashed.
	 * @return
	 */
	public static int hash(int seed, int toHash) {
		return first(seed) + toHash;
	}
}
