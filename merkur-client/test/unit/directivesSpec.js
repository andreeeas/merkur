'use strict';

describe('Directives', function () {

  var element, scope, _;

  // Directives-Modul laden
  beforeEach(module('merkurClientApp.directives'));

  describe('scrollpane', function () {

    beforeEach(inject(function ($rootScope, $compile, $window) {
      element = angular.element('<div scrollpane></div>');
      scope = $rootScope;
      _ = $window._;
      scope.logMessages = [];
      _.times(100,function() {
        scope.logMessages.push(_.random(0,100));
      });
      $compile(element)(scope);
      scope.$digest();
    }));

    it('should set scoll-pane class on element', function() {
      expect(element).toHaveClass('scroll-pane');
    });

    it('should create a scoll pane container', function() {
      var container = element.find('.jspContainer');
      expect(container.length).toBe(1);
    });

    it('should have a jsp data attribute', function() {
      var jsp = element.data('jsp');
      expect(jsp).toBeDefined();
    });

    it('should reinitialise api when log messages are added', function() {
      var jsp = element.data('jsp');
      spyOn(jsp, 'reinitialise');

      scope.logMessages.push(1);
      scope.$digest();

      expect(jsp.reinitialise).toHaveBeenCalled();

      jsp.reinitialise.reset();
      var ten = 10;
      _.times(ten, function() {
        scope.logMessages.push(1);
        scope.$digest();
      });

      expect(jsp.reinitialise.callCount).toBe(ten);
    });

  });

});