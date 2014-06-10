'use strict';

describe('Controller: ResultsDetailCtrl', function () {

    // load the controller's module
    beforeEach(module('patientviewApp'));

    var ResultsDetailCtrl,
        scope;

    // Initialize the controller and a mock scope
    beforeEach(inject(function ($controller, $rootScope) {
        scope = $rootScope.$new();
        ResultsDetailCtrl = $controller('ResultsDetailCtrl', {
            $scope: scope
        });
    }));

    /*it('should init ok', function () {
        scope.init();
        expect(scope.resultsLoading).toBeDefined();
        //expect(scope.resultsLoading).toBe(false);
    });*/
});
