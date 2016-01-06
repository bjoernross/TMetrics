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

public class DictionarySentimentClassifierTest {

	@Test
	public final void testDetermineSentiment() {
		DictionarySentimentClassifier dsc = new DictionarySentimentClassifier();
		Float sentiment;
		
		// no sentiment words
		sentiment = dsc.determineSentiment("Kittens are","en");
		assertEquals(0, sentiment, 0);
		
		// positive words alone
		sentiment = dsc.determineSentiment("Kittens are great :)","en");
		assertEquals(1, sentiment, 0);
		
		// negative words alone
		sentiment = dsc.determineSentiment("Kittens are boring","en");
		assertEquals(-1, sentiment, 0);
				
		// same amount of positive and negative words
		sentiment = dsc.determineSentiment("Kittens are great but dogs are boring","en");
		assertEquals(0, sentiment, 0);
		
		// more negative words than positive words
		sentiment = dsc.determineSentiment("Kittens are great but dogs are awfully boring","en");
		assertEquals(-0.33, sentiment, 0.01);
		
		// more positive words than negative words
		sentiment = dsc.determineSentiment("Cute kittens are terrifically amazing but dogs are lame","en");
		assertEquals(0.5, sentiment, 0.01);

		// punctuation
		sentiment = dsc.determineSentiment("Dogs are what I hate!!","en");
		assertEquals(-1, sentiment, 0);
		
		// tweets in other languages
		sentiment = dsc.determineSentiment("Hunde hasse ich!!","de");
		assertNull(sentiment);
		
		// Oops! we ignore emoticons
		sentiment = dsc.determineSentiment("Kittens are boring :)","en");
		assertEquals(-1, sentiment, 0);
		
		// Oops! we don't know any grammar or syntactical rules
		sentiment = dsc.determineSentiment("I hate dogs that are unhappy","en");
		assertEquals(-1, sentiment, 0);
		
		sentiment = dsc.determineSentiment("I hate dogs that are not happy","en");
		assertEquals(0, sentiment, 0);
	}
}