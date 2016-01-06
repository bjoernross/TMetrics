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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a sparse matrix (a matrix that has a large amount of zeroes)
 * somewhat efficiently by only saving values that do not equal zero.
 * 
 * @author Erwin, Björn
 */
public class SparseMatrix implements Serializable {

	private static final long serialVersionUID = 1L;

	// the matrix is represented as an ArrayList of ArrayLists (rows) of
	// 2-tuples: (column, value)
	private ArrayList<ArrayList<ColumnValueTuple>> matrix;

	// the number of columns is difficult to compute, so we'll keep track of it
	// manually
	private int ncol;

	/**
	 * Initialize the matrix
	 */
	public SparseMatrix() {
		this.matrix = new ArrayList<ArrayList<ColumnValueTuple>>();
		this.ncol = 0;
	}

	/**
	 * Initialize the matrix with a specific number of columns
	 */
	public SparseMatrix(int ncol) {
		this.matrix = new ArrayList<ArrayList<ColumnValueTuple>>();
		this.ncol = ncol;
	}

	/**
	 * Get number of rows
	 * 
	 * @return
	 */
	public int nrow() {
		return matrix.size();
	}

	/**
	 * Get number of columns
	 * 
	 * @return
	 */
	public int ncol() {
		return ncol;
	}

	/**
	 * Set a value in the matrix, growing it if necessary.
	 * 
	 * Do not explicitly set values to zero because it is a waste of space; all
	 * values are initialized as zero anyway. Unless, of course, you want the
	 * matrix to grow to that size, or want to set a value to zero that had
	 * another value before.
	 * 
	 * 
	 * @param i
	 *            row
	 * @param j
	 *            column
	 * @param element
	 */
	public void set(int i, int j, float element) {
		// @Nice-To-Have: handle inserted zeroes better

		ArrayList<ColumnValueTuple> row;

		// check if row i exists
		if (i < matrix.size()) {
			// if it does, get the row
			row = matrix.get(i);

			// insert the element if column not exists
			boolean ifFound = false;
			for (ColumnValueTuple f : row) {
				if (f.column == j) {
					f.value = element;
					ifFound = true;
				}
			}
			if (!ifFound) {
				row.add(new ColumnValueTuple(j, element));
			}

			// save the modified row
			// matrix.set(i, row);
		} else {

			// if not, add new empty rows until i is reached
			while (i > matrix.size()) {
				matrix.add(new ArrayList<ColumnValueTuple>());
			}

			// then, create the row, add the element and and add the row to the
			// matrix
			row = new ArrayList<ColumnValueTuple>();
			row.add(new ColumnValueTuple(j, element));
			matrix.add(row);
		}

		// keep track of the column count:
		// if the column did not exist before, increment column count
		if (ncol <= j) {
			ncol = j + 1;
		}
	}

	/**
	 * Get the element at the specified position
	 * 
	 * @param i
	 *            row
	 * @param j
	 *            column
	 * @return the element at position (i,j)
	 * @throws IndexOutOfBoundsException
	 *             if position does not exists.
	 */
	public Float get(int i, int j) throws IndexOutOfBoundsException {

		// get row i
		ArrayList<ColumnValueTuple> row = matrix.get(i);

		// find column j
		for (ColumnValueTuple f : row) {
			if (f.column == j) {
				return f.value;
			}
		}

		// could not find position, out of bounds...
		throw new IndexOutOfBoundsException(
				"could not access in sparse matrix at position " + i + "-" + j);

	}

	/**
	 * Multiply the matrix with a vector
	 * 
	 * The vector is multiplied from the right, i. e. this method computes M *
	 * v, or t(v) * M where t(v) is the transpose of v.
	 * 
	 * The matrix must have exactly as many columns as the vector has elements,
	 * otherwise an exception is thrown
	 * 
	 * @param vector
	 * @return
	 */
	public List<Float> multiply(List<Float> vector) {
		if (vector.size() != ncol) {
			throw new IllegalArgumentException("Vector size of "
					+ vector.size() + " differs from matrix column count of "
					+ ncol);
		}
		List<Float> result = new ArrayList<Float>();
		Float currentSum = (float) 0.0;

		// we don't actually transpose here. we just walk through the rows
		// instead of the columns
		for (List<ColumnValueTuple> row : matrix) {
			// sum of row times vector
			currentSum = (float) 0;
			for (ColumnValueTuple f : row) {
				currentSum += f.getValue() * vector.get(f.getColumn());
			}
			result.add(currentSum);
		}

		return result;
	}

	/**
	 * Calculates the Quiring product of the matrix and a vector.
	 * 
	 * The Quiring product is defined as follows: For a matrix with n rows and a
	 * vector of length n, perform a scalar multiplication (vector times scalar)
	 * by multiplying the matrix row with the index i with the corresponding
	 * vector element at position i for all i so that 0 < i < n.
	 * 
	 * The Quiring product is the vector of column sums of the resulting matrix.
	 * 
	 * The Quiring product in named in honor of Erwin Quiring, who stated that
	 * there is no name for this function. Now there is.
	 * 
	 * @return Quiring product of the matrix and a vector
	 */
	public List<Float> quiringProduct(List<Float> vector) {
		if (matrix.size() != vector.size()) {
			throw new IllegalArgumentException("Vector size of "
					+ vector.size() + " differs from matrix row number of "
					+ matrix.size());
		}
		List<Float> result = new ArrayList<Float>();
		for (int i = 0; i < ncol(); i++) {
			result.add((float) 0);
		}
		int currentRow = 0;
		float product = 0;
		for (List<ColumnValueTuple> row : matrix) {
			for (ColumnValueTuple f : row) {
				product = vector.get(currentRow) * f.getValue();
				result.set(f.getColumn(), result.get(f.getColumn()) + product);
			}
			currentRow++;
		}
		return result;
	}

