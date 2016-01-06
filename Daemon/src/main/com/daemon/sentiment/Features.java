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
 * Used to set the wanted features for which the tweets are analyzed - in
 * sentiment analysis or clustering.
 * 
 * @author Erwin, Björn
 * 
 */
public class Features implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;

	public boolean useEmoticons = false;
	public boolean useDictionary = false;
	public boolean useNegations = false;
	public boolean usePOSTagger = false;
	public boolean useUnigrams = false;
	public boolean useBigrams = false;
	public boolean useTrigrams = false;
	public boolean use4Grams = false;

	public Features useEmoticons(boolean useEmoticons) {
		this.useEmoticons = useEmoticons;
		return this;
	}

	public Features useDictionary(boolean useDictionary) {
		this.useDictionary = useDictionary;
		return this;
	}

	public Features useNegations(boolean useNegations) {
		this.useNegations = useNegations;
		return this;
	}

	public Features usePOSTagger(boolean usePOSTagger) {
		this.usePOSTagger = usePOSTagger;
		return this;
	}

	public Features useUnigrams(boolean useUnigrams) {
		this.useUnigrams = useUnigrams;
		return this;
	}

	public Features useBigrams(boolean useBigrams) {
		this.useBigrams = useBigrams;
		return this;
	}

	public Features useTrigrams(boolean useTrigrams) {
		this.useTrigrams = useTrigrams;
		return this;
	}

	public Features use4Grams(boolean use4Grams) {
		this.use4Grams = use4Grams;
		return this;
	}
}
