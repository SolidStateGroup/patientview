'use strict';

// used when contacting unit due to forgotten password
angular.module('patientviewApp').controller('ContactUnitCtrl', ['GroupService', 'StaticDataService', '$scope',
    '$rootScope', 'UtilService', function (GroupService,StaticDataService,$scope,$rootScope,UtilService) {

    $scope.contactUnit = {};
    $scope.pw = '';
    $scope.months = UtilService.generateMonths();
    $scope.years = UtilService.generateYears();
    $scope.days = UtilService.generateDays();
    $scope.contactUnit.selectedYear = '';
    $scope.contactUnit.selectedMonth = '';
    $scope.contactUnit.selectedDay = '';

    $scope.init = function() {
        getAllPublic();
    };

    var getAllPublic = function() {
        GroupService.getAllPublic().then(function(groups) {
            $scope.specialties = [];
            $scope.childUnits = [];

            // separate SPECIALTY from UNIT groups
            groups.forEach(function(group) {
                if (group.groupType.value === 'SPECIALTY') {
                    $scope.specialties.push(group);
                } else if (group.groupType.value === 'UNIT') {
                    $scope.childUnits.push(group);
                }
            });
        });
    };

    $scope.submit = function () {

        var groupId = 0;
        $scope.contactUnit.id = null;

        $scope.successMessage = null;
        $scope.errorMessage = null;

        var formOk = true;

        if (typeof $scope.contactUnit.unit === 'undefined') {
            $scope.errorMessage = 'Please select a unit';
            formOk = false;
        } else {
            groupId = $scope.contactUnit.unit;
        }

        if (UtilService.validateEmail($scope.contactUnit.email)) {
            $scope.errorMessage = 'Please enter a valid email address';
            formOk = false;
        }

        if (typeof $scope.contactUnit.specialty !== 'undefined') {
            for (var i = 0; i < $scope.specialties.length; i++) {
                if ($scope.specialties[i].id === $scope.contactUnit.specialty) {
                    $scope.contactUnit.specialty = $scope.specialties[i].id;
                    break;
                }
            }
        }

        if (!UtilService.validationDate($scope.contactUnit.selectedDay,
            $scope.contactUnit.selectedMonth,
            $scope.contactUnit.selectedYear)) {
            $scope.errorMessage = 'Please enter a valid date';
            formOk = false;
        } else {
            $scope.contactUnit.dateOfBirth = $scope.contactUnit.selectedDay +
                '-' + $scope.contactUnit.selectedMonth +
                '-' +  $scope.contactUnit.selectedYear;
        }

        if (formOk) {
            GroupService.passwordRequest(groupId, $scope.contactUnit).then(function () {
                $scope.successMessage = 'The password request has been sent';
            }, function (result) {
                $scope.errorMessage = 'Error: ' + result.data;
            });
        }
    };
    
    $scope.refreshUnits = function() {
        $scope.units = [];
        if (typeof $scope.contactUnit.specialty !== 'undefined') {
            $scope.childUnits.forEach(function(unit) {
                if (_.findWhere(unit.parentGroups, {id: $scope.contactUnit.specialty})) {
                    $scope.units.push(unit);
                }
            });
        }
    };

    $scope.init();

}]);
