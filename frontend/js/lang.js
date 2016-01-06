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
//Wechsel der Sprache
function switchToLanguage(language) {
	//Nichts tun, wenn die Sprache sich nicht aendert
	if (activeLanguage == language) {
		if (DEBUG) console.log("didnt change language. still: "+language);
		return;
	}
	//Den angeklickten Sprachlink optisch hervorheben
	updateLanguageLinks(language);
	
	//Aenderungen im localStorage speichern
	saveActiveLanguageToLocalStorage(language);
	
	//Globale Variable der Sprache anpassen
	var oldLanguage = activeLanguage;
	activeLanguage = language;
	
	//Die Sprachansicht wechseln, falls man sich nicht in der Startansicht befindet
	if (searchItems.length > 0) {		
		processLanguageChange(language, oldLanguage);
	}
}

//Triggert Anzeige-Events und kuemmert sich um die Verwaltung der XHRs beim Sprachwechsel
function processLanguageChange(language, oldLanguage) {
	if (DEBUG) console.log("new active lang = "+activeLanguage);
	
	//Aenderungen der Anzeige triggern
	$(document).trigger("tm.lang.requestUpdate", [activeTabId, language]);
	
	//Nachschauen, ob diese Sprache schon geladen wurde
	if (isNewLanguage(language, activeTabId)) {
		//Anfragen der alten Sprache in den Passiv-Pool verschieben
		for (var viewId = 0; viewId < NUM_LANG_VIEWS; viewId++) {
			var viewObject = getViewObject(activeTabId, oldLanguage, viewId);
			//Falls die Anfrage noch nicht abgearbeitet ist, soll sie in den Passiv-Pool verschoben werden
			if (!viewObject.isLoaded) {
				moveXHRObject(viewObject.xhro, XHR_PRIORITY_PASSIVE);
			}
			//View aktualisieren, falls sie angezeigt werden soll, aber noch nicht gerendert wurde
			$(document).trigger("tm.view.requestUpdate", [activeTabId, language, viewId]);
		}
	} else {
		for (var viewId = 0; viewId < NUM_LANG_VIEWS; viewId++) {
			var newViewObject = getViewObject(activeTabId, language, viewId);
			var oldViewObject = getViewObject(activeTabId, oldLanguage, viewId);
			//Alle neue Requests, die angezeigt werden sollen, in den Aktiv-Pool verschieben
			if (activeViews[viewId] && !newViewObject.isLoaded) {			
				moveXHRObject(newViewObject.xhro, XHR_PRIORITY_ACTIVE);
			}
			//Alle Requests der alten Sprache in den Passiv-Pool verschieben
			if (!oldViewObject.isLoaded) {			
				moveXHRObject(oldViewObject.xhro, XHR_PRIORITY_PASSIVE);
			}
			//View aktualisieren, falls sie angezeigt werden soll, aber noch nicht gerendert wurde
			$(document).trigger("tm.view.requestUpdate", [activeTabId, language, viewId]);
		}
		
		//Metadaten anpassen
		updateMetadata(activeTabId, language);
	}
}

//Grafische Anpassung des Sprachfilters. Hebt die aktive Einstellung hervor.
function updateLanguageLinks(language) {
	//Alle List Items deaktivieren
	$("#langSettingsInner").find("li").removeClass("active");
	//Nachschauen, ob es einen Link zur aktuellen Sprache gibt, ansonsten "Other" waehlen
	var $link = $("#langSettingsInner .langLink[title='" + language + "']");
	if ($link.size() > 0) {		
		//List Item der aktuellen Sprache auf aktiv setzen
		$link.parent().addClass("active");
	} else {
		var $otherListItem = $("#langSettingsInner ul li:last-child");
		//Sprache von "Other" anpassen auf aktuelle Sprache
		$otherListItem.find("a").attr("title", language);
		//List Item der aktuellen Sprache auf aktiv setzen
		$otherListItem.addClass("active");
	}
}

//Erzeugt einen String fuer die Ansicht einer einzelnen Sprache (inkl. aller sprachabhaengigen Ansichten)
function createLangWrapString(searchId, language) {
	return "<div id='langWrap" + searchId + "-" + language + "' class='langWrap" + searchId + "'>" + 
		createViewString(VIEW_SENTIMENT_ANALYSIS, searchId, language) +
		createViewString(VIEW_TWEETS_PER_HOUR, searchId, language) +
		createViewString(VIEW_SENTIMENT_PER_HOUR, searchId, language) +
		createViewString(VIEW_RELATED_HASHTAGS, searchId, language) +
		createViewString(VIEW_TAG_CLOUD, searchId, language) +
		createViewString(VIEW_CLUSTER_ANALYSIS, searchId, language) +
		createViewString(VIEW_CLUSTER_ANALYSIS_TWEETS, searchId, language) +
	"</div>"; 
}

//Funktion, die angibt, ob bereits ein HTML-Objekt zur angegebenen Sprache und Suchbegriff existiert
//und somit gibt es auch an, ob der Ladevorgang der einzelnen Ansichten dieser Kombination aus Sprache und Suchbegriff bereits angetriggert wurde
function isNewLanguage(language, tabId) {
	//Falls es noch kein entsprechendes langWrap-Div-Objekt in einem Einzel-Tab gibt, handelt es sich um eine neue Kombination aus Sprache und Suchbegriff
	return ($("#langWrap" + searchItems[tabId].id + "-" + language).size() == 0);
}