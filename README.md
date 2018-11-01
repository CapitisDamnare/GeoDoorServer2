## GeoDoorServer2

**Dieses Java Programm sollte in Kombination mit der GeoDoor Android App benutzt werden.**

**Link: https://github.com/JustForFunDeveloper/GeoDoor_V2_0**

## Vision
Ein Serverprogramm läuft auf einem Raspberry PI welcher mit dem Internent verbunden ist.
Dieser Server steuert das elektrische Eingangstor. Der Server erhält Kommandos von der GeoDoor Android App.
Mit Hilfe von GPS und des SmartPhones ist es möglich dem "SmartHome" zu sagen, wann das Tor zu öffnen ist.

## Ziel dieses Projektes
Das Ziel dieses Projektes ist es dieses Program mit einem elektrischen Tor zu verbinden.
Das kann entweder durch eine I/O Schnittstelle (z.B: Raspberry PI), EIB Verbindung via LAN Schnittstelle oder mittels einer Verbindung zu einem bestehenden OpenHAB System realisiert werden.
Die verbundenen User sollen einfach per Knopfdruck angezeigt und bei Bedarf deren Berechtigung frei geschalten werden können.
Die User Daten werden in einer SQL Lite Datenbank gespeichert

## Master Branch
Derzeit wurde eine eine Verbindung mit dem OpenHAB System hergestellt, welches sich im KNX EIB Netz befindet.
Das Programm besitzt im Moment eine rudimentäre Konsole welche es erlaubt Nutzer anzuzeigen sowie frei zu schalten.

## Status
In Kürze wird es hier eine kompilierte Beta Version des Programms geben, welche einfach auf einem Raspbery PI gestartet werden kann.

## JavaDoc
https://JustForFunDeveloper.github.io/GeoDoorServer2/

