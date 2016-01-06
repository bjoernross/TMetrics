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
//Silly workaround fuer D3, da man kein Parameter an drawTagCloud uebergeben kann...
var tempTabId = -1;
var tempLanguage = "";

function saveSearchItemTagCloud(tabId, language) {
	var query = '/rest/results/tagcloud/?id='+searchItems[tabId].id +
		((language === "") ? "" : "&lang=" + language);

    var dataObject = getDataObject(tabId, language);
    var priority = (activeViews[VIEW_TAG_CLOUD]) ? XHR_PRIORITY_ACTIVE : XHR_PRIORITY_PASSIVE;
    
    var xhro = new XHRObject(query, "GET", priority, 
    	//doneFunction
		function(response) {
    		//Daten abspeichern
			dataObject.view[VIEW_TAG_CLOUD].data = response.data;
			dataObject.view[VIEW_TAG_CLOUD].isLoaded = true;
			dataObject.view[VIEW_TAG_CLOUD].isRendered = false;
	
			$(document).trigger("tm.view.dataLoaded", [tabId, language, VIEW_TAG_CLOUD]);
		},
    	//failFunction
    	function(event) {
			//Falls die Anfrage abgebrochen wird, soll keine Fehlermeldung erscheinen
			if(event.statusText !== "abort") {
				showServerError(10 + 10 * VIEW_TAG_CLOUD, getViewPanel(tabId, language, VIEW_TAG_CLOUD));			
			}
    	}
    );
    dataObject.view[VIEW_TAG_CLOUD].xhro = xhro;
    
    //In die XHRObkjekt-Verwaltung einfuegen
    addXHRObject(xhro);
}
var Start = new Date();

//Tag Cloud zu den Tweets fuer 'searchItem' anzeigen
function showSingleTagCloud(tabId, language) {
	//Silly workaround fuer D3, da man kein Parameter an drawTagCloud uebergeben kann...
	tempTabId = tabId;
	tempLanguage = language;
	
	var panel = getViewPanel(tabId, language, VIEW_TAG_CLOUD);
	
	var tagCloudText = getDataObject(tabId, language).view[VIEW_TAG_CLOUD].data.text;
    var tweetCount = getDataObject(tabId, language).view[VIEW_TAG_CLOUD].data.count;
    
    if (tweetCount == 0) {
    	getViewObject(tabId, language, VIEW_TAG_CLOUD).isRendered = true;
    	$("#tabBody" + searchItems[tabId].id + " #langWrap" + searchItems[tabId].id + "-" + language + " ." + viewClasses[VIEW_TAG_CLOUD] + " .viewLoading").fadeOut(FADEOUT_TIME, function() {
    		showNoDataWarning(panel);
    	});
    	return;
    }
    
    panel.show();
	
	var width = panel.find(".viewBody").width();

    panel.find(".panel-heading").text("Tag Cloud ("  +tweetCount+" tweets)");

	var words = parseText(tagCloudText, tabId);
	var fontSize = d3.scale["log"]().range([10, 100]);
	if (words.length) fontSize.domain([+words[words.length - 1].value || 1, +words[0].value]);
	
	var newWords = Array();
	newWords = words.slice(0,100);
	
	d3.layout.cloud()
		.size([width, 300])
		.words(newWords)
		.timeInterval(100)
		.text(function(d) { return d.key; })
		.padding(5)
		.rotate(0)
		.font("Impact")
		.fontSize(function(d) { return fontSize(+d.value); })
		.on("end", function(a,b){drawTagCloud(a,b);})
		.start();
}

function drawTagCloud(data, bounds) {
	var panelBody = getViewPanel(tempTabId, tempLanguage, VIEW_TAG_CLOUD).find(".viewBody");
	
	var fill = d3.scale.category20();
	var w = panelBody.width(), h = 300;

	panelBody.empty();

	var svg = d3.select(panelBody.selector).append("svg")
          		.attr("width", w)
          		.attr("height", h);
	var vis = svg.append("g")
           		.attr("transform", "translate(" + [w >> 1, h >> 1] + ")");
	var background = svg.append("g");
	scale = bounds ? Math.min(
		w / Math.abs(bounds[1].x - w / 2),
		w / Math.abs(bounds[0].x - w / 2),
		h / Math.abs(bounds[1].y - h / 2),
		h / Math.abs(bounds[0].y - h / 2)) / 2 : 1;
	var text = vis.selectAll("text")
		.data(data, function(d) { return d.text.toLowerCase(); });
	text.transition()
		.duration(1000)
		.attr("transform", function(d) { return "translate(" + [d.x, d.y] + ")rotate(" + d.rotate + ")"; })
		.style("font-size", function(d) { return d.size + "px"; });
	text.enter().append("text")
		.attr("text-anchor", "middle")
		.attr("transform", function(d) { return "translate(" + [d.x, d.y] + ")rotate(" + d.rotate + ")"; })
		.style("font-size", function(d) { return d.size + "px"; })
		.on("click", function(d) {
			submitSearchItemParam(d.key);
		})
		.transition()
		.duration(1000)
		.style("opacity", 1);
	text.style("font-family", function(d) { return d.font; })
		.style("fill", function(d) { return fill(d.text.toLowerCase()); })
		.text(function(d) { return d.text; });
	var exitGroup = background.append("g")
		.attr("transform", vis.attr("transform"));
	var exitGroupNode = exitGroup.node();
	text.exit().each(function() {
		exitGroupNode.appendChild(this);
	});
	exitGroup.transition()
		.duration(1000)
		.style("opacity", 1e-6)
		.remove();
	vis.transition()
		.delay(1000)
		.duration(750)
		.attr("transform", "translate(" + [w >> 1, h >> 1] + ")scale(" + scale + ")");
	getViewObject(tempTabId, tempLanguage, VIEW_TAG_CLOUD).isRendered = true;
	if (DEBUG) console.log("tagCloud Rendered!");
	$("#tabBody" + searchItems[tempTabId].id + " #langWrap" + searchItems[tempTabId].id + "-" + tempLanguage + " ." + viewClasses[VIEW_TAG_CLOUD] + " .viewLoading").fadeOut(FADEOUT_TIME);
}

