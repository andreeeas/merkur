'use strict';

/**
* merkurClientApp.services Module
*
* Stellt Servcices und Konstanten bereit.
*/
var merkurClientAppServices = angular.module('merkurClientApp.services', []);

// Defaults für Konfiguration

// Endpunkt-Adresse für die Kommunikation mit dem Websocket-Server
merkurClientAppServices.value('messages.serverWebsocketEndpoint', 'http://localhost:9090/merkur-server/socket');

// Anzahl maximal angezeigter Nachrichten
merkurClientAppServices.value('messages.maxEntriesShown', 10);

// der Routing-Key der Nachrichten, die über den Websocket-Server beim Message-Broker abonniert werden
merkurClientAppServices.value('messages.source', '/topic/Generator.de.#');