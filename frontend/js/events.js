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
 // Die jQuery-Funktionen hide und show ueberschreiben, sodass sie Events abfeuern
var _oldhide = $.fn.hide;
$.fn.hide = function(speed, callback) {
    $(this).trigger('hide'); //Eigenes Event
    return _oldhide.apply(this,arguments); //Alte Funktion aufrufen
};

var _oldshow = $.fn.show;
$.fn.show = function(speed, callback) {
    $(this).trigger('show'); //Eigenes Event
    return _oldshow.apply(this,arguments); //Alte Funktion aufrufen
};

//Ansichten updaten
$(document).on("tm.view.requestUpdate", function(event, tabId, language, viewId) {
	if (DEBUG) console.log("requestUpdate. tabId = "+tabId+", language = "+language+", viewId = "+viewId+" => "+activeViews[viewId]);
	//Ueberpruefen, ob der Benutzer diese Ansicht als aktiv markiert hat
	if (activeViews !== null && activeViews[viewId]) { 
		//Das Panel dieser Ansicht sichtbar machen
		showView(searchItems[tabId].id, language, viewId);
		
		//Ueberpruefen, ob die Daten fuer diese Ansicht fertig geladen wurden und ob es sich um den aktuellen Tab und die aktuelle Sprache handelt
		var viewObject = getViewObject(tabId, language, viewId);
		if (viewObject.isLoaded && !viewObject.isRendered && tabId == activeTabId && (language == activeLanguage || viewId == VIEW_LANGUAGE_DISTRIBUTION)) {
			//Diese Ansicht neu rendern
			viewFunctions[viewId](tabId, language); //Anmerkung: Sprachunabhaengige Ansichten ignorieren den zweiten Parameter
		}
	} else {
		//Diese Ansicht verstecken
		hideView(searchItems[tabId].id, language, viewId);
	}
});

//Anfragen der Ansichten sind fertig geladen
$(document).on("tm.view.dataLoaded", function(event, tabId, language, viewId) {
	if (DEBUG) console.log("loading complete. tab:"+tabId+", lang:"+language+", view:"+viewId);
	//Ueberpruefen, ob die Daten fuer den Compare-Tab geladen wurden
	if (isCompareActive) {
		processMultipleView(activeLanguage, VIEW_SENTIMENT_ANALYSIS);
		processMultipleView(activeLanguage, VIEW_TWEETS_PER_HOUR);
	}		
	//Die entsprechende Ladeanimation ausfaden und ein requestUpdate-Event triggern
	if (viewId == VIEW_TAG_CLOUD) {
		//Die Ladeanimation fuer Tagcloud wird als Ausnahme erst nach dem Rendern ausgefaded
		$(document).trigger("tm.view.requestUpdate", [tabId, language, viewId]);
	} else if (viewId == VIEW_LANGUAGE_DISTRIBUTION) {
		$("#tabBody" + searchItems[tabId].id + " ." + viewClasses[viewId] + " .viewLoading").fadeOut(FADEOUT_TIME, function() {
			$(document).trigger("tm.view.requestUpdate", [tabId, language, viewId]);
		});
	} else {
		$("#tabBody" + searchItems[tabId].id + " #langWrap" + searchItems[tabId].id + "-" + language + " ." + viewClasses[viewId] + " .viewLoading").fadeOut(FADEOUT_TIME, function() {
			$(document).trigger("tm.view.requestUpdate", [tabId, language, viewId]);
		});
	}
});

$(document).on("tm.lang.requestUpdate", function(event, tabId, language) {
	var searchId = searchItems[tabId].id;
	//Den div der alten Sprache ausblenden
	$(".langWrap"+searchId).addClass("hidden");
	
	//Nachschauen, ob diese Sprache schon geladen wurde
	if (isNewLanguage(language, tabId)) {
		//Die passenden divs der neuen Sprache erzeugen
		$("#tabBody"+searchId).append(createLangWrapString(searchId, language));
		//Laden der views starten
		saveSearchItemLangData(tabId, language);
	} else {
		//Den div der neuen Sprache einblenden
		$("#langWrap"+searchId+"-"+language).removeClass("hidden");
	}
});

