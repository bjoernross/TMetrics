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
//Konstanten
var CAP_LIMIT = 10; //Maximale Anzahl Suchbegriffe in der Tabelle
var TWEET_LIMIT = 500; //Maximale Anzahl an Tweets im Popup
var TWEET_LOADING = 10; //Anzahl an Tweets die im Popup nachgeladen werden
var GOOGLEMAPS_ZOOM = 7; //Zoom-Level in links to Google Maps
var FADEOUT_TIME = 150; //Konstante fuer die Zeit einer Fade-Out-Animation in ms
var FADEIN_TIME = 250; //Konstante fuer die Zeit einer Fade-In-Animation in ms
var TIMEOUT_INITIAL_LOOKUP = 20000; //Konstante fuer die Zeit bevor das erste Mal nachgeschaut wird, ob zu einem neuen Suchbegriff Daten vorliegen
var TIMEOUT_INTERVAL_LOOKUP = 3000; //Konstante fuer die Zeit bevor ein weiteres Mal nachgeschaut wird, ob zu einem neuen Suchbegriff Daten vorliegen
var DEBUG = false; //Konsolen-Logging de-/aktivieren

$(function() { // document.ready wrapper
	//Globale Variablen
	searchItems = new Array();
	
	//Doc: https://ivv5web01.uni-muenster.de:16023/trac/datamining/wiki/DataMining/FrontEnd#Datenhaltung
	//http://stackoverflow.com/questions/12610394/javascript-classes
	SearchItem = (function() {
	    // Konstruktor
	    function SearchItem(id, name, tabId) {
	        this.id = id;
	        this.name = name;
	        this.tabId = tabId;
	        this.isNewTimeout = null; //Timeout-Funktion fuer neue Suchbegriffe, die nachschaut, ob bereits Daten vom Daemon geholt wurden
	        this.all = new DataObject();
	        this.en = new DataObject();
	        this.de = new DataObject();
	        this.other = new DataObject();
	        this.languageDistribution = new ViewObject();
	    };

	    return SearchItem;
	})();
	
	DataObject = (function() {
	    // Konstruktor
	    function DataObject() {
	    	this.view = new Array();
	    	for (var i = 0; i < NUM_LANG_VIEWS; i++) {
	    		this.view.push(new ViewObject());
	    	}
	    	this.count = "Loading";
	    };

	    return DataObject;
	})();
	
	ViewObject = (function() {
		//Konstruktor
		function ViewObject() {
			this.data = {};
			this.xhro = new XHRObject();
			this.isLoaded = false;
			this.isRendered = false;
		};
		
		return ViewObject;
	})();
	
	//Globale Variable zur Speicherung der ID des aktiven Tabs (-1, falls kein Tab aktiv, bzw. vorhanden)
	activeTabId = -1;

	//Globale Variable zur Speicherung des aktiven Sprachfilters (leerer String, falls nicht gefiltert wird)
	activeLanguage = loadActiveLanguageFromLocalStorage();
	updateLanguageLinks(activeLanguage);
	
	//Boolean-Array, das angibt, welche Ansichten aktiv sind
	activeViews = loadActiveViewsFromLocalStorage();
	
	//Gloable Variable, die angibt, ob aktuell der CompareTab angeschaut wird
	isCompareActive = false;
	
	//Typeahead Lib fuer das Input-Feld initialisieren
	$('#searchBox').typeahead([{
		remote: {
			url: '/rest/queries/suggestions/?q=%QUERY', 
			datatype: 'json',
			filter: function (parsedResponse) {
	            // parsedResponse ist das Array, das vom Backend zurueckgegeben wird
				// Davon wird lediglich das data-Feld benoetigt
	            return parsedResponse.data;
	        }
		},
		limit: 5,
		valueKey: "String"
	}]);
	
	//Bootstrap Tooltips initialisieren
	$(document).tooltip({
	    selector: '[data-toggle=tooltip]'
	});
	
	//Ladeanimationsbild und Ansichtsauswahlbilder vorladen
	setTimeout(function() {
		new Image().src = "../img/loading.gif";
		for (var i = 0; i < NUM_VIEWS; i++) {			
			new Image().src = "../img/selectionTiles/" + viewClasses[i] + ".png";
		}
	}, 10);
	
	//Automatischer Fokus auf die Suchleiste, wenn die Seite geladen wird
	$('#searchBox').focus();
});

