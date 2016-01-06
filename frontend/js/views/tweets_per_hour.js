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
function saveSearchItemTimeCurve(tabId, language) {
	var query = '/rest/results/countPerHour/?id='+searchItems[tabId].id + 
		((language === "") ? "" : "&lang=" + language);

	var dataObject = getDataObject(tabId, language);
	var priority = (activeViews[VIEW_TWEETS_PER_HOUR]) ? XHR_PRIORITY_ACTIVE : XHR_PRIORITY_PASSIVE;
    
    var xhro = new XHRObject(query, "GET", priority, 
    	//doneFunction
		function(response) {
	    	//Daten abspeichern
			dataObject.view[VIEW_TWEETS_PER_HOUR].data = response.data;
			dataObject.view[VIEW_TWEETS_PER_HOUR].isLoaded = true;
			dataObject.view[VIEW_TWEETS_PER_HOUR].isRendered = false;
			
			$(document).trigger("tm.view.dataLoaded", [tabId, language, VIEW_TWEETS_PER_HOUR]);
		},
    	//failFunction
    	function(event) {
			//Falls die Anfrage abgebrochen wird, soll keine Fehlermeldung erscheinen
			if(event.statusText !== "abort") {
				showServerError(10 + 10 * VIEW_TWEETS_PER_HOUR, getViewPanel(tabId, language, VIEW_TWEETS_PER_HOUR));		
			}
    	}
    );
    dataObject.view[VIEW_TWEETS_PER_HOUR].xhro = xhro;
    
    //In die XHRObkjekt-Verwaltung einfuegen
    addXHRObject(xhro); 
}

//Einzelne Tweets mit Zeitpunkt speichern
function saveTweetsDate(searchId, searchItem, date) {
	return $.Deferred(function() {
		var self = this;
		
		var endDate = new Date(date);
		endDate.setTime(endDate.getTime() + (60*60*1000)); //Eine Stunde hinzufuegen 
		
		paramString = "start=" + dateToString(date) + "&end=" + dateToString(endDate);

		//Sprachfilter verwenden, falls einer ausgewaehlt wurde
        if(activeLanguage !== ""){
            paramString += "&lang=" + activeLanguage;
        }

		subtitle = "Time range: " + dateToString(date) + " to " + dateToString(endDate);
		
		$.when(saveTweets(searchId, searchItem, subtitle, paramString))
		.done(function() {
			self.resolve();
		});
	});
}

//Einzelne News mit speichern
function saveNews(searchId, searchItem, date) {
	return $.Deferred(function() {
        var self = this;
		$.get("/rest/results/news/?id="+searchId + 
				//Sprachfilter verwenden, falls einer ausgewaehlt wurde
				(activeLanguage !== "" ? "&lang=" + activeLanguage : "") + 
				'&day=' + date.getDate() + '&month=' + (date.getMonth() + 1) + '&year=' + date.getFullYear())
		.done(function(response) {
			$("#modalTweet").data("news", response.data);
			self.resolve();
		})
		.fail(function() {
			showServerError(11 + 10 * VIEW_TWEETS_PER_HOUR, getViewPanel(tabId, language, VIEW_TWEETS_PER_HOUR));
		});
	});
}

