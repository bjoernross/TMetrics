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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.tmetrics.logging.LogManager;
import com.tmetrics.logging.Logger;
import com.tmetrics.util.ListUtil;
import com.tmetrics.util.SparseMatrix;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.Tree;

/**
 * Class which extracts features from given tweets and stores them in a matrix
 * where the features are the columns and the tweets are the rows. Also stores
 * the names of the extracted features (i.e. column names), to be able to
 * recognize them in new tweets.
 * 
 * The class can be used for clustering and analyzing the sentiment of tweets.
 * 
 * @author Erwin, Björn
 * 
 */
public class FeatureMatrix implements java.io.Serializable {

	// necessary to serialize the matrix
	private static final long serialVersionUID = 1L;

	// names of the features and their position (column) in the feature matrix
	private Map<String, Integer> featureNames;

	// feature matrix
	private SparseMatrix featureMatrix;

	// the data (tweets) from which the features will be extracted.
	private SentimentSourceData sourceData;

	// which features to use
	private final boolean useEmoticons;
	private final boolean useDictionary;
	private final boolean useNegations;
	private final boolean usePOSTagger;
	private final boolean useUnigrams;
	private final boolean useBigrams;
	private final boolean useTrigrams;
	private final boolean use4Grams;

	// initialise other sentiment classifiers whose output will be used for some
	// features
	private final EmoticonSentimentClassifier emoticonClassifier;
	private final DictionarySentimentClassifier dictionaryClassifier;

	// Natural Language Parser:
	private LexicalizedParser lexicalizedParser = null;

	// characters to use as delimiters when tokenizing tweets
	private static final String TOKENIZER_DELIM = " \t\n\r\f,.:;?![]()*\"";

	// regular expression to match URLs
	private static final String URL_PATTERN = "(https?:\\/\\/)([\\da-z.-]+).([a-z.]{2,6})([\\/\\w.-]*)*\\/?";
	// Explanation:
	// (https?:\\/\\/)
	// a URL is a string that begins with either https:// or http://
	// ([\\da-z.-]+)
	// followed by one or more numbers, letters, dots, or hypens (e. g. google)
	// .
	// followed by a dot
	// ([a-z.]{2,6})
	// followed by two to six letters or dots (e.g. com)
	// ([\\/\\w.-]*)*
	// and then any number of groups of forward slashes, letters, numbers,
	// underscores, spaces, dots, or hyphens
	// \\/?
	// and, optionally, a final forward slash
	// source:
	// http://net.tutsplus.com/tutorials/other/8-regular-expressions-you-should-know/
	// adapted for Java

	// log exceptions to logs/Sentiment.log
	static private Logger _logger = LogManager.getLogger("logs/Sentiment.log");

	/**
	 * Constructs the feature matrix, filling it.
	 * 
	 * Before using, create an instance of Features to specify the desired
	 * features.
	 */
	public FeatureMatrix(Features builder, SentimentSourceData sourceData) {

		// Set the necessary variables
		this.featureNames = new HashMap<String, Integer>();
		this.featureMatrix = new SparseMatrix();
		this.emoticonClassifier = new EmoticonSentimentClassifier();
		this.dictionaryClassifier = new DictionarySentimentClassifier();
		this.sourceData = sourceData;

		// Set features
		this.useEmoticons = builder.useEmoticons;
		this.useDictionary = builder.useDictionary;
		this.useNegations = builder.useNegations;
		this.usePOSTagger = builder.usePOSTagger;
		this.useUnigrams = builder.useUnigrams;
		this.useBigrams = builder.useBigrams;
		this.useTrigrams = builder.useTrigrams;
		this.use4Grams = builder.use4Grams;

		if (this.usePOSTagger) {
			// load parser
			this.loadParser();
		}

		// Check if negations are supported for this language
		if (this.useNegations) {
			if (!this.sourceData.getLanguage().equals("en")) {
				_logger.log("Negations are not supported for "
						+ this.sourceData.getLanguage());
			}
		}

		// fill the feature matrix
		this.createFeatureMatrix();
	}

	/**
	 * Load the Stanford Parser object - depending on language - necessary to do
	 * POS analysis. Parser object is null, if language is not supported.
	 */
	private void loadParser() {

		// load depending on given language the corresponding lang-model.
		switch (this.sourceData.getLanguage()) {
		case "en":
			lexicalizedParser = LexicalizedParser
					.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
			break;
		default:
			_logger.log("POS TAGGING is not supported for "
					+ this.sourceData.getLanguage());
			break;
		}

	}