function submitSearchItem() {
	var searchItem = $('#searchBox').val();
	submitSearchItemParam(searchItem);
}

//Suchbegriff wurde erstmals abgeschickt. Falls das Limit fuer Suchbegriffe noch nicht erreicht wurde, wird das Eintragen des aktuellen Begriffs eingeleitet
function submitSearchItemParam(searchItem) {
	//Uberpruefen, ob ein weiterer Suchbegriff hinzugeufegt werden darf oder ob das Limit ueberschritten ist
	if ($('#searchItemsTabBar li').length > CAP_LIMIT) { // > statt >= wegen des compareTabs
		showAlert("You can't search for more than <strong>" + CAP_LIMIT + "</strong> search items at a time.<br />"+
			"Delete at least one search item from the search item table and try again.", 
			$("#alertContainer"), "danger", true);
		$('#searchBox').typeahead('setQuery', '');
		$('#searchBox').focus();
	} else {
		var query = "/rest/queries/contains/?q="+encodeURIComponent(searchItem);
		var xhro = new XHRObject(query, "GET", XHR_PRIORITY_SPECIAL, 
	    	//doneFunction
			function(response) {
				if (response.data === true) {
					//Suchbegriff (existiert bereits) weiterverarbeiten
					processSearchItem(searchItem, false); 
					$('#searchBox').typeahead('setQuery', '');
					$('#searchBox').focus();
				} else {
					//Neuer Suchbegriff
					showModalDialog(searchItem);
				}
			},
	    	//failFunction
	    	function(event) {
				//Falls die Anfrage abgebrochen wird, soll keine Fehlermeldung erscheinen
				if(event.statusText !== "abort") {
					showServerError(1, $("#alertContainer"));			
				}
	    	}
	    );

	    //In die XHRObkjekt-Verwaltung einfuegen
	    addXHRObject(xhro);
	}
}

// Holt die ID des Suchbegriffs und triggert die Ausgabe an
// Falls der Suchbegriff bereits existiert, wird der passende Tab eingeblendet
function processSearchItem(searchItem, isNew) {
	var query = "/rest/queries/bystring/?q="+encodeURIComponent(searchItem);
	var xhro = new XHRObject(query, "GET", XHR_PRIORITY_SPECIAL, 
    	//doneFunction
		function(response) {
			var searchId = response.data.id;
		
			//Nachschauen, ob bereits ein Suchbegriff mit dieser ID existiert
			var found = false;
			for (var i = 0; i < searchItems.length; i++) {
				if (searchItems[i].id === searchId) {
					found = true;
				}
			}
			
			//In die Analyseansicht wechseln, wenn noetig
			showAnalysisView();
			
			if (found == false) {
				var tabId = searchItems.length;
				//if (DEBUG) console.log("new active tab#", activeTabId);
				//Erzeugt ein neues SearchItem Objekt und haengt dieses ans Ende des globalen searchItems-Array
				searchItems.push(new SearchItem(searchId, searchItem, tabId));
				//Den neuen Suchbegriff im localStorage speichern
				addSearchItemToLocalStorage(searchItem);
				//Falls keine gespeicherten aktiven Anischten im localStorage liegen, muessen diese neu ausgewaehlt werden
				if (isFalseArray(activeViews)) {
					showModalActiveViews();
				}
				//Einen neuen Tab fuer den Suchbegriff erstellen und diesen anzeigen			
				addItemToTabs(searchId, searchItem, activeLanguage);
				
				//Es wird ein Einzel-Tab angezeigt
				isCompareActive = false;
				
				//Extra Wartezeit mit Ladebalken fuer neuen Suchbegriff
				if (isNew) {
					processNewItem(tabId, activeLanguage, true, false);
				} else {
					//Laden der Daten (REST-Anfragen) zu diesem Suchbegriff anstossen
					saveSearchItemData(tabId, activeLanguage);
				}
			} else {
				//Zum Tab mit diesem Begriff wechseln, da Suchbegriff bereits existiert
				$("#tabHead" + searchId).tab('show');
			}
		},
    	//failFunction
    	function(event) {
			//Falls die Anfrage abgebrochen wird, soll keine Fehlermeldung erscheinen
			if(event.statusText !== "abort") {
				showServerError(2, $("#alertContainer"));			
			}
    	}
    );

    //In die XHRObkjekt-Verwaltung einfuegen
    addXHRObject(xhro);
}

