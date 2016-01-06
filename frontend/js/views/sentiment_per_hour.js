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
function saveSearchItemSentimentTimeCurve(tabId, language) {
	var query = '/rest/results/sentimentPerHour/?id='+searchItems[tabId].id +
		((language === "") ? "" : "&lang=" + language);

    var dataObject = getDataObject(tabId, language);
    var priority = (activeViews[VIEW_SENTIMENT_PER_HOUR]) ? XHR_PRIORITY_ACTIVE : XHR_PRIORITY_PASSIVE;
    
    var xhro = new XHRObject(query, "GET", priority, 
    	//doneFunction
    	function(response) {
			//Daten abspeichern
			dataObject.view[VIEW_SENTIMENT_PER_HOUR].data = response.data;
			dataObject.view[VIEW_SENTIMENT_PER_HOUR].isLoaded = true;
			dataObject.view[VIEW_SENTIMENT_PER_HOUR].isRendered = false;
			
			$(document).trigger("tm.view.dataLoaded", [tabId, language, VIEW_SENTIMENT_PER_HOUR]);
		},
    	//failFunction
    	function(event) {
			//Falls die Anfrage abgebrochen wird, soll keine Fehlermeldung erscheinen
			if(event.statusText !== "abort") {
				showServerError(10 + 10 * VIEW_SENTIMENT_PER_HOUR, getViewPanel(tabId, language, VIEW_SENTIMENT_PER_HOUR));			
			}
    	}
    );
    dataObject.view[VIEW_SENTIMENT_PER_HOUR].xhro = xhro;
    
    //In die XHRObkjekt-Verwaltung einfuegen
    addXHRObject(xhro);
}

//Einzelne Tweets mit Zeitpunkt und Sentiment speichern
function saveTweetsDateAndSentiment(searchId, searchItem, date, sentiment) {
	return $.Deferred(function() {
		var self = this;
		
		var endDate = new Date(date);
		endDate.setTime(endDate.getTime() + (60*60*1000)); //Eine Stunde hinzufuegen 
		
		paramString = "start=" + dateToString(date) + "&end=" + dateToString(endDate) + "&sent=" + sentiment;

        if(language !== ""){
            paramString += '&lang='+language;
        }

		subtitle = "Time range: " + dateToString(date) + " to " + dateToString(endDate) + ". Showing only " + sentiment + " tweets.";;
		
		$.when(saveTweets(searchId, searchItem, subtitle, paramString))
		.done(function() {
			self.resolve();
		});
	});
}

