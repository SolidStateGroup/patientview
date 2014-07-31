'use strict';

angular.module('patientviewApp').controller('JoinRequestCtrl', ['GroupService', 'JoinRequestService', 'StaticDataService', '$scope', '$rootScope', 'UtilService', function (GroupService,JoinRequestService,StaticDataService,$scope,$rootScope,UtilService) {

    $scope.joinRequest = {};

    $scope.months = UtilService.generateMonths();
    $scope.years = UtilService.generateYears();
    $scope.days = UtilService.generateDays();

    StaticDataService.getLookupByTypeAndValue('GROUP', 'UNIT').then(function(lookup){
        GroupService.getAllByType(lookup.id).then(function(units) {
            $scope.units = units;
        });
    });

    StaticDataService.getLookupByTypeAndValue('GROUP', 'SPECIALTY').then(function(lookup){
        GroupService.getAllByType(lookup.id).then(function(specialties) {
            $scope.specialties = specialties;
        });
    });

    $scope.submit = function () {

        var groupId = 0;
        $scope.joinRequest.id = null;

        $scope.successMessage = null;
        $scope.errorMessage = null;

        var formOk = true;

        if (!$scope.joinRequest.selectedYear) {
            $scope.errorMessage = '- Please select a year';
            formOk = false;
        }

        if (!$scope.joinRequest.selectedMonth) {
            $scope.errorMessage = '- Please select a month';
            formOk = false;
        }

        if (!$scope.joinRequest.selectedDay) {
            $scope.errorMessage = '- Please select a day';
            formOk = false;
        }

        if (UtilService.validateEmail($scope.joinRequest.email)) {
            $scope.errorMessage = '- Invalid format for email';
            formOk = false;
        }

        // FIX ME - get the object from the select list.
        if (typeof $scope.joinRequest.specialty !== 'undefined') {
            for (var i = 0; i < $scope.specialties.length; i++) {
                if ($scope.specialties[i].id === $scope.joinRequest.specialty) {
                    $scope.joinRequest.specialty = $scope.specialties[i].id;
                    break;
                }
            }
        }


        if (typeof $scope.joinRequest.unit === 'undefined') {
            $scope.errorMessage = '- Please select a unit to join';
            formOk = false;
        } else {
            groupId = $scope.joinRequest.unit;
        }


        $scope.joinRequest.dateOfBirth = $scope.joinRequest.selectedDay +
            '-' + $scope.joinRequest.selectedMonth +
            '-' +  $scope.joinRequest.selectedYear;

        if (formOk) {
            JoinRequestService.new(groupId, $scope.joinRequest).then(function () {
                $scope.successMessage = 'The join request has been saved';
            }, function (result) {
                $scope.errorMessage = '- The join request has not been submitted ' + result;
            });
        }

    };

    $scope.refreshUnits = function() {
        if (typeof $scope.joinRequest.specialty !== 'undefined') {
            GroupService.getChildren($scope.joinRequest.specialty).then(function (units) {
                $scope.units = units;
            });
         }
    };

}]);