//Suchbegriff zum Speichern zum Server schicken
function insertSearchItemIntoDatabase(searchItem) {
	var query = "/rest/queries/post/?q="+encodeURIComponent(searchItem);
	var xhro = new XHRObject(query, "POST", XHR_PRIORITY_SPECIAL, 
    	//doneFunction
		function(response) {
			//Den Suchbegriff weiterverarbeiten, wobei uebergeben wird, dass es sich um einen neuen Suchbegriff handelt
			processSearchItem(searchItem, true);
		},
    	//failFunction
    	function(event) {
			//Falls die Anfrage abgebrochen wird, soll keine Fehlermeldung erscheinen
			if(event.statusText !== "abort") {
				showServerError(3, $("#alertContainer"));			
			}
    	}
    );

    //In die XHRObkjekt-Verwaltung einfuegen
    addXHRObject(xhro);
}

function processNewItem(tabId, language, isFirstTime, isAvailable) {
	if (DEBUG) console.log("processNewItem. isAvailable = ", isAvailable, "isFirstTime", isFirstTime);
	//Beim ersten Aufruf dieser Funktion soll laenger gewartet werden, bis beim Daemon nachgeschaut wird, ob schon Daten zum neuen Suchbegriff vorliegen
	//Zusaetzlich muss auch die Progressbar gezeichnet werden
	if (isFirstTime) {
		//Die Resultatsansichten ausblenden, solange der Daemon noch keine Ergebnisse hat
		$("#tabBody" + searchItems[tabId].id + " .viewPanel").addClass("hiddenNewItem");
		//Ladebalken anzeigen
		$("#tabBody" + searchItems[tabId].id).append(
				"<div class='progress progress-striped active'>" +
					"<div id='progressBar" + tabId + "' class='progress-bar'  role='progressbar' style='width: 0;'>" +
					"</div>" +
				"</div>");
		//Verzoegerten Aufruf mit initialer Ladezeit starten
		startDelayedLookup(tabId, language, TIMEOUT_INITIAL_LOOKUP, FADEOUT_TIME);
	} else {		
		if (isAvailable) { //Falls nun Daten zu diesem Suchbegriff vorhanden sind
			//Ladebalken nicht mehr anzeigen
			$("#progressBar" + tabId).parent(".progress").hide();
			//Es sind Daten verfuegbar, also sollten diese geladen und angezeigt werden
			saveSearchItemData(tabId, language);
			$("#tabBody" + searchItems[tabId].id + " .viewPanel").removeClass("hiddenNewItem");
		} else { //Falls immer noch keine Daten zu diesem Suchbegriff vorhanden sind
			//Ladebalken zuruecksetzen
			var bar = $("#progressBar" + tabId);
			bar.css("transition", "width 0s linear 0s");
			bar.width(0);
			//Erneute verzoegerete Abfrage senden, mit TIMEOUT_INTERVAL_LOOKUP Verzoegerung
			startDelayedLookup(tabId, language, TIMEOUT_INTERVAL_LOOKUP, 0);
		}
	}
}