//Element fuer zeitlichen Verlauf des Sentiments anzeigen
function showSingleSentimentTimeCurve(tabId, language) {
	//Benoetigte Variablen holen
	var searchId = searchItems[tabId].id;
	var searchItem = searchItems[tabId].name;
	
	var datesPositive = getDataObject(tabId, language).view[VIEW_SENTIMENT_PER_HOUR].data.positive_counts.dates;
	var datesNegative = getDataObject(tabId, language).view[VIEW_SENTIMENT_PER_HOUR].data.negative_counts.dates;
	
	var posData = getDataObject(tabId, language).view[VIEW_SENTIMENT_PER_HOUR].data.positive_counts.counts;
	var negData = getDataObject(tabId, language).view[VIEW_SENTIMENT_PER_HOUR].data.negative_counts.counts;
	
	var sum = 0;
	var allSum = 0;
	var schrittWeite = 12;
	for (var index = posData.length-1; index >= 0; index-- ) {
		for (var i = 1; i <= schrittWeite; i++) {
			var addindex = index - i;
			if (addindex < 0) {
				addindex = 0;
			}
			posData[index] += posData[addindex];
			negData[index] += negData[addindex];
		}
		
		sum = posData[index] + negData[index];
		if (sum > 0 ) {
			posData[index] = Math.round(posData[index] / sum * 100 * 100) / 100;
			negData[index] = Math.round(negData[index] / sum * 100 * 100) / 100;
		}
		allSum += sum;
	}
	posData.splice(0,schrittWeite);
	negData.splice(0,schrittWeite);
	
	//Damit highcharts die richtige Groesse uebergeben bekommt, muss das betreffende Panel sichtbar sein
	var panel = getViewPanel(tabId, language, VIEW_SENTIMENT_PER_HOUR);
	panel.show();

	//Falls keine Daten da sind...
	if (typeof datesPositive === "undefined" || datesPositive.length == 0 ||
		typeof datesNegative === "undefined" || datesNegative.length == 0 || allSum == 0) { //Falls keine Daten da sind, braucht nichts angezeigt zu werden
		showNoDataWarning(panel);
		getViewObject(tabId, language, VIEW_SENTIMENT_PER_HOUR).isRendered = true;
		return;
	}
	
	

	//Chart ohne Inhalt erstellen
	panel.find(".viewBody").highcharts({
		chart: {
			type: 'area',
            zoomType: 'x',
            spacingRight: 20,
            events: {
            	load: function(event) {
            		if(DEBUG) console.log("sentimentPerHour Rendered!");
            		getViewObject(tabId, language, VIEW_SENTIMENT_PER_HOUR).isRendered = true;
            	}
            }
        },
        title: {
        	text: 'Sentiment Time Curve for '+searchItem,
            x: -20 //center
        },
        subtitle: {
        	x: -20, //center
            text: document.ontouchstart === undefined ?
                'Click and drag in the plot area to zoom in' :
                'Pinch the chart to zoom in'
        },
        xAxis: {
            type: 'datetime',
            maxZoom: 24 * 3600000, // Ein Tag
            title: {
                text: null
            }
        },
        yAxis: {
            title: {
                text: 'Sentiment Percentage'
            },
            max : 100,
            min : 0
        },
        plotOptions: {
        	area: {
        		stacking: 'normal',
        		lineWidth: 1,
                marker: {
                    enabled: false
                },
                states: {
                    hover: {
                        lineWidth: 1
                    }
                }
        	}
        },
        tooltip: {
        	shared: true,
            valueSuffix: '%'
        },
        series: [],
        colors: negposneutralColors
    });
	
	//Chart mit Inhalt befuellen
	var chart = panel.find(".viewBody").highcharts();
	
	firstDate = datesNegative[0]; //example date: 2013-11-29T13:37:42.053
	year = parseInt(firstDate.substring(0, 4)); //example: 2013
	month = parseInt(firstDate.substring(5, 7)); //example: 11
	day = parseInt(firstDate.substring(8, 10)); //example: 29
	hour = parseInt(firstDate.substring(11, 13)); //example: 13
	
	chart.addSeries({
		//type: 'line',
        name: 'negative',
        pointInterval: 3600 * 1000, //1 Stunde
        pointStart: Date.UTC(year, month-1, day, hour),
        data: negData,
        point:{
            events:{
                click: function (event) { // Liste von Tweets als Popup anzeigen
                	$.when(saveTweetsDateAndSentiment(searchId, searchItem, new Date(this.x), this.series.name))
                	.done(function() {
                    	showTweets();
                    	$("#modalTweet").modal();
                	});
                }
            }
        }  
    });
	
	//Series fuer positive Tweets einfuegen
	var firstDate = datesPositive[0]; //example date: 2013-11-29T13:37:42.053
	var year = parseInt(firstDate.substring(0, 4)); //example: 2013
	var month = parseInt(firstDate.substring(5, 7)); //example: 11
	var day = parseInt(firstDate.substring(8, 10)); //example: 29
	var hour = parseInt(firstDate.substring(11, 13)); //example: 13
	
	chart.addSeries({
		//type: 'line',
        name: 'positive',
        pointInterval: 3600 * 1000, //1 Stunde
        pointStart: Date.UTC(year, month-1, day, hour),
        data: posData,
        point:{
            events:{
                click: function (event) { // Liste von Tweets als Popup anzeigen
                	$.when(saveTweetsDateAndSentiment(searchId, searchItem, new Date(this.x), this.series.name))
                	.done(function() {
                    	showTweets();
                    	$("#modalTweet").modal();
                	});
                }
            }
        }  
    });
}
