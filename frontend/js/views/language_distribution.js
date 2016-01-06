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
function saveSearchItemLanguages(tabId) {
	var query ='/rest/results/languages/?id='+searchItems[tabId].id;
	var priority = (activeViews[VIEW_LANGUAGE_DISTRIBUTION]) ? XHR_PRIORITY_ACTIVE : XHR_PRIORITY_PASSIVE;
    
    var xhro = new XHRObject(query, "GET", priority, 
    	//doneFunction
		function(response) {
	    	//Daten abspeichern
	    	searchItems[tabId].languageDistribution.data = response.data;
			searchItems[tabId].languageDistribution.isLoaded = true;
			searchItems[tabId].languageDistribution.isRendered = false;
			
			//Es wird explizit keine Sprache uebergeben, da die Sprachverteilung davon unabhaengig ist
			$(document).trigger("tm.view.dataLoaded", [tabId, "", VIEW_LANGUAGE_DISTRIBUTION]);
		},
    	//failFunction
    	function(event) {
			//Falls die Anfrage abgebrochen wird, soll keine Fehlermeldung erscheinen
			if(event.statusText !== "abort") {
				showServerError(10 + 10 * VIEW_LANGUAGE_DISTRIBUTION, getViewPanel(tabId, "", VIEW_LANGUAGE_DISTRIBUTION));			
			}
    	}
    );
    searchItems[tabId].languageDistribution.xhro = xhro;
    
    //In die XHRObkjekt-Verwaltung einfuegen
    addXHRObject(xhro);
}

function showSingleLanguages(tabId) {
	// Benoetigte Variablen holen
	var searchItem = searchItems[tabId].name;
	var lLanguageCounts = searchItems[tabId].languageDistribution.data;

	var panel = getViewPanel(tabId, "", VIEW_LANGUAGE_DISTRIBUTION);
	panel.show();

	if (typeof lLanguageCounts === "undefined" || lLanguageCounts.length == 0) {
		showNoDataWarning(panel);
		getViewObject(tabId, "", VIEW_LANGUAGE_DISTRIBUTION).isRendered = true;
		return;
	}

	var iTotalTweets = 0;
	var iOtherLanguagesCount = 0;

	for (i in lLanguageCounts) {
		iTotalTweets += lLanguageCounts[i].count;
	}

	var barinput = [];
	var pieinput = [];

    for (i in lLanguageCounts) {
		// only display languages with more than 1%
		if (lLanguageCounts[i].count * 100 / iTotalTweets > 1) {
    		barinput.push({ name: lLanguageCounts[i].iso_code, data: [lLanguageCounts[i].count]});
			pieinput.push([lLanguageCounts[i].iso_code, lLanguageCounts[i].count]);
		} else {
			iOtherLanguagesCount += lLanguageCounts[i].count;
		}
	}

	// sum up other languages as "other"
	if (iOtherLanguagesCount > 0) {
		barinput.push({ name: "Other", data: [iOtherLanguagesCount]});
		pieinput.push(["Other", iOtherLanguagesCount]);
	}
	
	panel.find(".viewBody div:first").highcharts(
	{
		chart: {
        		type: 'column',
                events: {
                	load: function(event) {
                		if(DEBUG) console.log("languageDistribution Rendered!");
                		getViewObject(tabId, "", VIEW_LANGUAGE_DISTRIBUTION).isRendered = true;
                	}
                }
    		},
    	title: {
        		text: 'Absolute Language Analysis for ' + searchItem
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
    			events: {
    				//Sprache wechseln
        			click: function (event) { 
        				//Bei der zusammengefassten Kategorie kann nicht gewechselt werden
        				if (this.name == "Other") return;
        				//Ansonsten Sprache wechseln
        				switchToLanguage(this.name);
        				//TODO: Dafuer sorgen, dass z.B. bei "en" "English" angezeigt wird
        			}
    			}
    		}
    	},
    	series: barinput,
		colors: randomColors50
	});

	panel.find(".viewBody div:last").highcharts({
        title: {
        		text: 'Relative Languages Analysis for ' + searchItem
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
    		name: 'Languages',
    		data: pieinput,
    		point: {
    			events: {
    				//Sprache wechseln
        			click: function (event) { 
        				//Bei der zusammengefassten Kategorie kann nicht gewechselt werden
        				if (this.name == "Other") return;
        				//Ansonsten Sprache wechseln
        				switchToLanguage(this.name);
        				//TODO: Dafuer sorgen, dass z.B. bei "en" "English" angezeigt wird
        			}
    			}
    		},
    		colors: randomColors50
    	}]
	});
}