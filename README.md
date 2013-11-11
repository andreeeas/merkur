# Merkur

Merkur ist ein Message-orientiertes Framework zur Systemanalyse. Als Datenbasis werden Log-Nachrichten genutzt. Die zentralen Aufgaben in Bezug auf diese sind:

- Aggregierung
- Visualisierung
- Filterung

## Betrieb

### Voraussetzungen

- JDK >= 1.6

### Installation

#### Unix und OSX

1. Herunterladen des Frameworks bzw. der Datei **dist/merkur.zip**
2. Entpacken der Datei **dist/merkur.zip** am gewünschten Ort. Das zip-File enthält die notwendigen Komponenten Merkur-Logback, Merkur-Server und Merkur-Client. Für die Installation bitte in die korrespondierende README.md des jeweiligen Moduls schauen.
3. Installation des Message-Brokers(RabbitMQ) nach der Anleitung unter http://www.rabbitmq.com/download.html. Wer unter OSX [Homebrew](http://http://brew.sh) installiert hat, kann den Message-Broker auch über folgenden Befehl installieren:

   ```bash
   brew install rabbitmq
   ```

**Hinweis**: Die Installation unter Windows ist nicht getestet. Für den Message-Broker wird entweder Unix oder OSX empfohlen.

## Entwicklung

Die Installation für Entwickler wird im jeweiligen Modul genauer beschreiben.

## Module

Das Framework ist eingeteilt in die folgenden Module:

### Merkur-Generator

Der Generator ist eine Applikation, die für die Erzeugung von Log-Nachrichten zuständig ist. Über das Logging-Backend(**Log4J** oder **Logback**) wird ein Appender konfiguriert, der die Log-Nachrichten an den Message-Broker weiterleitet.

**Aufgabe:** Erzeugung von Log-Nachrichten

### Merkur-Logback

Das Logback-Modul stellt einen Logback-Appender bereit, der es ermöglicht, Log-Nachrichten als Nachrichten an den Message-Broker zur versenden.

**Aufgabe:** Anbindung des Logging-Backends Logback an den Message-Broker 

### Merkur-Server

Der Server fungiert als Bindeglied zwischen dem Message-Broker und dem Client. Er stellt einen Websocket-Endpunkt bereit, der den Push-Mechanismus der Log-Nachrichten in Richtung Client ermöglicht.

**Aufgabe:** Verbindung des Client mit dem Message-Broker

### Merkur-Client

Der Client stellt eine grafische Oberfläche zur Verfügung. Diese ermöglicht es dem Nutzer, die vom Generator erzeugten Log-Nachrichten zu abonnieren. Einmal abonnierte Log-Nachrichten können gefiltert und auch wieder abbestellt werden.

**Aufgabe:** Aggregierung, Visualisierung und Filterung der über den Server empfangenen Log-Nachrichten

### Merkur-Infrastucture

Das Infrastruktur-Modul stellt Gradle-Tasks zur Installation der vom Framework benötigten Infrastruktur-Komponenten bereit. Aktuell ist dies die Installation des Message-Brokers(RabbitMQ).

**Aufgabe:** Bereitstellung von Gradle-Tasks für die Installation der Infrastruktur

## Infrastruktur-Komponenten

- [RabbitMQ](http://www.rabbitmq.com/)