//Einen verzoegerte Nachfrage (mit timeout ms) beim Daemon stellen und den Ladebalken entsprechend animieren
function startDelayedLookup(tabId, language, timeout, waitTimeout) {
	//Ladebalken-Animation starten
	var bar = $("#progressBar" + tabId);
	setTimeout(function(){
		bar.css("transition", "width " + (timeout / 1000) + "s linear");
		bar.css("width", "100%");
	},waitTimeout);
	//Den verzoegerten Aufruf starten, der schaut, ob der Daemon bereits geladen hat.
	searchItems[tabId].isNewTimeout = setTimeout(function() {
		//Nachschauen, ob der Daemon bereits Daten zum Suchbegriff geholt hat
		checkDataAvailable(tabId, language); 
	}, timeout);
}

//Ueberprueft, ob der Daemon bereits mindestens einmal nach einem bestimmten Suchbegriff gesucht hat
function checkDataAvailable(tabId, language) {
	var query = "/rest/queries/hasDaemonFetched/?id=" + searchItems[tabId].id;
	var xhro = new XHRObject(query, "GET", XHR_PRIORITY_ACTIVE, 
    	//doneFunction
		function(response) {
			//Abfangen, wenn die interne Verarbeitung fehlschlaegt
			if (response.data === null) {
				showServerError(4, $("#alertContainer"));
				return;
			}
			processNewItem(tabId, language, false, response.data);
		},
    	//failFunction
    	function(event) {
			//Falls die Anfrage abgebrochen wird, soll keine Fehlermeldung erscheinen
			if(event.statusText !== "abort") {
				showServerError(5, $("#alertContainer"));			
			}
    	}
    );

    //In die XHRObkjekt-Verwaltung einfuegen
    addXHRObject(xhro);
}

//Speichert alle relevaten Daten 
function saveSearchItemData(tabId, language) {	
	//Laden der Daten fuer die einzelnen Ansichten starten
	saveSearchItemLangData(tabId, language);
	saveSearchItemLanguages(tabId);
}

//Speichert alle sprachabhaengigen Daten
function saveSearchItemLangData(tabId, language) {
	saveSearchItemMetaData(tabId, language);
	saveSearchItemSentiment(tabId, language);
	saveSearchItemTimeCurve(tabId, language);
	saveSearchItemSentimentTimeCurve(tabId, language);
	saveSearchItemHashtags(tabId, language);
	saveSearchItemTagCloud(tabId, language);
	saveSearchItemClusterResults(tabId, language);
	saveSearchItemClusterResultsTweets(tabId, language);
}

//Holt die Metadaten zu einem Suchbegriff vom REST-Service und speichert sie ab
//Von den Metadaten wird lediglich die Anzahl der Tweets verwendet
function saveSearchItemMetaData(tabId, language) {
	//Alte Metadaten-Informationen zuruecksetzen
	updateMetadata(tabId, language);
	
	var query ="/rest/queries/metadata/?id=" + searchItems[tabId].id + 
		((language === "") ? "" : "&lang=" + language);
	
	var xhro = new XHRObject(query, "GET", XHR_PRIORITY_ACTIVE, 
		//doneFunction
		function(response) {
			//Metadaten speichern und anzeigen
			getDataObject(tabId, language).count = response.data.count;
			updateMetadata(tabId, language);
		},
		//failFunction
		function(event) {
			//Falls die Anfrage abgebrochen wird, soll keine Fehlermeldung erscheinen
			if(event.statusText !== "abort") {
				showServerError(6, $("#alertContainer"));
			}
		}
	);
	
	//In die XHRObkjekt-Verwaltung einfuegen
	addXHRObject(xhro);
}

function updateMetadata(tabId, language) {
	//Die Tweet Anzahl des aktuellen Tabs mit aktuellem Sprachfilter holen
	var count = getDataObject(tabId, language).count;
	//Die Anzeige im Tooltip aktualisieren
	$("#tabHead"+searchItems[tabId].id).parent()
		.attr("title", count + " tweets")
		.tooltip("fixTitle");
}

//Modaldialog anzeigen
function showModalDialog(searchItem) {
	$('.modal-body > strong').text(searchItem);
	$("#modalNewSearch").modal();
}

