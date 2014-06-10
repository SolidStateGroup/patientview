'use strict';

describe('Controller: ResultsCtrl', function () {

    // load the controller's module
    beforeEach(module('patientviewApp'));

    var ResultsCtrl,
        scope;

    // Initialize the controller and a mock scope
    beforeEach(inject(function ($controller, $rootScope) {
        scope = $rootScope.$new();
        ResultsCtrl = $controller('ResultsCtrl', {
            $scope: scope
        });
        //scope.init();
    }));

    /*it('should init ok', function () {
        scope.init();
        expect(scope.resultsLoading).toBeDefined();
        //expect(scope.resultsLoading).toBe(false);
    });*/
});
