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
$(function() { // document.ready wrapper
	//wird nur vom compareTab genutzt
	loadFunctions = new Array(2);
	loadFunctions[VIEW_SENTIMENT_ANALYSIS] = saveSearchItemSentiment;
	loadFunctions[VIEW_TWEETS_PER_HOUR] = saveSearchItemTimeCurve;
	
	showFunctions = new Array(2);
	showFunctions[VIEW_SENTIMENT_ANALYSIS] = showMultipleSentiment;
	showFunctions[VIEW_TWEETS_PER_HOUR] = showMultipleTweetsPerHour;
});

//TODO: Fancy immer reinladen wenn eins fertig ist...
function processMultipleView(language, viewId) {
	//Zunaechst Ladeansicht anzeigen
	$("#" + viewClasses[viewId] + "Compare .viewLoading").show();
	$("#" + viewClasses[viewId] + "Compare .viewBody").addClass("hidden");
	
	for (var i = 0; i < searchItems.length; i++) {
		var viewObject = getViewObject(i, language, viewId);
		//Nachschauen, ob zum jeweiligen Suchbegriff schon Daten vorliegen
		if (!viewObject.isLoaded) {
			//Nachschauen, ob das Laden der Daten des jeweiligen Suchbegriffs schon angestossen wurde
			if (viewObject.xhro.query == undefined) {
				//Laden anstossen, falls noch nicht geschehen
				loadFunctions[viewId](i, language); //TODO: Hier wird evtl. der Passiv-Pool verwendet, wenn Sentiment Analysis nicht sichtbar ist...
			}
			//Es wurde noch nicht alles fertig geladen
			return;
		} 
	}
	
	//Nur anzeigen, wenn alle Daten da sind
	//Ladeanimation ausblenden
	$("#" + viewClasses[viewId] + "Compare .viewLoading").fadeOut(FADEOUT_TIME, function() {			
		//Chart anzeigen
		$("#" + viewClasses[viewId] + "Compare .viewBody").removeClass("hidden");
		showFunctions[viewId](language);
	});
}

//Element fuer  zeitlichen Verlauf anzeigen und die Daten fuer alle Begriffe in der Tabelle anzeigen
function showMultipleSentiment(language) {
	//Leeren Chart erstellen
	$("#sentimentAnalysisCompare .viewBody").highcharts({
		chart: {
			type: 'column'
		},
        title: {
        	text: 'Relative Sentiment Analysis Comparison'
		},
        xAxis: {
        	categories: []
        },
        yAxis: {
        	min: 0, 
        	title: {
        		text: 'Number of Tweets'
			}
        },
        tooltip: {
        	valueDecimals: 2,
    		valueSuffix: '%'
		},
        plotOptions: {
        	column: {
        		pointPadding: 0.2,
        		borderWidth: 0,
        	}
		},
        series: [
            {name: 'positive', data: []},
            {name: 'negative', data: []},
            {name: 'neutral', data: []}
        ],
        colors: posnegneutralColors
    });
	var chart = $("#sentimentAnalysisCompare .viewBody").highcharts();
	
	//Alle Werte eintragen
	for (var i = 0; i < searchItems.length; i++) {
		//Positiv, negativ, neutral-Werte eintragen
		var sentimentDataObject = getViewObject(i, language, VIEW_SENTIMENT_ANALYSIS).data; 
		var total = sentimentDataObject.positive + sentimentDataObject.neutral + sentimentDataObject.negative;
		if (total != 0) {			
			chart.series[0].addPoint(100 * sentimentDataObject.positive / total);
			chart.series[1].addPoint(100 * sentimentDataObject.negative / total);
			chart.series[2].addPoint(100 * sentimentDataObject.neutral / total);
			//Den Suchbegriff als Kategorie hinzufuegen
			var categories = chart.xAxis[0].categories;
			categories.push(searchItems[i].name);
			chart.xAxis[0].setCategories(categories);
		}
		//TODO: Chart nicht anzeigen, falls keine Daten vorliegen
	}
}

//Element fuer die Stimmungsanalyse anzeigen und die Daten fuer alle Begriffe in der Tabelle anzeigen
function showMultipleTweetsPerHour(language) {
	//Leeren Chart erstellen
	$("#tweetsPerHourCompare .viewBody").highcharts({
        chart: {
            zoomType: 'x',
            spacingRight: 20
        },
        title: {
            text: 'Comparison of Tweets per Hour'
        },
        subtitle: {
            text: document.ontouchstart === undefined ?
                'Click and drag in the plot area to zoom in' :
                'Pinch the chart to zoom in'
        },
        xAxis: {
            type: 'datetime',
            maxZoom: 24 * 3600000, // ein Tag
            title: {
                text: null
            }
        },
        yAxis: {
            title: {
                text: 'Tweets per Hour'
            }
        },
        tooltip: {
            shared: true,
            valueSuffix: ' Tweets'
        },
        legend: {
            borderWidth: 0
        },
        plotOptions: {
            line: {
                lineWidth: 1,
                marker: {
                    enabled: false
                },
                states: {
                    hover: {
                        lineWidth: 1
                    }
                },
            }
        },

        series: []
    });
	
	var chart = $("#tweetsPerHourCompare .viewBody").highcharts();
	
	//Alle Werte eintragen
	for (var i = 0; i < searchItems.length; i++) {
		//Positiv, negativ, neutral-Werte eintragen
		var data = getViewObject(i, language, VIEW_TWEETS_PER_HOUR).data.graph; 
		
		if (typeof data === "undefined" || data.length == 0) { //Falls keine Daten da sind, braucht nichts angezeigt zu werden
			continue;
		}
		
		var firstDate = data[0].date; //example date: 2013-11-29T13:37:42.053
		var year = parseInt(firstDate.substring(0, 4)); //example: 2013
		var month = parseInt(firstDate.substring(5, 7)); //example: 11
		var day = parseInt(firstDate.substring(8, 10)); //example: 29
		var hour = parseInt(firstDate.substring(11, 13)); //example: 13
		
		//Counts anzeigen vorbereiten
		var countsPlotData = new Array(data.length);
		for (var j = 0; j < data.length; ++j) {
			countsPlotData[j] = data[j].count;
		}
		
		chart.addSeries({
        	type: 'line',
            name: searchItems[i].name,
            pointInterval: 3600 * 1000, //1 Stunde
            pointStart: Date.UTC(year, month-1, day, hour),
            data: countsPlotData,
        });
	}
}
