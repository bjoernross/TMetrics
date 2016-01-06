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

import static org.junit.Assert.*;

import org.junit.Test;

public class EmoticonSentimentClassifierTest {

	@Test
	public final void testDetermineSentiment() {
		EmoticonSentimentClassifier esc = new EmoticonSentimentClassifier();
		float sentiment;
		
		sentiment = esc.determineSentiment("Kittens are great :)","en");
		assertEquals(1, sentiment, 0);
		
		sentiment = esc.determineSentiment("Kittens are great :-)","en");
		assertEquals(1, sentiment, 0);
		
		sentiment = esc.determineSentiment("Kittens are great :d","en");
		assertEquals(1, sentiment, 0);

		sentiment = esc.determineSentiment("Kittens are great :D","en");
		assertEquals(1, sentiment, 0);
		
		sentiment = esc.determineSentiment("Kittens are great :-d","en");
		assertEquals(1, sentiment, 0);
		
		sentiment = esc.determineSentiment("Kittens are great :-D","en");
		assertEquals(1, sentiment, 0);
		
		sentiment = esc.determineSentiment("Kittens are great :p","en");
		assertEquals(1, sentiment, 0);
		
		sentiment = esc.determineSentiment("Kittens are great :P","en");
		assertEquals(1, sentiment, 0);
		
		sentiment = esc.determineSentiment("Kittens are great :-p","en");
		assertEquals(1, sentiment, 0);
		
		sentiment = esc.determineSentiment("Kittens are great :P","en");
		assertEquals(1, sentiment, 0);
		
		sentiment = esc.determineSentiment("Kittens are boring","en");
		assertEquals(0, sentiment, 0);
		
		sentiment = esc.determineSentiment("Kittens are boring :(","en");
		assertEquals(-1, sentiment, 0);
		
		sentiment = esc.determineSentiment("Kittens are boring :-(","en");
		assertEquals(-1, sentiment, 0);
	}

}
