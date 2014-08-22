'use strict';

angular.module('patientviewApp').controller('ContactUnitCtrl', ['GroupService', 'StaticDataService', '$scope', '$rootScope', 'UtilService', function (GroupService,StaticDataService,$scope,$rootScope,UtilService) {

    $scope.joinRequest = {};
    $scope.pw = '';
    $scope.months = UtilService.generateMonths();
    $scope.years = UtilService.generateYears();
    $scope.days = UtilService.generateDays();
    $scope.joinRequest.selectedYear = 2000;
    $scope.joinRequest.selectedMonth = '01';
    $scope.joinRequest.selectedDay = '01';

    StaticDataService.getLookupByTypeAndValue('GROUP', 'SPECIALTY').then(function(lookup){
        GroupService.getAllByType(lookup.id).then(function(specialties) {
            $scope.specialties = [];
            specialties.forEach(function(entry) {

                if (entry.visibleToJoin === true) {
                    $scope.specialties.push(entry);
                    // Lets default to Renal and requery the units
                    if (entry.name === 'Renal') {
                        $scope.joinRequest.specialty = entry.id;
                        $scope.refreshUnits();
                    }
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
            $scope.errorMessage = '- Please select a unit';
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
            GroupService.contactUnit(groupId, $scope.joinRequest).then(function () {
                $scope.successMessage = 'The password request has been sent';
            }, function (result) {
                $scope.errorMessage = '- The password request has not been submitted ' + result.data;
            });
        }

    };

    $scope.refreshUnits = function() {
        if (typeof $scope.joinRequest.specialty !== 'undefined') {
            GroupService.getChildren($scope.joinRequest.specialty).then(function (units) {
                $scope.units = [];
                units.forEach(function(entry) {
                    if (entry.visibleToJoin === true) {
                        $scope.units.push(entry);
                    }
                    $scope.joinRequest.unit = entry.id;
                });

            });
        }
    };

}]);
