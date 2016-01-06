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
//Anzahl der verschiedenen Ansichten
NUM_VIEWS = 8; 
//Anzahl der verschiedenen Ansichten, die von der eingestellten Filtersprache abhaengig sind
NUM_LANG_VIEWS = 7;

//globale IDs der einzelnen Ansichten
VIEW_SENTIMENT_ANALYSIS = 0;
VIEW_TWEETS_PER_HOUR = 1;
VIEW_SENTIMENT_PER_HOUR = 2;
VIEW_RELATED_HASHTAGS = 3;
VIEW_TAG_CLOUD = 4;
VIEW_CLUSTER_ANALYSIS = 5;
VIEW_CLUSTER_ANALYSIS_TWEETS = 6;
VIEW_LANGUAGE_DISTRIBUTION = 7;

var posnegneutralColors = ['#8bbc21',
						   '#EA3D00',
						   '#2f7ed8', 
						   '#0d233a', 
						   '#910000', 
						   '#1aadce', 
						   '#492970',
						   '#f28f43', 
						   '#77a1e5', 
						   '#c42525', 
						   '#a6c96a'];
						   
var negposneutralColors = ['#EA3D00',
						   '#8bbc21',
						   '#2f7ed8', 
						   '#0d233a', 
						   '#910000', 
						   '#1aadce', 
						   '#492970',
						   '#f28f43', 
						   '#77a1e5', 
						   '#c42525', 
						   '#a6c96a'];
						   
var moreColors = ['#2f7ed8', 
				  '#0d233a', 
				  '#8bbc21', 
				  '#910000', 
				  '#1aadce', 
				  '#492970',
				  '#f28f43', 
				  '#77a1e5', 
				  '#c42525', 
				  '#a6c96a',
				  '#4572A7', //Highcharts 2.x default colors
				  '#AA4643', 
				  '#89A54E', 
				  '#80699B', 
				  '#3D96AE', 
				  '#DB843D', 
				  '#92A8CD', 
				  '#A47D7C', 
				  '#B5CA92'];

var randomColors50 = ['#2cb013',
				  '#251683',
				  '#e37b68',
				  '#be60d0',
				  '#892052',
				  '#629eff',
				  '#8be0db',
				  '#dcafc0',
				  '#2efb22',
				  '#c33540',
				  '#0c7ba3',
				  '#62dddb',
				  '#762c99',
				  '#e2257d',
				  '#e7c37e',
				  '#5beb2b',
				  '#db24a0',
				  '#0d5dc0',
				  '#bf3331',
				  '#7f59fa',
				  '#9bfe17',
				  '#988b67',
				  '#65eb77',
				  '#c7abea',
				  '#17d757',
				  '#7da139',
				  '#e121fd',
				  '#961e66',
				  '#c01f62',
				  '#2dc233',
				  '#49789a',
				  '#df9bd8',
				  '#d1530d',
				  '#905da5',
				  '#db04c7',
				  '#5fea41',
				  '#eabe48',
				  '#4a6f66',
				  '#d3eb90',
				  '#22f691',
				  '#367c25',
				  '#74c80b',
				  '#e594fe',
				  '#16a5f5',
				  '#0b01ef',
				  '#60e755',
				  '#acdf9f',
				  '#892fab',
				  '#f81f69',
				  '#7d8be1'];
	
