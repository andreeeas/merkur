# Merkur-Server

Dieses Dokument beschreibt die Installation bzw. Entwicklung mit dem Modul Merkur-Server.

## Voraussetzungen

Zusätzlich zu den Voraussetzungen unter Betrieb im Hauptartikel gelten folgende:

- [Git](http://git-scm.com/)
- [Gradle](http://gradle.org/)

## Installation(Betrieb)

Nach dem Entpacken des zip-Files **dist/merkur.zip** kann die enthaltene Datei merkur-server-${VERSION}.war in einem beliebigen Servlet-Container wie z.B. Apache Tomcat deployt werden.

## Installation(Entwicklung)

```bash
git clone https://github.com/andreeeas/merkur.git
cd merkur/merkur-server
../gradlew build // baut die war-Datei(Ausgabe im Ordner build/libs/)
../gradlew cargoRunLocal -i // deployt die war-Datei in einem Apache Tomcat und startet diesen inkl. Logausgabe
../gradlew tasks // listet weitere mögliche Tasks auf
```

## Entwicklung

Merkur-Server ist eine Applikation, die durch die folgenden Links inspiriert wurde:

- [Spring-Websocket-Portfolio](https://github.com/rstoyanchev/spring-websocket-portfolio)
- [Blog-Artikel über Websocket-Support in Spring 4(1)](http://assets.spring.io/wp/WebSocketBlogPost.html)
- [Blog-Artikel über Websocket-Support in Spring 4(2)](http://blog.gopivotal.com/products/websocket-architecture-in-spring-4-0)