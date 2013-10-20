'use strict';

describe('Services', function () {

  // Service-Modul laden
  beforeEach(module('merkurClientApp.services'));

  describe('Underscore', function () {

    it('should contain an underscore.js service', inject(['_',function (_) {
      expect(_).toBeDefined();
    }]));

  });
});
