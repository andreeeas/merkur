'use strict';

angular.module('merkurClientApp.controllers', [])
  .controller('MainCtrl', ['$scope','$window','defaultWebsocketEndpoints','defaultMaxEntriesShown','$filter','$timeout','$log',
    function ($scope,$window,defaultWebsocketEndpoints,defaultMaxEntriesShown,$filter,$timeout,$log) {

    var socket, client, _ = $window._;

    // scope Variablen
    $scope.websocketEndpoint = '';
    $scope.websocketEndpoints = defaultWebsocketEndpoints;
    $scope.connected = false;
    $scope.logMessagesSource = '';
    $scope.selectedLogSubscription = '';
    $scope.filter = '';

    $scope.logMessages = [];
    $scope.logSubscriptions = [];
    
    $scope.maxEntriesShown = defaultMaxEntriesShown;

    var digest = _.throttle(function() {
      $log.info('Digesting ...');
      $scope.$digest();
    }, 1000);

    // Funktion zur Verbindung zum Websocket-Server
    $scope.connect = function() {
      socket = new SockJS($scope.websocketEndpoint);
      client = Stomp.over(socket);
      client.connect('', '', function(frame) {
        $log.info('Verbunden, Frame:\n' + frame);
        $scope.$apply(function() {
          $scope.connected = true;
        });
      }, function(error) {
        $log.error('STOMP Fehler: ' + error);
        $scope.$apply(function() {
          $scope.connected = false;
        });
      });
    };

    // Funktion zur Trennung der Verbindung mit dem Websocket-Server
    $scope.disconnect = function() {
      client.disconnect(function() {
        $log.info('Disconnected');
        $scope.connected = false;
      });
    };

    // Funktion zum Abschließen eines Abonnements
    $scope.subscribe = function () {
      // Abonnieren
      $log.info('Abonniere Quelle "'+$scope.logMessagesSource+'"');
      var logSubscriptionId = client.subscribe('/topic/'+$scope.logMessagesSource, function(message) {
        if (message.body.indexOf($scope.filter) > -1) {
          $scope.logMessages.push(message);
          if ($scope.logMessages.length > $scope.maxEntriesShown) {
            $scope.logMessages.shift();
          }
          digest();
        }
      });

      // Abonnement merken
      var logSubscription = {'id':logSubscriptionId,'source':$scope.logMessagesSource};
      $scope.logSubscriptions.push(logSubscription);
      $scope.logMessagesSource = '';
    };

    // Funktion zum abmelden eines Abonnements
    $scope.unsubscribe = function () {
      $log.info('Entferne Abonnement mit Id "'+$scope.selectedLogSubscription+'"');
      client.unsubscribe($scope.selectedLogSubscription);
      $scope.logSubscriptions = _.reject($scope.logSubscriptions, function(logSubscription) {
        return logSubscription.id === $scope.selectedLogSubscription;
      });
    };

    // Funktion zur Überprüfung auf vorhandene Abonnements
    $scope.hasLogSubscriptions = function () {
      return $scope.logSubscriptions.length > 0;
    };

  }]);