# Merkur-Client

Dieses Dokument beschreibt die Installation bzw. Entwicklung mit dem Merkur-Client.

## Voraussetzungen

Zusätzlich zu den Voraussetzungen unter Betrieb im Hauptartikel gelten folgende:

- [Git](http://git-scm.com/)
- [NodeJS](http://nodejs.org/)
- [Yeoman](http://yeoman.io/)(Bower, Yo und Grunt)
- [Ruby/Rubygem](https://www.ruby-lang.org/de/)

## Installation(Betrieb)

Nach dem Entpacken des zip-Files **dist/merkur.zip** kann die enthaltene Datei merkur-client.zip in einem beliebigen HTTP-Server wie z.B. Apache HTTPD deployt werden. Sie enthält einen Ordner dist, der wiederum die Inhalte der Webapplikation beinhaltet.

## Installation(Entwicklung)

```bash
git clone https://github.com/andreeeas/merkur.git
cd merkur/merkur-client
npm install
gem install compass(root)
bower install
```

## Entwicklung

Merkur-Client ist eine Webapplikation, die über den Yeoman-Generator [angular](https://github.com/yeoman/generator-angular) entstanden ist. Für weitere Hinweise zur Entwicklung bitte die Informationen auf der verlinkten Seite hinzuziehen.