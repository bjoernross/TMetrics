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
var XHR_DEBUG = false;
//XHttpRequest Priority Pool
var XHR_NUM_PRIORITIES = 3;
XHR_PRIORITY_PASSIVE = 0;
XHR_PRIORITY_ACTIVE = 1;
XHR_PRIORITY_SPECIAL = 2;
XHR_SUBPRIORITY_NORMAL = 0;
XHR_SUBPRIORITY_HIGH = 1;
XHR_SUBPRIORITY_HIGHEST = 2;
XHR_SLOTS = new Array();
XHR_SLOTS[XHR_PRIORITY_PASSIVE] = 2;
XHR_SLOTS[XHR_PRIORITY_ACTIVE] = 3;
XHR_SLOTS[XHR_PRIORITY_SPECIAL] = 1;
XHR_NONE = -1;
XHR_POOL = 0; //ID fuer Pool
XHR_QUEUE = 1; //ID fuer Queue

XHRObject = (function() {
    // Konstruktor
    function XHRObject(query, type, priority, doneFunc, failFunc) {
    	this.request = new XMLHttpRequest();
        this.query = query; //REST URL
        this.type = type; //GET oder POST
        this.priority = priority; //poolId/queueId
        this.doneFunction = doneFunc;
        this.failFunction = failFunc;
        //if(XHR_DEBUG) console.log("new xhro created:"+query);
    };

    return XHRObject;
})();

xhrPool = new Array();
xhrQueue = new Array();
for (var i = 0; i < XHR_NUM_PRIORITIES; i++) {
	xhrPool.push(new Array());
	xhrQueue.push(new Array());
}

function addXHRObject(xhro) {
	//Im Pool mit passender Prioritaet nachschauen, ob noch Platz ist
	if (hasFreeSlot(xhro.priority)) {
		//Dann einfach einfuegen
		addToPool(xhro, xhro.priority);
	} else { 
		//Kein Platz, also in passende Warteschlange einfuegen
		xhrQueue[xhro.priority].push(xhro);
		if(XHR_DEBUG) console.log("QUEUE["+xhro.priority+"]. ADDED "+xhro.query+". length:"+ xhrQueue[xhro.priority].length);
	}
}

function addToPool(xhro, poolId) {
	xhrPool[poolId].push(xhro);
	//Anfrage starten
	xhro.request = $.ajax({
		type: xhro.type,
		url: xhro.query
	})
	.done(function(response) { xhro.doneFunction(response);})
	.fail(function(response) { xhro.failFunction(response);})
	.always(function(response) {
		if(response.statusText !== "abort") {
			if(XHR_DEBUG) console.log("internal finisher.")
			finishXHRObject(xhro);
		}
	});
	if(XHR_DEBUG) console.log("POOL ["+poolId+"]. ADDED "+xhro.query+". length:"+ xhrPool[poolId].length);
}

//Entfernt ein XHRObjekt aus dem entrpechenden Pool/Queue. Gibt true, wenn es entfernt wurde, false wenn kein Objekt xhro gefunden wurde
function removeXHRObject(xhro) {
	//Anfrage abbrechen
	xhro.request.abort();
	if (xhro.priority == undefined) {
		return false;
	}
	//Schauen, ob das XHRObjekt im Pool oder in der Queue ist
	var index = xhrPool[xhro.priority].indexOf(xhro);
	//XHRObjekt aus Pool/Queue entfernen
	if (index != -1) {
		//XHRObjekt aus Pool entfernen
		xhrPool[xhro.priority].splice(index, 1);
		if(XHR_DEBUG) console.log("POOL ["+xhro.priority+"]. REMOVED "+xhro.query+". length:"+ xhrPool[xhro.priority].length);
		return true;
	} else {
		index = xhrQueue[xhro.priority].indexOf(xhro);
		if (index != -1) {
			//XHRObjekt aus Queue entfernen
			xhrQueue[xhro.priority].splice(index, 1);
			if(XHR_DEBUG) console.log("QUEUE["+xhro.priority+"]. REMOVED "+xhro.query+". length:"+ xhrQueue[xhro.priority].length);
			return true;
		}
	}
	
	if(XHR_DEBUG) console.log("Tried to remove xhro that could not be found!");
	return false;
}

