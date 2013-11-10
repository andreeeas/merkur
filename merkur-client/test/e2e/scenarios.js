'use strict';



describe('Merkur Client', function() {

  var websocketEndpoint = 'http://localhost:9090/merkur-server/socket';
  
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
      sleep(1);
    });
    
    describe('No subscriptions to log messages exist', function() {

      it('should display notification for connection', function() {
        expect(repeater('.row-fluid.notifications li','Notifications').count()).toBe(1);
      });

      // TODO: Fix
      // it('should have a deactivated websocketEndpoint selectbox', function() {
      //   expect(element('#websocketEndpoint','Websocket endpoint selectbox').css('display')).toBe('none');
      // });

      // TODO: Fix
      // it('should have a deactivated Verbinden button', function() {
      //   expect(element('#connect','Verbinden button').css('display')).toBe('none');
      // });

      it('should have an activated subscribe button', function() {
        expect(element('#subscribe','disconnect button').css('display')).toBe('block');
      });

      it('should have an activated disconnect button', function() {
        expect(element('#disconnect','disconnect button').css('display')).toBe('block');
      });

    });

    describe('subscriptions to log messages exist', function() {
      
      beforeEach(function () {
        input('logMessagesSource').enter('Generator.#');
        sleep(1);
        element('#subscribe').click();
        sleep(2);
      });

      it('should display notification for subscription', function() {
        expect(repeater('.row-fluid.notifications li','Notifications').count()).toBe(2);
      });

      it('should contain log messages', function() {
        expect(repeater('.row-fluid.logmessages li','Log messages').count()).toBeGreaterThan(1);
      });

      it('should contain a subscription in the corresponding select', function() {
        expect(repeater('.row-fluid.subscriptions select option','Subscriptions').count()).toBe(2);
      });

      it('should unsubscribe from that subscription when clicking on the corresponding button', function() {
        select('selectedLogSubscription').option('sub-0');
        sleep(1);
        element('#unsubscribe').click();
        expect(repeater('.row-fluid.subscriptions select option','Subscriptions').count()).toBe(1);
        // expect 3 as we do the connect, subscribe and unsubscribe step
        expect(repeater('.row-fluid.notifications li','Notifications').count()).toBe(3);
      });

    });

  });

});