//Modaldialog bestaetigen
function confirmModal(searchItem) {
	$("#modalNewSearch").modal("hide");
	insertSearchItemIntoDatabase(searchItem);
}

//Gibt das Daten-Objekt fuer den entsprechenden Tab entsprechend der Filter-Sprache zurueck
function getDataObject(tabId, language) {		
	if (language === "") {
		return searchItems[tabId].all;
	} else if (language === "en") {
		return searchItems[tabId].en;
	} else if (language === "de") {
		return searchItems[tabId].de;
	} else {
		return searchItems[tabId].other;
	}
}

//Gibt das View-Objekt fuer den entsprechenden Tab entsprechend der Filter-Sprache und der verwendeten viewId zurueck
//Rueckgabe null, falls kein Tab aktiv ist
function getViewObject(tabId, language, viewId) {
	if (tabId >= 0 && searchItems[tabId] != undefined) {
		if (viewId < NUM_LANG_VIEWS) {
			return getDataObject(tabId, language).view[viewId];
		} else {
			if (viewId == VIEW_LANGUAGE_DISTRIBUTION) {
				return searchItems[tabId].languageDistribution;
			}
			//Hier koennte man weitere Sprachunabhaengige Ansichten erweitern
		}
	} else {		
		return null;
	}
}

//Bricht alle XMLHttpRequests zu einem Suchbegriff ab, d.h. fuer alle seine Sprachen und Ansichten
function abortViewRequests(tabId) {
	searchItems[tabId].languageDistribution.xhro.request.abort();
	for (var viewId = 0; viewId < NUM_LANG_VIEWS; viewId++) {
		if (searchItems[tabId].all.view[viewId].xhro.query !== undefined)
			finishXHRObject(searchItems[tabId].all.view[viewId].xhro);
		if (searchItems[tabId].en.view[viewId].xhro.query !== undefined)
			finishXHRObject(searchItems[tabId].en.view[viewId].xhro);
		if (searchItems[tabId].de.view[viewId].xhro.query !== undefined)
			finishXHRObject(searchItems[tabId].de.view[viewId].xhro);
		if (searchItems[tabId].other.view[viewId].xhro.query !== undefined)
			finishXHRObject(searchItems[tabId].other.view[viewId].xhro);
	}
}

//Funktion zum Anzeigen von Warnmeldungen/Fehlern
/*params: 
 * message: Die Nachricht, die man anzeigen lassen will (kann auch HTML sein)
 * parent: Der direkte Vorfahre im DOM als jQueryObject
 * type(optional):{"warning", "danger", "info", "success"}; default: "warning"
 * closeable(optional): true|false; default: false; Gibt an, ob der Alert schlie\DFbar ist
 */
function showAlert(message, $parent, type, closeable) {
	//Optionale Parameter auf default-Werte setzen, falls noetig
	if (type === undefined) type = "warning";
	if (closeable === undefined) closeable = true;
	//Bereits vorhandene Fehlermeldungen loeschen
	$parent.find(".alert").remove();
	//String fuer den Button zum Schliessen erzeugen, falls noetig
	var closeString = (closeable) ? "<button type='button' class='close' data-dismiss='alert' aria-hidden='true'><span class='glyphicon glyphicon-remove'></span></button>" : "";
	//Den Alert erzeugen
	$parent.append("<div class='alert alert-" + type + "'>" + closeString + message + "</div>");
}

//Anzeige einer Standard-Fehlermeldung bei einem internen Server-Fehler
function showServerError() {
	showServerError(200, $("#alertContainer"));
}

