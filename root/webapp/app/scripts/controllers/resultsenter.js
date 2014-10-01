'use strict';

angular.module('patientviewApp').controller('ResultsEnterCtrl',['$scope', 'ObservationService', 'ObservationHeadingService', 'UtilService',
function ($scope, ObservationService, ObservationHeadingService, UtilService) {

    $scope.addResultCluster = function(resultCluster) {
        if (resultCluster !== undefined) {

            var userResultCluster = {};
            userResultCluster.resultCluster = resultCluster;
            userResultCluster.day = $scope.days[0];
            userResultCluster.month = $scope.months[0];
            userResultCluster.year = $scope.years[0];
            userResultCluster.hour = $scope.hours[0];
            userResultCluster.minute = $scope.minutes[0];
            userResultCluster.values = {};

            $scope.userResultClusters.push(userResultCluster);
        }
    };

    $scope.removeUserResultCluster = function(resultCluster) {
        $scope.userResultClusters.splice($scope.userResultClusters.indexOf(resultCluster), 1);
    };

    $scope.cancel = function() {
        $scope.userResultClusters = [];
    };

    $scope.save = function() {
        ObservationService.saveResultClusters($scope.loggedInUser.id, $scope.userResultClusters).then(function() {
            $scope.successMessage = "Results successfully sent to PatientView, you can add more results below if you need.";
            $scope.userResultClusters = [];
        }, function () {
            alert('Cannot save your results');
        })
    };

    $scope.userDataValid = function () {
        for (var i=0;i<$scope.userResultClusters.length;i++) {
            var resultCluster = $scope.userResultClusters[i];

            if (!UtilService.validationDate(resultCluster.day, resultCluster.month, resultCluster.year)) {
                return false;
            }
        }
        return true;
    };

    var init = function() {
        $scope.loading = true;
        $scope.userResultClusters = [];
        $scope.days = UtilService.generateDays();
        $scope.months = UtilService.generateMonths();
        $scope.years = UtilService.generateYears();
        $scope.hours = UtilService.generateHours();
        $scope.minutes = UtilService.generateMinutes();

        ObservationHeadingService.getResultClusters().then(function(resultClusters) {

            $scope.resultClusters = resultClusters;
            $scope.loading = false;
        }, function () {
            alert('Cannot get result clusters');
        })
    };

    init();
}]);
