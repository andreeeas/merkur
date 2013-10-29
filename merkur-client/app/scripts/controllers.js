'use strict';

angular.module('merkurClientApp.controllers', [])
  .controller('MainCtrl', ['$scope','$window','defaultWebsocketEndpoints','defaultMaxEntriesShown','$timeout',
    function ($scope,$window,defaultWebsocketEndpoints,defaultMaxEntriesShown,$timeout) {

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
    $scope.notifications = [];
    
    $scope.maxEntriesShown = defaultMaxEntriesShown;

    var digest = _.throttle(function() {
      $scope.$digest();
    }, 1000);

    // Funktion zur Verbindung zum Websocket-Server
    $scope.connect = function() {
      socket = new SockJS($scope.websocketEndpoint);
      client = Stomp.over(socket);
      client.connect('', '', function(frame) {
        addNotification('Verbunden mit "'+$scope.websocketEndpoint+'"','success');
        $scope.$apply(function() {
          $scope.connected = true;
        });
      }, function(error) {
        addNotification('STOMP Fehler: "'+error+'"','error');
        $scope.$apply(function() {
          $scope.connected = false;
        });
      });
    };

    // Funktion zur Trennung der Verbindung mit dem Websocket-Server
    $scope.disconnect = function() {
      client.disconnect(function() {
        _.each($scope.logSubscriptions, function(logSubscription) {
          doUnsubsribe(logSubscription.id);
        });
        addNotification('Verbindung zu "'+$scope.websocketEndpoint+'" getrennt','warning');
        $scope.connected = false;
      });
    };

    // Funktion zum Abschließen eines Abonnements
    $scope.subscribe = function () {
      // Abonnieren
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
      addNotification('Quelle "'+$scope.logMessagesSource+'" abonniert','info');
      $scope.logMessagesSource = '';
    };

    // Funktion zum abmelden eines Abonnements
    $scope.unsubscribe = function () {
      doUnsubsribe($scope.selectedLogSubscription);
    };

    var doUnsubsribe = function(logSubscription) {
      client.unsubscribe(logSubscription);
      $scope.logSubscriptions = _.reject($scope.logSubscriptions, function(subscription) {
        return subscription.id === logSubscription;
      });
      addNotification('Abonnement mit Id "'+logSubscription+'" entfernt','info');
    };

    // Funktion zur Überprüfung auf vorhandene Abonnements
    $scope.hasLogSubscriptions = function () {
      return $scope.logSubscriptions.length > 0;
    };

    var addNotification = function(message, level) {
      var notification = {
        'message':message,
        'level':'alert-'+level
      };
      $scope.notifications.push(notification);
      $timeout(function() {
        $scope.notifications = _.without($scope.notifications,notification);
      }, 5000);
    };

    // Funktion zur Überprüfung auf vorhandene Benachrichtigungen
    $scope.hasNotifications = function () {
      return $scope.notifications.length > 0;
    };

    // Funktion zur Überprüfung auf vorhandene Log-Nachrichten
    $scope.hasLogMessages = function () {
      return $scope.logMessages.length > 0;
    };

  }]);