//Tab wird gewechselt (Alle ausser Compare-Tab)
$("#contentContainer").on("shown.bs.tab", ".searchItemsTabHead", function (event) {
	isCompareActive = false;
	//Die searchId des neuen Tabs holen
	var tabHeadName = $(event.target).attr("id");
	var searchId = getIdFromTabHead(tabHeadName);
	//Den Tab wechseln und evtl. Ansichten updaten
	switchToTab(searchId);
});
//Tab wird gewechselt (Compare-Tab)
$("#contentContainer").on("shown.bs.tab", "#tabHeadCompare", function (event) {
	isCompareActive = true;
	processMultipleView(activeLanguage, VIEW_SENTIMENT_ANALYSIS);
	processMultipleView(activeLanguage, VIEW_TWEETS_PER_HOUR);
});


//Dropdown-Menue zur Auswahl der Sprache
$(".langLink").on("click", function() {
	//Die neue Sprache holen
	var language = $(this).attr("title");
	//Zur neuen Sprache wechseln
	switchToLanguage(language);
});

//Eventhandler registrieren: Suchelementbutton
$('#tmStartbox').on('click','#searchButton', function(event){
	submitSearchItem();
});
// Eventhandler registrieren: Entertaste
$('#tmStartbox').on('keydown','#searchBox', function(event){
	if(event.keyCode == 13) {
		submitSearchItem();
	}
});
// Entertaste im Modaldialog
$('#modalNewSearch').on('keydown', function(event){
	if(event.keyCode == 13) {
		confirmModal($('.modal-body > strong').text());
	}
});

//Button 'Yes' im Modaldialog. Schickt das Ergebnis ab
$("#modalNewSearch").on('click', '#modalConfirmButton', function(event){
	confirmModal($('.modal-body > strong').text());
});

//Links, die zurueck zur Startansicht fuehren
$('.toStartView').on('click', function(event){
	event.preventDefault();
	showInitialView();
});

//Einen einzelnen Suchbegriff verwerfen, bzw. den zugehoerigen Tab schliessen
$('#contentContainer').on('click', '.searchItemsTabHead .glyphicon-remove', function (event) {
	event.stopPropagation(); //Stoppt das Hochbubblen des Events -> Um zu vermeiden, dass ein Tab aktiviert wird, kurz bevor es geschlossen wird
	
	//Die tabId des zu schliessenden Tabs holen
	var tabHeadName = $(event.target).parent().attr("id");
	var searchId = getIdFromTabHead(tabHeadName);
	//Den Tab schliessen
	removeItemFromTabs(searchId);
});

//Automatisches Nachladen beim ModalScroll
var loadingTweets = false; //Variable, die angibt, ob bereits Tweets nachgeladen werden (weil in diesem Falle, nicht noch mehr nachgeladen werden muss)
//TODO: Load enough tweets initially, so the scroll event fires at all....
$("#modalTweet").on("scroll", function() {
	//Nachladen triggern, falls noch nicht nachgeladen wird und fast ganz runter gescrolled wurde
	if (!loadingTweets && $("#modalTweet").scrollTop() >= $("#modalTweet .modal-dialog").height() - $(window).height()) {
		loadingTweets = true;
		showTweets();
	}
});

//Schliessen der Anzeigen fuer einzelne Tweets
$("#modalTweet").on("hide.bs.modal", function() {
	//Loeschen des News-Tabs, falls noetig
	if ($("#newsTab").size() > 0) {
		$("#newsTabHead").remove();
		$("#newsTab").remove();
	}
	$("#modalTweetBody").empty(); //Loeschen der bisher angezeigten Tweets
	$("#modalTweet").removeData(); //Loeschen aller zwischengespeicherten Daten
});

//Klick auf einen Usernamen, wenn einzelne Tweets angezeigt werden
$("#modalTweet").on("click", ".tweetAuthor a", function(event) {
	var idString = $(event.target).parents(".tweetAuthor").attr("id"); //example: user214721tweet8931240
	var idx = idString.indexOf("tweet");
	var userId = idString.substring(4, idx); // -> example: 214721
	var tweetId = idString.substring(idx + 5, idString.length); // -> example: 8931240
	showUserMetadata(userId, tweetId);
});

