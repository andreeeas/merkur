'use strict';

describe('Constants', function () {

  // Constants-Modul laden
  beforeEach(module('merkurClientApp.constants'));

  describe('messages.serverWebsocketEndpoint', function () {

    it('should return the serverWebsocketEndpoint', inject(['messages.serverWebsocketEndpoint',function (serverWebsocketEndpoint) {
      expect(serverWebsocketEndpoint).toEqual('http://localhost:9090/merkur-server/socket');
    }]));

  });

  describe('messages.maxEntriesShown', function () {

    it('should return the maximum number of shown messages', inject(['messages.maxEntriesShown',function (maxEntriesShown) {
      expect(maxEntriesShown).toBe(10000);
    }]));

  });

});