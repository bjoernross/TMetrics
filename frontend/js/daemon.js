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
$(function(){ // document.ready wrapper
	//Event Handler fuer Prioritaetsauswahl
	$('#contentContainer').on('change', '.priorityInput', function (e) {
		var searchIdString = $(this).parents('.searchTermRow').attr('id'); //Beispiel: searchTerm17
	    var searchId = searchIdString.substring(10, searchIdString.length); //Beispiel: 17
	    postNewPriority(searchId, this.value);
	});
	
	//Event Handler fuer (De-)Aktivierung von Suchbegriffen
	$('#contentContainer').on('change', '.activeCheckbox', function (e) {
		var searchIdString = $(this).attr('id'); //Beispiel: checkbox17
	    var searchId = searchIdString.substring(8, searchIdString.length); //Beispiel: 17
	    //console.log("id="+searchId+", checked="+$(this).is(":checked"));
	    postNewActiveFlag(searchId, $(this).is(":checked"));
	});
	
	//Aktuelle Daten laden
	function loadDaemonStatus() {
		var query = "/rest/queries/status/";
		$.get(query)
		.done(function(response) {
			//Metadaten hinzufuegen
			$("#totalCount").text(response.data.total_count);
			$("#activeCount").text(response.data.active_count);
			//Dynamisch Tabellenzeilen erstellen
			for (var i = 0; i < response.data.search_terms.length; i++) {
				addItemToTable(response.data.search_terms[i].id, response.data.search_terms[i].name,
						response.data.search_terms[i].priority, response.data.search_terms[i].active,
						response.data.search_terms[i].created_at, response.data.search_terms[i].time_last_fetched,
						response.data.search_terms[i].interval_length, response.data.search_terms[i].in_iteration);
			}
		})
		.fail(function(event) {
			//Falls die Anfrage abgebrochen wird, soll keine Fehlermeldung erscheinen
			if(event.statusText !== "abort") {
				showServerError(100, $("#alertContainer"));			
			}
    	});
	}
	
	function addItemToTable(searchId, searchTerm, priority, active, createdAt, timeLastFetched, intervalLength, inIteration) {
		//String fuer das Select-Feld erzeugen, wobei die gegebene Prioritaet vorselektiert ist
		var priorityString = "<select class='form-control priorityInput'>";
		for (var i = -2; i <= 2; i++) {
			priorityString += (i === priority) ? "<option selected='selected'>" + i +"</option>" : "<option>" + i +"</option>"; 
		}
		priorityString += "</select>";
		
		var child = "<tr id='searchTerm" + searchId + "' class='searchTermRow'>" +
					"<td>" + searchTerm + "</td>"+
	    			"<td>" + createdAt + "</td>"+
	    			"<td>" + timeLastFetched + "</td>"+
	    			"<td>" + intervalLength + "</td>"+
	    			"<td><span class='" + ((inIteration) ? "checkedCell" : "uncheckedCell") + "'></span></td>"+
	    			"<td>"+ priorityString + "</td>"+
	    		//	"<td><span class='" + ((active) ? "checkedCell" : "uncheckedCell") + "'></span></td>"+
					"<td>" +
	    				"<input type='checkbox' id='checkbox" + searchId + "' class='activeCheckbox'" + ((active) ? " checked='checked'" : "") + " />" +
	    				"<label for='checkbox" + searchId + "'></label>" +
    				"</td>"+
				"</tr>";
		$('#statusData').append(child);			
	}
	
	//Prioritaet eines SUchbegriffs aendern
	function postNewPriority(searchId, newPriority) {
		var query = "/rest/queries/postPriority/?id=" + searchId + "&p=" + newPriority;
		$.post(query)
		.done(function(response) {
			//Datenbank wurde angepasst. Es ist nichts sonst zu tun.
		})
		.fail(function(event) {
			//Falls die Anfrage abgebrochen wird, soll keine Fehlermeldung erscheinen
			if(event.statusText !== "abort") {
				showServerError(101, $("#alertContainer"));			
			}
    	});
	}
	
	//Eine Suchbegriff fuer den Daemon (de-)aktivieren
	function postNewActiveFlag(searchId, newActiveFlag) {
		var query = "/rest/queries/postActiveFlag/?id=" + searchId + "&active=" + newActiveFlag;
		$.post(query)
		.done(function(response) {
			//Datenbank wurde angepasst. Es ist nichts sonst zu tun.
		})
		.fail(function(event) {
			//Falls die Anfrage abgebrochen wird, soll keine Fehlermeldung erscheinen
			if(event.statusText !== "abort") {
				showServerError(102, $("#alertContainer"));			
			}
    	});
	}
	
	//Direkt beim Seitenstart laden
	loadDaemonStatus();
});