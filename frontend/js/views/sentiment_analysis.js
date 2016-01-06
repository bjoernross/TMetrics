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
function saveSearchItemSentiment(tabId, language) {
	var query ='/rest/results/sentiments/?id='+searchItems[tabId].id +
		((language === "") ? "" : "&lang=" + language);
    
    var dataObject = getDataObject(tabId, language);
    var priority = (activeViews[VIEW_SENTIMENT_ANALYSIS]) ? XHR_PRIORITY_ACTIVE : XHR_PRIORITY_PASSIVE;
    
    var xhro = new XHRObject(query, "GET", priority, 
    	//doneFunction
    	function(response) {
    		//Daten abspeichern
    		dataObject.view[VIEW_SENTIMENT_ANALYSIS].data = response.data;
    		dataObject.view[VIEW_SENTIMENT_ANALYSIS].isLoaded = true;
    		dataObject.view[VIEW_SENTIMENT_ANALYSIS].isRendered = false;
    		
    		$(document).trigger("tm.view.dataLoaded", [tabId, language, VIEW_SENTIMENT_ANALYSIS]);
    	},
    	//failFunction
    	function(event) {
			//Falls die Anfrage abgebrochen wird, soll keine Fehlermeldung erscheinen
			if(event.statusText !== "abort") {
				showServerError(10 + 10 * VIEW_SENTIMENT_ANALYSIS, getViewPanel(tabId, language, VIEW_SENTIMENT_ANALYSIS));
			}
    	}
    );
    dataObject.view[VIEW_SENTIMENT_ANALYSIS].xhro = xhro;
    
    //In die XHRObkjekt-Verwaltung einfuegen
    addXHRObject(xhro);
}

//Weiterverarbeitung zum Tweets anzeigen. Abspeichern und dann anzeigen.
function processTweetsSentiment(searchId, searchItem, sentiment) {
	$.when(saveTweetsSentiment(searchId, searchItem, sentiment)) //sentiment = "positive" | "negative" | "neutral"
	.done(function() {
    	showTweets();
    	$("#modalTweet").modal();
	});
}

//Einzelne Tweets mit Sentiment speichern
function saveTweetsSentiment(searchId, searchItem, sentiment) {
	return $.Deferred(function() {
		var self = this;

        paramString = "sent=" + sentiment;

        if(activeLanguage !== ""){
            paramString = paramString+'&lang='+activeLanguage;
        }

		subtitle = "Showing only " + sentiment + " tweets.";
		
		$.when(saveTweets(searchId, searchItem, subtitle, paramString))
		.done(function() {
			self.resolve();
		});
	});
}

//Element fuer die Stimmungsanalyse anzeigen und die Daten fuer 'searchItem' anzeigen
function showSingleSentiment(tabId, language) {
	//Benoetigte Variablen holen
	var searchId = searchItems[tabId].id;
	var searchItem = searchItems[tabId].name;
	
	var positive = getDataObject(tabId, language).view[VIEW_SENTIMENT_ANALYSIS].data.positive;
	var negative = getDataObject(tabId, language).view[VIEW_SENTIMENT_ANALYSIS].data.negative;
	var neutral = getDataObject(tabId, language).view[VIEW_SENTIMENT_ANALYSIS].data.neutral;
	
	//Damit highcharts die richtige Groesse uebergeben bekommt, muss das betreffende Panel sichtbar sein
	var panel = getViewPanel(tabId, language, VIEW_SENTIMENT_ANALYSIS);
	panel.show();

	//Es gibt nichts relevantes anzuzeigen
	if (positive == 0 && negative == 0 && neutral == 0) { 
		showNoDataWarning(panel);
		getViewObject(tabId, language, VIEW_SENTIMENT_ANALYSIS).isRendered = true;
		return;
	}
	
	
	
	//Balkendiagramm; absoluter Vergleich
	panel.find(".viewBody div:first").highcharts({
		chart: {
            type: 'column',
            events: {
            	load: function(event) {
            		if(DEBUG) console.log("sentimentAnalysis Rendered!");
            		getViewObject(tabId, language, VIEW_SENTIMENT_ANALYSIS).isRendered = true;
            	}
            }
        },
        title: {
            text: 'Absolute Sentiment Analysis for ' + searchItem
        },
        xAxis: {
            categories: [searchItem]
        },
        yAxis: {
            min: 0,
            title: {
                text: 'Number of Tweets'
            }
        },
        tooltip: {
            valueSuffix: ' tweets'
        },
        plotOptions: {
        	column: {
                pointPadding: 0.2,
                borderWidth: 0,
                events:{
                    click: function (event) { // Liste von Tweets als Popup anzeigen
                    	processTweetsSentiment(searchId, searchItem, this.name);
                    }
                }
            }
        },
        series: [{
            name: 'positive',
            data: [positive]
        }, {
            name: 'negative',
            data: [negative]
        }, {
            name: 'neutral',
            data: [neutral]
        }],
        colors: posnegneutralColors
    });
	
	//Kuchendiagramm; relativer Vergleich
	panel.find(".viewBody div:last").highcharts({
        title: {
            text: 'Relative Sentiment Analysis for ' + searchItem
        },
        tooltip: {
        	enabled: false
        },
        plotOptions: {
            pie: {
                dataLabels: {
                    enabled: true,
                    color: '#000000',
                    connectorColor: '#000000',
                    format: '<b>{point.name}</b>: {point.percentage:.2f} %'
                }
            }
        },
        series: [{
            type: 'pie',
            name: 'Sentiment',
            data: [
                ['positive', positive],
                ['negative', negative],
                ['neutral', neutral]
            ],
            point:{
                events:{
                    click: function (event) { // Liste von Tweets als Popup anzeigen
                    	processTweetsSentiment(searchId, searchItem, this.name);
                    }
                }
            } 
        }],
        colors: posnegneutralColors
    });
}
