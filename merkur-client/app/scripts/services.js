'use strict';

/**
* merkurClientApp.services Module
*
* Provides services and constants for Merkur-Client
*/
var merkurClientAppServices = angular.module('merkurClientApp.services', []);

// Stomp service
merkurClientAppServices.service('stompService', ['messages.serverWebsocketEndpoint', function (messagesServerWebsocketEndpoint) {
  var socket = new SockJS(messagesServerWebsocketEndpoint);
  this.client = Stomp.over(socket);
}]);

// endpoint address for websocket server
merkurClientAppServices.value('messages.serverWebsocketEndpoint', 'http://localhost:8080/merkur-server/socket');

// number of maximum entries shown
merkurClientAppServices.value('messages.maxEntriesShown', 10);

// topic of messages fetched from stomp client
// TODO : Über GUI konfigurierbar machen
merkurClientAppServices. value('messages.topic', '/topic/price.stock.*');
// merkurClientAppServices. value('messages.topic', '/topic/counter');
// merkurClientAppServices. value('messages.topic', '/topic/uuid');