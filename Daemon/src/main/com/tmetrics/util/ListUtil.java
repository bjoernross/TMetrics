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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.daemon.Package;

/**
 * Helper class to provide methods for lists.
 * 
 * @author Erwin, Björn, Torsten
 */
public class ListUtil {

	/**
	 * Vector subtraction
	 * 
	 * Modifies the first parameter but also returns it for convenience. Vectors
	 * must be of same length.
	 * 
	 * @param a
	 *            Vector to subtract from
	 * @param b
	 *            Vector to subtract
	 * @return Result
	 */
	public static List<Float> subtract(List<Float> a, List<Float> b) {
		if (a.size() != b.size()) {
			throw new IllegalArgumentException("Length of vector a ("
					+ a.size() + ") does not equal length of vector b ("
					+ b.size() + ")");
		}
		List<Float> c = new ArrayList<Float>();

		for (int i = 0; i < a.size(); i++) {
			c.add(a.get(i) - b.get(i));
		}
		return c;
	}

	/**
	 * Scalar multiplication. Modifies the first parameter but also returns it
	 * for convenience.
	 * 
	 * @param a
	 *            Vector
	 * @param f
	 *            Scalar
	 * @return Result
	 */
	public static List<Float> multiply(List<Float> a, Float f) {
		List<Float> result = new ArrayList<Float>();
		for (int i = 0; i < a.size(); i++) {
			result.add(a.get(i) * f);
		}
		return result;
	}

	/**
	 * Scalar product of two vectors
	 * 
	 * Be careful: Null entries are treated as 0! If one vector is longer than
	 * the other, higher positions are be ignored
	 * 
	 * @param a
	 *            First vector to multiply
	 * @param b
	 *            Second vector to multiply
	 * @return Scalar product
	 */
	public static float scalarProduct(List<Float> a, List<Float> b) {
		float sum = 0;
		for (int i = 0; i < a.size() && i < b.size(); i++) {
			if (a.get(i) != null && b.get(i) != null) {
				sum += a.get(i) * b.get(i);
			}

		}
		return sum;
	}

	/**
	 * Create a List of the specified size filled with null, in order to be able
	 * to use set() to set elements in arbitrary order.
	 * 
	 * @param numberOfElements
	 * @return
	 */
	public static <T> List<T> createNullList(int numberOfElements) {
		List<T> list = new ArrayList<T>(numberOfElements);
		for (int j = 0; j < numberOfElements; j++) {
			list.add(null);
		}
		return list;

	}

	/**
	 * Mean of the squares of the elements of a vector, e.g. for mean squared
	 * error computation
	 * 
	 * @param vector
	 *            Vector whose elements are to be squared and summed
	 * @return Sum of squares of vector elements
	 */
	public static float meanSquared(List<Float> vector) {
		Float squareSum = (float) 0;
		for (Float element : vector) {
			squareSum += element * element;
		}
		return squareSum / vector.size();
	}

	/**
	 * Arithmetic Mean of absolute values of Vector Elements, e.g. for mean
	 * absolute error computation
	 * 
	 * @param vector
	 *            Vector whose elements are to be averaged
	 * @return Mean
	 */
	public static float meanAbsolute(List<Float> vector) {
		Float sum = (float) 0;
		for (Float element : vector) {
			// sum += (element);
			sum += Math.abs(element);
		}
		return sum / vector.size();
	}
	
	/**
	 * Checks whether the given value is contained within the list
	 * (check by value or reference).
	 * @param list The list to be checked.
	 * @param value The value to be found. Either by value or reference.
	 * @return Return true if the value is contained within the list, false otherwise.
	 */
	public static <T> boolean contains(final List<T> list, T value) {
		for (final T elem : list) {
			if (elem == value || value != null && value.equals(elem)) {
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Clones the given package list (flat).
	 * @param packages Package list to be cloned.
	 * @return A (flat) cloned version of the given package list.
	 */
	public static List<Package> flatClone(List<Package> packages) {
		List<Package> copy = new LinkedList<Package>();
		
		for (Package pack : packages) {
			copy.add(pack);
		}
		
		return copy;
	}
}