	/**
	 * Get feature names as a map.
	 * 
	 * Use this method if you want to find the position (column) of a feature in
	 * the feature matrix. Use getFeatureNamesOrdered() instead if you want to
	 * print a list of feature names in the correct order.
	 * 
	 * @return
	 */
	Map<String, Integer> getFeatureNames() {
		return featureNames;
	}

	/**
	 * Get feature names as a list, in the correct order.
	 * 
	 * Use this method if you want to print a list of feature names. Use
	 * getFeatureNames() instead if you want to find the position (column) of a
	 * feature in the feature matrix.
	 * 
	 * @return a list of feature names in the right column order of the design
	 *         (feature) matrix
	 */
	List<String> getFeatureNamesOrdered() {
		List<String> featureNamesOrdered = ListUtil.createNullList(featureNames
				.size());

		// Sort in O(n) ;)
		for (String column : featureNames.keySet()) {
			int number = featureNames.get(column);
			featureNamesOrdered.set(number, column);
		}
		return featureNamesOrdered;
	}

	/**
	 * Return the feature matrix as sparse matrix (without feature names, only
	 * the values in matrix).
	 * 
	 * @return SparseMatrix
	 */
	public SparseMatrix getFeatureMatrixAsSparseMatrix() {
		return this.featureMatrix;
	}

	/**
	 * Return the EmoticonSentimentClassifier used in the construction of the
	 * matrix
	 * 
	 * @return EmoticonSentimentClassifier
	 */
	public EmoticonSentimentClassifier getEmoticonClassifier() {
		return emoticonClassifier;
	}

	/**
	 * Return the DictionarySentimentClassifier used in the construction of the
	 * matrix
	 * 
	 * @return DictionarySentimentClassifier
	 */
	public DictionarySentimentClassifier getDictionaryClassifier() {
		return dictionaryClassifier;
	}

	/**
	 * Creates the feature matrix, based on the defined features.
	 */
	private void createFeatureMatrix() {

		// keep track of which column we're at
		int currentCol = 0;

		// a column for the constant (y intercept)
		featureNames.put("$tmetrics$regression_constant", currentCol);
		currentCol++;

		// if emoticon features are to be used
		if (useEmoticons) {
			// create columns for emoticon features
			featureNames.put("$tmetrics$emoticon_sentiment", currentCol);
			currentCol++;
			featureNames.put("$tmetrics$emoticon_pos_count", currentCol);
			currentCol++;
			featureNames.put("$tmetrics$emoticon_neg_count", currentCol);
			currentCol++;
		}

		// if dictionary features are to be used
		if (useDictionary) {
			// create columns for dictionary features
			featureNames.put("$tmetrics$dictionary_sentiment", currentCol);
			currentCol++;
			featureNames.put("$tmetrics$dictionary_pos_count", currentCol);
			currentCol++;
			featureNames.put("$tmetrics$dictionary_neg_count", currentCol);
			currentCol++;
		}

		// create a feature vector from each of the tweets and add it to the
		// matrix
		for (LabeledTweetContainer tweet : this.sourceData.getTweets()) {
			featureMatrix.concatenateVertically(createFeatureVector(
					tweet.getTweetText(), true));
		}

	}