//Klick auf Feature, um dessen Training-Tweets anzuzeigen
$("#modalTweet").on("click", ".sentimentFeatureUnigram span", function(event) {
	var idString = $(event.target).parents(".sentimentFeatureUnigram").attr("id");
	console.log("Unigram: " + idString);	
	showImportantTrainingTweets(idString, "unigram");
});
$("#modalTweet").on("click", ".sentimentFeatureBigram span", function(event) {
	var idString = $(event.target).parents(".sentimentFeatureBigram").attr("id");
	console.log("Bigram: " + idString);
	showImportantTrainingTweets(idString, "bigram");
});
$("#modalTweet").on("click", ".sentimentFeatureTrigram span", function(event) {
	var idString = $(event.target).parents(".sentimentFeatureTrigram").attr("id");
	console.log("Trigram: " + idString);
	showImportantTrainingTweets(idString, "trigram");
});
$("#modalTweet").on("click", ".sentimentFeatureFourgram span", function(event) {
	var idString = $(event.target).parents(".sentimentFeatureFourgram").attr("id");
	console.log("Fourgram: " + idString);
	showImportantTrainingTweets(idString, "fourgram");
});


var selectionCheckboxes = $('#activeViewsCheckboxes').find('input');
var selectionTiles = $('#activeViewsCheckTiles .selectionTile');
var $activeViewsFooterBox = $('#modalActiveViews').find('.modal-footer');
var meinTimeout = null;

//Die Sichtbarkeit einer einzelnen Ansicht wurde geaendert
$('#activeViewsCheckTiles').on('click', '.selectionTile', function(event) {
	//Die Ansicht hervorheben bei Aktivierung, bzw. ausgrauen bei Deaktivierung
	$(this).toggleClass('active');
	//Die zugehoerige viewId bestimmen
	var rawString = $(this).attr("id");
	var viewClass = rawString.substring(0, rawString.length - 4); //xxxxxxxTile -> xxxxxxx
	var viewId = viewClasses.indexOf(viewClass);
	
	//Die Ansicht mit der bestimmten viewId aktivieren
	changeActiveViews(viewId);
});

//Kurzbeschreibung der einzelnen Ansichten im Footer ein-/ausblenden
var $activeViewsFooterBox = $('#modalActiveViews').find('.modal-footer');
$("#modalActiveViews").on('shown.bs.modal', function() {
	var defaultTimeout = null;
	$('#activeViewsCheckTiles').on('mouseover.infobox', '.selectionTile', function(event) {
		clearTimeout(defaultTimeout);
		$activeViewsFooterBox.addClass('active');
		$activeViewsFooterBox.html(this.dataset.shortdesc);
	}).on('mouseleave.infobox', '.selectionTile', function(event) {
		$activeViewsFooterBox.removeClass('active');
		clearTimeout(defaultTimeout);
		defaultTimeout = setTimeout(setDefaultTextToActiveViewsFooter, 1700);
	});
}).on('hidden.bs.modal', function() {
	$('#activeViewsCheckTiles').off('.infobox');
	setDefaultTextToActiveViewsFooter();
});
var setDefaultTextToActiveViewsFooter = function () {
	$activeViewsFooterBox.html('information needed? hover the tile!');
};

//Navbar Links (Status, About, Contact)
$('.openInOverlay').on('click', function(event) {
	event.preventDefault();
	var URItoLoad = event.currentTarget.href;
	var position = URItoLoad.indexOf("daemon.html");
	if (~position) { //~-1 = 0; ~n = -(n+1)
		//go for iFrame
		var iframe = document.createElement('iframe');
		iframe.src = URItoLoad;
		iframe.style.width = "100%";
		iframe.style.height = "100%";
		iframe.frameBorder = 0;
		$("#modelInfoBody").html(iframe);
	} else { //load it, fill it
		$.get(URItoLoad, function(data) {
			$("#modelInfoBody").html(data);
		});
	}
	$("#modalInfoLoad .modal-header h4").html(event.target.innerHTML);
	$("#modalInfoLoad").modal();
});

//Link zum Ansichten auswaehlen angeklickt
$('#viewSettingsLink').on('click', function(event) {
	showModalActiveViews();
});

//TODO: Rausfinden warum das nicht geht...
/*
//In der RelatedHashtags Ansicht einen neuen Suchbegriff hinzufuegen
$("#contentContainer").on("click", ".relatedHashtags .highcharts-axis-labels:first text", function() {
    submitSearchItemParam("#"+$(this).text());
});*/
