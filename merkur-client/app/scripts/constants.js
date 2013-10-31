'use strict';

/**
* merkurClientApp.constants Modul
*
* Stellt Konstanten bereit.
*/
var merkurClientAppConstants = angular.module('merkurClientApp.constants', []);

// Endpunkt-Adressen für die Kommunikation mit dem Websocket-Server
merkurClientAppConstants.constant('defaultWebsocketEndpoints', [
  {name:'Tomcat 7',url:'http://localhost:9090/merkur-server/socket'},
  {name:'Tomcat 8',url:'http://localhost:8080/merkur-server-0.1/socket'}
]);

// Anzahl maximal angezeigter Nachrichten
merkurClientAppConstants.constant('defaultMaxEntriesShown', 200);