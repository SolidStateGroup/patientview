'use strict';

describe('Service: MenuService', function () {

    // load the service's module
    beforeEach(module('patientviewApp'));

    // instantiate service
    var Menuservice;
    beforeEach(inject(function (_MenuService_) {
        Menuservice = _MenuService_;
    }));

    it('should do something', function () {
        expect(!!Menuservice).toBe(true);
    });

});
