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
//Speichert Clusterergebnisse zu einer Suchbegriffid
function saveSearchItemClusterResults(tabId, language) {
	var query = "/rest/results/getDataGroups/?id=" + searchItems[tabId].id + "&limit=750" +
		((language === "") ? "" : "&lang=" + language);
	
	var dataObject = getDataObject(tabId, language);
	var priority = (activeViews[VIEW_CLUSTER_ANALYSIS]) ? XHR_PRIORITY_ACTIVE : XHR_PRIORITY_PASSIVE;
    
    var xhro = new XHRObject(query, "GET", priority, 
    	//doneFunction
		function(response) {
	    	//Daten abspeichern
			dataObject.view[VIEW_CLUSTER_ANALYSIS].data = response.data;
			dataObject.view[VIEW_CLUSTER_ANALYSIS].isLoaded = true;
			dataObject.view[VIEW_CLUSTER_ANALYSIS].isRendered = false;
	
			$(document).trigger("tm.view.dataLoaded", [tabId, language, VIEW_CLUSTER_ANALYSIS]);
		},
    	//failFunction
    	function(event) {
			//Falls die Anfrage abgebrochen wird, soll keine Fehlermeldung erscheinen
			if(event.statusText !== "abort") {
				showServerError(10 + 10 * VIEW_CLUSTER_ANALYSIS, getViewPanel(tabId, language, VIEW_CLUSTER_ANALYSIS));			
			}
    	}
    );
    dataObject.view[VIEW_CLUSTER_ANALYSIS].xhro = xhro;
    
    //In die XHRObkjekt-Verwaltung einfuegen
    addXHRObject(xhro);
}

//Cluster Ergebnisse tabelarisch anzeigen
function showSingleClusters(tabId, language) {
	//Benoetigte Variablen holen
	var searchItem = searchItems[tabId].name;
	
	var dataObject = getDataObject(tabId, language);
	
	var panel = getViewPanel(tabId, language, VIEW_CLUSTER_ANALYSIS);
	panel.show();
	
	if (dataObject.view[VIEW_CLUSTER_ANALYSIS].data == null) {
		showNoDataWarning(panel);
		getViewObject(tabId, language, VIEW_CLUSTER_ANALYSIS).isRendered = true;
		return;
	}
	
	var seriesData = dataObject.view[VIEW_CLUSTER_ANALYSIS].data.series;
	var totalCount = dataObject.view[VIEW_CLUSTER_ANALYSIS].data.total_count;
	
	//Cluster Analyse visualisieren	
	panel.find(".viewBody").highcharts({
        chart: {
            type: 'scatter',
            zoomType: 'xy',
            events: {
            	load: function(event) {
            		if(DEBUG) console.log("clusertAnalysis Rendered!");
            		getViewObject(tabId, language, VIEW_CLUSTER_ANALYSIS).isRendered = true;
            	}
            }
        },
        title: {
            text: 'Clusters for ' + searchItem
        },
        subtitle: {
//            text: 'Tweet count ' + totalCount
        	text: 'Hashtag count ' + totalCount
        },
        xAxis: {
        	lineWidth: 0,
			minorGridLineWidth: 0,
			lineColor: 'transparent',
			        
			labels: {
			enabled: false
			},
			minorTickLength: 0,
			tickLength: 0
        },
        yAxis: {
        	title : null,
        	lineWidth: 0,
			minorGridLineWidth: 0,
			lineColor: 'transparent',
			        
			labels: {
			enabled: false
			},
			minorTickLength: 0,
			tickLength: 0
        },
        legend: {
        	enabled : false
        },
        tooltip: {
        	useHTML: true,
        	formatter: function() {    		
        		var tid = seriesData[this.series.index].ids[this.series.data.indexOf( this.point )];
        		var tweetText = "Tweet data not found";
        		
        		//Bitte nicht loeschen
        		//hier steht der Quellcode fuer das alte Clustering nach Tweets
//        		$.ajax({
//        			url: "/rest/results/tweet/?id="+tid,
//        			async : false,
//        			timeout : 500
//
//        			})
//        			.done(function(response) {
//        				tweetText = response.data.text;
//        				tweetText = tweetText.replace(/(#\S+)/g, '<b>$1</b>');
//        			})
//        			.fail(function(event) {
//								//Falls die Anfrage abgebrochen wird, soll keine Fehlermeldung erscheinen
//								if(event.statusText !== "abort") {
//									showServerError(11 + 10 * VIEW_CLUSTER_ANALYSIS, getViewPanel(tabId, language, VIEW_CLUSTER_ANALYSIS));			
//								}
//							});
        		//Wenn man nur die Hashtags clustert, soll nur der Hashtagtext angezeigt werden. In diesem Fall ersetzt er das was in der id steht
        		tweetText = tid;
        		
				return "<b>Text</b><br><div style='width: 300px; white-space:normal;'>" + tweetText + "<\div>";
        	}
        },
        series: seriesData
    });
}