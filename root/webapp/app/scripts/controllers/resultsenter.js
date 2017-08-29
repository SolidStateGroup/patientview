'use strict';

angular.module('patientviewApp').controller('ResultsEnterCtrl',['$scope', 'ObservationService', 'ObservationHeadingService', 'UtilService',
function ($scope, ObservationService, ObservationHeadingService, UtilService) {

    $scope.getObservationHeadingPlaceholder = function(observationHeading) {
        var placeholder = observationHeading.heading;

        if (observationHeading.units) {
            placeholder = placeholder + ' (' + observationHeading.units + ')';
        }

        return placeholder;
    };

    $scope.addResultCluster = function(resultCluster) {
        if (resultCluster !== undefined) {

            delete $scope.successMessage;

            var currentDate = new Date();
            var i;
            var userResultCluster = {};
            userResultCluster.resultCluster = resultCluster;
            userResultCluster.values = {};

            for (i=0;i<$scope.days.length;i++) {
                if (parseInt($scope.days[i]) === currentDate.getDate()) {
                    userResultCluster.day = $scope.days[i];
                }
            }
            for (i=0;i<$scope.months.length;i++) {
                if (parseInt($scope.months[i]) === currentDate.getMonth() + 1) {
                    userResultCluster.month = $scope.months[i];
                }
            }
            for (i=0;i<$scope.years.length;i++) {
                if (parseInt($scope.years[i]) === currentDate.getFullYear()) {
                    userResultCluster.year = $scope.years[i];
                }
            }

            for (i=0;i<$scope.hours.length;i++) {
                if (parseInt($scope.hours[i]) === currentDate.getHours()) {
                    userResultCluster.hour = $scope.hours[i];
                }
            }
            for (i=0;i<$scope.minutes.length;i++) {
                if (parseInt($scope.minutes[i]) === currentDate.getMinutes()) {
                    userResultCluster.minute = $scope.minutes[i];
                }
            }

            // for Dialysis Treatment allow to enter only one result at the time
            // as we have custom endpoints for this
            if(resultCluster.id == '3'){
                $scope.dialysis = true;
                $scope.userResultClusters = [];
                $scope.userResultClusters.push(userResultCluster);
            }else{
                $scope.userResultClusters.push(userResultCluster);
                $scope.dialysis = false;
                // TODO: if we have Dialysis Treatment in the list remove it
            }
        }
    };

    $scope.removeUserResultCluster = function(resultCluster) {
        $scope.userResultClusters.splice($scope.userResultClusters.indexOf(resultCluster), 1);
        $scope.dialysis = false;
    };

    $scope.cancel = function() {
        $scope.userResultClusters = [];
        $scope.dialysis = false;
    };

    $scope.save = function() {
        ObservationService.saveResultClusters($scope.loggedInUser.id, $scope.userResultClusters, $scope.dialysis).then(function() {
            $scope.successMessage = 'Results successfully sent to PatientView. If required, more results can be added below.';
            $scope.userResultClusters = [];
            $scope.dialysis = false;
        }, function () {
            alert('Cannot save your results');
        });
    };

    $scope.userDataValid = function () {
        for (var i=0;i<$scope.userResultClusters.length;i++) {
            var resultCluster = $scope.userResultClusters[i];

            // check date is ok
            if (!UtilService.validationDateNoFuture(resultCluster.day, resultCluster.month, resultCluster.year)) {
                return false;
            }

            // Dialysis Treatment specific check, need all values entered
            if($scope.dialysis
                && hasValue(resultCluster.HdHours)
                && hasValue(resultCluster.HdLocation)
                && hasValue(resultCluster.eprex)
                && hasValue(resultCluster.TargetWeight)
                && hasValue(resultCluster.PreWeight)
                && hasValue(resultCluster.PostWeight)
                && hasValue(resultCluster.UfVolume)
                && hasValue(resultCluster.pulse)
                && hasValue(resultCluster.PreBpsys)
                && hasValue(resultCluster.PreBpdia)
                && hasValue(resultCluster.PostBpsys)
                && hasValue(resultCluster.PostBpdia)
                && hasValue(resultCluster.hypotension)
                && hasValue(resultCluster.BodyTemperature)
                && hasValue(resultCluster.bps)
                && hasValue(resultCluster.DialFlow)
                && hasValue(resultCluster.LitresProcessed)) {
                return true;
            }

            // check at least one entry in results
            for(var prop in resultCluster.values) {
                if (resultCluster.values.hasOwnProperty(prop)) {
                    if (resultCluster.values[prop] !== null) {
                        return true;
                    }
                }
            }
        }
        return false;
    };

    var hasValue = function(value) {
        return value !== undefined;
    };

    var init = function() {
        $scope.loading = true;
        $scope.dialysis = false;
        $scope.userResultClusters = [];
        $scope.days = UtilService.generateDays();
        $scope.months = UtilService.generateMonths();
        $scope.years = UtilService.generateYears2000();
        $scope.hours = UtilService.generateHours();
        $scope.minutes = UtilService.generateMinutes();

        ObservationHeadingService.getResultClusters().then(function(resultClusters) {
            $scope.resultClusters = resultClusters;
            $scope.selectedResultCluster = resultClusters[0];
            $scope.loading = false;
        }, function () {
            alert('Cannot get result clusters');
        });
    };

    init();
}]);
