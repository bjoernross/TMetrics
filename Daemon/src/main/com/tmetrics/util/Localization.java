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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.joda.time.Duration;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

/**
 * A utility class offering functionality for localization and time.
 * @author Torsten
 */
public class Localization {
	
	// UTC calendar
	public static Calendar UTC = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	
	// Local calendar
	public static Calendar LOCAL = Calendar.getInstance(TimeZone.getDefault());

	// Used for converting SQL Time to joda-time Duration
	public static final PeriodFormatter DURATION_FORMATTER = new PeriodFormatterBuilder()
			.printZeroIfSupported()
			.minimumPrintedDigits(2)
			.appendHours()
			.appendSeparator(":")
			.printZeroIfSupported()
			.minimumPrintedDigits(2)
			.appendMinutes()
			.appendSeparator(":")
			.printZeroIfSupported()
			.minimumPrintedDigits(2)
			.appendSeconds()
			.toFormatter();
	
	// Used for converting a datetime to a duration
	public static final PeriodFormatter DURATION_DATE_FORMATTER = new PeriodFormatterBuilder()
			.appendYears()
			.appendSeparator("-")
			.appendMonths()
			.appendSeparator("-")
			.appendDays()
			.appendSeparator(" ")
			.appendHours()
			.appendSeparator(":")
			.appendMinutes()
			.appendSeparator(":")
			.appendSeconds()
			.toFormatter();
	
	public static DateFormat DATEANDTIME_FORMATTER = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
	
	public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormat.forPattern("YYYY-MM-dd");
	
	/**
	 * Returns a stringified version of the given duration in the form of
	 * HH:mm:ss.
	 * @param duration The duration to be stringified.
	 * @return A stringified version of the given duration.
	 */
	public static String printDuration(Duration duration) {
		return Localization.DURATION_FORMATTER.print(duration.toPeriod());
	}
	
	/**
	 * Returns the stringified current time in the format YYYY-MM-dd HH:mm:ss.
	 * @return The stringified current time.
	 */
	public static String now() {
		return Localization.DATEANDTIME_FORMATTER.format(new Date());
	}
}
