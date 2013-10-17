'use strict';

/**
* merkurClientApp.services Modul
*
* Stellt Servcices bereit.
*/
var merkurClientAppServices = angular.module('merkurClientApp.services', []);

// Underscore per _ zur Verfügung stellen
merkurClientAppServices.factory('_', function() {
  return window._;
});