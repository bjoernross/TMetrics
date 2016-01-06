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
package com.news.test;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import com.news.PeaksUtil;

/**
 * Tests the PeaksUtil to detect peaks, without marking every value, and (if
 * present) at least marking 8/50 values that are local or global max
 * 
 * @author olaf
 * 
 */
public class PeaksUtilTest {

	private ArrayList<Integer> testSetRegular = new ArrayList<Integer>();
	private ArrayList<Integer> testSetStartPeak = new ArrayList<Integer>();
	private ArrayList<Integer> testSetEndPeak = new ArrayList<Integer>();
	private ArrayList<Integer> testSetPeakMaxAtPeakStart = new ArrayList<Integer>();
	private ArrayList<Integer> testSetPeakMaxAtPeakEnd = new ArrayList<Integer>();
	private ArrayList<Integer> testSetShortLeftPeak = new ArrayList<Integer>();
	private ArrayList<Integer> testSetShortRightPeak = new ArrayList<Integer>();
	private ArrayList<Integer> testSetRegularSolution = new ArrayList<Integer>();
	private ArrayList<Integer> testSetStartPeakSolution = new ArrayList<Integer>();
	private ArrayList<Integer> testSetEndPeakSolution = new ArrayList<Integer>();
	private ArrayList<Integer> testSetPeakMaxAtPeakStartSolution = new ArrayList<Integer>();
	private ArrayList<Integer> testSetPeakMaxAtPeakEndSolution = new ArrayList<Integer>();
	private ArrayList<Integer> testSetShortLeftPeakSolution = new ArrayList<Integer>();
	private ArrayList<Integer> testSetShortRightPeakSolution = new ArrayList<Integer>();

	@Before
	public void setUp() throws Exception {
		// Generate TestSets
		PeaksUtil.MINDATACOUNTFORPEAKS = 2;
		PeaksUtil.PEAKTHRESHOLDALPHA = 0.95;
		for (Integer i = 0; i < 50; i++) {
			testSetEndPeak.add((int) Math.round(Math.exp((i + 9.) / 10))
					+ ((i == 25) ? 220 : 0));
			testSetStartPeak.add((int) Math.round(Math.exp((58. - i) / 10))
					+ ((i == 25) ? 220 : 0));
			testSetPeakMaxAtPeakStart.add(((i >= 10 && i <= 20) ? 1 : 0)
					+ ((i == 10) ? 1 : 0));
			testSetPeakMaxAtPeakEnd.add(((i >= 10 && i <= 20) ? 1 : 0)
					+ ((i == 20) ? 1 : 0));
			testSetRegular.add(((i % 6 == 0) ? 60 : 0) + ((i % 6 == 1) ? 80 : 0)
					+ ((i % 6 == 2) ? 100 : 0) + ((i % 6 == 3) ? 80 : 0)
					+ ((i % 6 == 4) ? 60 : 0) + i);
		}
		testSetShortLeftPeak.add(2);
		testSetShortLeftPeak.add(1);
		testSetShortRightPeak.add(1);
		testSetShortRightPeak.add(2);
		// List Solutions
		testSetStartPeakSolution.add(0);
		testSetStartPeakSolution.add(25);
		testSetEndPeakSolution.add(49);
		testSetEndPeakSolution.add(25);
		testSetPeakMaxAtPeakStartSolution.add(10);
		testSetPeakMaxAtPeakEndSolution.add(20);
//		testSetRegularSolution.add(2);
//		testSetRegularSolution.add(8);
//		testSetRegularSolution.add(14);
//		testSetRegularSolution.add(20);
//		testSetRegularSolution.add(26);
		testSetRegularSolution.add(32);
		testSetRegularSolution.add(38);
		testSetRegularSolution.add(44);
		testSetRegularSolution.add(49);
		testSetShortLeftPeakSolution.add(0);
		testSetShortRightPeakSolution.add(1);
	}

	private void assertPeakResult(ArrayList<Integer> testSet,
			ArrayList<Integer> solution, String name) {
		// get result
		ArrayList<Integer> result = PeaksUtil.findPeaks(testSet);
		// at least the solution values should be detected
		for (Integer s : solution) {
			Assert.assertTrue("In testSet " + name
					+ " a peak wasn't detected at position " + s,
					result.contains(s));
		}
		// not all values should be marked as peaks
		if (testSet.size() > 1) {
			Assert.assertTrue(
					"In testset "
							+ name
							+ " the list of peaks should be shorter than list of entrys",
					result.size() < testSet.size());
		}
	}

	@After
	public void tearDown() throws Exception {
	}

	/*
	 * regular input
	 */
	@Test
	public void testFindPeaksOnRegularInput() {
		assertPeakResult(testSetRegular, testSetRegularSolution, "RegularInput");
	}

	/*
	 * regular input: peak at first index
	 */
	@Test
	public void testFindPeaksOnStartPeakInput() {
		assertPeakResult(testSetStartPeak, testSetStartPeakSolution,
				"StartPeakInput");
	}

	/*
	 * regular input: peak at last index
	 */
	@Test
	public void testFindPeaksOnPeakMaxAtPeakStartInput() {
		assertPeakResult(testSetPeakMaxAtPeakStart,
				testSetPeakMaxAtPeakStartSolution, "PeakMaxAtPeakStartInput");
	}

	/*
	 * regular input: peakmax at first peak index
	 */
	@Test
	public void testFindPeaksOntestSetPeakMaxAtPeakEndInput() {
		assertPeakResult(testSetPeakMaxAtPeakEnd,
				testSetPeakMaxAtPeakEndSolution, "PeakMaxAtPeakEndInput");
	}

	/*
	 * regular input: peakmax at last peak index
	 */
	@Test
	public void testFindPeaksOnEndPeakInput() {
		assertPeakResult(testSetEndPeak, testSetEndPeakSolution, "EndPeakInput");
	}
	
	/*
	 * regular input: short lists
	 */
	@Test
	public void testFindPeaksOnShortInput() {
		assertPeakResult(testSetShortLeftPeak, testSetShortLeftPeakSolution, "ShortLeftPeakInput");
		assertPeakResult(testSetShortRightPeak, testSetShortRightPeakSolution, "ShortRightPeakInput");
	}

	/*
	 * unusual input: empty list
	 */
	@Test
	public void testEmptyListInput() {
		Assert.assertTrue(
				"An empty inputlist should return an empty list as result",
				PeaksUtil.findPeaks(new ArrayList<Integer>()).size() == 0);
	}

	/*
	 * unusual input: null
	 */
	@Test
	public void testNullInput() {
		Assert.assertTrue("A null input should return an empty list as result",
				PeaksUtil.findPeaks(null).size() == 0);
	}
}
