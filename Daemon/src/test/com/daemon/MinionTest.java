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
import java.util.LinkedList;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Test;

import com.daemon.database.SearchTerm;
import com.tmetrics.util.Localization;

public class MinionTest {
	@Test
	public void testMinionTweetIsTooOld() {
		DateTime tweetDateNewer1 = new DateTime(2013, 12, 3, 12, 30, 23);
		DateTime tweetDateNewer2 = new DateTime(2013, 12, 4, 15, 10, 12);
		
		DateTime tweetDateOlder1 = new DateTime(2013, 12, 3, 0, 17, 53);
		DateTime tweetDateOlder2 = new DateTime(2013, 12, 2, 23, 57, 1);
		
		DateTime oldStart = new DateTime(2013, 12, 3, 2, 18, 45);

		assertFalse("Minion.tweetIsTooOld(tweetDateNewer1, oldStart) returned " + Minion.tweetIsTooOld(tweetDateNewer1, oldStart) + ", expected false.", Minion.tweetIsTooOld(tweetDateNewer1, oldStart));
		assertFalse("Minion.tweetIsTooOld(tweetDateNewer2, oldStart) returned " + Minion.tweetIsTooOld(tweetDateNewer2, oldStart) + ", expected false.", Minion.tweetIsTooOld(tweetDateNewer2, oldStart));
		assertTrue("Minion.tweetIsTooOld(tweetDateOlder1, oldStart) returned " + Minion.tweetIsTooOld(tweetDateOlder1, oldStart) + ", expected true.", Minion.tweetIsTooOld(tweetDateOlder1, oldStart));
		assertTrue ("Minion.tweetIsTooOld(tweetDateOlder2, oldStart) returned " + Minion.tweetIsTooOld(tweetDateOlder2, oldStart) + ", expected true.",  Minion.tweetIsTooOld(tweetDateOlder2, oldStart));
	}
	
	@Test
	public void testMinionFilterSearchTerms() {
		List<SearchTerm> terms = new LinkedList<SearchTerm>();
		SearchTerm term1 = new SearchTerm("Test 1", new DateTime());
		SearchTerm term2 = new SearchTerm("Test 2", new DateTime());

		terms.add(term1);
		terms.add(new SearchTerm("No Filter", new DateTime()));
		terms.add(term1);
		terms.add(new SearchTerm("No Filter", new DateTime()));
		terms.add(term2);
		terms.add(new SearchTerm("No Filter", new DateTime()));
		terms.add(new SearchTerm("No Filter", new DateTime()));
		terms.add(term1);
		terms.add(new SearchTerm("No Filter", new DateTime()));
		terms.add(new SearchTerm("No Filter", new DateTime()));
		terms.add(term1);
		terms.add(term2);
		terms.add(term2);
		
		Minion.filterSearchTerms(terms, term1);

		assertTrue("terms.size() returned " + terms.size() + ", expected 9.", terms.size() == 9);
		assertTrue("terms.contains(term1) returned " + terms.contains(term1) + ", expected false.", terms.contains(term1) == false);
		
		Minion.filterSearchTerms(terms, term2);
		
		assertTrue("terms.size() returned " + terms.size() + ", expected 6.", terms.size() == 6);
		assertTrue("terms.contains(term2) returned " + terms.contains(term2) + ", expected false.", terms.contains(term2) == false);
		
		for (SearchTerm term : terms) {
			assertTrue("term.getTerm() returned " + term.getTerm() + ", expected 'No Filter'.", term.getTerm().equals("No Filter"));
		}
	}
	
	@Test
	public void testMinionCalculateIntervalLength() {
		SearchTerm term1 = new SearchTerm("test1", new DateTime());
		term1.setPriority(0);
		SearchTermMetaData data1 = new SearchTermMetaData(term1);
		data1.setTweetCount(20);
		data1.setOldestTweetDate(new DateTime(2013, 12, 13, 14, 20, 34));
		data1.setNewestTweetDate(new DateTime(2013, 12, 13, 14, 42, 12));
		
		double timeDiff = Localization.DURATION_FORMATTER.parsePeriod("00:21:38").toStandardDuration().getMillis() / 60000d;
		double tpm = ((double)data1.getTweetCount()) / timeDiff;
		int intervalLengthInMin = (int) ((1 / tpm) * 0.9 * 100);
		Duration intervalLength = new Duration(intervalLengthInMin * 60000L);
		
		try {
			URL url = Resources.getResourceURL("daemon.properties");
			DaemonProperties props = new DaemonProperties(URLDecoder.decode(url.getFile(), "UTF-8"));

			assertTrue("Minion.calculateIntervalLength(data1) returned " + Minion.calculateIntervalLength(data1, null, props, null).getMillis() + ", expected " + intervalLength.getMillis() + ".", Minion.calculateIntervalLength(data1, null, props, null).getMillis() == intervalLength.getMillis());
	
		}
		catch (IOException ex) {
			fail(ex.getMessage());
		}
	}
	
	@Test
	public void testMinionMaxIntervalLength() {
		SearchTerm term1 = new SearchTerm("test1", new DateTime());
		term1.setPriority(0);
		SearchTermMetaData data1 = new SearchTermMetaData(term1);
		data1.setTweetCount(2);
		data1.setOldestTweetDate(new DateTime(2011, 12, 13, 14, 20, 34));
		data1.setNewestTweetDate(new DateTime(2013, 12, 13, 14, 42, 12));
		
		Duration intervalLength = Localization.DURATION_FORMATTER.parsePeriod("156:00:00").toStandardDuration();
		
		try {
			URL url = Resources.getResourceURL("daemon.properties");
			DaemonProperties props = new DaemonProperties(URLDecoder.decode(url.getFile(), "UTF-8"));

			assertTrue("Minion.calculateIntervalLength(data1) returned " + Minion.calculateIntervalLength(data1, null, props, null).getMillis() + ", expected " + intervalLength.getMillis() + ".", Minion.calculateIntervalLength(data1, null, props, null).getMillis() == intervalLength.getMillis());
	
		}
		catch (IOException ex) {
			fail(ex.getMessage());
		}
	}
}
