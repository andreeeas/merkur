'use strict';



describe('Merkur Client', function() {

  var websocketEndpoint = 'http://localhost:8080/merkur-server-0.1/socket';
  
  // vor jedem Testfall zur Startseite navigieren
  beforeEach(function () {
    browser().navigateTo('/');
  });

  it('should stay on startpage when location hash/fragment is empty', function() {
    expect(browser().location().path()).toBe('/');
  });

  it('should render the three standard containers when user is on startpage', function() {
    expect(element('.configuration','Configuration container').count()).toBe(1);
    expect(element('.notifications','Notification container').count()).toBe(1);
    expect(element('.logmessages','Log messages container').count()).toBe(1);
  });

  describe('Connection is not active', function() {
    
    describe('No value is selected', function() {

      it('should have an empty websocketEndpoint selectbox', function() {
        expect(element('#websocketEndpoint','Websocket endpoint selectbox').val()).toBe('');
      });

      it('should have a deactivated Verbinden button', inject(function() {
        expect(element('#connect:disabled','Verbinden button').count()).toBe(1);
      }));

    });

    describe('A value is selected', function() {

      beforeEach(function () {
        select('websocketEndpoint').option(websocketEndpoint);
      });

      it('should have selected a websocketEndpoint', function() {
        expect(element('#websocketEndpoint','Websocket endpoint selectbox').val()).toBe(websocketEndpoint);
      });

      it('should have an activated Verbinden button', function() {
        expect(element('#connect:enabled','Verbinden button').count()).toBe(1);
      });

      it('should have a deactivated subscribe button', function() {
        expect(element('#subscribe:visible','disconnect button').count()).toBe(0);
      });

      it('should have a deactivated disconnect button', function() {
        expect(element('#disconnect:visible','disconnect button').count()).toBe(0);
      });

    });

  });

  describe('Connection is active', function() {

    beforeEach(function () {
      select('websocketEndpoint').option(websocketEndpoint);
      element('#connect').click();
    });
    
    describe('No subscriptions to log messages exist', function() {

      // TODO: Fix
      it('should have a deactivated websocketEndpoint selectbox', function() {
        expect(element('#websocketEndpoint','Websocket endpoint selectbox').css('display')).toBe('none');
      });

      // TODO: Fix
      it('should have a deactivated Verbinden button', function() {
        expect(element('#connect','Verbinden button').css('display')).toBe('none');
      });

      it('should have an activated subscribe button', function() {
        expect(element('#subscribe','disconnect button').css('display')).toBe('block');
      });

      it('should have an activated disconnect button', function() {
        expect(element('#disconnect','disconnect button').css('display')).toBe('block');
      });

    });

    describe('one subscription to log messages exists', function() {
      
      // TODO: Implementieren

    })

  });

});