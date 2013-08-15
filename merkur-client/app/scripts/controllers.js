'use strict';

angular.module('merkurClientApp.controllers', [])
  .controller('MainCtrl', ['$scope','stompService','messages.maxEntriesShown','messages.topic',function ($scope, stompService, messagesMaxEntriesShown, messagesTopic) {
    $scope.awesomeThings = [];

    stompService.client.connect('', '', function(frame) {
      console.log('Connected ' + frame);
      stompService.client.subscribe(messagesTopic, function(message) {
        $scope.$apply(function () {
          $scope.awesomeThings.push(message.body);
          if ($scope.awesomeThings.length > messagesMaxEntriesShown) {
            $scope.awesomeThings.shift();
          }
        });
      });
    }, function(error) {
      console.log('STOMP protocol error ' + error);
    });

  }]);