	/**
	 * creates a feature vector for a given (tweet)text
	 * 
	 * @param text
	 *            - text from which features will be extracted
	 * @param addNewFeatures
	 *            Whether new features (e.g. previously unknown unigrams) should
	 *            be added to the model (as during training) or not (as during
	 *            testing and classification). In other words: if
	 *            determineSentiment calls this method, no new features should
	 *            be added, so set addNewFeatures = false, if
	 *            createFeatureMatrix calls this methods, set addNewFeatures =
	 *            true.
	 * @return a SparseMatrix, which has one only one row. Therefore usable as
	 *         vector.
	 */
	SparseMatrix createFeatureVector(String text, boolean addNewFeatures) {
		SparseMatrix featureVector = new SparseMatrix();

		// the constant (y intercept), i. e. the sentiment value for a text
		// where all other features equal zero
		featureVector.set(0, 0, (float) 1.0);

		// // convert the string to lower case
		// text = text.toLowerCase();

		// replace URLs with $tmetrics$url
		// this is done before creating the unigram features, so we are
		// essentially creating a boolean feature "tweet contains a url" -
		// individual URLs are unlikely to reoccur in a lot of tweets but the
		// fact that a tweet contains one might be useful
		// this is done before creating the emoticon feature to prevent things
		// like http:// causing recognition of the emoticon :/
		// and it is done before creating the dictionary features to prevent
		// sentiment-carrying words in URLs to affect the sentiment of the text
		text = text.replaceAll(URL_PATTERN, "\\$tmetrics\\$url");

		// convert the string to lower case
		String textLowerCase = text.toLowerCase();

		// tokenize text
		StringTokenizer tokenizer;
		tokenizer = new StringTokenizer(textLowerCase, TOKENIZER_DELIM);
		int countedTokens = tokenizer.countTokens();

		// Token list - used for bigrams, ..., 4-grams
		List<String> tokens = new ArrayList<String>(countedTokens);
		// Token list - storing advanced nlp tags - used for unigrams
		List<String> nlpTokens = new ArrayList<String>(countedTokens);

		// Store Tokenizer Results in Array - easier to perform different
		// feature analysis on token list.
		String curToken;
		while (tokenizer.hasMoreTokens()) {
			curToken = tokenizer.nextToken();
			tokens.add(curToken);
			nlpTokens.add(curToken);
		}

		// A. generate emoticon features
		if (useEmoticons) {
			featureVector = addEmoticonFeature(textLowerCase, featureVector);
		}

		// B. generate dictionary features
		if (useDictionary) {
			featureVector = addDictionaryFeature(textLowerCase, featureVector);
		}

		// C. generate negation features
		if (useNegations) {
			nlpTokens = addNegations(textLowerCase, nlpTokens);
		}

		// D. generate POS Tagger features
		if (usePOSTagger) {
			nlpTokens = addPOSTags(tokens, nlpTokens);
		}

		// E. generate Unigrams features
		if (useUnigrams) {
			featureVector = addUnigramsFeature(nlpTokens, featureVector,
					addNewFeatures);
		}

		// F. generate N-Grams features
		if (useBigrams && useTrigrams && use4Grams) {
			featureVector = generateNGramsFeature(tokens, featureVector,
					addNewFeatures);
		}

		return featureVector;

	}

	// ********** Features are following ***********

	/**
	 * Add emoticon features
	 * 
	 * Uses the EmoticonSentimentClassifier (class member) to determine three
	 * features: (1) the number of positive and (2) the number of negative
	 * emoticons in the text as well as (3) the overall sentiment, as determined
	 * by the equation (p - n) / (p - n)
	 * 
	 * @param text
	 *            Text of the tweet
	 * @param featureVector
	 *            The feature vector before emoticon features have been added
	 * @return The feature vector after emoticon features have been added
	 */
	private SparseMatrix addEmoticonFeature(String text,
			SparseMatrix featureVector) {
		// emoticon features
		float emoticonSentiment;
		int emoticonPosCount;
		int emoticonNegCount;

		emoticonSentiment = emoticonClassifier.determineSentiment(text, "en");
		emoticonPosCount = emoticonClassifier.getLastTextPosCount();
		emoticonNegCount = emoticonClassifier.getLastTextNegCount();
		featureVector.set(0, featureNames.get("$tmetrics$emoticon_sentiment"),
				emoticonSentiment);
		featureVector.set(0, featureNames.get("$tmetrics$emoticon_pos_count"),
				emoticonPosCount);
		featureVector.set(0, featureNames.get("$tmetrics$emoticon_neg_count"),
				emoticonNegCount);

		return featureVector;
	}

	/**
	 * Add emoticon features
	 * 
	 * Uses the DictionarySentimentClassifier (class member) to determine three
	 * features: (1) the number of positive and (2) the number of negative words
	 * in the text as well as (3) the overall sentiment, as determined by the
	 * equation (p - n) / (p - n)
	 * 
	 * @param text
	 *            Text of the tweet
	 * @param featureVector
	 *            The feature vector before word dictionary features have been
	 *            added
	 * @return The feature vector after word dictionary features have been added
	 */
	private SparseMatrix addDictionaryFeature(String text,
			SparseMatrix featureVector) {
		// dictionary features
		float dictionarySentiment;
		int dictionaryPosCount;
		int dictionaryNegCount;

		dictionarySentiment = dictionaryClassifier.determineSentiment(text,
				"en");
		dictionaryPosCount = dictionaryClassifier.getLastTextPosCount();
		dictionaryNegCount = dictionaryClassifier.getLastTextNegCount();
		featureVector.set(0,
				featureNames.get("$tmetrics$dictionary_sentiment"),
				dictionarySentiment);
		featureVector.set(0,
				featureNames.get("$tmetrics$dictionary_pos_count"),
				dictionaryPosCount);
		featureVector.set(0,
				featureNames.get("$tmetrics$dictionary_neg_count"),
				dictionaryNegCount);

		return featureVector;
	}

