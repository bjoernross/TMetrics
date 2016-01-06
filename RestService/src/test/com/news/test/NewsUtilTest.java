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

import static org.junit.Assert.*;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import com.news.NewsUtil;
import com.restservice.dto.NewsItem;

/**
 * 
 * @author olaf
 * 
 */
public class NewsUtilTest {

	// Folgende Tweets spiegeln nicht die Meinung des Autors wieder sondern sind zufaellige Fragmente aus bestehenden Tweets
	private static String accumulatedTestTweets = "I SUPPOSE I SHOULD TELL YOU WHAT THIS BITCH IS THINKING mein name ist angela merkel & ich halte nun meine neujahrsansprache. yo, bitch. Eine Frage betreffend der Urheberrechte an der Neujahrsansprache Herr @RegSprecher Wer hat die Ansprache verfasst. Frau Merkel selbst? Grad Merkel´s Neujahrsansprache nachgeholt: Boa, immer diese abwartende Haltung! Wie in der Sauna, bloß kein Schweiß aufs Holz! #Merkel: Helmkamera ausgewertet. Skiunfall 100% ausgeschlossen. Wohl eher nach #Neujahrsansprache vomStuhl gekippt. Seiberts Erklärung folgt";

	/*
	 * listWordProportions reagular input
	 */
	@Test
	public void testListWordProportionsRegularInput() {
		HashMap<String, Double> result = NewsUtil
				.listWordProportions("this is a test text test teXt tExt TEXT text TExt");
		Assert.assertTrue("listWordProportions has to filter stopwords (this)",
				!result.containsKey("this"));
		Assert.assertTrue("listWordProportions has to filter stopwords (is)",
				!result.containsKey("is"));
		Assert.assertTrue("listWordProportions has to filter stopwords (a)",
				!result.containsKey("a"));
		Assert.assertTrue(
				"listWordProportions must not filter non-stopwords (test)",
				result.containsKey("test"));
		Assert.assertTrue(
				"listWordProportions must not filter non-stopwords (text)",
				result.containsKey("text"));
		Assert.assertTrue(
				"listWordProportions has to calculate the correct proportions of words (test)",
				result.get("test").equals(.25));
		Assert.assertTrue(
				"listWordProportions has to calculate the correct proportions of words (text)",
				result.get("text").equals(.75));
	}

	/*
	 * listWordProportions unusual input: empty String
	 */
	@Test
	public void testListWordProportionsEmptyStringInput() {
		HashMap<String, Double> result = NewsUtil.listWordProportions("");
		Assert.assertTrue(
				"listWordProportions has to return a Hashmap when input is an empty String",
				result != null);
		Assert.assertTrue(
				"listWordProportions has to return an empty Hashmap when input is an empty String",
				result.keySet().size() == 0);
	}

	/*
	 * listWordProportions unusual input: null
	 */
	@Test
	public void testListWordProportionsNullInput() {
		HashMap<String, Double> result = NewsUtil.listWordProportions(null);
		Assert.assertTrue(
				"listWordProportions has to return a Hashmap when input is null",
				result != null);
		Assert.assertTrue(
				"listWordProportions has to return an empty Hashmap when input is null",
				result.keySet().size() == 0);
	}

	/*
	 * koehlerScoring test for wanted properties: - max. score is about 1 -
	 * score > 0 for valid inputs - if the words occurrence in both texts is
	 * about 20% it gives about 0.5 points - if the words occurrence in both
	 * texts is about 2% it gives about 0.2 points - doesn't require the word
	 * occurrence proportions to be equal
	 */
	@Test
	public void testKoehlerScoringRegularInput() {
		assertScoring("maxScore should be about 1", 1.,
				NewsUtil.koehlerScoring(1., 1.), .2);
		assertScoring("score for (0,0) should be 0", .0,
				NewsUtil.koehlerScoring(.0, .0), .0);
		assertScoring("score for (0.02,0.02) should be 0.2", 0.2,
				NewsUtil.koehlerScoring(.02, .02), .1);
		assertScoring("score for (0.2,0.2) should be 0.5", 0.5,
				NewsUtil.koehlerScoring(.2, .2), .1);
		assertScoring(
				"score for (0.2,0.2) and (0.1,0.3) should be almost equal",
				NewsUtil.koehlerScoring(.1, .3),
				NewsUtil.koehlerScoring(.2, .2), .1);
	}

	/*
	 * koehlerScoring unusual input: <= 0
	 */
	@Test
	public void testKoehlerScoringInvalidInput() {
		assertScoring("score for invalid inputs should be 0 (-1,-1)", 0.,
				NewsUtil.koehlerScoring(-1., -1.), .0);
		assertScoring("score for invalid inputs should be 0 (2,2)", 0.,
				NewsUtil.koehlerScoring(2., 2.), .0);
	}

	private void assertScoring(String s, Double expectation, Double result,
			Double epsilon) {
		Assert.assertTrue("score should be non negative", result >= 0);
		Assert.assertTrue(s, Math.abs(expectation - result) <= epsilon);
	}

	/*
	 * rateText regular input
	 */
	@Test
	public void testRateTextRegularInput() {
		HashMap<String, Double> baseTruth = NewsUtil
				.listWordProportions("this text is the great example");
		Double rating1 = NewsUtil.rateText("another great example", baseTruth);
		Double rating2 = NewsUtil.rateText("well, not so great indeed",
				baseTruth);
		Assert.assertTrue(
				"rateText produces bad results: rating should be greater than one for similar texts (text1)",
				rating1 > .0);
		Assert.assertTrue(
				"rateText produces bad results: rating should be greater than one for similar texts (text2)",
				rating2 > .0);
		Assert.assertTrue(
				"rateText produces bad results: rating of (text1) should be higher than (text2)",
				rating1 > rating2);
	}

	/*
	 * rateText unusual input: empty String
	 */
	@Test
	public void testRateTextEmptyStringInput() {
		HashMap<String, Double> baseTruth = NewsUtil
				.listWordProportions("this text is the great example");
		Double rating = NewsUtil.rateText("", baseTruth);
		Assert.assertTrue("rateText should rate an empty text 0", rating == 0);
	}

	/*
	 * rateText unusual input: null String
	 */
	@Test
	public void testRateTextNullInput() {
		HashMap<String, Double> baseTruth = NewsUtil
				.listWordProportions("this text is the great example");
		Double rating = NewsUtil.rateText(null, baseTruth);
		Assert.assertTrue("rateText should rate a null input 0", rating == 0);
	}

	/*
	 * rateText unusual input: empty Map
	 */
	@Test
	public void testRateTextEmptyScoreTableInput() {
		HashMap<String, Double> baseTruth = NewsUtil.listWordProportions("");
		Double rating = NewsUtil.rateText("this text is the great example",
				baseTruth);
		Assert.assertTrue(
				"rateText should rate any input 0 if the scoretable is empty",
				rating == 0);
	}

	/*
	 * rateText unusual input: null Map
	 */
	@Test
	public void testRateTextNullScoreTableInput() {
		Double rating = NewsUtil.rateText("test text", null);
		Assert.assertTrue(
				"rateText should rate any input 0 if score table is null",
				rating == 0);
		rating = NewsUtil.rateText("", null);
		Assert.assertTrue(
				"rateText should rate any input 0 if score table is null",
				rating == 0);
		rating = NewsUtil.rateText(null, null);
		Assert.assertTrue(
				"rateText should rate any input 0 if score table is null",
				rating == 0);
	}


}