$(function() { // document.ready wrapper
	//Funktionen 
	viewFunctions = new Array(NUM_VIEWS);
	viewFunctions[VIEW_SENTIMENT_ANALYSIS] = showSingleSentiment;
	viewFunctions[VIEW_TWEETS_PER_HOUR] = showSingleTimeCurve;
	viewFunctions[VIEW_SENTIMENT_PER_HOUR] = showSingleSentimentTimeCurve;
	viewFunctions[VIEW_RELATED_HASHTAGS] = showSingleHashtags;
	viewFunctions[VIEW_TAG_CLOUD] = showSingleTagCloud;
	viewFunctions[VIEW_CLUSTER_ANALYSIS] = showSingleClusters;
	viewFunctions[VIEW_CLUSTER_ANALYSIS_TWEETS] = showSingleClustersTweets;
	viewFunctions[VIEW_LANGUAGE_DISTRIBUTION] = showSingleLanguages;
	
	viewClasses = new Array(NUM_VIEWS);
	viewClasses[VIEW_SENTIMENT_ANALYSIS] = "sentimentAnalysis";
	viewClasses[VIEW_TWEETS_PER_HOUR] = "tweetsPerHour";
	viewClasses[VIEW_SENTIMENT_PER_HOUR] = "sentimentPerHour";
	viewClasses[VIEW_RELATED_HASHTAGS] = "relatedHashtags";
	viewClasses[VIEW_TAG_CLOUD] = "tagCloud";
	viewClasses[VIEW_CLUSTER_ANALYSIS] = "clusterAnalysis";
	viewClasses[VIEW_CLUSTER_ANALYSIS_TWEETS] = "clusterAnalysisTweets";
	viewClasses[VIEW_LANGUAGE_DISTRIBUTION] = "languageDistribution";
	
	viewHeadlines = new Array(NUM_VIEWS);
	viewHeadlines[VIEW_SENTIMENT_ANALYSIS] = "Sentiment Analysis";
	viewHeadlines[VIEW_TWEETS_PER_HOUR] = "Tweets with News over Time";
	viewHeadlines[VIEW_SENTIMENT_PER_HOUR] = "Sentiment over Time";
	viewHeadlines[VIEW_RELATED_HASHTAGS] = "Related Hashtags";
	viewHeadlines[VIEW_TAG_CLOUD] = "Tag Cloud";
	viewHeadlines[VIEW_CLUSTER_ANALYSIS] = "Cluster Analysis Hashtags";
	viewHeadlines[VIEW_CLUSTER_ANALYSIS_TWEETS] = "Cluster Analysis Tweets";
	viewHeadlines[VIEW_LANGUAGE_DISTRIBUTION] = "Language Distribution";	
});

//Layout der Webseite in den Ausgangszustand versetzen. Hat nur einen Effekt, falls die Seite nicht im Ausgangszustand ist.
function showInitialView() {
	if ($('#tmStartbox').hasClass('toTop')) {
		// Logo und Inputfeld ausblenden, verschieben und wieder einblenden
		$("#tmStartbox").fadeOut(FADEOUT_TIME, function() {
			$(this).removeClass('toTop').fadeIn(FADEIN_TIME);
		});
		// Tab Box ausblenden
		//Als Callback! Sonst laeuft die Animation nicht sauber. Besser erst nach vollendeter Animation aufraeumen...
		$('#contentContainer').fadeOut(FADEOUT_TIME, clearSearchItems);
		
		//Den Link fuer die Einstellungen der Ansichten ausblenden
		$('#viewSettingsLink').hide();
		
		//Fokus auf das Suchfeld und Loeschen des alten Begriffs
		$('#searchBox').typeahead('setQuery', '');
		$('#searchBox').focus();
	}
}

//Setzt alle Suchbegriffe zurueck. Sowohl die Daten, als auch die Tabs mit Inhalt.
function clearSearchItems() {
	//Alle XMLHttpRequests abbrechen
	for (var tabId = 0; tabId < searchItems.length; tabId++) {
		abortViewRequests(tabId);
	}
	//searchItems Array zuruecksetzen
	searchItems = new Array();
	//Tabs loeschen
	$('#searchItemsTabBar li').not($('#tabHeadCompare').parent('li')).remove();
	$('#searchItemsContainers div').not('#tabBodyCompare').remove();
	//LocalSotrage aufraeumen
	savedSearchItems = [];
	localStorage.setItem('savedSearchItems', '');
}

//In die Analyse-Ansicht mit Resultats-Tabs wechseln, falls diese Ansicht nicht sowieso schon angezeigt wird.
function showAnalysisView() {
	if (!$('#tmStartbox').hasClass('toTop')) {
		$('ul.searchItemsTabs li').hide();
		// Logo und Inputfeld ausblenden, verschieben, wieder einblenden und Tab Box einblenden
		$("#tmStartbox").fadeOut(FADEOUT_TIME, function() {
			$(this).addClass('toTop').add('#contentContainer').fadeIn(FADEIN_TIME);
		});
		
		//Den Link fuer die Einstellungen der Ansichten einblenden
		$('#viewSettingsLink').show();
	}
}

