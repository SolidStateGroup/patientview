'use strict';

angular.module('patientviewApp').controller('ResultsDetailCtrl',['$scope', '$routeParams', '$location', 'ObservationHeadingService', 'ObservationService',
function ($scope, $routeParams, $location, ObservationHeadingService, ObservationService) {

    $scope.init = function() {
        var i;

        // if query parameters not set redirect to results
        if ($routeParams.code === undefined) {
            $location.path('/results');
        }

        // handle single result type from query parameter, todo: multiple result types
        $scope.code = $routeParams.code;

        if ($scope.code instanceof Array) {
            $scope.code = $scope.code[0];
        }

        $scope.getObservationHeadings();
        $scope.getObservations();
    };

    $scope.getObservationHeadings = function() {
        ObservationHeadingService.getAll().then(function(observationHeadings) {
            console.log(observationHeadings);
        }, function() {
            alert('Error retrieving results');
        });
    };

    $scope.getObservations = function() {
        ObservationService.getByCode($scope.loggedInUser.id, $scope.code).then(function(observations) {
            console.log(observations);
        }, function() {
            alert('Error retrieving results');
        });
    };

    $scope.init();
}]);
