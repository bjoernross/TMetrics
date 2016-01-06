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
//Speichert einzelne Tweets zu einem Suchbegriff ab
function saveTweets(searchId, searchItem, subtitle, paramString) {
	return $.Deferred(function () {
		var self = this;
		var query = "/rest/results/tweets/?id="+searchId + "&limit=" + TWEET_LIMIT + "&" + paramString;
		var xhro = new XHRObject(query, "GET", XHR_PRIORITY_ACTIVE, 
	    	//doneFunction
			function(response) {
				//DEPRECATED: Alter Aufruf
				$("#modalTweet").data("tweets", response.data);
				$("#modalTweet").data("pointer", -1); //Position des zuunterst angezeigten Tweets im Tweet-Array (= -1 falls noch nichts angezeigt ist)
				
				$("#modalTweetSearchItem").text(searchItem); //Name des Suchbegriffs im Titel eintragen
				$("#modalTweetCount").text(response.data.length); //Anzahl der Tweets im Panel Footer
				$("#modalTweetSubtitle").text(subtitle); //Untertitel des modalen Dialogs aktualisieren
				self.resolve();
			},
	    	//failFunction
	    	function(event) {
				//Falls die Anfrage abgebrochen wird, soll keine Fehlermeldung erscheinen
				if(event.statusText !== "abort") {
					showServerError(7, $("#alertContainer"));			
				}
	    	}
	    );

	    //In die XHRObkjekt-Verwaltung einfuegen
	    addXHRObject(xhro);
	});
}

//Zeigt Tweets an, die am modalen Dialog gespeichert sind. 
//Bei jedem Aufruf werden TWEET_LOADING mehr Tweets angezeigt, bis
function showTweets() {
	var pointer = $("#modalTweetBody").children().length; //Anzahl der bisher angezeigten Tweets
	var totalTweets = ($("#modalTweet").data("tweets") === undefined) ? 0 : $("#modalTweet").data("tweets").length; //Der Check auf undefined ist noetig, da sonst bei Scrollen und gleichzeitgem wegklicken des Dialogs showTweets aufgerufen wird, aber keine Tweets mehr vorhanden sind
	if (pointer >= totalTweets) { //Nicht mehr Tweets laden als da sind
		return;
	}
	var tweetData = $("#modalTweet").data("tweets");
	for (var i = pointer; i < pointer + TWEET_LOADING; i++) { //TWEET_LOADING Tweets ans untere Ende des Popups anfuegen
		if (i < totalTweets) { //nur hinzufuegen, falls Daten vorliegen
			//Als anzuzeigende Sprache nehme den vom RestService zur Verfuegung gestellten String, falls dieser existiert; sonst nehme den ISO Code
			var lang = (tweetData[i].tweet.lang.string === null) ?  tweetData[i].tweet.lang.iso_code : tweetData[i].tweet.lang.string;
			//Generiert einen Smiley passend zum Sentiment String (z.B. lachend, wenn positiv)
			var smiley = "&#128527;"; //Standard ist der neutrale Smiley
			if (tweetData[i].tweet.sentiment.string === "positive") {
				smiley = "&#128522;";
			} else if (tweetData[i].tweet.sentiment.string === "negative") {
				smiley = "&#128542;";
			}

			// Fuege ein neues Panel fuer diesen Tweet hinzu
			appendModalTweetPanel(tweetData[i].tweet.id,
					tweetData[i].tweet.text, 
					tweetData[i].user.id,
					tweetData[i].user.name, 
					tweetData[i].user.screen_name,
					tweetData[i].tweet.created_at,
					tweetData[i].tweet.retweet_count,
					tweetData[i].tweet.coordinate_longitude,
					tweetData[i].tweet.coordinate_latitude,
					lang,
					tweetData[i].tweet.sentiment.value,
					smiley,
					tweetData[i].tweet.sentimentFeatures);
		}
	}

	loadingTweets = false;
}

//News anzeigen
function showNews() {
	//NewsTab hinzufuegen
	$("#modalTweet .nav-tabs").append("<li id='newsTabHead'><a href='#newsTab' data-toggle='tab'>News</a></li>");
	$("#modalTweet .tab-content").append("<div class='tab-pane' id='newsTab'><div id='modalNewsBody' class='modal-body'></div></div>");
	//Daten einfuegen
	var news = $("#modalTweet").data("news");
	if (news !== null && news.news !== null) {
		$("#modalNewsBody").empty();
		$.each(news.news, function(i, val){
			appendNewsRow(val.provider, val.title, val.url, val.rating, val.text);
		});
	}
}