	/**
	 * Concatenates the current matrix and another matrix by adding the rows of
	 * the other matrix to the current one
	 * 
	 * If the number of columns differs, new columns are implicitly initialised
	 * with default values of zero for existing rows
	 * 
	 * @param matrix2
	 * @return
	 */
	public void concatenateVertically(SparseMatrix matrix2) {
		// System.out.println("Before concat: " + this);
		// System.out.println("Matrix 2 : " + matrix2);
		this.matrix.addAll(matrix2.matrix);
		// for (ArrayList<ColumnValueTuple> row : matrix2.matrix) {
		// this.matrix.add(row);
		// }
		// keep track of column count
		ncol = Math.max(ncol, matrix2.ncol);
		// System.out.println("Result of concat: " + this);
	}

	@Override
	/**
	 * Get a string representation. Caution: Can be very large for large matrices! 
	 * 
	 * @return String representation
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		final String empty = "0\t";
		List<Float> rowTemp;

		if (nrow() == 0 || ncol() == 0) {
			return "Matrix is empty, has " + nrow() + " rows and " + ncol()
					+ " columns.";
		}

		for (List<ColumnValueTuple> row : matrix) {
			// fill empty rows with ncol zeroes and move on to the next row
			if (row.size() == 0) {
				for (int i = 0; i <= ncol; i++) {
					sb.append(empty);
				}
				sb.append("\n");
				continue;
			}
			rowTemp = ListUtil.createNullList(ncol);

			// rows that are not empty
			for (ColumnValueTuple f : row) {
				rowTemp.set(f.getColumn(), f.getValue());
			}

			for (Float element : rowTemp) {
				if (element == null) {
					sb.append(empty);
				} else {
					sb.append(element + "\t");
				}
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * Converts the matrix into a two-dimensional array
	 * 
	 * A very bad idea for big matrices!
	 * 
	 * @return
	 */
	public float[][] toArray() {
		float[][] array = new float[matrix.size()][ncol];
		for (int i = 0; i < matrix.size(); i++) {
			for (ColumnValueTuple tuple : matrix.get(i)) {
				array[i][tuple.getColumn()] = tuple.getValue();
			}
		}
		return array;
	}

	/**
	 * Get a column of the matrix as a list of Floats
	 * 
	 * @param column
	 *            index of the column
	 * @return List of Floats with elements
	 */
	public List<Float> getColumnAsList(int column) {

		// create a temporary array filled with zeroes
		float[] array = new float[nrow()];
		for (int i = 0; i < matrix.size(); i++) {
			for (ColumnValueTuple tuple : matrix.get(i)) {
				if (tuple.getColumn() == column) {
					array[i] = tuple.getValue();
				}
			}
		}

		// copy array contents into list
		List<Float> list = new ArrayList<Float>();
		for (float f : array) {
			list.add(f);
		}
		return list;
	}

	/**
	 * Get a row of the matrix as a list of Floats
	 * 
	 * @param row
	 *            index of the row
	 * @return List of Floats with elements
	 */
	public List<Float> getRowAsList(int row) {

		// create a temporary array filled with zeroes
		float[] array = new float[ncol];
		for (ColumnValueTuple tuple : matrix.get(row)) {
			array[tuple.getColumn()] = tuple.getValue();
		}

		// copy array contents into list
		List<Float> list = new ArrayList<Float>();
		for (float f : array) {
			list.add(f);
		}
		return list;
	}

	/**
	 * Returns the sum of values of the given row
	 * @param row
	 * @return
	 */
	public float rowSum(int row) {
		float sum = 0;
		for (ColumnValueTuple tuple : matrix.get(row)) {
			sum += tuple.getValue();
		}
		return sum;
	}
	
	/**
	 * Divides each row value of given row by the given value
	 * @param value
	 * @param row
	 */
	public void divideEachRowValueBy(float value, int row) {
		for (ColumnValueTuple tuple : matrix.get(row)) {
			tuple.setValue(tuple.getValue() / value);
		}
	}
	
	/**
	 * Represents a column-value tuple in the matrix. To be used in the row list
	 * 
	 * @author Erwin, Björn
	 */
	private class ColumnValueTuple implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private int column;
		private Float value;

		/**
		 * Create a new column value tuple
		 * 
		 * @param column
		 * @param value
		 */
		ColumnValueTuple(int column, Float value) {
			this.column = column;
			this.value = value;
		}

		/**
		 * Get column
		 * 
		 * @return column index
		 */
		int getColumn() {
			return column;
		}

		/**
		 * Get value
		 * 
		 * @return value
		 */
		Float getValue() {
			return value;
		}
		
		/**
		 * Set value
		 */
		void setValue(Float val) {
			this.value = val;
		}
	}

}