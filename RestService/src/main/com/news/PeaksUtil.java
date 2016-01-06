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
package com.news;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

import org.joda.time.LocalDateTime;

import com.restservice.dto.CountAndNewsPerHour;
import com.restservice.dto.CountPeaksNewsAndDate;

/**
 * @author olaf
 * 
 */
public class PeaksUtil {

	private static final String PEAKS_PROPERTIES_FILE_PATH = System
			.getProperty("user.home") + "/peaks.properties";

	public static Double PEAKTHRESHOLDALPHA = 0.8;

	public static Integer MINDATACOUNTFORPEAKS = 2;

	/**
	 * properties file reading (or writing if not exists)
	 */
	static {
		// try to open and read properties file
		Properties props = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(PEAKS_PROPERTIES_FILE_PATH);
			props.load(fis);

			String peakthresholdalpha = props
					.getProperty("peaks.peakthresholdalpha");
			String mindatacountforpeaks = props
					.getProperty("peaks.mindatacountforpeaks");

			PEAKTHRESHOLDALPHA = Double.parseDouble(peakthresholdalpha);
			MINDATACOUNTFORPEAKS = Integer.parseInt(mindatacountforpeaks);
		} catch (Exception e) {
			// try to write properties file
			System.out.println("Cannot load peaks properties from path "
					+ PEAKS_PROPERTIES_FILE_PATH
					+ ". Using default values, creating default file.");
			e.printStackTrace();
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(PEAKS_PROPERTIES_FILE_PATH);
				props.setProperty("peaks.peakthresholdalpha",
						PEAKTHRESHOLDALPHA.toString());
				props.setProperty("peaks.mindatacountforpeaks",
						MINDATACOUNTFORPEAKS.toString());
				props.store(fos, "Peaks Properties");
			} catch (Exception e2) {
				// or just use default values
				System.out.println("Cannot create peaaks properties at path "
						+ PEAKS_PROPERTIES_FILE_PATH + ".");
				e2.printStackTrace();
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (Exception e3) {
						e3.printStackTrace();
					}
				}
			}

		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Searches peaks in a list of integers. Peaks defined as the maximum of a
	 * coherent set of values that are above an percentage of all values.
	 * 
	 * @param counts
	 *            list of Integers
	 * @return list of indices where peaks were identified
	 */
	public static ArrayList<Integer> findPeaks(ArrayList<Integer> counts) {
		ArrayList<Integer> peaks = new ArrayList<Integer>();
		if (counts == null)
			return peaks;
		if (counts.size() < MINDATACOUNTFORPEAKS)
			return peaks;
		// calculate global peak threshold
		@SuppressWarnings("unchecked")
		ArrayList<Integer> sortedCounts = (ArrayList<Integer>) counts.clone();
		Collections.sort(sortedCounts);
		Integer threshold = sortedCounts.get((int) Math.floor((sortedCounts
				.size() - 1) * PEAKTHRESHOLDALPHA));
		// find connected values above the threshold (peaks)
		Integer leftPeaklimiter = 0;
		Integer rightPeaklimiter = 0;
		while (leftPeaklimiter < counts.size()) {
			// find a left peak limit
			if (counts.get(leftPeaklimiter) < threshold) {
				leftPeaklimiter++;
				continue;
			}
			// find connected right peak limit
			rightPeaklimiter = leftPeaklimiter;
			while (rightPeaklimiter + 1 < counts.size()
					&& counts.get(rightPeaklimiter + 1) >= threshold)
				rightPeaklimiter++;
			// find peak's max
			Integer peakMax = 0;
			Integer peakMaxIndex = 0;
			for (Integer i = leftPeaklimiter; i <= rightPeaklimiter; i++) {
				if (counts.get(i) > peakMax) {
					peakMax = counts.get(i);
					peakMaxIndex = i;
				}
			}
			peaks.add(peakMaxIndex);
			// continue search for peaks
			leftPeaklimiter = rightPeaklimiter + 1;
		}
		return peaks;
	}

	public static ArrayList<Integer> findPeaks24(
			CountAndNewsPerHour countPerHour) {
		ArrayList<Integer> peaks = new ArrayList<Integer>();
		ArrayList<Integer> peakDays = new ArrayList<Integer>();
		if (countPerHour == null)
			return peaks;
		if (countPerHour.getGraph() == null)
			return peaks;
		if (countPerHour.getGraph().size() < MINDATACOUNTFORPEAKS)
			return peaks;
		// add first hours of day before first value (fill from midnight to
		// first value)
		CountAndNewsPerHour newCountPerHour = new CountAndNewsPerHour();
		Integer dummyEntryCount = countPerHour.getGraph().get(0).getRawDate()
				.getHourOfDay();
		for (Integer i = dummyEntryCount; i > 0; i--) {
			LocalDateTime dummyDate = ((LocalDateTime) countPerHour.getGraph()
					.get(0).getRawDate()).minusHours(i);
			CountPeaksNewsAndDate dummy = new CountPeaksNewsAndDate();
			dummy.setCount(0);
			dummy.setRawDate(dummyDate);
			dummy.setPeak(false);
			newCountPerHour.getGraph().add(dummy);
		}

		newCountPerHour.getGraph().addAll(countPerHour.getGraph());

		// get daily maximums
		ArrayList<Integer> countPerDay = new ArrayList<Integer>();
		Integer currentDayIndex = -1;
		for (Integer i = 0; i < newCountPerHour.getGraph().size(); i++) {
			CountPeaksNewsAndDate entry = newCountPerHour.getGraph().get(i);
			if (i % 24 == 0) {
				countPerDay.add(0);
				currentDayIndex++;
			}
			if (entry.getCount() > countPerDay.get(currentDayIndex)) {
				countPerDay.set(currentDayIndex, entry.getCount());
			}
		}
		// find peak days
		peakDays = findPeaks(countPerDay);
		// find day's peak of peak days
		for (Integer peakDayIndex : peakDays) {
			Integer startHourIndex = peakDayIndex * 24;
			Integer maxValue = 0;
			Integer maxValueIndex = startHourIndex;
			for (Integer i = 0; i < 24; i++) {
				Integer index = startHourIndex + i;
				if (index < newCountPerHour.getGraph().size()) {
					Integer value = newCountPerHour.getGraph().get(index)
							.getCount();
					if (value >= maxValue) {
						maxValue = value;
						maxValueIndex = index;
					}
				}
			}
			peaks.add(maxValueIndex - dummyEntryCount);
		}
		return peaks;
	}
}