function parseText(text, tabId) {
	// From Jonathan Feinberg's cue.language, see lib/cue.language/license.txt.
	var stopWordsEnglish = /^(retweet|rt|twitter|i|me|my|myself|we|us|our|ours|ourselves|you|your|yours|yourself|yourselves|he|him|his|himself|she|her|hers|herself|it|its|itself|they|them|their|theirs|themselves|what|which|who|whom|whose|this|that|these|those|am|is|are|was|were|be|been|being|have|has|had|having|do|does|did|doing|will|would|should|can|could|ought|i'm|you're|he's|she's|it's|we're|they're|i've|you've|we've|they've|i'd|you'd|he'd|she'd|we'd|they'd|i'll|you'll|he'll|she'll|we'll|they'll|isn't|aren't|wasn't|weren't|hasn't|haven't|hadn't|doesn't|don't|didn't|won't|wouldn't|shan't|shouldn't|can't|cannot|couldn't|mustn't|let's|that's|who's|what's|here's|there's|when's|where's|why's|how's|a|an|the|and|but|if|or|because|as|until|while|of|at|by|for|with|about|against|between|into|through|during|before|after|above|below|to|from|up|upon|down|in|out|on|off|over|under|again|further|then|once|here|there|when|where|why|how|all|any|both|each|few|more|most|other|some|such|no|nor|not|only|own|same|so|than|too|very|say|says|said|shall)$/,
	    stopWordsGerman = /^(retweet|rt|twitter|ich|mir|mich|wir|uns|du|dir|dich|ihr|euch|er|ihm|ihn|sie|ihr|es|ihnen|sich|mein|meine|meiner|meines|meinen|meinem|dein|deine|deiner|deines|deinen|deinem|sein|seine|seiner|seines|seinen|seinem|ihre|ihrer|ihres|ihren|ihrem|unser|unsere|unserer|unseres|unseren|unserem|euer|eure|eures|eurer|euren|eurem|wer|wessen|wem|wen|was|wie|warum|wieso|weshalb|welcher|welche|welches|welchen|welchem|welcher|dieser|diese|dieses|diesem|diesen|jener|jene|jenes|jenen|jenem|sein|bin|bist|ist|sind|seid|war|warst|waren|wart|wär|wäre|wärst|wären|wärt|haben|habe|hab|hast|hat|habt|hatte|hattest|hatten|hattet|hätte|hättest|hätten|hättet|werden|werde|wirst|wird|werdet|würde|würdest|würdet|können|kann|kannst|könnt|konnte|konntest|konnten|konntet|sollen|soll|sollst|sollt|sollte|solltest|sollten|solltet|ein|einer|eine|eines|einer|einem|einen|der|die|das|des|dessen|deren|dem|der|den|und|oder|aber|doch|dass|sondern|wenn|falls|wie|als|während|von|auf|bei|durch|mit|für|fürs|über|zwischen|in|im|vor|nach|unter|unten|aus|an|innen|außen|wieder|weiter|dann|einmal|hier|da|dort|wann|wo|wie|alle|alles|jeder|jede|jedes|jedem|jeden|wenig|wenige|mehr|meiste|meisten|anderer|andere|anderes|anderem|anderen|einige|einiges|nein|weder|noch|nicht|nur|gleich|gleiche|gleicher|gleiches|gleichem|gleichen|so|sehr|sagen|sage|sagst|sagt|sagt|ja|man|müssen|muss|musst|müsst|weil|wegen|denn|seit|ohne|auch|damit|kein|also|zu|zur|zum|mal|vom|vor|von|würd|ins)$/,
	    punctuation = /[– — !¡"&()*+,-\.\/:;<=>?¿\[\\\]^`\{|\}~#„“«»]+/g,
	    wordSeparators = /[\s\u3031-\u3035\u309b\u309c\u30a0\u30fc\uff70]+/g,
	    discard = /^(@|https?:)/;
	var tags = {};
	var cases = {};
	var stopWords = stopWordsEnglish;
	var lLanguageCounts = searchItems[tabId].languageDistribution;

	//Falls Deutsch als Sprache ausgewaehlt wurde oder falls deutsch die haeufigste Sprache ist, soll die deutsche Stopwortliste verwendet werden
	if (activeLanguage == "de" || (typeof lLanguageCounts !== "undefined" && lLanguageCounts.length > 0 && lLanguageCounts[0].iso_code == "de")) {
		stopWords = stopWordsGerman;
	}

	text.split(wordSeparators).forEach(function(word) {
		if (discard.test(word)) return;
		word = word.replace(punctuation, "");
		if (word.length < 3) return;
		if (stopWords.test(word.toLowerCase())) return;
		word = word.substr(0, 30);
		word = word.toLowerCase();
		cases[word] = word;
		tags[word] = (tags[word] || 0) + 1;
	});
	
	tags = d3.entries(tags).sort(function(a, b) { return b.value - a.value; });
	tags.forEach(function(d) { d.key = cases[d.key]; });
	return tags;
}
