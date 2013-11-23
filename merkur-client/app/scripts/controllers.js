'use strict';

angular.module('merkurClientApp.controllers', [])
  .controller('MainCtrl', ['$scope','$window','defaultWebsocketEndpoints','defaultMaxEntriesShown','defaultMaxNotificationsShown','$timeout',
    function ($scope,$window,defaultWebsocketEndpoints,defaultMaxEntriesShown,defaultMaxNotificationsShown,$timeout) {

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
    $scope.maxNotificationsShown = defaultMaxNotificationsShown;

    /*
     * Funktionen
     *
     * Bestehend aus Hilfsfunktionen und solchen Funktionen, 
     * die dem Scope zur Interaktion mit der GUI zugeorndet sind.
     */

    var digest = _.throttle(function() {
      $scope.$digest();
    }, 100);

    // Verbindung zum Websocket-Server
    $scope.connect = function() {
      socket = new SockJS($scope.websocketEndpoint);
      client = Stomp.over(socket);
      client.connect('', '', function(frame) {
        addNotification('Verbunden mit "'+$scope.websocketEndpoint+'"','success');
        $scope.$apply(function() {
          $scope.connected = true;
        });
      }, function(error) {
        addNotification('Fehler beim Verbinden: "'+error+'"','error');
        $scope.$apply(function() {
          $scope.connected = false;
        });
      });
    };

    // Trennung der Verbindung mit dem Websocket-Server
    $scope.disconnect = function() {
      client.disconnect(function() {
        _.each($scope.logSubscriptions, function(logSubscription) {
          doUnsubsribe(logSubscription.id);
        });
        addNotification('Verbindung zu "'+$scope.websocketEndpoint+'" getrennt','warning');
        $scope.connected = false;
      });
    };

    // Abschließen eines Abonnements
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

    // Kündigen eines Abonnements
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

    // Überprüfung auf vorhandene Abonnements
    $scope.hasLogSubscriptions = function () {
      return $scope.logSubscriptions.length > 0;
    };

    // Überprüfung auf vorhandene Benachrichtigungen
    $scope.hasNotifications = function () {
      return $scope.notifications.length > 0;
    };

    // Überprüfung auf vorhandene Log-Nachrichten
    $scope.hasLogMessages = function () {
      return $scope.logMessages.length > 0;
    };

    // Hilfsfunktion zum Hinzufügen einer Benachrichtigung
    var addNotification = function(message, level) {
      var notification = {
        'message':message,
        'level':'alert-'+level
      };
      $scope.notifications.push(notification);
      if ($scope.notifications.length > $scope.maxNotificationsShown) {
        $scope.notifications.shift();
      }
    };

    /*
     * Keyboard-Shortcuts
     * 
     * Diese dienen hauptsächlich der besseren Usability bzw. der erhöhten Arbeitsgeschwindigkeit
     */

    // Hilfe-Dialog öffnen
    Mousetrap.bind('?', function() {
      angular.element('#help').click();
    });

    // Auswahl des Websocket-Endpunkts öffnen
    Mousetrap.bind('o c', function() {
      angular.element('#websocketEndpoint').select2('open');
      return false;
    });

    // Auswahl der Abonnements öffnen
    Mousetrap.bind('o s', function() {
      angular.element('#subscriptions').select2('open');
      return false;
    });

    // Verbindungs-Button klicken
    Mousetrap.bind('c c', function() {
      angular.element('#connect').click();
      return false;
    });

    // Trennen-Button klicken
    Mousetrap.bind('c d', function() {
      angular.element('#disconnect').click();
      return false;
    });

    // Abonnieren-Button klicken
    Mousetrap.bind('c s', function() {
      angular.element('#subscribe').click();
      return false;
    });

    // Kündigen-Button klicken
    Mousetrap.bind('c u', function() {
      angular.element('#unsubscribe').click();
      return false;
    });

    // Schrift verkleinern klicken
    Mousetrap.bind('-', function() {
      angular.element('#font-smaller').click();
      return false;
    });

    // Schrift vergrößern klicken
    Mousetrap.bind('+', function() {
      angular.element('#font-bigger').click();
      return false;
    });

    // Schrift auf Standardgröße setzen
    Mousetrap.bind('c 0', function() {
      angular.element('#font-default').click();
      return false;
    });

    // Quelle-Feld fokussieren
    Mousetrap.bind('g s', function() {
      angular.element('#source').focus();
      return false;
    });

    // Filter-Feld fokussieren
    Mousetrap.bind('g f', function() {
      angular.element('#filter').focus();
      return false;
    });

    // Log-Nachrichten löschen
    Mousetrap.bind('ctrl+l', function() {
      $scope.logMessages = [];
      return false;
    });

  }]);