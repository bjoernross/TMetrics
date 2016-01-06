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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.joda.time.Duration;

import com.tmetrics.util.Localization;

/**
 * A class representing the contents of a daemon properties file.
 * @author Torsten
 */
public class DaemonProperties {
	
	public Integer maxShortTermsCount;
	public Integer maxLongTermsCount;

	public Duration[] expirationDurations;
	
	public Integer[] expirationFactor;
	
	// The default interval length is set to Twitter's rate limit window
	public Duration defaultIntervalLength;

	// The buffer of twitter requests that should be still present after the minion
	// finished its requests.
	// E. g. we have 180 requests (as given by Twitter) but want a buffer of 30
	// requests. So the minion actually just starts 150 (= 180 - 30) requests.
	public Integer rateLimitBuffer;
	
	public Integer maxRateLimit;
	
	// The amount of tweets the bag of tweets can hold
	public Integer maxBagSize;
	
	// The amount of Tweets the Limited minion(instant search) will fetch
	// small values mean we get faster results (as storing them takes some time as well)
	public Integer limitSearchTerms;
	
	// how often should we wake up? in ms
	public Integer sleepDuration; 
	
	public Boolean spawnLimitedMinion;
	
	// The amount of time that has to pass before the bag is emptied (unless it has already
	// been emptied, because it overflowed)
	public Duration maxBagNotEmptiedDuration;
	
	public Duration profileResetTime;

	// Used to throttle the number of tweets that we want to fetch
	// each session for a "used-up" search term
	public Double throttleFactor;
	
	// Used to map user defined priorities to internal factors
	public Double[] priorityFactors;
	
	public Integer defaultPriorityIndex;
	
	public Duration maxIntervalLength;
	
	// The outlier factor is used, if we find only one tweet and have to update the
	// interval length to something by hand and not formula. This factor multiplies
	// the old interval length and yields the new interval length.
	public Integer outlierFactor;
	
	public Integer unlimitedRequestsPerSearchTerm;

	public Integer maxHeapSize;
	
	public Integer twitterRateLimit;
	
	public Integer maxTreasurers;
	
	/**
	 * Creates an empty properties "file"
	 */
	public DaemonProperties() {
	}
	