function appendNewsRow(provider, title, url, rating, text) {
	$("#modalNewsBody").append("" +
			"<div class='panel'>"+
				"<div class='panel-heading clearfix'>"+
					"<span>" + provider + "</span>" +
				"</div>"+
				"<div class='panel-body'>"+
				"<a href='" + url + "' target='_blank'>" + "<h4>" + title + "</h4>" + "</a>"  +
				    "<span>" + text + "</span>" +
				"</div>"+
				

			"</div>");
}

// der Einflussfaktor zum Sentiment-Wert basierend auf dem Wort-Dictionary
// liefert ein float zurück
function getSentimentWordsValue(features)
{
    if (features === null || features.words === null)
    {
        return 0;
    }
    
	// Wort-Sentiment: Anzahl positiver/negativer Woerter multipliziert mit dem entsprechenden Parameter (vom Modell bestimmt)
    var numPositiveWords = features.words.positive_words.length;
    var numNegativeWords = features.words.negative_words.length;

    var positiveParam = features.words.pos_count_param;
    var negativeParam = features.words.neg_count_param;

    var positiveSentiment = numPositiveWords * positiveParam;
    var negativeSentiment = numNegativeWords * negativeParam;

	// Generischer Einflussfaktor: wieder Produkt aus Anzahl Wörter und dem bestimmten Parameter
    var sentiment = features.words.sentiment * features.words.sentiment_param;

    var totalSentiment = positiveSentiment + negativeSentiment + sentiment;
    
    return totalSentiment;
}

// der Einflussfaktor zum Sentiment-Wert basierend auf dem Emoticon-Dictionary
// liefert ein float zurück
function getSentimentEmoticonsValue(features)
{
    if (features === null || features.emoticons === null)
    {
        return 0;
    }
    
	// Emoticon-Sentiment: Anzahl positiver/negativer Emoticons multipliziert mit dem entsprechenden Parameter (vom Modell bestimmt)
    var numPositiveEmoticons = features.emoticons.positive_words.length;
    var numNegativeEmoticons = features.emoticons.negative_words.length;

    var positiveParam = features.emoticons.pos_count_param;
    var negativeParam = features.emoticons.neg_count_param;

    var positiveSentiment = numPositiveEmoticons * positiveParam;
    var negativeSentiment = numNegativeEmoticons * negativeParam;

	// Generischer Einflussfaktor: wieder Produkt aus Anzahl Wörter und dem bestimmten Parameter
    var sentiment = features.emoticons.sentiment * features.emoticons.sentiment_param;

    var totalSentiment = positiveSentiment + negativeSentiment + sentiment;
    
    return totalSentiment;
}

// der Einflussfaktor zum Sentiment-Wert basierend auf den Unigrams (Ausdrücken bestehend aus einem Wort)
// Wert eines Unigrams wurde vom Modell bestimmt
// liefert ein float zurück
function getSentimentUnigramsValue(features)
{
    if (features === null || features.unigrams === null)
    {
        return 0;
    }
    
    var totalSentiment = 0.0;
    
	// alle Unigram-Werte aufsummieren
    for (i in features.unigrams)
    {
        totalSentiment += features.unigrams[i].parameter;
    }
    
    return totalSentiment;
}

// der Einflussfaktor zum Sentiment-Wert basierend auf den Bigrams (Ausdrücken bestehend aus zwei Woertern)
// Wert eines Unigrams wurde vom Modell bestimmt
// liefert ein float zurück
function getSentimentBigramsValue(features)
{
    if (features === null || features.bigrams === null)
    {
        return 0;
    }
    
    var totalSentiment = 0.0;
        
	// alle Bigram-Werte aufsummieren
    for (i in features.bigrams)
    {
        totalSentiment += features.bigrams[i].parameter;
    }
    
    return totalSentiment;
}

// der Einflussfaktor zum Sentiment-Wert basierend auf den Trigrams (Ausdrücken bestehend aus drei Woertern)
// Wert eines Unigrams wurde vom Modell bestimmt
// liefert ein float zurück
function getSentimentTrigramsValue(features)
{
    if (features === null || features.trigrams === null)
    {
        return 0;
    }
    
    var totalSentiment = 0.0;
    
	// alle Trigram-Werte aufsummieren
    for (i in features.trigrams)
    {
        totalSentiment += features.trigrams[i].parameter;
    }
    
    return totalSentiment;
}

