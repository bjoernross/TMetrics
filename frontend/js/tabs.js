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
//Alle Views eines Tabs updaten und diese aktivieren (activeTabId setzen)
function switchToTab(searchId) {
	//Die zugehoerige tabId holen
	var tabId = getTabIdFromSearchId(searchId);
	
	//Globale Variable des aktuellen Tabs anpassen
	var oldTabId = activeTabId;
	activeTabId = tabId;
	
	//Die Sprachansicht aktualisieren
	$(document).trigger("tm.lang.requestUpdate", [tabId, activeLanguage]);
	
	for (var viewId = 0; viewId < NUM_VIEWS; viewId++) {
		var newViewObject = getViewObject(tabId, activeLanguage, viewId);
		var oldViewObject = getViewObject(oldTabId, activeLanguage, viewId);
		//Alle neue Requests, die angezeigt werden sollen, in den Aktiv-Pool verschieben
		if (activeViews[viewId] && !newViewObject.isLoaded) {			
			moveXHRObject(newViewObject.xhro, XHR_PRIORITY_ACTIVE);
		}
		//Alle Requests des alten Tabs in den Passiv-Pool verschieben, falls es einen alten Tab gab
		if (oldViewObject != null && !oldViewObject.isLoaded) {			
			moveXHRObject(oldViewObject.xhro, XHR_PRIORITY_PASSIVE);
		}
		//View aktualisieren, falls sie angezeigt werden soll, aber noch nicht gerendert wurde
		$(document).trigger("tm.view.requestUpdate", [tabId, activeLanguage, viewId]);
	}
	
	//Metadaten anpassen
	updateMetadata(tabId, activeLanguage);
	
	if (DEBUG) console.log("new active tab = "+activeTabId);
}

//Fuegt einen Suchbegriff als neuen Tab ein
function addItemToTabs(searchId, searchItem, language) {
	var itemTabHead = 
		"<li data-toggle='tooltip' data-placement='auto bottom' title='Loading tweets'>" +
			"<a id='tabHead" + searchId + "' class='searchItemsTabHead' data-target='#tabBody" + searchId + "' data-toggle='tab'>" +
				"<span class='searchItemTitle'>" + searchItem + "</span> <span class='glyphicon glyphicon-remove'></span>" +
			"</a>" +
		"</li>";
	
	var itemTabBody = 
		"<div class='analysisWrap tab-pane fade' id='tabBody" + searchId + "'>" +
			createViewString(VIEW_LANGUAGE_DISTRIBUTION, searchId, language) +
			createLangWrapString(searchId, language) + 
		"</div>";

	//Den Tab einfuegen ans Ende der Suchbegriffe (also direkt vor der Vergelichsansicht) einfuegen
	$("#tabHeadCompare").parent().before(itemTabHead);
	$("#tabBodyCompare").before(itemTabBody);
	
	//Den Compare-Tab einblenden, falls es nun mehr als einen Tab gibt und der Compare Tab noch nicht angezeigt wurde
	if (searchItems.length === 2) {
		$("#tabHeadCompare").parent("li").fadeIn(FADEIN_TIME);
	}
	
	//Den soeben hinzugefuegten Tab einblenden
	$("#tabHead" + searchId).tab('show');
}

//Loescht einen bestehenden Tab
function removeItemFromTabs(searchId) {
	//Metadata-Tooltip ausblenden
	$("#tabHead"+searchId).parent().tooltip("hide");
	//Falls der letzte Tab geschlossen wird, zur Startansicht wechseln
	if (searchItems.length === 1) {
		showInitialView();
	} else {
		//Die Elemente des TabHead und den TabBody des zu entfernenden Tabs bestimmen
		var tabBodyToRemove = $("#tabBody" + searchId);
		var tabHeadToRemove = $("#tabHead" + searchId).parent("li");
		//Schauen, ob man auf Tab #0 wechseln sollte. Falls man den aktuell aktiven Tab geschlossen hat oder
		//Dies darf aber erst nach dem Loeschen das aktiven Tabs geschehen, weil es sonst Fehler gaebe, falls der aktive Tab auch der erste Tab ist
		var needTabChange = false;
		if (tabHeadToRemove.hasClass('active') || isCompareActive) {
			needTabChange = true;
		}
		//Den CompareTab ausblenden und den einzig uebrigen Tab aktivieren
		if (searchItems.length === 2) {
			$("#tabHeadCompare").parent("li").fadeOut(FADEOUT_TIME);
		}

		//Den Tab ausfaden
		tabHeadToRemove.fadeOut(FADEOUT_TIME, function() {
			//Den Tab loeschen
			tabBodyToRemove.remove();
			tabHeadToRemove.remove();
			//Die zugehoerigen Daten loeschen
			removeSearchItemData(searchId);
			//Auf Tab #0 wechseln, falls noetig
			if (needTabChange) {
				$('#searchItemsTabBar li:first-child a').tab('show');
			}
		});
	}
}

//Setzt einen einzelnen Suchbegriff zurueck
function removeSearchItemData(searchId) {
	var tabId = getTabIdFromSearchId(searchId);
	//Fuer neue Suchbegriffe die Timeout-Funktion clearen
	if (searchItems[tabId].isNewTimeout != null) {
		clearTimeout(searchItems[tabId].isNewTimeout);
	}
	//Suchbegriff aus dem localStorage entfernen
	removeSearchItemFromLocalStorage(searchItems[tabId].name);
	//XMLHttpRequests fuer diesen Suchbegriff abbrechen
	abortViewRequests(tabId);
	//Suchbegriff aus dem globalen Array entfernen
	searchItems.splice(tabId, 1);
	if (DEBUG) console.log("item["+tabId+"] removed. new searchItems[] = ", searchItems);
}