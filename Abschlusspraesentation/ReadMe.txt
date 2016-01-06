1. Um die Präsentation zu erzeugen, muss man die Main.tex datei im Ordner preambel kompilieren. Dort können natürlich auch weitere Packete hinzugefügt/entfernt werden.

2. Die einzelnen Kapitel sind im Ordner chapter angelegt. Wenn man den Dateinamen ändern oder einen neuen Kapitel hinzufuegen möchte, dann erstellt/renamed man eine entsprechende Datei im chapter-Ordner und modifiziert entsprechend die Maint.tex im Ordner preambel 
(ganz unten den entsprechenden /include{../chapter/newName.tex} ergänzen.)

3. Bilder sollten im Ordner img abgelegt werden.

4. Quellcode für Listings sollten im Ordner src abgelegt werden.

Anmerkung zu pgf:
Sollte jemand pgf-Grafiken erzeugen und auslagern wollen, würde ich sie als Bilder betrachten, den entsprechenden Quellcode im img-Ordner platzieren und dann includen. 

Desweiteren muss man die Anweisung \tikzexternalize in der preambel auskommentieren. Das bewirkt, dass alle pgf-Grafiken erstellt werden. Danach sollte man die Anweisung wieder einkommentieren. Das bewirkt, dass die Grafiken, die in den einzelnen pdf's erzeigt werden, nachher nicht nochmal kompiliert werden, sondern einfach nur eingebunden, was sehr viel schneller ist.
