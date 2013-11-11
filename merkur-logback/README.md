# Merkur-Logback

Dieses Dokument beschreibt die Installation bzw. Entwicklung mit dem Modul Merkur-Logback.

## Voraussetzungen

Zusätzlich zu den Voraussetzungen unter Betrieb im Hauptartikel gelten folgende:

- [Git](http://git-scm.com/)
- [Gradle](http://gradle.org/)

## Installation(Betrieb)

Nach dem Entpacken des zip-Files **dist/merkur.zip** kann die enthaltene Datei merkur-logback-${VERSION}.jar im Classpath einer beliebigen Java-Anwendung abgelegt werden. Dadurch ist es möglich, mit der jeweiligen Anwendung Log-Nachrichten an den Message-Broker zu schicken.

Ein Beispiel für eine logback.xml kann im Modul Merkur-Generator in der Datei **src/main/resources/logback.xml** eingesehen werden. Die möglichen Parameter sind identisch mit denen des [AmqpAppender](http://docs.spring.io/spring-amqp/api/org/springframework/amqp/rabbit/log4j/AmqpAppender.html).

## Installation(Entwicklung)

```bash
git clone https://github.com/andreeeas/merkur.git
cd merkur/merkur-logback
../gradlew build // baut die jar-Datei(Ausgabe im Ordner build/libs/)
../gradlew tasks // listet weitere mögliche Tasks auf
```

## Entwicklung

Merkur-Logback ist ein Logback-Appender, der auf Basis des [AmqpAppender](http://docs.spring.io/spring-amqp/api/org/springframework/amqp/rabbit/log4j/AmqpAppender.html) aus dem Spring-Rabbit Projekt entstanden ist. Grundlage für die Migration von Log4J nach Logback war diese [Anleitung](http://logback.qos.ch/manual/migrationFromLog4j.html) genutzt.