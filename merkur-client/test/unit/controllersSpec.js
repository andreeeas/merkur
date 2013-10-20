'use strict';

describe('Controllers', function () {

  // Controller-Modul laden
  beforeEach(module('merkurClientApp.controllers'));

  describe('MainCtrl', function () {

    var scope,
        messagesMaxEntriesShown,
        messagesServerWebsocketEndpoint,
        ctrl;

    // Controller initialisieren
    beforeEach(inject(
                ['$rootScope','$controller',
        function ($rootScope, $controller) {
      scope = $rootScope.$new();
      messagesMaxEntriesShown = 10000;
      messagesServerWebsocketEndpoint = 'dummyEndpoint';
      ctrl = $controller('MainCtrl', {
        $scope: scope,
        'messages.serverWebsocketEndpoint': messagesServerWebsocketEndpoint,
        'messages.maxEntriesShown': messagesMaxEntriesShown,
        '_': window._
      });
    }]));

    it('should create an empty source of log messages', function () {
      expect(scope.messagesSource).toEqual('');
    });

    it('should create an empty filter for log messages', function () {
      expect(scope.messagesFilter).toEqual('');
    });

    it('should create an empty list of log messages', function () {
      expect(scope.logMessages.length).toBe(0);
    });

    it('should create an empty list of log subscriptions', function () {
      expect(scope.logSubscriptions.length).toBe(0);
    });

    it('should create a max number for log messages initialised with constant value', function () {
      expect(scope.messagesMaxEntriesShown).toBe(messagesMaxEntriesShown);
    });

  });
});
