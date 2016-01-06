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
package com.news;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import com.restservice.dto.NewsItem;
import com.restservice.util.PropertiesUtil;

/**
 * 
 * Stopwords are from http://norm.al/2009/04/14/list-of-english-stop-words/ and
 * http://solariz.de/de/deutsche_stopwords.htm
 * 
 * @author olaf
 *
 */
public class NewsUtil {

	private static final String NEWS_PROPERTIES_FILE_PATH = System
			.getProperty("user.home") + "/news.properties";

	public static Integer TOPNEWSCOUNT = 3;

	public static String[] DATEFORMATS = { "\\d.\\m.\\y" }; // "\\m/\\d/\\y"

	public static String[][] SEARCHPATHS = {
			{ "Bing Web",
					"http://www.bing.com/search?q=\\keyword+\\date&format=RSS" },
			{ "Bing News",
					"http://www.bing.com/news/search?q=\\keyword+\\date&format=RSS" },
			{
					"Google News",
					"https://news.google.com/news/feeds?pz=1&cf=all&as_qdr=a&ned=de&hl=de&q=\\keyword+\\date&output=rss" } };

	private static String[] STOPWORDSARRAY = { "a", "about", "above", "above",
			"across", "after", "afterwards", "again", "against", "all",
			"almost", "alone", "along", "already", "also", "although",
			"always", "am", "among", "amongst", "amoungst", "amount", "an",
			"and", "another", "any", "anyhow", "anyone", "anything", "anyway",
			"anywhere", "are", "around", "as", "at", "back", "be", "became",
			"because", "become", "becomes", "becoming", "been", "before",
			"beforehand", "behind", "being", "below", "beside", "besides",
			"between", "beyond", "bill", "both", "bottom", "but", "by", "call",
			"can", "cannot", "cant", "co", "con", "could", "couldnt", "cry",
			"de", "describe", "detail", "do", "done", "down", "due", "during",
			"each", "eg", "eight", "either", "eleven", "else", "elsewhere",
			"empty", "enough", "etc", "even", "ever", "every", "everyone",
			"everything", "everywhere", "except", "few", "fifteen", "fify",
			"fill", "find", "fire", "first", "five", "for", "former",
			"formerly", "forty", "found", "four", "from", "front", "full",
			"further", "get", "give", "go", "had", "has", "hasnt", "have",
			"he", "hence", "her", "here", "hereafter", "hereby", "herein",
			"hereupon", "hers", "herself", "him", "himself", "his", "how",
			"however", "hundred", "ie", "if", "in", "inc", "indeed",
			"interest", "into", "is", "it", "its", "itself", "keep", "last",
			"latter", "latterly", "least", "less", "ltd", "made", "many",
			"may", "me", "meanwhile", "might", "mill", "mine", "more",
			"moreover", "most", "mostly", "move", "much", "must", "my",
			"myself", "name", "namely", "neither", "never", "nevertheless",
			"next", "nine", "no", "nobody", "none", "noone", "nor", "not",
			"nothing", "now", "nowhere", "of", "off", "often", "on", "once",
			"one", "only", "onto", "or", "other", "others", "otherwise", "our",
			"ours", "ourselves", "out", "over", "own", "part", "per",
			"perhaps", "please", "put", "rather", "re", "same", "see", "seem",
			"seemed", "seeming", "seems", "serious", "several", "she",
			"should", "show", "side", "since", "sincere", "six", "sixty", "so",
			"some", "somehow", "someone", "something", "sometime", "sometimes",
			"somewhere", "still", "such", "system", "take", "ten", "than",
			"that", "the", "their", "them", "themselves", "then", "thence",
			"there", "thereafter", "thereby", "therefore", "therein",
			"thereupon", "these", "they", "thickv", "thin", "third", "this",
			"those", "though", "three", "through", "throughout", "thru",
			"thus", "to", "together", "too", "top", "toward", "towards",
			"twelve", "twenty", "two", "un", "under", "until", "up", "upon",
			"us", "very", "via", "was", "we", "well", "were", "what",
			"whatever", "when", "whence", "whenever", "where", "whereafter",
			"whereas", "whereby", "wherein", "whereupon", "wherever",
			"whether", "which", "while", "whither", "who", "whoever", "whole",
			"whom", "whose", "why", "will", "with", "within", "without",
			"would", "yet", "you", "your", "yours", "yourself", "yourselves",
			"the", "ab", "bei", "da", "deshalb", "ein", "für", "haben", "hier",
			"ich", "ja", "kann", "machen", "muesste", "nach", "oder", "seid",
			"sonst", "und", "vom", "wann", "wenn", "wie", "zu", "bin", "eines",
			"hat", "manche", "solches", "an", "anderm", "bis", "das", "deinem",
			"demselben", "dir", "doch", "einig", "er", "eurer", "hatte",
			"ihnen", "ihre", "ins", "jenen", "keinen", "manchem", "meinen",
			"nichts", "seine", "soll", "unserm", "welche", "werden", "wollte",
			"während", "alle", "allem", "allen", "aller", "alles", "als",
			"also", "am", "ander", "andere", "anderem", "anderen", "anderer",
			"anderes", "andern", "anders", "auch", "auf", "aus", "bist",
			"bsp.", "daher", "damit", "dann", "dasselbe", "dazu", "daß",
			"dein", "deine", "deinen", "deiner", "deines", "dem", "den",
			"denn", "denselben", "der", "derer", "derselbe", "derselben",
			"des", "desselben", "dessen", "dich", "die", "dies", "diese",
			"dieselbe", "dieselben", "diesem", "diesen", "dieser", "dieses",
			"dort", "du", "durch", "eine", "einem", "einen", "einer", "einige",
			"einigem", "einigen", "einiger", "einiges", "einmal", "es",
			"etwas", "euch", "euer", "eure", "eurem", "euren", "eures", "ganz",
			"ganze", "ganzen", "ganzer", "ganzes", "gegen", "gemacht",
			"gesagt", "gesehen", "gewesen", "gewollt", "hab", "habe", "hatten",
			"hin", "hinter", "ihm", "ihn", "ihr", "ihrem", "ihren", "ihrer",
			"ihres", "im", "in", "indem", "ist", "jede", "jedem", "jeden",
			"jeder", "jedes", "jene", "jenem", "jener", "jenes", "jetzt",
			"kein", "keine", "keinem", "keiner", "keines", "konnte", "können",
			"könnte", "mache", "machst", "macht", "machte", "machten", "man",
			"manchen", "mancher", "manches", "mein", "meine", "meinem",
			"meiner", "meines", "mich", "mir", "mit", "muss", "musste", "müßt",
			"nicht", "noch", "nun", "nur", "ob", "ohne", "sage", "sagen",
			"sagt", "sagte", "sagten", "sagtest", "sehe", "sehen", "sehr",
			"seht", "sein", "seinem", "seinen", "seiner", "seines", "selbst",
			"sich", "sicher", "sie", "sind", "so", "solche", "solchem",
			"solchen", "solcher", "sollte", "sondern", "um", "uns", "unse",
			"unsen", "unser", "unses", "unter", "viel", "von", "vor", "war",
			"waren", "warst", "was", "weg", "weil", "weiter", "welchem",
			"welchen", "welcher", "welches", "werde", "wieder", "will", "wir",
			"wird", "wirst", "wo", "wolle", "wollen", "wollt", "wollten",
			"wolltest", "wolltet", "würde", "würden", "z.B.", "zum", "zur",
			"zwar", "zwischen", "über", "aber", "abgerufen", "abgerufene",
			"abgerufener", "abgerufenes", "acht", "allein", "allerdings",
			"allerlei", "allgemein", "allmählich", "allzu", "alsbald",
			"andererseits", "andernfalls", "anerkannt", "anerkannte",
			"anerkannter", "anerkanntes", "anfangen", "anfing", "angefangen",
			"angesetze", "angesetzt", "angesetzten", "angesetzter", "ansetzen",
			"anstatt", "arbeiten", "aufgehört", "aufgrund", "aufhören",
			"aufhörte", "aufzusuchen", "ausdrücken", "ausdrückt", "ausdrückte",
			"ausgenommen", "ausser", "ausserdem", "author", "autor", "außen",
			"außer", "außerdem", "außerhalb", "bald", "bearbeite",
			"bearbeiten", "bearbeitete", "bearbeiteten", "bedarf", "bedurfte",
			"bedürfen", "befragen", "befragte", "befragten", "befragter",
			"begann", "beginnen", "begonnen", "behalten", "behielt", "beide",
			"beiden", "beiderlei", "beides", "beim", "beinahe", "beitragen",
			"beitrugen", "bekannt", "bekannte", "bekannter", "bekennen",
			"benutzt", "bereits", "berichten", "berichtet", "berichtete",
			"berichteten", "besonders", "besser", "bestehen", "besteht",
			"beträchtlich", "bevor", "bezüglich", "bietet", "bisher",
			"bislang", "bis", "bleiben", "blieb", "bloss", "bloß", "brachte",
			"brachten", "brauchen", "braucht", "bringen", "bräuchte", "bzw",
			"böden", "ca.", "dabei", "dadurch", "dafür", "dagegen", "dahin",
			"damals", "danach", "daneben", "dank", "danke", "danken", "dannen",
			"daran", "darauf", "daraus", "darf", "darfst", "darin", "darum",
			"darunter", "darüber", "darüberhinaus", "dass", "davon", "davor",
			"demnach", "denen", "dennoch", "derart", "derartig", "derem",
			"deren", "derjenige", "derjenigen", "derzeit", "desto", "deswegen",
			"diejenige", "diesseits", "dinge", "direkt", "direkte", "direkten",
			"direkter", "doppelt", "dorther", "dorthin", "drauf", "drei",
			"dreißig", "drin", "dritte", "drunter", "drüber", "dunklen",
			"durchaus", "durfte", "durften", "dürfen", "dürfte", "eben",
			"ebenfalls", "ebenso", "ehe", "eher", "eigenen", "eigenes",
			"eigentlich", "einbaün", "einerseits", "einfach", "einführen",
			"einführte", "einführten", "eingesetzt", "einigermaßen", "eins",
			"einseitig", "einseitige", "einseitigen", "einseitiger", "einst",
			"einstmals", "einzig", "ende", "entsprechend", "entweder",
			"ergänze", "ergänzen", "ergänzte", "ergänzten", "erhalten",
			"erhielt", "erhielten", "erhält", "erneut", "erst", "erste",
			"ersten", "erster", "eröffne", "eröffnen", "eröffnet", "eröffnete",
			"eröffnetes", "etc", "etliche", "etwa", "fall", "falls", "fand",
			"fast", "ferner", "finden", "findest", "findet", "folgende",
			"folgenden", "folgender", "folgendes", "folglich", "fordern",
			"fordert", "forderte", "forderten", "fortsetzen", "fortsetzt",
			"fortsetzte", "fortsetzten", "fragte", "frau", "frei", "freie",
			"freier", "freies", "fuer", "fünf", "gab", "ganzem", "gar", "gbr",
			"geb", "geben", "geblieben", "gebracht", "gedurft", "geehrt",
			"geehrte", "geehrten", "geehrter", "gefallen", "gefiel",
			"gefälligst", "gefällt", "gegeben", "gehabt", "gehen", "geht",
			"gekommen", "gekonnt", "gemocht", "gemäss", "genommen", "genug",
			"gern", "gestern", "gestrige", "getan", "geteilt", "geteilte",
			"getragen", "gewissermaßen", "geworden", "ggf", "gib", "gibt",
			"gleich", "gleichwohl", "gleichzeitig", "glücklicherweise", "gmbh",
			"gratulieren", "gratuliert", "gratulierte", "gute", "guten",
			"gängig", "gängige", "gängigen", "gängiger", "gängiges",
			"gänzlich", "haette", "halb", "hallo", "hast", "hattest", "hattet",
			"heraus", "herein", "heute", "heutige", "hiermit", "hiesige",
			"hinein", "hinten", "hinterher", "hoch", "hundert", "hätt",
			"hätte", "hätten", "höchstens", "igitt", "immer", "immerhin",
			"important", "indessen", "info", "infolge", "innen", "innerhalb",
			"insofern", "inzwischen", "irgend", "irgendeine", "irgendwas",
			"irgendwen", "irgendwer", "irgendwie", "irgendwo", "je",
			"jedenfalls", "jederlei", "jedoch", "jemand", "jenseits", "jährig",
			"jährige", "jährigen", "jähriges", "kam", "kannst", "kaum",
			"keines", "keinerlei", "keineswegs", "klar", "klare", "klaren",
			"klares", "klein", "kleinen", "kleiner", "kleines", "koennen",
			"koennt", "koennte", "koennten", "komme", "kommen", "kommt",
			"konkret", "konkrete", "konkreten", "konkreter", "konkretes",
			"konnten", "könn", "könnt", "könnten", "künftig", "lag", "lagen",
			"langsam", "lassen", "laut", "lediglich", "leer", "legen", "legte",
			"legten", "leicht", "leider", "lesen", "letze", "letzten",
			"letztendlich", "letztens", "letztes", "letztlich", "lichten",
			"liegt", "liest", "links", "längst", "längstens", "mag", "magst",
			"mal", "mancherorts", "manchmal", "mann", "margin", "mehr",
			"mehrere", "meist", "meiste", "meisten", "meta", "mindestens",
			"mithin", "mochte", "morgen", "morgige", "muessen", "muesst",
			"musst", "mussten", "muß", "mußt", "möchte", "möchten", "möchtest",
			"mögen", "möglich", "mögliche", "möglichen", "möglicher",
			"möglicherweise", "müssen", "müsste", "müssten", "müßte",
			"nachdem", "nacher", "nachhinein", "nahm", "natürlich", "nacht",
			"neben", "nebenan", "nehmen", "nein", "neu", "neue", "neuem",
			"neuen", "neuer", "neues", "neun", "nie", "niemals", "niemand",
			"nimm", "nimmer", "nimmt", "nirgends", "nirgendwo", "nutzen",
			"nutzt", "nutzung", "nächste", "nämlich", "nötigenfalls", "nützt",
			"oben", "oberhalb", "obgleich", "obschon", "obwohl", "oft", "per",
			"pfui", "plötzlich", "pro", "reagiere", "reagieren", "reagiert",
			"reagierte", "rechts", "regelmäßig", "rief", "rund", "sang",
			"sangen", "schlechter", "schließlich", "schnell", "schon",
			"schreibe", "schreiben", "schreibens", "schreiber", "schwierig",
			"schätzen", "schätzt", "schätzte", "schätzten", "sechs", "sect",
			"sehrwohl", "sei", "seit", "seitdem", "seite", "seiten", "seither",
			"selber", "senke", "senken", "senkt", "senkte", "senkten",
			"setzen", "setzt", "setzte", "setzten", "sicherlich", "sieben",
			"siebte", "siehe", "sieht", "singen", "singt", "sobald", "sodaß",
			"soeben", "sofern", "sofort", "sog", "sogar", "solange",
			"solc hen", "solch", "sollen", "sollst", "sollt", "sollten",
			"solltest", "somit", "sonstwo", "sooft", "soviel", "soweit",
			"sowie", "sowohl", "spielen", "später", "startet", "startete",
			"starteten", "statt", "stattdessen", "steht", "steige", "steigen",
			"steigt", "stets", "stieg", "stiegen", "such", "suchen",
			"sämtliche", "tages", "tat", "tatsächlich", "tatsächlichen",
			"tatsächlicher", "tatsächliches", "tausend", "teile", "teilen",
			"teilte", "teilten", "titel", "total", "trage", "tragen",
			"trotzdem", "trug", "trägt", "tun", "tust", "tut", "txt", "tät",
			"ueber", "umso", "unbedingt", "ungefähr", "unmöglich",
			"unmögliche", "unmöglichen", "unmöglicher", "unnötig", "unsem",
			"unser", "unsere", "unserem", "unseren", "unserer", "unseres",
			"unten", "unterbrach", "unterbrechen", "unterhalb", "unwichtig",
			"usw", "vergangen", "vergangene", "vergangener", "vergangenes",
			"vermag", "vermutlich", "vermögen", "verrate", "verraten",
			"verriet", "verrieten", "version", "versorge", "versorgen",
			"versorgt", "versorgte", "versorgten", "versorgtes",
			"veröffentlichen", "veröffentlicher", "veröffentlicht",
			"veröffentlichte", "veröffentlichten", "veröffentlichtes", "viele",
			"vielen", "vieler", "vieles", "vielleicht", "vielmals", "vier",
			"vollständig", "voran", "vorbei", "vorgestern", "vorher", "vorne",
			"vorüber", "völlig", "während", "wachen", "waere", "warum",
			"weder", "wegen", "weitere", "weiterem", "weiteren", "weiterer",
			"weiteres", "weiterhin", "weiß", "wem", "wen", "wenig", "wenige",
			"weniger", "wenigstens", "wenngleich", "wer", "werdet", "weshalb",
			"wessen", "wichtig", "wieso", "wieviel", "wiewohl", "willst",
			"wirklich", "wodurch", "wogegen", "woher", "wohin", "wohingegen",
			"wohl", "wohlweislich", "womit", "woraufhin", "woraus", "worin",
			"wurde", "wurden", "währenddessen", "wär", "wäre", "wären",
			"zahlreich", "zehn", "zeitweise", "ziehen", "zieht", "zog",
			"zogen", "zudem", "zuerst", "zufolge", "zugleich", "zuletzt",
			"zumal", "zurück", "zusammen", "zuviel", "zwanzig", "zwei",
			"zwölf", "ähnlich", "übel", "überall", "überallhin", "überdies",
			"übermorgen", "übrig", "übrigens" };