// der Einflussfaktor zum Sentiment-Wert basierend auf den Fourgrams (Ausdrücken bestehend aus vier Woertern)
// Wert eines Unigrams wurde vom Modell bestimmt
// liefert ein float zurück
function getSentimentFourgramsValue(features)
{
    if (features === null || features.fourgrams === null)
    {
        return 0;
    }
    
    var totalSentiment = 0.0;
        
	// alle Fourgram-Werte aufsummieren
    for (i in features.fourgrams)
    {
        totalSentiment += features.fourgrams[i].parameter;
    }
    
    return totalSentiment;
}

// produziert die Detail-Ausgabe des Reiters zum Einflussfaktors Wort-Dictionary
// beinhaltet die dynamische Erzeugung von HTML-Code
// liefert einen String zurück
function getSentimentWords(features)
{
    if (features === null)
    {
        return "No sentiment model available";
    }

    var result = "";

    if (features.words != null)
    {
        var numPositiveWords = features.words.positive_words.length;
        var numNegativeWords = features.words.negative_words.length;

        var positiveParam = features.words.pos_count_param;
        var negativeParam = features.words.neg_count_param;

        var positiveSentiment = numPositiveWords * positiveParam;
        var negativeSentiment = numNegativeWords * negativeParam;

        var sentiment = features.words.sentiment * features.words.sentiment_param;

        var totalSentiment = positiveSentiment + negativeSentiment + sentiment;

        if (numPositiveWords > 0)
        {
            result += "Sentiment from positive words: " + positiveSentiment + "<br>";
            result += "Influenced by " + numPositiveWords + " positive words (parameter " + positiveParam + ")<br>";
            result += "<br>";
        }
        
        if (numNegativeWords > 0)
        {
            result += "Sentiment from negative words: " + negativeSentiment + "<br>";
            result += "Influenced by " + numNegativeWords + " negative words (parameter " + negativeParam + ")<br>";
            result += "<br>";
        }
        
        result += "Derived sentiment: " + sentiment + "<br>";
        result += "<br>";
        
		// Tabelle aufmachen, falls mindestens ein positives oder negatives Wort vorhanden ist
        if (numPositiveWords > 0 || numNegativeWords > 0)
        {
            result += "<table class='table'>";
            result +=     "<tr>";
            result +=         "<th>Positive Words</th>";
            result +=         "<th>Negative Words</th>";
            result +=     "</tr>";
            
			// Gemeinsame Tabelle für positive und negative Wörter: Zeilen auch hinzufügen, falls keine Einträge existieren
            var numLines = Math.max(features.words.positive_words.length, features.words.negative_words.length);
            
            for (var i = 0; i < numLines; i++)
            {
                result += "<tr>";
                
                if (i < features.words.positive_words.length)
                {
                    result += "<td>" + features.words.positive_words[i] + "</td>";
                }
                else
                {
                    result += "<td></td>";
                }
                
                if (i < features.words.negative_words.length)
                {
                    result += "<td>" + features.words.negative_words[i] + "</td>";
                }
                else
                {
                    result += "<td></td>";
                }
                
                result += "</tr>";
            }

            result += "</table>";
        }
    }
    
    return result;
}

