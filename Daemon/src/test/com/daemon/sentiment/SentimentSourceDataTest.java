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
package com.daemon.sentiment;

import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * To test the SentimentSourceData, the object that reads the tweets from
 * database for doing sentiment analysis or clustering.
 * 
 * @author eq
 * 
 */
public class SentimentSourceDataTest {

	@Test
	public void testReadDataFromDB() {
		SentimentSourceData sourceDataTest = new SentimentSourceData(null,
				"en", "DaemonTest");
		try {
			sourceDataTest.readDataFromDB((float) 0.67, false);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Something went wrong while reading senti data from test daemon database");
		}
		List<String> texts = sourceDataTest.getTweetTexts();

		// for(int i = 0; i < texts.size(); i++)
		// System.out.println("Text: "+texts.get(i));
		// System.out.println(texts.size());

		String text = texts.get(0).substring(0, 4);
		assertEquals("NYT,", text);
		text = texts.get(1).substring(0, 6);
		assertEquals("Hahaha", text);

		// As we have three labeled tweets, we expect 2 tweets (0.67 percentage
		// training data):
		assertEquals(2, texts.size());
	}

	@Test
	public void testReadClusterDataFromDB() {

		// We test four options:
		// A. Use search_term_id && language
		// B. Use search_term_id, but no language
		// C. Do not use search_term_id, but language
		// D. Neither use id, nor language

		// A.
		SentimentSourceData sourceDataTest = new SentimentSourceData((long) 2,
				"en", "DaemonTest");

		try {
			sourceDataTest.readClusterDataFromDB(5);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Something went wrong while reading cluster data from test daemon database");
		}
		List<String> ids = sourceDataTest.getTweetIds();
		assertEquals("408367280917975040", ids.get(0));
		assertEquals("408371058828664832", ids.get(4));

		// B.
		SentimentSourceData sourceDataTest2 = new SentimentSourceData((long) 2,
				null, "DaemonTest");

		try {
			sourceDataTest2.readClusterDataFromDB(5);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Something went wrong while reading cluster data from test daemon database");
		}
		List<String> ids2 = sourceDataTest2.getTweetIds();
		assertEquals("408367280917975040", ids2.get(0));
		assertEquals("408367792900280320", ids2.get(1));
		assertEquals("408368608725983232", ids2.get(4));

		// C.
		SentimentSourceData sourceDataTest3 = new SentimentSourceData(null,
				"en", "DaemonTest");

		try {
			sourceDataTest3.readClusterDataFromDB(5);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Something went wrong while reading cluster data from test daemon database");
		}
		List<String> ids3 = sourceDataTest3.getTweetIds();
		assertEquals("395908486778740737", ids3.get(0));
		assertEquals("404403413586546688", ids3.get(4));

		// D.
		SentimentSourceData sourceDataTest4 = new SentimentSourceData(null,
				null, "DaemonTest");

		try {
			sourceDataTest4.readClusterDataFromDB(5);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Something went wrong while reading cluster data from test daemon database");
		}
		List<String> ids4 = sourceDataTest4.getTweetIds();
		assertEquals("378065271002382336", ids4.get(0));
		assertEquals("397903424030658560", ids4.get(4));

	}

}
