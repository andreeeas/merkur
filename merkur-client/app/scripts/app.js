'use strict';

angular.module('merkurClientApp',
  [
    'ngRoute',
    'ui.select2',
    'ui.bootstrap.collapse',
    'ui.bootstrap.accordion',
    'ui.bootstrap.transition',
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