//Element fuer zeitlichen Verlauf anzeigen und die Daten fuer 'searchItem' anzeigen
function showSingleTimeCurve(tabId, language) {	
	var searchId = searchItems[tabId].id;
	var searchItem = searchItems[tabId].name;
	
	var data = getDataObject(tabId, language).view[VIEW_TWEETS_PER_HOUR].data.graph;
	
	//Damit highcharts die richtige Groesse uebergeben bekommt, muss das betreffende Panel sichtbar sein
	var panel = getViewPanel(tabId, language, VIEW_TWEETS_PER_HOUR);
	panel.show();
	
	if (typeof data === "undefined" || data.length == 0) { //Falls keine Daten da sind, braucht nichts angezeigt zu werden
		showNoDataWarning(panel);
    	getViewObject(tabId, language, VIEW_TWEETS_PER_HOUR).isRendered = true;
		return;
	}
	
	var firstDate = data[0].date; //example date: 2013-11-29T13:37:42.053
	var year = parseInt(firstDate.substring(0, 4)); //example: 2013
	var month = parseInt(firstDate.substring(5, 7)); //example: 11
	var day = parseInt(firstDate.substring(8, 10)); //example: 29
	var hour = parseInt(firstDate.substring(11, 13)); //example: 13
	
	//Counts anzeigen vorbereiten
	var countsPlotData = new Array(data.length);
	for (var i = 0; i < data.length; ++i) {
		countsPlotData[i] = data[i].count;
	}
	
	//News/Peaks anzeigen vorbereiten
	var newsPlotData = new Array();
	var newsHeadlineIndices = new Array();
	for (var i = 0; i < data.length; ++i) {
		//Nur einfuegen, falls ein Peak zu diesem Zeitpunkt vorliegt
		if (data[i].peak) {
			//Datum parsen
			var yearNews = parseInt(data[i].date.substring(0, 4)); 
			var monthNews = parseInt(data[i].date.substring(5, 7)); 
			var dayNews = parseInt(data[i].date.substring(8, 10)); 
			var hourNews = parseInt(data[i].date.substring(11, 13));
			//Daten fuer einen identifizierten NewsPeak einfuegen
			newsPlotData.push([Date.UTC(yearNews, monthNews-1, dayNews, hourNews), data[i].count]);
			newsHeadlineIndices.push(i);
		}
	}
	
	
	panel.find(".viewBody").highcharts({
		chart: {
            zoomType: 'x',
            spacingRight: 20,
            events: {
            	load: function(event) {
            		if(DEBUG) console.log("tweetsPerHour Rendered!");
            		getViewObject(tabId, language, VIEW_TWEETS_PER_HOUR).isRendered = true;
            	}
            }
        },
        title: {
        	text: 'Time Curve for '+searchItem,
            x: -20 //center
        },
        subtitle: {
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
                text: 'Tweets per Hour'
            }
        },
        tooltip: {
            formatter: function() {
                var s = ''+ Highcharts.dateFormat('%A, %b %e, %H:%M', this.x) +'';
                s += '<br\>' + this.series.name + ': ' + this.y + ' Tweets';
                
                if (this.series.index == 1) {
                	var pointIndex = newsHeadlineIndices[this.series.data.indexOf(this.point)];
                	for (var i = 0; i < data[pointIndex].news.length; i++) {
                		s += '<br\>' + data[pointIndex].news[i];
                	}
                }

                return s;
            }
        },
        legend: {
            enabled: false
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
                }
            }
        },
        series: [{
        	type: 'line',
            name: searchItem,
            pointInterval: 3600 * 1000, //1 Stunde
            pointStart: Date.UTC(year, month-1, day, hour),
            data: countsPlotData,
            point:{
                events:{
                    click: function (event) { // Liste von Tweets als Popup anzeigen
                    	$.when(saveTweetsDate(searchId, searchItem, new Date(this.x)), saveNews(searchId, searchItem, new Date(this.x)))
                    	.done(function() {
                    		showNews();
                        	showTweets();
                        	$('#modalTweet a[href="#tweetsTab"]').tab('show');
                        	$("#modalTweet").modal();
                    	});
                    }
                }
            }      
        },
        {
        	type: 'scatter',
            name: searchItem,
            data: newsPlotData,
            marker: {
                symbol: 'circle',
                radius: 5,
                fillColor: '#FF0000'
            },
            point:{
                events:{
                    click: function (event) { // Liste von Tweets als Popup anzeigen
                    	$.when(saveTweetsDate(searchId, searchItem, new Date(this.x)), saveNews(searchId, searchItem, new Date(this.x)))
                    	.done(function() {
                    		showNews();
                        	showTweets();
                        	$('#modalTweet a[href="#newsTab"]').tab('show');
                        	$("#modalTweet").modal();
                    	});
                    }
                }
            }   
        }]
        
    });
}