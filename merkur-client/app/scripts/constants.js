'use strict';

/**
* merkurClientApp.constants Modul
*
* Stellt Konstanten bereit.
*/
var merkurClientAppConstants = angular.module('merkurClientApp.constants', []);

// Endpunkt-Adressen für die Kommunikation mit dem Websocket-Server
merkurClientAppConstants.constant('defaultWebsocketEndpoints', [
  {name:'Showcase-Server',url:'http://localhost:8080/merkur-server-0.1/socket'}
]);

// Anzahl maximal angezeigter Nachrichten
merkurClientAppConstants.constant('defaultMaxEntriesShown', 200);

// Anzahl maximal angezeigter Benachrichtigungen
merkurClientAppConstants.constant('defaultMaxNotificationsShown', 3);