//Entfernt ein Objekt aus dem Pool/der Queue und fuegt ggf. einen Wartekandidat in den Pool ein
function finishXHRObject(xhro) {
	//Objekt entfernen
	var removeSuccesful = removeXHRObject(xhro); //War Loeschung erfolgreich
	if (!removeSuccesful) 
		return false;
	
	//Falls im Pool nun etwas frei ist, sollte dieser Platz aufgefuellt werden
	if (hasFreeSlot(xhro.priority)) {
		//Naechstes Objekt aus der Queue holen
		var nextXHRObject = getNextXHRObject(xhro.priority);
		if (nextXHRObject === undefined) {
			//if(XHR_DEBUG) console.log("REMOVED nothing. queue[" + xhro.priority + "] empty.");
			//Es ist kein Objekt mehr in der Warteschlange
			//TODO: Evtl. aus anderen Schlangen etwas holen?!
		} else {
			if(XHR_DEBUG) console.log("POOL ["+xhro.priority+"]. can move in:"+xhrPool[xhro.priority].length+" < "+XHR_SLOTS[xhro.priority]);
			//Den leeren Platz aus der Warteschlange auffuellen
			addToPool(nextXHRObject, nextXHRObject.priority);
		}
	}
	return true;
}

//Gibt das naechste XHRObject der angegebenen Warteschlange zurueck. undefined falls keine ELement vorhanden ist
function getNextXHRObject(queueId) {
	if (queueId == XHR_PRIORITY_SPECIAL) {
		//FIFO
		var fifo = xhrQueue[queueId].shift(); 
		if (fifo != undefined) {			
			if(XHR_DEBUG) console.log("QUEUE["+fifo.priority+"]. REMOVED "+fifo.query+". length:"+ xhrQueue[fifo.priority].length);
		}
		return fifo;
	} else {
		//LIFO
		var lifo = xhrQueue[queueId].pop();
		if (lifo != undefined) {			
			if(XHR_DEBUG) console.log("QUEUE["+lifo.priority+"]. REMOVED "+lifo.query+". length:"+ xhrQueue[lifo.priority].length);
		}
		return lifo;
	}
}

function moveXHRObject(xhro, newPriority) {
	//Ueberpruefen, ob das Objekt nicht schon im Pool/in der Queue mit newPriority ist
	if (xhrPool[newPriority].indexOf(xhro) != -1 || xhrQueue[newPriority].indexOf(xhro) != -1)
		return;
	
	if(XHR_DEBUG) console.log("moving from "+xhro.priority+" to "+newPriority+": "+xhro.query);
	//Objekt loeschen und ersetzen
	var finishSuccesful = finishXHRObject(xhro); //War Loeschung und evtl Einfuegung erfolgreich
	if (!finishSuccesful) 
		return;

	//Prioritaet des Objekts anpassen
	xhro.priority = newPriority;
	//Nun das XHRObjekt moeglichst in den Pool mit newPriority einfuegen, falls nicht moeglich in die entsprechende Queue
	if (hasFreeSlot(newPriority)) {
		addToPool(xhro, newPriority);
	} else {
		//In die Queue einfuegen
		xhrQueue[newPriority].push(xhro);
		if(XHR_DEBUG) console.log("QUEUE["+xhro.priority+"]. ADDED "+xhro.query+". length:"+ xhrQueue[xhro.priority].length);
	}
}

function hasFreeSlot(poolId) {
	//if(XHR_DEBUG) console.log("check free: "+xhrPool[poolId].length+" < "+XHR_SLOTS[poolId]);
	return xhrPool[poolId].length < XHR_SLOTS[poolId];
}

function incrementPriority(priority) {
	if (priority == XHR_PRIORITY_SPECIAL) {
		return XHR_PRIORITY_SPECIAL;
	} else {
		return priority + 1;		
	}
}

function decrementPriority(priority) {
	if (priority == XHR_PRIORITY_PASSIVE) {
		return XHR_PRIORITY_PASSIVE;
	} else {
		return priority - 1;		
	}
}