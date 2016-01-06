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
package com.restservice.util;

import java.io.FileInputStream;
import java.util.Properties;

public class RestUtil {

	private static final String REGRESSION_PROPERTIES_FILE_PATH = System.getProperty("user.home") + "/regression.properties";
	
	//Border between negative (includes border) and neutral (excludes border) sentiment
	public static float SENTIMENT_LOWER_BORDER = -0.33f;
	//Border between positive (includes border) and neutral (excludes border) sentiment
	public static float SENTIMENT_UPPER_BORDER =  0.33f;
	
	static
	{
		Properties props = new Properties();
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream(REGRESSION_PROPERTIES_FILE_PATH);
			props.load(fis);
			
			String lower_border = props.getProperty("rest.sentiment.lower_border");
			String upper_border = props.getProperty("rest.sentiment.upper_border");
			
			SENTIMENT_LOWER_BORDER = Float.parseFloat(lower_border); 
			SENTIMENT_UPPER_BORDER = Float.parseFloat(upper_border); 
		}
		catch (Exception e)
		{
			SENTIMENT_LOWER_BORDER = -0.33f;
			SENTIMENT_UPPER_BORDER = 0.33f;
			
			System.out.println("Cannot load rest.sentiment properties from path " + REGRESSION_PROPERTIES_FILE_PATH + ". Using default values.");
			e.printStackTrace();
		}
		finally
		{
			if (fis != null)
			{
				try
				{
					fis.close();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	/*
	 * @param isoCode Language from ISO 639-1
	 * @return Representation of this language in English if possible, null else
	 */
	public static String getEnglishLanguageString(String isoCode) {
		if (isoCode.equalsIgnoreCase("en")) {
			return "en";
		}
		return null;
	}
	
	/*
	 * @param sentimentValue Result from the sentiment analysis for a tweet. Ranges from -1 to 1
	 * @return "positive" if sentimentValue >= {@link RestUtil#SENTIMENT_UPPER_BORDER}, "negative" if sentimentValue <= {@link RestUtil#SENTIMENT_LOWER_BORDER}, "neutral" else
	 */
	public static String getSentimentString(float sentimentValue) {
		if (sentimentValue <= SENTIMENT_LOWER_BORDER) {
			return "negative";
		} else if (sentimentValue >= SENTIMENT_UPPER_BORDER) {
			return "positive";
		} else {
			return "neutral";
		}
	}
}
