'use strict';

/**
* merkurClientApp.services Modul
*
* Stellt Directives bereit.
*/
var merkurClientAppDirectives = angular.module('merkurClientApp.directives', []);

merkurClientAppDirectives.directive('scrollpane',['$compile',function($compile)
{
  return {
    restrict: 'A',
    link: function(scope , element , attrs) {
      element.addClass('scroll-pane');
      element.jScrollPane({
        showArrows:true,
        stickToBottom:true
      });
      var api = element.data('jsp');
      scope.$watch(function(scope) {
        return scope.logMessages.length;
      },function(newValues, oldValues) {
        api.reinitialise();
      });
    }
  };
}]);