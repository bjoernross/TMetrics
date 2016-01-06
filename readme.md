Tmetrics besteht aus folgenden Teilen:
* einer MySQL-Datenbank für die Speicherung von Daten der Twitter-REST-API, darunter Tweets und Nutzer
* dem Daemon, einer Java-Anwendung, um zu benutzerdefinierten Suchbegriffen Daten von dieser API abzufragen
* dem REST Service, einem in Java mittels Jersey implementierten Web-Service, der die Daten aus der Datenbank auf Anfrage aggregiert und Analysen durchführt
* dem Web-Frontend, das in JavaScript implementiert wurde und Nutzern vielfältige Möglichkeiten zur explorativen Analyse der Daten zur Verfügung stellt

Zu den bereitgestellten Analysen gehören Sentimentanalyse, Clustering und die Identifizierung von Peaks und dazugehörigen Nachrichten. Die vollständige Dokumentation befindet sich in der Datei `Abschlussbericht/Tmetrics.pdf`.

Tmetrics wurde im Wintersemester 2013/14 am Institut für Informatik der Westfälischen Wilhelms-Universität Münster von zehn Studenten des im Rahmen des Projektseminars *Data Mining* entwickelt und unter den Bedingungen der GNU General Public License Version 3 veröffentlicht. Der volle Lizenztext findet sich in der Datei `license/gpl.txt`.

```
Dieses Programm ist Freie Software: Sie können es unter den Bedingungen
der GNU General Public License, wie von der Free Software Foundation,
Version 3 der Lizenz oder (nach Ihrer Wahl) jeder neueren
veröffentlichten Version, weiterverbreiten und/oder modifizieren.

Dieses Programm wird in der Hoffnung, dass es nützlich sein wird, aber
OHNE JEDE GEWÄHRLEISTUNG, bereitgestellt; sogar ohne die implizite
Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK.
Siehe die GNU General Public License für weitere Details.

Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.
```
