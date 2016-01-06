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
function saveSearchItemHashtags(tabId, language) {
	var query ='/rest/results/hashtagstatistics/?id='+searchItems[tabId].id +
		((language === "") ? "" : "&lang=" + language);

    var dataObject = getDataObject(tabId, language);
    var priority = (activeViews[VIEW_RELATED_HASHTAGS]) ? XHR_PRIORITY_ACTIVE : XHR_PRIORITY_PASSIVE;
    
    var xhro = new XHRObject(query, "GET", priority, 
    	//doneFunction
		function(response) {
	    	//Daten abspeichern
			dataObject.view[VIEW_RELATED_HASHTAGS].data = response.data;
			dataObject.view[VIEW_RELATED_HASHTAGS].isLoaded = true;
			dataObject.view[VIEW_RELATED_HASHTAGS].isRendered = false;
			
			$(document).trigger("tm.view.dataLoaded", [tabId, language, VIEW_RELATED_HASHTAGS]);
		},
    	//failFunction
    	function(event) {
			//Falls die Anfrage abgebrochen wird, soll keine Fehlermeldung erscheinen
			if(event.statusText !== "abort") {
				showServerError(10 + 10 * VIEW_RELATED_HASHTAGS, getViewPanel(tabId, language, VIEW_RELATED_HASHTAGS));			
			}
    	}
    );
    dataObject.view[VIEW_RELATED_HASHTAGS].xhro = xhro;
    
    //In die XHRObkjekt-Verwaltung einfuegen
    addXHRObject(xhro);
}

//Weiterverarbeitung zum Tweets anzeigen. Abspeichern und dann anzeigen.
function processTweetsHashtag(searchId, searchItem, hashTag, hashTagName) {
    $.when(saveTweetsHashtag(searchId, searchItem, hashTag, hashTagName)) //sentiment = "positive" | "negative" | "neutral"
        .done(function() {
            showTweets();
            $("#modalTweet").modal();
        });
}

function saveTweetsHashtag(searchId, searchItem, hashTag, hashTagName) {
    return $.Deferred(function() {
        var self = this;

        paramString = "hashtag=" + hashTag;

        if(activeLanguage !== ""){
            paramString = paramString+'&lang='+activeLanguage;

        }
        subtitle = "Showing only tweets with hashtag: " + hashTagName;

        $.when(saveTweets(searchId, searchItem, subtitle, paramString))
            .done(function() {
                self.resolve();
            });
    });
}


//Hashtags zu searchId als bar chart anzeigen
function showSingleHashtags(tabId, language) {
	// Benoetigte Variablen holen
	var searchId = searchItems[tabId].id;
	var searchItem = searchItems[tabId].name;
	var dataObject = getDataObject(tabId, language);
	var lHashtagTexts = dataObject.view[VIEW_RELATED_HASHTAGS].data.hashtag_texts;
    var lHashtagIds = dataObject.view[VIEW_RELATED_HASHTAGS].data.hashtag_ids;
	var lCounts = dataObject.view[VIEW_RELATED_HASHTAGS].data.counts;

	var panel = getViewPanel(tabId, language, VIEW_RELATED_HASHTAGS);
	panel.show();

	if (typeof lHashtagTexts === "undefined" || lHashtagTexts.length == 0) {
		showNoDataWarning(panel);
		getViewObject(tabId, language, VIEW_RELATED_HASHTAGS).isRendered = true;
		return;
	}


	panel.find(".viewBody").highcharts({
		chart: {
			type: 'column',
            events: {
            	load: function(event) {
            		if(DEBUG) console.log("relatedHashtags Rendered!");
            		getViewObject(tabId, language, VIEW_RELATED_HASHTAGS).isRendered = true;
            	}
            }
		},

		title: {
			text: 'Most Frequent Hashtags for ' + searchItem
		},

		legend: {
			enabled: false
		},

		xAxis: {
			categories: lHashtagTexts,

			labels: {
                y : 20,
				style: {
					fontSize: '18px'
				}
			}
		},

		yAxis: {
			min: 0,
			title: {
				text: 'Number of Tweets'
			}
		},

		tooltip: {
			formatter: function() { return '<span style="color:#2f7ed8">' + this.point.category + '</span>: <b>' + this.point.y + ' tweets</b><br/>'; }
		},

		plotOptions: {
			column: {
				pointPadding: 0.2,
				borderWidth: 0
			}
		},

		series: [{
			name: 'Hashtags',
			data: lCounts,
			point: {
				events: {
					click: function(event) {
                        var i = lHashtagTexts.indexOf(event.currentTarget.category)
                        processTweetsHashtag(searchId, searchItem, lHashtagIds[i], event.currentTarget.category);
					}
				}
			}
		}]
	});

    panel.find(".viewBody .highcharts-axis-labels:first text").click(function() {
        submitSearchItemParam("#"+$(this).text());
    });

    //TODO: Ins CSS File direkt schreiben...
    panel.find(".viewBody .highcharts-axis-labels:first text").hover(function() {
        $(this).css('fontWeight', 'bold');
        $(this).css('fill', '#2f7ed8');
        $(this).css('cursor', 'pointer');
    }, function() {
        $(this).css('fontWeight', 'normal');
        $(this).css('fill', '#666666');
    });
}