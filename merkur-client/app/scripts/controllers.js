'use strict';

angular.module('merkurClientApp.controllers', [])
  .controller('MainCtrl', ['$scope','stompService','messages.maxEntriesShown','messages.topic',function ($scope, stompService, messagesMaxEntriesShown, messagesTopic) {
    $scope.awesomeThings = [];
    $scope.query = '';
    $scope.messagesMaxEntriesShown = messagesMaxEntriesShown;

    stompService.client.connect('', '', function(frame) {
      console.log('Connected ' + frame);
      stompService.client.subscribe(messagesTopic, function(message) {
        if (message.body.indexOf($scope.query) > -1) {
          $scope.$apply(function () {
            $scope.awesomeThings.push(message.body);
            if ($scope.awesomeThings.length > messagesMaxEntriesShown) {
              $scope.awesomeThings.shift();
            }
          });
        }
      });
    }, function(error) {
      console.log('STOMP protocol error ' + error);
    });
  }]);