function showServerError(errorCode, $parent) {
	var $loadingDiv = $parent.find(".viewLoading");
	//Falls es sich, um einen Fehler in einer Ansicht handelt, soll die Ladeanimation ausgeblendet werden
	if ($loadingDiv.size() == 0) {
		showAlert("<strong>Internal Server Error #" + errorCode + "</strong>. Sorry, your request could not be processed. Please contact <strong>info&#64;tmetrics.de</strong> and try again later.", $parent, "danger", true);
	} else {		
		$loadingDiv.fadeOut(FADEOUT_TIME, function() {		
			showAlert("<strong>Internal Server Error #" + errorCode + "</strong>. Sorry, your request could not be processed. Please contact <strong>info&#64;tmetrics.de</strong> and try again later.", $parent, "danger", false);
		});
	}
}

function showNoDataWarning($parent) {
	showAlert("<strong>Not enough data available for this view.</strong>", $parent, "warning", false);
}

//Funktion zum Anzeigen von Warnmeldungen/Fehlern an fixer Poisiotn, die nach einiger Zeit selbst verschwinden
//Momentan nicht mehr im Gebrauch, da Warnungen nicht einfach so verschwinden sollten!
/*params: 
 * message: Die Nachricht, die man anzeigen lassen will (kann auch HTML sein)
 * type(optional):{"warning", "danger", "info", "success"}; default: "warning"
 * closeable(optional): true|false; default: false; Gibt an, ob der Alert schliessbar ist
 * time(optional): Zeit in ms, bevor der Alert automatisch verschwindet; default: 5000
 */
function showAlertFade(message, type, closeable, time) {
	//Optionale Parameter (die nicht in showAlert behandelt werden) auf default-Werte setzen, falls noetig
	if (time === undefined) time = 5000;
	
	$("#contentContainer").prepend('<div class="alert-fade"></div>');
	showAlert(message, $("#contentContainer .alert-fade"), type, closeable);
	setTimeout(function() {	
		$(".alert-fade").fadeOut(function () {
			$(".alert-fade").remove();
		});
	}, time);
}

//Extrahiert aus dem Bezeichner eines TabHeads die searchId
function getIdFromTabHead(tabHeadName) {
	return parseInt(tabHeadName.substring(7, tabHeadName.length)); //tabHead17 -> 17
}

//Gibt den Index (tabId) zurueck dessen Suchbegriff die angegebene ID hat. Falls es keine passende tabId gibt, wird searchItems.length zurueckgegeben -> compareTab
function getTabIdFromSearchId(searchId) {
	//Alle Suchbegriffe nach der searchId durchsuchen
	for (var i = 0; i < searchItems.length; i++) {
		if (searchItems[i].id === searchId) {
			return i;
		}
	}
	//Default
	return searchItems.length;
}

//Wandelt ein Date-Objekt in die entsprechende String-Repraesentation nach ISO 8601 um
//Ausgabeformat: YYYY-MM-DDThh:mmZ , wobei T und Z Literale sind
function dateToString(date) {
	var year = date.getUTCFullYear();
	var month = convertToTwoDigits(date.getUTCMonth() + 1); //Month faengt bei 0 an in Javascript, also muessen wir 1 addieren
	var day = convertToTwoDigits(date.getUTCDate());
	var hour = convertToTwoDigits(date.getUTCHours());
	var minute = convertToTwoDigits(date.getUTCMinutes());
	
	return year+"-"+month+"-"+day+"T"+hour+":"+minute; 
}

//Fuegt vor Zahlen mit nur einer Ziffer eine 0 ein, zweistellige Zahlen werden nicht veraendert
//Zahlen mit drei Ziffern oder negative Zahlen erzeguen einen Fehler
function convertToTwoDigits(number) {
	if (number < 0) throw new RangeError("Can't convert a negative number to two digits.");
	
	numberString = "" + number;
	
	switch (numberString.length) {
	case 1:
		return "0"+numberString;
	case 2:
		return numberString;
	default:
		throw new RangeError("Can't convert a number to two digits, that already has three or more digits.");
	}
}

//Fuegt digits Nullen vor number ein. Resultat ist ein String
//Wird bisher nicht benoetigt
function addTrailingZeros(number, digits) {
	var zeros = "";
	for (var i = 0; i < digits; i++) {
		zeros += "0";
	}
	return zeros + number;
}