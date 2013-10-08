# Merkur

Merkur ist ein Message-orientiertes Framework zur Systemanalyse. Als Datenbasis werden Log-Nachrichten von Applikationen genutzt. Die zentralen Aufgaben in Bezug auf diese sind:

- Aggregierung
- Visualisierung
- Filterung

## Voraussetzungen

- [Git](http://git-scm.com/)
- [NodeJS](http://nodejs.org/)
- [Yeoman](http://yeoman.io/)
- [Ruby/Rubygem](https://www.ruby-lang.org/de/)

## Installation

### Unix

1. Klonen des Frameworks
2. Installation des Message-Brokers(RabbitMQ)

   ```bash
   ./gradlew merkur-infrastructure:installRabbitmqUnix
   ```

3. Installation der Detanbank(MongoDB)

   ```bash
   ./gradlew merkur-infrastructure:installMongodbUnix
   ```

### OSX

1. Klonen des Frameworks

2. Installation des Message-Brokers(RabbitMQ)

   ```bash
   ./gradlew merkur-infrastructure:installRabbitmqOSX
   ```

3. Installation der Detanbank(MongoDB)

   ```bash
   ./gradlew merkur-infrastructure:installMongodbOSX
   ```

## Module

Das Framework ist eingeteilt in die folgenden Module:

### Merkur-Generator

Der Generator ist eine Applikation, die für die Erzeugung von Log-Nachrichten zuständig ist. Über das Logging-Backend(**Log4J** oder **Logback**) wird ein Appender konfiguriert, der die Log-Nachrichten an den Message-Broker weiterleitet.

**Aufgabe:** Erzeugung von Log-Nachrichten

### Merkur-Logback

Das Logback-Modul stellt einen Logback-Appender bereit, der es ermöglicht, Log-Nachrichten als Nachrichten an den Message-Broker zur versenden.

**Aufgabe:** Anbindung des Logging-Backends Logback an den Message-Broker 

### Merkur-Server

Der Server fungiert als Bindeglied zwischen dem Message-Broker und Client. Er stellt einen Websocket-Endpunkt bereit, der den Push-Mechanismus der Log-Nachrichten in Richtung Client ermöglicht.

**Aufgabe:** Verbindung des Client mit dem Message-Broker

### Merkur-Client

Der Client stellt eine grafische Oberfläche zur Verfügung. Diese ermöglicht es dem Nutzer, die vom Generator erzeugten Log-Nachrichten zu abonnieren. Einmal abonnierte Log-Nachrichten können gefiltert und auch wieder abbestellt werden.

**Aufgabe:** Aggregierung, Visualisierung und Filterung der über den Server empfangenen Log-Nachrichten

### Merkur-Infrastucture

Das Infrastruktur-Modul stellt Gradle-Tasks zur Installation der vom Framework benötigten Infrastruktur-Komponenten bereit. Darunter fallen u.a. die Installation des Message-Brokers(RabbitMQ) und der Datenbank(MongoDB).

**Aufgabe:** Bereitstellung von Gradle-Tasks für die Installation der Infrastruktur

## Infrastruktur-Komponenten

- [RabbitMQ](http://www.rabbitmq.com/)
- [MongoDB](http://www.mongodb.org/)

## Bibliotheken

TODO: Ergänzen