//Zeigt eine einzelne Ansicht
function showView(searchId, language, viewId) {
	if (viewId === VIEW_LANGUAGE_DISTRIBUTION) {
		$("#" + viewClasses[viewId] + searchId).removeClass("hidden");
	} else {		
		$("#" + viewClasses[viewId] + searchId + "-" + language).removeClass("hidden");
	}
}

//Versteckt eine einzelne Ansicht
function hideView(searchId, language, viewId) {
	if (viewId === VIEW_LANGUAGE_DISTRIBUTION) {
		$("#" + viewClasses[viewId] + searchId).addClass("hidden");
	} else {		
		$("#" + viewClasses[viewId] + searchId + "-" + language).addClass("hidden");
	}
}

//Modaldialog zur Auswahl der Views anzeigen
function showModalActiveViews() {
	//Falls activeViews vernuenftige Daten enthaelt, werden die entsprechenden Ansichten als aktiv markiert
	if (activeViews != null) {
		var tiles = $('#activeViewsCheckTiles > span');
		$("#modalActiveViews input:checkbox").each(function(index) {
			this.checked = activeViews[index];
			if (this.checked) tiles.eq(index).addClass('active');
		});
	}
	$("#modalActiveViews").modal();
}

//Ein Tile zur Aenderung der Sichtbarkeit einer einzelnen Ansicht wurde angeklickt
function changeActiveViews(viewId) {
	//Ansicht (de)-aktivieren
	activeViews[viewId] = !activeViews[viewId];
	
	//Aenderungen im localStorage speichern
	saveActiveViewsToLocalStorage(activeViews);
	
	//Die Aenderungen dem XHR Pool System mitteilen
	var xhro = getViewObject(activeTabId, activeLanguage, viewId).xhro;
	//Falls aktiviert wurde, muss die Prioritaet erhoeht werden, andernfalls verringert.
	var newPriority = (activeViews[viewId]) ? incrementPriority(xhro.priority) : decrementPriority(xhro.priority);
	moveXHRObject(xhro, newPriority);
	
	//Anzeige updaten
	$(document).trigger("tm.view.requestUpdate", [activeTabId, activeLanguage, viewId]);
}

//Gibt das jQuery-Objekt eines spezifizierten Ansichten-Panels zurueck.
function getViewPanel(tabId, language, viewId) {
	//Die Sprachverteilung ist ein Spezialfall, da sie nicht vom Sprachfilter abhaengig ist
	if (viewId === VIEW_LANGUAGE_DISTRIBUTION) {
		return $("#" + viewClasses[viewId] + searchItems[tabId].id);
	} else {		
		return $("#" + viewClasses[viewId] + searchItems[tabId].id + "-" + language);
	}
}

//Erzeugt ein Panel einer Ansicht, abhaengig vom Suchbegriff und der Sprache 
function createViewString(viewId, searchId, language) {
	return "" +
	"<div id='" + viewClasses[viewId] + searchId + 
		((viewId === VIEW_LANGUAGE_DISTRIBUTION) ? "' " : "-" + language + "' ") + //Sprachverteilung ist unabhaengig von der Filtersprache 
		 "class='panel panel-primary viewPanel " + viewClasses[viewId] + " hidden'>" +
		"<div class='panel-heading'>" +
			viewHeadlines[viewId] +
		"</div>" +
		"<div class='panel-body viewBody'>" +
			createViewBodyString(viewId) + 
		"</div>" +
		"<div class='viewLoading'>Loading...</div>" +
	"</div>";
}
	
//Erzeugt den Body einer Ansicht und gibt diesen als String zurueck
function createViewBodyString(viewId) {
	if (viewId === VIEW_SENTIMENT_ANALYSIS || viewId === VIEW_LANGUAGE_DISTRIBUTION) {
		//Sentiment Analyse und Sprachverteilung haben einen linken und rechten Part
		return "<div class='col-sm-12 col-md-6'></div>" +
			   "<div class='col-sm-12 col-md-6'></div>";
	} else {	
		//Allgemeiner Fall: Keine Unterteilung 
		return "";
	}
}
