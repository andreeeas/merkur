'use strict';

angular.module('merkurClientApp.controllers', [])
  .controller('MainCtrl', ['$scope','messages.serverWebsocketEndpoint','messages.maxEntriesShown','_',function ($scope, messagesServerWebsocketEndpoint, messagesMaxEntriesShown, _) {

    // scope Variablen
    $scope.messagesSource = '';
    $scope.logMessages = [];
    $scope.logSubscriptions = [];
    $scope.messagesFilter = '';
    $scope.messagesMaxEntriesShown = messagesMaxEntriesShown;

    var socket = new SockJS(messagesServerWebsocketEndpoint);
    var client = Stomp.over(socket);
    client.connect('', '', function(frame) {
      console.log('Connected ' + frame);
    }, function(error) {
      console.log('STOMP protocol error ' + error);
    });

    // Funktion zum Abschließen eines Abonnements
    $scope.subscribe = function () {
      // Abonnieren
      var logSubscriptionId = client.subscribe('/topic/'+$scope.messagesSource, function(message) {
        if (message.body.indexOf($scope.messagesFilter) > -1) {
          $scope.$apply(function () {
            $scope.logMessages.push(message.body);
            if ($scope.logMessages.length > messagesMaxEntriesShown) {
              $scope.logMessages.shift();
            }
          });
        }
      });

      // Abonnement merken
      var logSubscription = {'id':logSubscriptionId,'source':$scope.messagesSource};
      $scope.logSubscriptions.push(logSubscription);
      $scope.messagesSource = '';
    };

    // Funktion zum abmelden eines Abonnements
    $scope.unsubscribe = function (logSubscription) {
      client.unsubscribe(logSubscription.id);
      $scope.logSubscriptions = _.without($scope.logSubscriptions, logSubscription);
    };

    // Funktion zur Überprüfung auf vorhandene Abonnements
    $scope.hasLogSubscriptions = function () {
      return $scope.logSubscriptions.length > 0;
    };

  }]);