	/**
	 * Loads the given properties file.
	 * @param filename The properties file to be loaded.
	 * @throws IOException Thrown, if the file cannot be opened or properties are
	 * missing / in a bad format.
	 */
	public DaemonProperties(String filename) throws IOException {
		Properties props = new Properties();
		
		FileInputStream fis = new FileInputStream(filename);
		
		props.load(fis);
		
		fis.close();
		
		String maxShortTermsCountString = props.getProperty("minion.maxShortTermsCount");
		String maxLongTermsCountString = props.getProperty("minion.maxLongTermsCount");
		String expirationDurationsString = props.getProperty("minion.expirationDurations");
		String expirationFactorString = props.getProperty("minion.expirationFactor");
		String defaultIntervalLengthString = props.getProperty("minion.defaultIntervalLength");
		String rateLimitBufferString = props.getProperty("minion.rateLimitBuffer");
		String limitSearchTermsString = props.getProperty("minion.limitSearchTerms");
		String throttleFactorString = props.getProperty("minion.throttleFactor");
		String priorityFactorsString = props.getProperty("minion.priorityFactors");
		String defaultPriorityIndexString = props.getProperty("minion.defaultPriorityIndex");
		String maxIntervalLengthString = props.getProperty("minion.maxIntervalLength");
		String outlierFactorString = props.getProperty("minion.outlierFactor");
		String unlimitedRequestsPerSearchTermString = props.getProperty("minion.unlimitedRequestsPerSearchTerm");
		String twitterRateLimitString = props.getProperty("master.twitterRateLimit");
		String maxBagNotEmptiedDurationString = props.getProperty("master.maxBagNotEmptiedDuration");
		String spawnLimitedMinionString = props.getProperty("master.spawnLimitedMinion");
		String maxBagSizeString = props.getProperty("master.maxBagSize");
		String sleepDurationString = props.getProperty("master.sleepDuration");
		String profileResetTimeString = props.getProperty("master.profileResetTime");
		String maxTreasurersString = props.getProperty("master.maxTreasurers");
		String maxHeapSizeString = props.getProperty("treasurer.maxHeapSize");
		
		// Check for valid properties file
		if (maxShortTermsCountString == null ||
			maxLongTermsCountString == null ||
			expirationDurationsString == null ||
			expirationFactorString == null ||
			defaultIntervalLengthString == null ||
			rateLimitBufferString == null ||
			limitSearchTermsString == null ||
			throttleFactorString == null ||
			priorityFactorsString == null ||
			defaultPriorityIndexString == null ||
			maxIntervalLengthString == null ||
			outlierFactorString == null ||
			unlimitedRequestsPerSearchTermString == null ||
			twitterRateLimitString == null ||
			spawnLimitedMinionString == null ||
			maxBagNotEmptiedDurationString == null ||
			maxBagSizeString == null ||
			sleepDurationString == null ||
			profileResetTimeString == null ||
			maxTreasurersString == null ||
			maxHeapSizeString == null)
		{
			throw new IOException("At least one of the properties in " + filename + " does not contain valid information.");
		}
		
		// Minion properties
		maxShortTermsCount = Integer.parseInt(maxShortTermsCountString);
		maxLongTermsCount = Integer.parseInt(maxLongTermsCountString);
		expirationDurations = fillDurationArray(expirationDurationsString);
		expirationFactor = fillIntegerArray(expirationFactorString);
		defaultIntervalLength = Localization.DURATION_FORMATTER.parsePeriod(defaultIntervalLengthString).toStandardDuration();
		rateLimitBuffer = Integer.parseInt(rateLimitBufferString);
		limitSearchTerms = Integer.parseInt(limitSearchTermsString);
		throttleFactor = Double.parseDouble(throttleFactorString);
		priorityFactors = fillDoubleArray(priorityFactorsString);
		defaultPriorityIndex = Integer.parseInt(defaultPriorityIndexString);
		maxIntervalLength = Localization.DURATION_FORMATTER.parsePeriod(maxIntervalLengthString).toStandardDuration();
		outlierFactor = Integer.parseInt(outlierFactorString);
		unlimitedRequestsPerSearchTerm = Integer.parseInt(unlimitedRequestsPerSearchTermString);
		
		// Master properties
		spawnLimitedMinion = Integer.parseInt(spawnLimitedMinionString) != 0 ? true : false;
		twitterRateLimit = Integer.parseInt(twitterRateLimitString);
		maxBagNotEmptiedDuration = Localization.DURATION_FORMATTER.parsePeriod(maxBagNotEmptiedDurationString).toStandardDuration();
		maxBagSize = Integer.parseInt(maxBagSizeString);
		sleepDuration = Integer.parseInt(sleepDurationString);
		profileResetTime = Localization.DURATION_FORMATTER.parsePeriod(profileResetTimeString).toStandardDuration();
		
		// This is a derived property and is not present in the properties file
		maxRateLimit = twitterRateLimit - rateLimitBuffer;
		
		maxTreasurers = Integer.parseInt(maxTreasurersString);
		
		// Treasurer properties
		maxHeapSize = Integer.parseInt(maxHeapSizeString);
	}
	
	/**
	 * Returns a double array with the comma separated values of props.
	 * @param props Comma separated double values.
	 * @return A double array with the comma separated values of props.
	 */
	private Double[] fillDoubleArray(String props) {
		String[] entries = props.split(",");
		Double[] arr = new Double[entries.length];

		for(int i = 0; i < entries.length; i++) {
			arr[i] = Double.parseDouble(entries[i]);
		}
		
		return arr;
	}
	
	/**
	 * Returns an integer array with the comma separated values of props.
	 * @param props Comma separated integer values.
	 * @return An integer array with the comma separated values of props.
	 */
	private Integer[] fillIntegerArray(String props) {
		String[] entries = props.split(",");
		Integer[] arr = new Integer[entries.length];

		for(int i = 0; i < entries.length; i++) {
			arr[i] = Integer.parseInt(entries[i]);
		}
		
		return arr;
	}	
	
	/**
	 * Returns a Duration array with the comma separated values of props.
	 * @param props Comma separated Duration values.
	 * @return A Duration array with the comma separated values of props.
	 */
	private Duration[] fillDurationArray(String props) {
		String[] entries = props.split(",");
		Duration[] arr = new Duration[entries.length];

		for(int i = 0; i < entries.length; i++) {
			arr[i] = Localization.DURATION_DATE_FORMATTER.parsePeriod(entries[i]).toStandardDuration();
		}
		
		return arr;
	}
}