	private static List<String> STOPWORDS = Arrays.asList(STOPWORDSARRAY);

	/**
	 * properties file reading (or writing if not exists)
	 */
	static {
		// try to open and read properties file
		Properties props = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(NEWS_PROPERTIES_FILE_PATH);
			props.load(fis);

			String topnewscount = props.getProperty("news.topnewscount");
			String dateformats = props.getProperty("news.dateformats");
			String searchpaths = props.getProperty("news.searchpaths");
			String stopwordsarray = props.getProperty("news.stopwords");

			DATEFORMATS = PropertiesUtil
					.unserializeCVSToStringArray1Dim(dateformats);
			SEARCHPATHS = PropertiesUtil
					.unserializeCVSToStringArray2Dim(searchpaths);
			STOPWORDSARRAY = PropertiesUtil
					.unserializeCVSToStringArray1Dim(stopwordsarray);
			TOPNEWSCOUNT = Integer.parseInt(topnewscount);

			STOPWORDS = Arrays.asList(STOPWORDSARRAY);
		} catch (Exception e) {
			// try to write properties file
			System.out.println("Cannot load news properties from path "
					+ NEWS_PROPERTIES_FILE_PATH
					+ ". Using default values, creating default file.");
			e.printStackTrace();
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(NEWS_PROPERTIES_FILE_PATH);
				props.setProperty("news.topnewscount", TOPNEWSCOUNT.toString());
				props.setProperty("news.dateformats",
						PropertiesUtil.serializeArray1DimToCSV(DATEFORMATS));
				props.setProperty("news.searchpaths",
						PropertiesUtil.serializeArray2DimToCSV(SEARCHPATHS));
				props.setProperty("news.stopwords",
						PropertiesUtil.serializeArray1DimToCSV(STOPWORDSARRAY));
				props.store(fos, "News Properties");
			} catch (Exception e2) {
				// or just use default values
				System.out.println("Cannot create news properties at path "
						+ NEWS_PROPERTIES_FILE_PATH + ".");
				e2.printStackTrace();
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (Exception e3) {
						e3.printStackTrace();
					}
				}
			}

		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static Double rateText(String text,
			HashMap<String, Double> originaltextCounts) {
		Double rating = .0;
		if (text == null || originaltextCounts == null)
			return rating;
		HashMap<String, Double> textCounts = listWordProportions(text);
		java.util.Iterator<Entry<String, Double>> iterator = textCounts
				.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, Double> entry = iterator.next();
			if (originaltextCounts.containsKey(entry.getKey())) {
				rating = rating
						+ koehlerScoring(entry.getValue(),
								originaltextCounts.get(entry.getKey()));
				// System.out.println("== " + entry.getKey());
			}
		}
		return rating;
	}

	/**
	 * combines word occurrence proportions to a score fulfills the following
	 * conditions: - max. score is about 1 - score > 0 for valid inputs - if the
	 * words occurrence in both texts is about 20% it gives about 0.5 points -
	 * if the words occurrence in both texts is about 2% it gives about 0.2
	 * points - doesn't require the word occurrence proportions to be equal
	 * 
	 * @param textProportion
	 * @param originalTextProportion
	 * @return
	 */
	public static Double koehlerScoring(Double textProportion,
			Double originalTextProportion) {
		if (textProportion <= .0 || originalTextProportion <= .0
				|| textProportion > 1. || originalTextProportion > 1.)
			return .0;
		return Math.log(1.2 + (double) textProportion
				+ (double) originalTextProportion);
	}

	public static HashMap<String, Double> listWordProportions(String text) {
		HashMap<String, Double> occurrences = new HashMap<String, Double>();
		if (text == null)
			return occurrences;
		String[] words = text.toLowerCase().split("[^(\\p{L}|\\p{N})]+");
		Integer nonStopwordCount = 0;
		for (String word : words) {
			if (!STOPWORDS.contains(word) && word != "") {
				if (occurrences.containsKey(word)) {
					occurrences.put(word, occurrences.get(word) + 1);
				} else {
					occurrences.put(word, 1.);
					// System.out.println(word);
				}
				nonStopwordCount++;
			}
		}
		// normalize
		java.util.Iterator<Entry<String, Double>> iterator = occurrences
				.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, Double> entry = iterator.next();
			entry.setValue(entry.getValue() / nonStopwordCount);
		}
		return occurrences;
	}

	
	public static String[][] getProviderURLs(String keyword, Integer day,
			Integer month, Integer year) throws InterruptedException,
			ExecutionException {
		// start rss feed fetching threads
		String urls[][] = new String[SEARCHPATHS.length][2];
		for (String dateformat : DATEFORMATS) {
			// for different time formats
			String date = dateformat;
			date = date.replaceAll("\\\\d", day.toString());
			date = date.replaceAll("\\\\m", month.toString());
			date = date.replaceAll("\\\\y", year.toString());
			for (Integer i = 0; i < SEARCHPATHS.length; i++) {
				// and the different providers
				String provider = SEARCHPATHS[i][0];
				String url = SEARCHPATHS[i][1];
				try {
					keyword = (keyword != null) ? URLEncoder.encode(keyword,
							"UTF-8") : "";
					date = URLEncoder.encode(date, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				url = url.replaceAll("\\\\keyword", keyword);
				url = url.replaceAll("\\\\date", date);
				urls[i][0] = provider;
				urls[i][1] = url;
			}
		}
		return urls;
	}
	
	public static ArrayList<NewsItem> filterNewsByRating(
			ArrayList<NewsItem> accumulatedFeeds) {
		// sort feeds by rating
		Collections.sort(accumulatedFeeds, new Comparator<NewsItem>() {
			public int compare(NewsItem f1, NewsItem f2) {
				return Double.compare(f2.getRating(), f1.getRating());
			}
		});
		// return the top news feeds
		ArrayList<NewsItem> topFeeds = new ArrayList<NewsItem>();
		for (Integer i = 0; i < NewsUtil.TOPNEWSCOUNT
				&& i < accumulatedFeeds.size(); i++) {
			topFeeds.add(accumulatedFeeds.get(i));
		}
		return topFeeds;
	}
}
