'use strict';

angular.module('patientviewApp').controller('ResultsEnterCtrl',['$scope', 'ObservationHeadingService',
function ($scope, ObservationHeadingService) {

    var init = function() {
        ObservationHeadingService.getResultClusters().then(function(resultClusters) {
            

        }, function () {
            alert('Cannot get result clusters');
        })
    };

    init();
}]);
