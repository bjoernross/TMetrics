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

/**
 * Defines what a class needs to do to be a sentiment classifier
 * 
 * @author Björn
 */
public interface SentimentClassifier {
	/**
	 * Determines the sentiment of a given text.
	 * 
	 * @param tweetText
	 *            Text to be analysed
	 * @param language
	 *            Language the text is in
	 * @return Returns 1 for positive, -1 for negative and 0 for neutral
	 *         sentiment; returns null if sentiment could not be determined
	 *         (e.g. because there is no classifier for the given language)
	 */
	public Float determineSentiment(String tweetText, String language);

}