// produziert die Detail-Ausgabe des Reiters zum Einflussfaktors Emoticon-Dictionary
// beinhaltet die dynamische Erzeugung von HTML-Code
// liefert einen String zurück
function getSentimentEmoticons(features)
{
    if (features === null)
    {
        return "No sentiment model available";
    }

    var result = "";

    if (features.emoticons != null)
    {
        var numPositiveEmoticons = features.emoticons.positive_words.length;
        var numNegativeEmoticons = features.emoticons.negative_words.length;

        var positiveParam = features.emoticons.pos_count_param;
        var negativeParam = features.emoticons.neg_count_param;

        var positiveSentiment = numPositiveEmoticons * positiveParam;
        var negativeSentiment = numNegativeEmoticons * negativeParam;

        var sentiment = features.emoticons.sentiment * features.emoticons.sentiment_param;

        var totalSentiment = positiveSentiment + negativeSentiment + sentiment;

        if (numPositiveEmoticons > 0)
        {
            result += "Sentiment from positive emoticons: " + positiveSentiment.toFixed(4) + "<br>";
            result += "Influenced by " + numPositiveEmoticons + " positive emoticons (parameter " + positiveParam.toFixed(4) + ")<br>";
            result += "<br>";
        }
        
        if (numNegativeEmoticons > 0)
        {
            result += "Sentiment from negative emoticons: " + negativeSentiment.toFixed(4) + "<br>";
            result += "Influenced by " + numNegativeEmoticons + " negative emoticons (parameter " + negativeParam.toFixed(4) + ")<br>";
            result += "<br>";
        }
        
        result += "Derived sentiment: " + sentiment.toFixed(4) + "<br>";
        result += "<br>";
        
		// Tabelle aufmachen, falls mindestens ein positives oder negatives Emoticon vorhanden ist
        if (numPositiveEmoticons > 0 || numNegativeEmoticons > 0)
        {
            result += "<table class='table'>";
            result +=     "<tr>";
            result +=         "<th>Positive Emoticons</th>";
            result +=         "<th>Negative Emoticons</th>";
            result +=     "</tr>";
            
			// Gemeinsame Tabelle fuer positive und negative Emoticons: Zeilen auch hinzufuegen, falls keine Eintraege vorhanden sind
            var numLines = Math.max(features.emoticons.positive_words.length, features.emoticons.negative_words.length);
            
            for (var i = 0; i < numLines; i++)
            {
                result += "<tr>";
                
                if (i < features.emoticons.positive_words.length)
                {
                    result += "<td>" + features.emoticons.positive_words[i] + "</td>";
                }
                else
                {
                    result += "<td></td>";
                }
                
                if (i < features.emoticons.negative_words.length)
                {
                    result += "<td>" + features.emoticons.negative_words[i] + "</td>";
                }
                else
                {
                    result += "<td></td>";
                }
                
                result += "</tr>";
            }

            result += "</table>";
        }
    }
    
    return result;
}

