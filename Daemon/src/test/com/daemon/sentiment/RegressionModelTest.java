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
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.tmetrics.dto.SentimentFeatures;

public class RegressionModelTest {

	public static RegressionModel model;

	@BeforeClass
	public static void setUpDB() {
		try {
			model = new RegressionModel(new Features().useUnigrams(true)
					.useBigrams(true).useTrigrams(true).use4Grams(true)
					.useDictionary(true).useEmoticons(true)
					.usePOSTagger(false).useNegations(false),"en",null,(float) 1,"DaemonTest");
//					new RegressionModel.RegressionModelBuilder((float) 1,
//					"en").useEmoticons(true).useDictionary(true)
//					.useAllCaps(false).useElongatedWords(true)
//					.useUnigrams(true).useBigrams(true).useTrigrams(true)
//					.use4Grams(true).useTestDB(true).build();
		} catch (Exception e) {
			e.printStackTrace();
			fail("Failed loading the regression model.");
		}

	}

	@Ignore
	public final void testRegressionModel() {
		fail("Not yet implemented"); // TODO
	}

	@Ignore
	public final void testGetSourceData() {
		fail("Not yet implemented"); // TODO
	}

	@Ignore
	public final void testGetFeatureNames() {
		fail("Not yet implemented"); // TODO
	}

	@Ignore
	public final void testGetFeatureNamesOrdered() {
		fail("Not yet implemented"); // TODO
	}

	@Ignore
	public final void testGetFeatureMatrix() {
		fail("Not yet implemented"); // TODO
	}

	@Ignore
	public final void testGetParameters() {
		fail("Not yet implemented"); // TODO
	}

	@Ignore
	public final void testDetermineSentiment() {
		fail("Not yet implemented"); // TODO
	}

	@Test
	public final void testDetermineImportantTrainingTweets() {
		List<LabeledTweetContainer> importantTrainingTweets = model
				.determineImportantTrainingTweets("$unigram$nigger");
		assertEquals(1, importantTrainingTweets.size());

		assertEquals("\"nigger\" -Paula Dean", importantTrainingTweets.get(0)
				.getTweetText());
		assertEquals(-1, importantTrainingTweets.get(0).getLabel(), 0.001);
		
		importantTrainingTweets = model
				.determineImportantTrainingTweets("$unigram$suck");
		assertEquals(1, importantTrainingTweets.size());

		assertEquals("NYT, quoting official on NSA \"they suck up every phone number they can in Germany\"  http://t.co/zTeG79JuGd", importantTrainingTweets.get(0)
				.getTweetText());
		assertEquals(-1, importantTrainingTweets.get(0).getLabel(), 0.001);
		
		importantTrainingTweets = model
				.determineImportantTrainingTweets("$unigram$on");
		assertEquals(2, importantTrainingTweets.size());
		assertEquals("NYT, quoting official on NSA \"they suck up every phone number they can in Germany\"  http://t.co/zTeG79JuGd", importantTrainingTweets.get(0)
				.getTweetText());
		assertEquals(-1, importantTrainingTweets.get(0).getLabel(), 0.001);
		assertEquals("Hahahahahahahaha !!! \"@Gotham3: Absolutely shocking!! Leaked photo proof of Barack Obama spying on Angela Merkel. http://t.co/kHNxfUvr7T\"", importantTrainingTweets.get(1).getTweetText());
		assertEquals(0, importantTrainingTweets.get(1).getLabel(), 0.001);
	}

	@Test
	public final void testDetermineSentimentDetails() {
		SentimentFeatures sentimentFeatures = model
				.determineSentimentDetails("Love me do");
		System.out.println(sentimentFeatures);
		assertEquals("love", sentimentFeatures.getWords().getPositiveWords().get(0));
		assertEquals(1, sentimentFeatures.getWords().getSentiment(), 0.001);
	}

	@Ignore
	public final void testExportModelToFile() {
		fail("Not yet implemented"); // TODO
	}

	@Ignore
	public final void testMain() {
		fail("Not yet implemented"); // TODO
	}

}
