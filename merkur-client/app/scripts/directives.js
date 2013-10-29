'use strict';

/**
* merkurClientApp.services Modul
*
* Stellt Directives bereit.
*/
var merkurClientAppDirectives = angular.module('merkurClientApp.directives', []);

merkurClientAppDirectives.directive('scrollpane',function()
{
  return {
    restrict: 'A',
    link: function(scope , element) {
      element.addClass('scroll-pane');
      element.jScrollPane({
        stickToBottom:true
      });
      var api = element.data('jsp');
      scope.$watch(function(scope) {
        return scope.logMessages.length;
      },function() {
        api.reinitialise();
      });
    }
  };
});

merkurClientAppDirectives.directive('tooltip',function()
{
  return {
    restrict: 'A',
    link: function(scope , element , attrs) {
      element.tooltip({
        title: function() {
          return scope[attrs.tooltip];
        }
      });
    }
  };
});