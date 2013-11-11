# Merkur-Generator

Dieses Dokument beschreibt die Entwicklung mit dem Modul Merkur-Generator.

## Voraussetzungen

Zusätzlich zu den Voraussetzungen unter Betrieb im Hauptartikel gelten folgende:

- [Git](http://git-scm.com/)
- [Gradle](http://gradle.org/)

## Installation(Betrieb)

Das Modul Merkur-Generator stellt lediglich eine Umgebung bereit, die konfigurierbare Log-Ausgaben liefert. Daher ist es für den Betrieb in den meisten Fällen weniger sinnvoll.

## Installation(Entwicklung)

```bash
git clone https://github.com/andreeeas/merkur.git
cd merkur/merkur-generator
../gradlew installApp // baut die jar-Datei inkl. ausführbarer Skripte für Unix und Windows(Ausgabe im Ordner build/install/)
./build/install/merkur-generator/bin/merkur-generator // führt den Generator mit seinen Defaults aus
../gradlew tasks // listet weitere mögliche Tasks auf
```

## Hinweise

Folgende Konfigurationsparameter können an den Aufruf mitgegeben werden:

- id(die ID der jeweiligen Generator-Instanz(wichtig zur Unterscheidung), Default: LogGenerator)
- delay(die Verzögerung in ms, in der Log-Nachrichten ausgegeben werden, Default: 1000)
- count(die Anzahl der Generator-Instanzen, die in einem JVM-Prozess laufen, Default: 1)

Die Parameter werden jeweils wie folgt an den Aufruf angehängt:

```bash
./build/install/merkur-generator/bin/merkur-generator --id=A --delay=500 // (ID: A, delay: 500, eine Instanz)
./build/install/merkur-generator/bin/merkur-generator --id=B --count=3 // (ID: A, delay: 1000, drei Instanzen)
```