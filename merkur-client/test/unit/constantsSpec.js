'use strict';

describe('Constants', function () {

  // Constants-Modul laden
  beforeEach(module('merkurClientApp.constants'));

  describe('defaultWebsocketEndpoints', function () {

    it('should return the serverWebsocketEndpoint', inject(['defaultWebsocketEndpoints',function (defaultWebsocketEndpoints) {
      expect(defaultWebsocketEndpoints).toEqual([
        {name:'Tomcat 7',url:'http://localhost:9090/merkur-server/socket'},
        {name:'Tomcat 8',url:'http://localhost:8080/merkur-server-0.1/socket'}
      ]);
    }]));

  });

  describe('defaultMaxEntriesShown', function () {

    it('should return the maximum number of shown messages', inject(['defaultMaxEntriesShown',function (defaultMaxEntriesShown) {
      expect(defaultMaxEntriesShown).toBe(200);
    }]));

  });

});