	/**
	 * Negation features
	 * 
	 * For example, the sentence "No one enjoys it." will be
	 * "No one_NEG enjoys_NEG it_NEG.".
	 * 
	 * This method could be expanded by adding to PUNCTIATION_PATTERN more
	 * delimiters, like "but, however,..."
	 * 
	 * @param text
	 *            Text of the tweet
	 * @param tokens
	 *            Tokenized text of the tweet
	 * @return Tokenized text of the tweet, with negations annotated by the
	 *         suffix "_NEG"
	 */
	private List<String> addNegations(String text, List<String> tokens) {

		StringTokenizer tokenizer;

		// NEGATION_PATTERN: the words that create a negation.
		// Split the string after a negation word, like never or n't.
		// Explanation:
		// (?<=(...|...)), <= these words must occur before the split position.
		// \b = only complete words should be used to split, e.g. noah will not
		// matched with \b.
		String NEGATION_PATTERN;

		// Choose right negation pattern (dependent of language).
		switch (this.sourceData.getLanguage()) {
		case "en":
			NEGATION_PATTERN = "(?<=\\b(never|no|nothing|nowhere|noone|none|not|havent|"
					+ "hasnt|hadnt|cant|couldnt|shouldnt|wont|"
					+ "wouldnt|dont|doesnt|didnt|isnt|arent|aint|n't)\\b)";
			break;
		default:
			return tokens;
		}

		// PUNCTUATION_PATTERN: the words that define the end of negation.
		// Escape \. for regex, and escape again for java ==> \\.
		String PUNCTUATION_PATTERN = "[(\\.),:;!\\?]";

		// Logic: Split at negation words, then again split each part at
		// punctuation word.
		// Then, add NEG-tag to each word between negation word and punctuation
		// word.
		// And add each word (all words, not only neg-tagged words) to the
		// tokens-list = represent the whole sentence.
		String[] parts = text.split(NEGATION_PATTERN);

		int k = 0;
		for (int i = 0; i < parts.length; i++) {

			String[] subparts = parts[i].split(PUNCTUATION_PATTERN);
			for (int j = 0; j < subparts.length; j++) {
				tokenizer = new StringTokenizer(subparts[j], TOKENIZER_DELIM);

				while (tokenizer.hasMoreTokens()) {
					String token = tokenizer.nextToken();

					// check if token at current position is equal to token in
					// tokenNegated-list:
					if (!token.equals(tokens.get(k)))
						System.out.println("FAIL");

					// i > 0: Begin after the first part, since negation starts
					// after split word.
					// j==0: Only the first subpart interests for negation,
					// before we splitted the parts[i].
					if (i > 0 && j == 0) {
						tokens.set(k, tokens.get(k) + " $NEG$");
					}
					k++;
				}

			}
		}

		return tokens;

	}

	/**
	 * POS tagging features
	 * 
	 * Words are tagged with their respective part-of-speech tag as determined
	 * by the Stanford parser
	 * 
	 * @param tokens
	 *            Tokenized text of the tweet
	 * @param tokensPOSTagged
	 *            Tokenized text of the tweet, possibly with negations from the
	 *            previous step
	 * @return Reference to the second parameter, which now has POS annotations,
	 *         e.g. "love $NN$"
	 */
	private List<String> addPOSTags(List<String> tokens,
			List<String> tokensPOSTagged) {
		Tree stanfordTree;

		// Parser needs the tokens-list in a HasWord format
		List<HasWord> sentence = new ArrayList<HasWord>();
		for (String token : tokens) {
			sentence.add(new Word(token));
		}

		// Parse the sentence
		stanfordTree = lexicalizedParser.apply(sentence);

		// add results (POS tags) in tokensPOSTagged-list
		int i = 0;
		for (CoreLabel label : stanfordTree.taggedLabeledYield()) {
			tokensPOSTagged.set(i,
					tokensPOSTagged.get(i) + " $" + label.toString("value")
							+ "$");
			i++;
		}

		return tokensPOSTagged;

	}

