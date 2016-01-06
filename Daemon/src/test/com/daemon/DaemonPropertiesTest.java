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

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;

import org.apache.ibatis.io.Resources;
import org.joda.time.Duration;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tmetrics.util.Localization;

public class DaemonPropertiesTest {
	private static DaemonProperties _props;
	
	@BeforeClass
	public static void before() throws IOException {
		URL url = Resources.getResourceURL("daemon.properties");
		_props = new DaemonProperties(URLDecoder.decode(url.getFile(), "UTF-8"));
	}
	
	@Test
	public void testAll() {
		
		// Create an empty properties "file" and fill it with the example file's data
		DaemonProperties props = new DaemonProperties();
		
		props.maxShortTermsCount = 1;
		props.maxLongTermsCount = 3;
		props.expirationDurations = new Duration[] {
				Localization.DURATION_DATE_FORMATTER.parsePeriod("00-00-01 00:00:00").toStandardDuration(),
				Localization.DURATION_DATE_FORMATTER.parsePeriod("00-00-07 00:00:00").toStandardDuration(),
		};
		props.expirationFactor = new Integer[] {
			3, 2, 1
		};
		props.defaultIntervalLength = Localization.DURATION_FORMATTER.parsePeriod("00:15:00").toStandardDuration();
		props.rateLimitBuffer = 20;
		props.limitSearchTerms = 10;
		props.throttleFactor = 0.9;
		props.priorityFactors = new Double[] {
			0.5, 0.75, 1.0, 1.5, 2.0
		};
		props.defaultPriorityIndex = 2;
		props.maxIntervalLength = Localization.DURATION_FORMATTER.parsePeriod("156:00:00").toStandardDuration();
		props.outlierFactor = 3;
		props.unlimitedRequestsPerSearchTerm = -1;
		props.spawnLimitedMinion = true;
		props.twitterRateLimit = 180;
		props.maxBagSize = 40000;
		props.maxRateLimit = props.twitterRateLimit - props.rateLimitBuffer;
		props.maxBagNotEmptiedDuration = Localization.DURATION_FORMATTER.parsePeriod("00:10:00").toStandardDuration();
		props.sleepDuration = 5000;
		props.profileResetTime = Localization.DURATION_FORMATTER.parsePeriod("00:16:30").toStandardDuration();
		props.maxTreasurers = 3;
		props.maxHeapSize = 500;
		
		// Test all
		assertTrue("maxShortTermsCount returned " + _props.maxShortTermsCount + ", expected " + props.maxShortTermsCount + ".",
				_props.maxShortTermsCount == props.maxShortTermsCount);
		assertTrue("maxLongTermsCount returned " + _props.maxLongTermsCount + ", expected " + props.maxLongTermsCount + ".",
				_props.maxLongTermsCount == props.maxLongTermsCount);

		for (int i = 0; i < _props.expirationDurations.length; ++i) {
			assertTrue("expirationDurations[" + i + "] returned " + _props.expirationDurations[i] + ", expected " + props.expirationDurations[i] + ".",
					_props.expirationDurations[i].getMillis() == props.expirationDurations[i].getMillis());
		}
		for (int i = 0; i < _props.expirationFactor.length; ++i) {
			assertTrue("expirationFactor[" + i + "] returned " + _props.expirationFactor[i] + ", expected " + props.expirationFactor[i] + ".",
					_props.expirationFactor[i] == props.expirationFactor[i]);
		}
		
		assertTrue("defaultIntervalLength returned " + _props.defaultIntervalLength + ", expected " + props.defaultIntervalLength + ".",
				_props.defaultIntervalLength.getMillis() == props.defaultIntervalLength.getMillis());
		assertTrue("rateLimitBuffer returned " + _props.rateLimitBuffer + ", expected " + props.rateLimitBuffer + ".",
				_props.rateLimitBuffer == props.rateLimitBuffer);
		assertTrue("throttleFactor returned " + _props.throttleFactor + ", expected " + props.throttleFactor + ".",
				Double.doubleToLongBits(_props.throttleFactor) == Double.doubleToLongBits(props.throttleFactor));

		for (int i = 0; i < _props.priorityFactors.length; ++i) {
			assertTrue("priorityFactors[" + i + "] returned " + _props.priorityFactors[i] + ", expected " + props.priorityFactors[i] + ".",
					Double.doubleToLongBits(_props.priorityFactors[i]) == Double.doubleToLongBits(props.priorityFactors[i]));
		}
		
		assertTrue("defaultPriorityIndex returned " + _props.defaultPriorityIndex + ", expected " + props.defaultPriorityIndex + ".",
				_props.defaultPriorityIndex == props.defaultPriorityIndex);
		assertTrue("maxIntervalLength returned " + _props.maxIntervalLength + ", expected " + props.maxIntervalLength + ".",
				_props.maxIntervalLength.getMillis() == props.maxIntervalLength.getMillis());
		assertTrue("outlierFactor returned " + _props.outlierFactor + ", expected " + props.outlierFactor + ".",
				_props.outlierFactor == props.outlierFactor);
		assertTrue("unlimitedRequestsPerSearchTerm returned " + _props.unlimitedRequestsPerSearchTerm + ", expected " + props.unlimitedRequestsPerSearchTerm + ".",
				_props.unlimitedRequestsPerSearchTerm == props.unlimitedRequestsPerSearchTerm);
		assertTrue("spawnLimitedMinion returned " + _props.spawnLimitedMinion + ", expected " + props.spawnLimitedMinion + ".",
				_props.spawnLimitedMinion.equals(props.spawnLimitedMinion));
		assertTrue("maxBagSize returned " + _props.maxBagSize + ", expected " + props.maxBagSize + ".",
				_props.maxBagSize.equals(props.maxBagSize));
		assertTrue("twitterRateLimit returned " + _props.twitterRateLimit + ", expected " + props.twitterRateLimit + ".",
				_props.twitterRateLimit.equals(props.twitterRateLimit));
		assertTrue("maxRateLimit returned " + _props.maxRateLimit + ", expected " + props.maxRateLimit + ".",
				_props.maxRateLimit.equals(props.maxRateLimit));
		assertTrue("maxBagNotEmptiedDuration returned " + _props.maxBagNotEmptiedDuration + ", expected " + props.maxBagNotEmptiedDuration + ".",
				_props.maxBagNotEmptiedDuration.getMillis() == props.maxBagNotEmptiedDuration.getMillis());
		assertTrue("sleepDuration returned " + _props.sleepDuration + ", expected " + props.sleepDuration + ".",
				_props.sleepDuration.equals(props.sleepDuration));
		assertTrue("profileResetTime returned " + _props.profileResetTime + ", expected " + props.profileResetTime + ".",
				_props.profileResetTime.getMillis() == props.profileResetTime.getMillis());
		assertTrue("maxTreasurers returned " + _props.maxTreasurers + ", expected " + props.maxTreasurers + ".",
				_props.maxTreasurers.equals(props.maxTreasurers));
		assertTrue("maxHeapSize returned " + _props.maxHeapSize + ", expected " + props.maxHeapSize + ".",
				_props.maxHeapSize.equals(props.maxHeapSize));
	}
}
