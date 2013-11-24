'use strict';

describe('Constants', function () {

  // Constants-Modul laden
  beforeEach(module('merkurClientApp.constants'));

  describe('defaultWebsocketEndpoints', function () {

    it('should return the serverWebsocketEndpoint', inject(['defaultWebsocketEndpoints',function (defaultWebsocketEndpoints) {
      expect(defaultWebsocketEndpoints).toEqual([
        {name:'Showcase-Server',url:'http://localhost:8080/merkur-server-0.1/socket'}
      ]);
    }]));

  });

  describe('defaultMaxEntriesShown', function () {

    it('should return the maximum number of shown messages', inject(['defaultMaxEntriesShown',function (defaultMaxEntriesShown) {
      expect(defaultMaxEntriesShown).toBe(200);
    }]));

  });

  describe('defaultMaxNotificationsShown', function () {

    it('should return the maximum number of shown notifications', inject(['defaultMaxNotificationsShown',function (defaultMaxNotificationsShown) {
      expect(defaultMaxNotificationsShown).toBe(3);
    }]));

  });

});