	/**
	 * Add n-gram features
	 * 
	 * An n-gram is a sequence of n words from a text. For example, the text
	 * "I love dogs" contains three unigrams, or 1-grams, "I", "love", and
	 * "dogs", two bigrams, or 2-grams, "I love" and "love dogs" and one 3-gram,
	 * "I love dogs".
	 * 
	 * This method walks through the tokenized text. Depending on whether the
	 * respective variables were set to true in the constructor parameter, the
	 * methods addBigramsFeature, addTrigramsFeature, and addFourgramsFeature
	 * are called to make the respective n-grams features.
	 * 
	 * @param tokens
	 *            Text of the tweet, after tokenization
	 * @param featureVector
	 *            The feature vector before word n-gram features have been added
	 * @param addNewFeatures
	 *            Whether new features (e.g. previously unknown bigrams) should
	 *            be added to the model (as during training) or not (as during
	 *            testing and classification).
	 * @return The feature vector after word n-gram features have been added
	 */
	private SparseMatrix generateNGramsFeature(List<String> tokens,
			SparseMatrix featureVector, boolean addNewFeatures) {

		// First token
		String token1;
		// Second token; used in bigrams,trigrams,4-grams
		String token2 = null;
		// Third token: used in trigrams and 4-grams
		String token3 = null;
		// 4th token: used in 4-grams
		String token4 = null;

		// Logic: We go in groups of four tokens through the list of tokens.
		// After one iteration, set token1 to token2, set token2 to token3 and
		// so on, and get for token4 the next token.

		// get iterator of tokens
		Iterator<String> iterator = tokens.iterator();

		// get init tokens
		if (iterator.hasNext()) {
			token2 = iterator.next();
			token2 = token2.toLowerCase();
		}
		if (iterator.hasNext()) {
			token3 = iterator.next();
			token3 = token3.toLowerCase();
		}
		if (iterator.hasNext()) {
			token4 = iterator.next();
			token4 = token4.toLowerCase();
		}

		for (int i = 0; i < tokens.size(); i++) {
			// ** a) move the group of tokens to the right by one position:
			token1 = token2;
			// a handling for token1 is not necessary, since we break in
			// for-loop when we are at the end of list.

			// if token3 is currently null, then we know, that after the
			// movement, token2 will be also null.
			if (token3 != null) {
				token2 = token3;
			} else {
				token2 = null;
			}
			if (token4 != null) {
				token3 = token4;
			} else {
				token3 = null;
			}
			if (iterator.hasNext()) {
				token4 = iterator.next();
				token4 = token4.toLowerCase();
			} else {
				token4 = null;
			}

			// ** b) now we can use token1, token2, token3, token4:

			// currently, createFeatureVector calls addUnigramsFeature directly,
			// but it could also be called here:
			// generate unigram features
			// if (useUnigrams) {
			// featureVector = addUnigramsFeature(token1, featureVector,
			// addNewFeatures);
			// }

			// generate bigram features
			if (useBigrams && token2 != null) {
				featureVector = addBigramsFeature(token1, token2,
						featureVector, addNewFeatures);
			}

			// generate trigram features
			if (useTrigrams && token3 != null) {
				featureVector = addTrigramsFeature(token1, token2, token3,
						featureVector, addNewFeatures);
			}

			// generate 4-gram features
			if (use4Grams && token4 != null) {
				featureVector = add4GramsFeature(token1, token2, token3,
						token4, featureVector, addNewFeatures);
			}

		}

		return featureVector;
	}

	/**
	 * Add unigram features
	 * 
	 * A unigram is a word from a text. For example, the sentence "I love dogs"
	 * contains three unigrams, or 1-grams, "I", "love", and "dogs".
	 * 
	 * Depending on whether useUnigrams was set to true in the constructor
	 * parameter, each unigram becomes a feature.
	 * 
	 * @param nlpTokens
	 *            Tweet text as list of tokens, possibly with annotations from
	 *            negation and POS tagging
	 * @param featureVector
	 *            The feature vector before word unigram features have been
	 *            added
	 * @param addNewFeatures
	 *            Whether new features (e.g. previously unknown bigrams) should
	 *            be added to the model (as during training) or not (as during
	 *            testing and classification).
	 * @return The feature vector after word unigram features have been added
	 */
	private SparseMatrix addUnigramsFeature(List<String> nlpTokens,
			SparseMatrix featureVector, boolean addNewFeatures) {
		int matrix_j;

		// Run through token list and create unigrams
		for (String token1 : nlpTokens) {

			// set prefix "$unigram$ for determineSentimentDetails, so that this
			// method knows the feature type.
			// By doing so, we do not have to change createFeatureVector.
			token1 = "$unigram$" + token1;

			// check if column exists in featureMatrix
			if (featureNames.containsKey(token1)) {
				// if so, get column number
				matrix_j = featureNames.get(token1);
				featureVector.set(0, matrix_j, 1);
			} else if (addNewFeatures == true) {
				// else, if we are growing the model, get number of new
				// column and add feature
				matrix_j = featureNames.size();
				featureNames.put(token1, matrix_j);
				featureVector.set(0, matrix_j, 1);
			}

		}
		return featureVector;

	}

