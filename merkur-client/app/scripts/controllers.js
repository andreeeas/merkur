'use strict';

angular.module('merkurClientApp.controllers', [])
  .controller('MainCtrl', ['$scope','messages.serverWebsocketEndpoint','messages.maxEntriesShown','messages.source',function ($scope, messagesServerWebsocketEndpoint, messagesMaxEntriesShown, messagesSource) {

    // scope Variablen
    $scope.messagesSource = messagesSource;
    $scope.logMessages = [];
    $scope.messagesFilter = '';
    $scope.messagesMaxEntriesShown = messagesMaxEntriesShown;

    // interne Variablen
    var subscriptions = [];

    var socket = new SockJS(messagesServerWebsocketEndpoint);
    var client = Stomp.over(socket);
    client.connect('', '', function(frame) {
      console.log('Connected ' + frame);
    }, function(error) {
      console.log('STOMP protocol error ' + error);
    });

    $scope.subscribe = function () {
      // Abonnement abschließen
      var subscription = client.subscribe($scope.messagesSource, function(message) {
        if (message.body.indexOf($scope.messagesFilter) > -1) {
          $scope.$apply(function () {
            $scope.logMessages.push(message.body);
            if ($scope.logMessages.length > messagesMaxEntriesShown) {
              $scope.logMessages.shift();
            }
          });
        }
        // Abonnement merken
        subscriptions.push(subscription);
      });
    };
  }]);