// produziert die Detail-Ausgabe des Reiters zum Einflussfaktors Unigrams
// beinhaltet die dynamische Erzeugung von HTML-Code
// liefert einen String zurück
function getSentimentUnigrams(features, id, language)
{
    if (features === null)
    {
        return "No sentiment model available";
    }

    var result = "";

    result += "<table class='table'>";
    result +=     "<tr>";
    result +=         "<th>Unigram</th>";
    result +=         "<th>Sentiment</th>";
    result +=     "</tr>";

	// fuer jedes Unigram stelle das Wort und seinen Parameter dar
    if (features.unigrams != null)
    {
        for (i in features.unigrams)
        {
            var string = features.unigrams[i].string;
            var parameter = features.unigrams[i].parameter;
			
			var dataString = string.replace(/#/g, "%23");
        
            if (parameter != 0 || true)
            {
                result += "<tr>";            	
		        result +=     "<td>" + "<span id='tweet" + id + "unigram" + i + "' data-feature='" + dataString + "' data-language='" + language + "' class='sentimentFeatureUnigram'>" + getColoredWord(string, parameter) + "</span>" + "</td>";
                result +=     "<td>" + parameter + "</td>";
                result += "</tr>";
            }

        }
    }

    result += "</table>";

    return result;
}

// produziert die Detail-Ausgabe des Reiters zum Einflussfaktors Bigrams
// beinhaltet die dynamische Erzeugung von HTML-Code
// liefert einen String zurück
function getSentimentBigrams(features, id, language)
{
    if (features === null)
    {
        return "No sentiment model available";
    }

    var result = "";
    var string, parameter;

    result += "<table class='table'>";
    result +=     "<tr>";
    result +=         "<th>Bigram</th>";
    result +=         "<th>Sentiment</th>";
    result +=     "</tr>";

	// fuer jedes Bigram stelle das Wortpaar und seinen Parameter dar
    if (features.bigrams != null)
    {
        for (i in features.bigrams)
        {
            var string = features.bigrams[i].string;
            var parameter = features.bigrams[i].parameter;
			
			var dataString = string.replace(/#/g, "%23");
        
            if (parameter != 0 || true)
            {
                result += "<tr>";            	
		        result +=     "<td>" + "<span id='tweet" + id + "bigram" + i + "' data-feature='" + dataString + "' data-language='" + language + "' class='sentimentFeatureBigram'>" + getColoredWord(string, parameter) + "</span>" + "</td>";
                result +=     "<td>" + parameter + "</td>";
                result += "</tr>";
            }

        }
    }

    result += "</table>";

    return result;
}

// produziert die Detail-Ausgabe des Reiters zum Einflussfaktors Trigrams
// beinhaltet die dynamische Erzeugung von HTML-Code
// liefert einen String zurück
function getSentimentTrigrams(features, id, language)
{
    if (features === null)
    {
        return "No sentiment model available";
    }

    var result = "";
    var string, parameter;

    result += "<table class='table'>";
    result +=     "<tr>";
    result +=         "<th>Trigram</th>";
    result +=         "<th>Sentiment</th>";
    result +=     "</tr>";

	// stelle fuer jedes Trigram das Wort-Tripel und seinen Parameter dar
    if (features.trigrams != null)
    {
        for (i in features.trigrams)
        {
            var string = features.trigrams[i].string;
            var parameter = features.trigrams[i].parameter;
			
			var dataString = string.replace(/#/g, "%23");
        
            if (parameter != 0 || true)
            {
                result += "<tr>";            	
		        result +=     "<td>" + "<span id='tweet" + id + "trigram" + i + "' data-feature='" + dataString + "' data-language='" + language + "' class='sentimentFeatureTrigram'>" + getColoredWord(string, parameter) + "</span>" + "</td>";
                result +=     "<td>" + parameter + "</td>";
                result += "</tr>";
            }

        }
    }

    result += "</table>";

    return result;
}

// produziert die Detail-Ausgabe des Reiters zum Einflussfaktors Wort-Dictionary
// beinhaltet die dynamische Erzeugung von HTML-Code
// liefert einen String zurück
function getSentimentFourgrams(features, id, language)
{
    if (features === null)
    {
        return "No sentiment model available";
    }

    var result = "";
    var string, parameter;

    result += "<table class='table'>";
    result +=     "<tr>";
    result +=         "<th>Fourgram</th>";
    result +=         "<th>Sentiment</th>";
    result +=     "</tr>";

	// fuer jedes Fourgram stelle das Wort-Quadrupel und seinen Parameter dar
    if (features.fourgrams != null)
    {
        for (i in features.fourgrams)
        {
            var string = features.fourgrams[i].string;
            var parameter = features.fourgrams[i].parameter;
			
			var dataString = string.replace(/#/g, "%23");
        
            if (parameter != 0 || true)
            {
                result += "<tr>";            	
		        result +=     "<td>" + "<span id='tweet" + id + "fourgram" + i + "' data-feature='" + dataString + "' data-language='" + language + "' class='sentimentFeatureFourgram'>" + getColoredWord(string, parameter) + "</span>" + "</td>";
                result +=     "<td>" + parameter + "</td>";
                result += "</tr>";
            }

        }
    }

    result += "</table>";

    return result;
}

// Eingabe: ngram: das einzufaerbende Wort
//               parameter: der zugehörige Parameter des Modells als Grundlage der Farbwahl
// Ausgabe: ngram in <span>-Tags, falls eingefärbt mit entsprechender Farb-Klasse
// jeweils vier Schattierungen von rot und grün abhängig vom Parameter
// neutrale Woerter: schwarz, d.h. keine Aenderung
function getColoredWord(ngram, parameter)
{
    var css = "";
	
	if (parameter > 0)
    {
        if (parameter > 0.05)
        {
            css = "sentimentColorPositive1";
        }
        else if (parameter > 0.04)
        {
            css = "sentimentColorPositive2";
        }
        else if (parameter > 0.025)
        {
            css = "sentimentColorPositive3";
        }
        else if (parameter > 0.01)
        {
            css = "sentimentColorPositive4";
        }
        else
        {
            return "<span>" + ngram + "</span>";
        }
    }
    else
    {
        if (parameter < -0.05)
        {
            css = "sentimentColorNegative1";
        }
        else if (parameter < -0.04)
        {
            css = "sentimentColorNegative2";
        }
        else if (parameter < -0.025)
        {
            css = "sentimentColorNegative3";
        }
        else if (parameter < -0.01)
        {
            css = "sentimentColorNegative4";
        }
        else
        {
            return "<span>" + ngram + "</span>";
        }
    }
    
    return "<span class='" + css + "'>" + ngram + "</span>";
}

// Eingabe: Text eines Tweets und Parameter des Sentiment-Modells
// Ausgabe: Derselbe Text inklusive HTML-Tags zur Einfärbung der Wörter gemäß ihres Sentiments
// Einfärbung mittels der Methode getColoredWord()
function getColoredText(tweetText, features)
{
	// keine Features vorhanden: Text kann nicht gefaerbt werden
	if (features == null || features.unigrams == null)
	{
		return tweetText;
	}

	// Text in Token aufteilen
    var text = "";
    var tokens = tweetText.split(" ");
	var token, searchToken;
	var parameter;
    
	// alle Token iterieren
	for (i in tokens)
	{
		token = tokens[i];
		
		// ngrams sind in lowercase und ohne Punktuation gespeichert
		// baue ein entsprechendes searchToken, um das Token in der Liste der Ngrams zu finden
		searchToken = token.replace(/[\.,-\/#!$%\^&\*;:{}=\-_`~()]/g, "");
		searchToken = searchToken.toLowerCase();
		
		parameter = 0;
		
		// falls Wort als Unigram enthalten, verwende den Parameter zum Einfaerben
		for (j in features.unigrams)
		{
			if (features.unigrams[j].string == searchToken)
			{
				parameter = features.unigrams[j].parameter;
			}
		}
		
		// benutze getColoredWord() zum Einfaerben und fuege das Wort dem Tweet-Text hinzu
		text += getColoredWord(token, parameter) + " ";
	}
    
    return text;
}

//Fuegt einen einzelnen Panel mit Tweet hinzu
function appendModalTweetPanel(id, text, userId, name, screenName, date, retweetCount, longitude, latitude, language, sentiment, smiley, sentimentFeatures) {
	var locationString = (longitude === 0 && latitude === 0) ? "-" : 
		"<a href='https://maps.google.com/maps?ll=" + longitude + "," + latitude + "&z=" + GOOGLEMAPS_ZOOM + "' target='_blank' >" +
			longitude + ", " + latitude +
		"</a>";
	$("#modalTweetBody").append("" +
	"<div id ='tweet" + id + "' class='panel'>"+
		"<div class='panel-heading clearfix'>"+
			"<span id='user" + userId + "tweet" + id + "' class='tweetAuthor user" + userId +"'><a>" + name + " (&#64;" + screenName + ")</a></span>"+
			"<span class='tweetDate'>" + formatDate(date) + "</span>" +
		"</div>"+
		"<div class='panel-body' data-toggle='collapse' href='#tweet" + id + "Body'>"+
			getColoredText(text, sentimentFeatures) +
		"</div>"+
		"<div id='tweet" + id + "Body' class='panel-footer tweetBody clearfix collapse'>"+
			"<span class='tweetRetweets tweetMetadata'>"+
				"<div>" + retweetCount +"</div>"+
				"<span>Retweets</span>"+
			"</span>"+
		 	"<span class='tweetCoordinates tweetMetadata'>"+
		 		"<div>" + locationString + "</div>"+
		 		"<span>Location</span>"+
	 		"</span>"+
		 	"<span class='tweetLanguage tweetMetadata'>"+
		 		"<div>" + language + "</div>"+
		 		"<span>Language</span>"+
	 		"</span>"+
			"<span class='tweetSentiment tweetMetadata'>"+
				"<div data-toggle='collapse' href='#sentimentFeatures" + id + "' data-placement='top' title='" + sentiment.toFixed(2) +"'>" + smiley + "</div>"+
				"<span>Sentiment</span>"+
			"</span>"+
		    "<div id='sentimentFeatures"+id +"' class='collapse'>" +

            "<ul class='nav nav-tabs'>" +
                "<li class='active tweetSentiment tweetSentimentdata'><a href='#sentimentWords"+ id + "' data-toggle='tab'>" + 
			"<div>" + 
				"<div>" + getSentimentWordsValue(sentimentFeatures).toFixed(4) + "</div>" + 
				"<span>Words</span>" + 
			"</div>" + 
		"</a></li>" +
                "<li class='tweetSentiment tweetSentimentdata'><a href='#sentimentEmos"+ id + "' data-toggle='tab'>" + 
			"<div>" + 
				"<div>" + getSentimentEmoticonsValue(sentimentFeatures).toFixed(4) + "</div>" + 
				"<span>Emoticons</span>" + 
			"</div>" + 
		"</a></li>" +
                "<li class='tweetSentiment tweetSentimentdata'><a href='#sentimentUnigrams"+ id + "' data-toggle='tab'>" +
			"<div>" + 
				"<div>" + getSentimentUnigramsValue(sentimentFeatures).toFixed(4) + "</div>" + 
				"<span>Unigrams</span>" + 
			"</div>" + 
		"</a></li>" +
                "<li class='tweetSentiment tweetSentimentdata'><a href='#sentimentBigrams"+ id + "' data-toggle='tab'>" + 
			"<div>" + 
				"<div>" + getSentimentBigramsValue(sentimentFeatures).toFixed(4) + "</div>" + 
				"<span>Bigrams</span>" + 
			"</div>" + 
		"</a></li>" +
                "<li class='tweetSentiment tweetSentimentdata'><a href='#sentimentTrigrams"+ id + "' data-toggle='tab'>" + 
			"<div>" + 
				"<div>" + getSentimentTrigramsValue(sentimentFeatures).toFixed(4) + "</div>" + 
				"<span>Trigrams</span>" + 
			"</div>" + 
		"</a></li>" +
                "<li class='tweetSentiment tweetSentimentdata'><a href='#sentimentFourgrams"+ id + "' data-toggle='tab'>" +
			"<div>" + 
				"<div>" + getSentimentFourgramsValue(sentimentFeatures).toFixed(4) + "</div>" + 
				"<span>Fourgrams</span>" + 
			"</div>" + 
		"</a></li>" +
            "</ul>" +

            "<div  class = 'tab-content'>" +
                "<div id='sentimentWords"+id +"' class='tab-pane active'>"+
                    "<div id='sentimentWords"+id +"body' class='modal-body'>"+
                    	getSentimentWords(sentimentFeatures) +
                    "</div>"+
                "</div>"+

                "<div id='sentimentEmos"+id +"' class='tab-pane'>"+
                    "<div id='sentimentEmos"+id +"body' class='modal-body'>"+
                    	getSentimentEmoticons(sentimentFeatures) +
                    "</div>"+
                "</div>"+

                "<div id='sentimentUnigrams"+id +"' class='tab-pane'>"+
                    "<div id='sentimentUnigrams"+id +"body' class='modal-body'>"+
                    	getSentimentUnigrams(sentimentFeatures, id, language) +
                    "</div>"+
                "</div>"+

                "<div id='sentimentBigrams"+id +"' class='tab-pane'>"+
                    "<div id='sentimentBigrams"+id +"body' class='modal-body'>"+
                    	getSentimentBigrams(sentimentFeatures, id, language) +
                    "</div>"+
                "</div>"+

                "<div id='sentimentTrigrams"+id +"' class='tab-pane'>"+
                    "<div id='sentimentTrigrams"+id +"body' class='modal-body'>"+
                    	getSentimentTrigrams(sentimentFeatures, id, language) +
                    "</div>"+
                "</div>"+

                "<div id='sentimentFourgrams"+id +"' class='tab-pane'>"+
                    "<div id='sentimentFourgrams"+id +"body' class='modal-body'>"+
                    	getSentimentFourgrams(sentimentFeatures, id, language) +
                    "</div>"+
                "</div>"+
            "</div>"+
        "</div>"+

	"</div>");
}

//Holt die important training tweets vom REST und zeigt diese an
function showImportantTrainingTweets(id, feature)
{
	if (!$("#" + id).data("popoverCreated"))
	{
		var string = $("#" + id).data("feature");
		var language = $("#" + id).data("language");
		
		console.log(string);
		
		var request = "/rest/results/trainingTweets/?feature=$" + feature + "$" + string;
		
		if (language != "undefined" && language != "")
		{
			request += "&language=" + language;
		}
	
		$.get(request)
		.done(function(response) {
			// popover erstellen
			createTrainingTweetPopover(id, response.data);
		})
		.fail(function()
		{
			showServerError();
		});
	}
}

// Erzeugt ein Popover zu einem Feature (mit Tweet Texts und Labels)
function createTrainingTweetPopover(id, tweets)
{
	// build popover content
	var sContent = "";

	for (var i in tweets)
	{
		var label = tweets[i].label;
		if (label != 0)
		{
			if (label == 0.5)
			{
				label = 0.02;
			}
			
			if (label == -0.5)
			{
				label = -0.02;
			}
		}
		//sContent += "<div>" + tweets[i].text + "</div><hr>";
		sContent += getColoredWord(tweets[i].text, label) + "<hr>";
	}
	
	if (sContent == "")
	{
		sContent = "<div>No training tweets available</div>";
	}
	else
	{
		sContent = "<div>Listing " + tweets.length + " training tweets</div><hr>" + sContent;
	}
	
	$("#" + id).popover({
		"html": true,
		"trigger": "click",
		"placement": "right",
		"content": sContent
	});
	
	$("#" + id).data("popoverCreated", true);
	
	$("#" + id).popover('toggle');
}

//Holt die User Metadaten vom REST Service (falls noetig) und zeigt diese an
function showUserMetadata(userId, tweetId) {
	if ($("#modalTweet").data("hasUser" + userId) === undefined) {
		var query = "/rest/results/user/?id="+userId;
		var xhro = new XHRObject(query, "GET", XHR_PRIORITY_ACTIVE, 
	    	//doneFunction
			function(response) {
				//popover erstellen
				createUserPopover(userId, tweetId, response.data.screen_name, response.data.url, response.data.created_at, 
					response.data.profile_image_url, response.data.description,  response.data.statuses_count,
					response.data.followers_count,  response.data.friends_count,  response.data.lang,  response.data.location);
				//flag setzen, welches anzeigt, ob bereits Daten vorhanden sind
				$("#modalTweet").data("hasUser"+userId, true);
				//popover anzeigen
				$("#user" + userId + "tweet" + tweetId + " a").popover('show');
			},
	    	//failFunction
	    	function(event) {
				//Falls die Anfrage abgebrochen wird, soll keine Fehlermeldung erscheinen
				if(event.statusText !== "abort") {
					showServerError(8, $("#alertContainer"));			
				}
	    	}
	    );

	    //In die XHRObkjekt-Verwaltung einfuegen
	    addXHRObject(xhro);
	}		
}

//Erzeugt ein Popover zu einem User (mit User Metadaten, ohne Anzeige)
function createUserPopover(userId, tweetId, screenName, link, date, image, description, tweets, follower, following, language, location) {
	//Schlechte Eingabedaten trotzdem vernuenftig anzeigen
	var linkString = (link !== null) ? 
		"<a href='" + link + "' class='userHomepageLink' data-toggle='tooltip' data-placement='bottom' title='User Homepage'>" +
			"<span class='glyphicon glyphicon-home'></span>" +
			"</a>" : "";
	var descriptionString = (description !== "") ? description : "<em>no user description.</em>";
	var locationString = (location !== "") ? location : "-";
	
	$(".user" + userId + " a").popover({
		"html": true,
		"trigger": "click",
		"placement": "bottom-right",
		"content": "" +
			"<div class='userInfo'>"+
				"<div class='userHeader clearfix'>"+
					"<a href='https://twitter.com/" + screenName +"' class='userTwitterLink' data-toggle='tooltip' data-placement='bottom' title='User profile on Twitter'>" +
						"<span>t</span>" +
					"</a>"+
					linkString +
					"<span class='userDate'>" + date + "</span>"+
				"</div>"+
				"<div class='userBody clearfix'>"+
					"<div class='userImgOuter'>"+
						"<div class='userImgInner'>"+
							"<img src='" + image + "' />"+
						"</div>"+
					"</div>"+
					"<span class='userDescription'>" + descriptionString + "</span>"+
				"</div>"+
				"<div class='userFooter clearfix'>"+
					"<div class='userTweets userMetadata'>"+
						"<div>" + tweets + "</div>"+
						"<span>Tweets</span>"+
					"</div>"+
					"<div class='userFollower userMetadata'>"+
						"<div>" + follower + "</div>"+
						"<span>Follower</span>"+
					"</div>"+
					"<div class='userFollowing userMetadata'>"+
						"<div>" + following + "</div>"+
						"<span>Following</span>"+
					"</div>"+
					"<div class='userLanguage userMetadata'>"+
						"<div>" + language + "</div>"+
						"<span>Language</span>"+
					"</div>"+
					"<div class='userLocation userMetadata'>"+
						"<div>" + locationString + "</div>"+
						"<span>Location</span>"+
					"</div>"+
				"</div>"+
			"</div>"
	});
}

// Formats a date and removes its milliseconds display
function formatDate(date) {
	return date.substring(0, date.length - 2);
}
