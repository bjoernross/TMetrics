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
var LS_DEBUG = false;
var savedSearchItems = new Array();

$(function(){
	// ***** Event Handler *****
	$('#tmStartbox').on('click','#formerSearchItemsStub a',function(event) {
		event.preventDefault();
		var recoveredItem = event.target.innerHTML;
		$('#searchBox').val(recoveredItem);
		$('#searchButton').trigger('click');
	});
	
	//Gespeicherte Suchbegriffe laden, falls vorhanden
	var formerSearchItemsStub = '<div class="btn-group col-sm-offset-2" id="formerSearchItemsStub"><button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown"><span class="titel">Former search terms</span> <span class="caret"></span></button><ul class="dropdown-menu" role="menu" id="formerSearchItems"></ul></div>';
	var storageRawItems = localStorage.getItem('savedSearchItems');
	if (storageRawItems != undefined && storageRawItems != "") {
		savedSearchItems = JSON.parse(storageRawItems);
		if (LS_DEBUG) console.log('savedSearchItems:', savedSearchItems);
		if (savedSearchItems.length > 0) {
			if (LS_DEBUG) console.log("Elemente im Array vorhanden:",savedSearchItems.length);
			$('#tmStartbox').append(formerSearchItemsStub);
			//Suchbegriffe anzeigen
			drawLocalStorageList();
			//Alle Suchbegriffe aus dem localStorage loeschen
			savedSearchItems = new Array();
			localStorage.setItem('savedSearchItems', JSON.stringify(savedSearchItems));
		}
	}
});
 
//Funktion zum Zeichnen eines UI-Elements, das die letzten Suchbegriffe anzeigt
function drawLocalStorageList() {
	if (savedSearchItems.length == 0) $('#formerSearchItemsStub').remove();
	else $('#formerSearchItems').empty();
	for (oneItem in savedSearchItems) {
		$('#formerSearchItems').append('<li><a href="">'+savedSearchItems[oneItem]+'</a></li>');
	}
}

//Funktion zum Entfernen eines List-Items in der Oberflaeche. 
function removeLocalStorageListItem(itemName) {
	//Falls es sich, um das letzte Listenelement handelt, wird die gesamte Liste geloescht
	if ($("#formerSearchItems").children().length === 1) {
		$("#formerSearchItemsStub").remove();
	} else {
		//Ansonsten wird das betreffende Element geloescht, falls vorhanden
		var listLink = $("#formerSearchItems a:contains(" + itemName + ")");
		if (listLink.length !== 0) {
			listLink.parent("li").remove();
		}
	}
}

//Einen einzelnen Suchbegriff zum localStorage hinzufuegen, falls noch nicht vorhanden
function addSearchItemToLocalStorage(item) {
	if (typeof item === 'string') {
		item = item.toLowerCase();
		if (LS_DEBUG) console.log("add: "+item);
		var position = savedSearchItems.indexOf(item);
		//Suchbegriff nur hinzufuegen, falls er noch nicht im Array ist 
		if (!~position) savedSearchItems.push(item); //~-1 = 0; ~n = -(n+1)
		if (LS_DEBUG) console.log(JSON.stringify(savedSearchItems));
		localStorage.setItem('savedSearchItems', JSON.stringify(savedSearchItems));
		//Anzeige aktualisieren
		removeLocalStorageListItem(item);
	}
}

//Einen einzelnen Suchbegriff aus dem localStorage entfernen, falls vorhanden
function removeSearchItemFromLocalStorage(item) {
	//Ueberpruefen, ob der Suchbegriff ueberhaupt im localStorage gespeichert ist, weil sonst auch nichts entfernt werden muss
	item = item.toLowerCase();
	if (LS_DEBUG) console.log("remove: "+item);
	var position = savedSearchItems.indexOf(item);

	if (~position) { //~-1 = 0; ~n = -(n+1)
		savedSearchItems.splice(position, 1);
		position = savedSearchItems.indexOf(item);
		if (LS_DEBUG) console.log(JSON.stringify(savedSearchItems));
		localStorage.setItem('savedSearchItems', JSON.stringify(savedSearchItems));
	}
}

//Das boolean-Array der aktiven Ansichten aus dem localStorage laden 
//Gibt bei Erfolg ein boolean-Array zurueck. Default alles auf false
function loadActiveViewsFromLocalStorage() {
	var storageRawViews = localStorage.getItem('activeViews');
	if (storageRawViews != undefined && storageRawViews != "") {
		var views = JSON.parse(storageRawViews);
		if (LS_DEBUG) console.log("activeViews geladen:",views);
		return views;
	}
	//Ein false-Array erzeugen als Default
	var falseArray = new Array();
	for (var i = 0; i < NUM_VIEWS; i++) {
		falseArray.push(false);
	}
	return falseArray;
}

//Gibt fuer ein boolsches Array true zurueck, falls es ausschlie�lich false enthaelt. Andernfalls false. 
function isFalseArray(boolArray) {
	for (var i = 0; i < boolArray.length; i++) {
		if (boolArray[i] === true) {
			return false;
		}
	}
	return true;
}

//Das boolean-Array der aktiven Ansichten im localStorage speichern
function saveActiveViewsToLocalStorage(views) {
	localStorage.setItem('activeViews', JSON.stringify(views));
	if (LS_DEBUG) console.log("activeViews gespeichert:",views);
}

//Den Sprachstring der aktuellen Sprache aus dem localStorage laden 
//Gibt bei Erfolg einen Sprachcode-String zurueck. Default: Leerer String
function loadActiveLanguageFromLocalStorage() {
	var storageLanguage = localStorage.getItem('activeLanguage');
	if (storageLanguage != undefined) {
		if (LS_DEBUG) console.log("activeLanguage geladen:",storageLanguage);
		return storageLanguage;
	}
	//Leerer String als Default-Rueckgabe
	return "";
}

//Das Sprachstring der aktuellen Sprache im localStorage speichern
function saveActiveLanguageToLocalStorage(language) {
	localStorage.setItem('activeLanguage', language);
	if (LS_DEBUG) console.log("activeLanguage gespeichert:",language);
}