	/**
	 * Add a bigram feature
	 * 
	 * @param token1
	 *            The first token in the bigram
	 * @param token2
	 *            The second token in the bigram
	 * @param featureVector
	 *            The feature vector before the bigram has been added
	 * @param addNewFeatures
	 *            Whether new features (e.g. previously unknown bigrams) should
	 *            be added to the model (as during training) or not (as during
	 *            testing and classification).
	 * @return The feature vector after the bigram has been added
	 */
	private SparseMatrix addBigramsFeature(String token1, String token2,
			SparseMatrix featureVector, boolean addNewFeatures) {
		int matrix_j;

		// Set prefix $bigram$ and whitespace as connector between words (since
		// we still split in whitespaces, there will be no conflict later)
		String token = "$bigram$" + token1 + " " + token2;

		// check if column exists in featureMatrix
		if (featureNames.containsKey(token)) {
			// if so, get column number
			matrix_j = featureNames.get(token);
			featureVector.set(0, matrix_j, 1);
		} else if (addNewFeatures == true) {
			// else, if we are growing the model, get number of new
			// column and add feature
			matrix_j = featureNames.size();
			featureNames.put(token, matrix_j);
			featureVector.set(0, matrix_j, 1);
		}

		return featureVector;
	}

	/**
	 * Add a trigram feature
	 * 
	 * @param token1
	 *            The first token in the trigram
	 * @param token2
	 *            The second token in the trigram
	 * @param token3
	 *            The third token in the trigram
	 * @param featureVector
	 *            The feature vector before the trigram has been added
	 * @param addNewFeatures
	 *            Whether new features (e.g. previously unknown trigrams) should
	 *            be added to the model (as during training) or not (as during
	 *            testing and classification).
	 * @return The feature vector before the trigram has been added
	 */
	private SparseMatrix addTrigramsFeature(String token1, String token2,
			String token3, SparseMatrix featureVector, boolean addNewFeatures) {
		int matrix_j;
		String token = "$trigram$" + token1 + " " + token2 + " " + token3;

		// check if column exists in featureMatrix
		if (featureNames.containsKey(token)) {
			// if so, get column number
			matrix_j = featureNames.get(token);
			featureVector.set(0, matrix_j, 1);
		} else if (addNewFeatures == true) {
			// else, if we are growing the model, get number of new
			// column and add feature
			matrix_j = featureNames.size();
			featureNames.put(token, matrix_j);
			featureVector.set(0, matrix_j, 1);
		}

		return featureVector;
	}

	/**
	 * Add a fourgram feature
	 * 
	 * @param token1
	 *            The first token in the fourgram
	 * @param token2
	 *            The second token in the fourgram
	 * @param token3
	 *            The third token in the fourgram
	 * @param token4
	 *            The fourth token in the fourgram
	 * @param featureVector
	 *            The feature vector before the fourgram has been added
	 * @param addNewFeatures
	 *            Whether new features (e.g. previously unknown fourgrams)
	 *            should be added to the model (as during training) or not (as
	 *            during testing and classification).
	 * @return The feature vector before the fourgram has been added
	 */
	private SparseMatrix add4GramsFeature(String token1, String token2,
			String token3, String token4, SparseMatrix featureVector,
			boolean addNewFeatures) {
		int matrix_j;
		String token = "$fourgram$" + token1 + " " + token2 + " " + token3
				+ " " + token4;

		// check if column exists in featureMatrix
		if (featureNames.containsKey(token)) {
			// if so, get column number
			matrix_j = featureNames.get(token);
			featureVector.set(0, matrix_j, 1);
		} else if (addNewFeatures == true) {
			// else, if we are growing the model, get number of new
			// column and add feature
			matrix_j = featureNames.size();
			featureNames.put(token, matrix_j);
			featureVector.set(0, matrix_j, 1);
		}

		return featureVector;
	}

}
