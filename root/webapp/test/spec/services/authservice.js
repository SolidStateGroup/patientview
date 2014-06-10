'use strict';

describe('Service: AuthService', function () {

    // load the service's module
    beforeEach(module('patientviewApp'));

    // instantiate service
    var authService;
    beforeEach(inject(function (_AuthService_) {
        authService = _AuthService_;
    }));

    it('should do something', function () {
        expect(!!authService).toBe(true);
    });

});
