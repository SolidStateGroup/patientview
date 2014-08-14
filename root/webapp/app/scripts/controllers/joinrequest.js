'use strict';

angular.module('patientviewApp').controller('JoinRequestCtrl', ['GroupService', 'JoinRequestService', 'StaticDataService', '$scope', '$rootScope', 'UtilService', function (GroupService,JoinRequestService,StaticDataService,$scope,$rootScope,UtilService) {

    $scope.joinRequest = {};
    $scope.pw = '';
    $scope.months = UtilService.generateMonths();
    $scope.years = UtilService.generateYears();
    $scope.days = UtilService.generateDays();
    $scope.joinRequest.selectedYear = 2000;
    $scope.joinRequest.selectedMonth = 1;
    $scope.joinRequest.selectedDay = 1;

    StaticDataService.getLookupByTypeAndValue('GROUP', 'SPECIALTY').then(function(lookup){
        GroupService.getAllByType(lookup.id).then(function(specialties) {
            $scope.specialties = specialties;
            specialties.forEach(function(entry) {

                // Lets default to Renal and requery the units
                if (entry.name === 'Renal') {
                    $scope.joinRequest.specialty = entry.id;
                    $scope.refreshUnits();
                }
            });
        });
    });

    $scope.submit = function () {

        var groupId = 0;
        $scope.joinRequest.id = null;

        $scope.successMessage = null;
        $scope.errorMessage = null;

        var formOk = true;

        if (typeof $scope.joinRequest.unit == 'undefined') {
            $scope.errorMessage = '- Please select a unit to join';
            formOk = false;
        } else {
            groupId = $scope.joinRequest.unit;
        }

        if (UtilService.validateEmail($scope.joinRequest.email)) {
            $scope.errorMessage = '- Invalid format for email';
            formOk = false;
        }

        if (typeof $scope.joinRequest.specialty !== 'undefined') {
            for (var i = 0; i < $scope.specialties.length; i++) {
                if ($scope.specialties[i].id === $scope.joinRequest.specialty) {
                    $scope.joinRequest.specialty = $scope.specialties[i].id;
                    break;
                }
            }
        }


        if (!UtilService.validationDate($scope.joinRequest.selectedDay,
                                        $scope.joinRequest.selectedMonth,
                                        $scope.joinRequest.selectedYear)) {
            $scope.errorMessage = '- Please enter a valid date';
            formOk = false;
        } else {
            $scope.joinRequest.dateOfBirth = $scope.joinRequest.selectedDay +
                '-' + $scope.joinRequest.selectedMonth +
                '-' +  $scope.joinRequest.selectedYear;
        }

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
                units.forEach(function(entry) {
                    $scope.joinRequest.unit = entry.id;
                });

            });
        }
    };

}]);
