'use strict';

angular.module('merkurClientApp',
  [
    'ngRoute',
    'merkurClientApp.controllers',
    'merkurClientApp.directives',
    'merkurClientApp.constants'
  ])
  .config(['$routeProvider', function ($routeProvider) {
    $routeProvider
      .when('/', {
        templateUrl: 'views/main.html',
        controller: 'MainCtrl'
      })
      .otherwise({
        redirectTo: '/'
      });
  }]);
