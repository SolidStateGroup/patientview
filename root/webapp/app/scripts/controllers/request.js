'use strict';

angular.module('patientviewApp').controller('RequestCtrl', ['GroupService', 'RequestService',
    'StaticDataService', '$scope', '$rootScope', 'UtilService','ENV','$timeout',
function (GroupService,RequestService,StaticDataService,$scope,$rootScope,UtilService,ENV,$timeout) {

    $scope.request = {};
    $scope.pw = '';
    $scope.months = UtilService.generateMonths();
    $scope.years = UtilService.generateYears();
    $scope.days = UtilService.generateDays();
    $scope.request.selectedYear = '';
    $scope.request.selectedMonth = '';
    $scope.request.selectedDay = '';

    GroupService.getAllPublic().then(function(groups) {
        $scope.specialties = [];
        $scope.childUnits = [];

        // separate SPECIALTY from UNIT groups
        groups.forEach(function(group) {
            if (group.visibleToJoin) {
                if (group.groupType.value === 'SPECIALTY') {
                    $scope.specialties.push(group);
                } else if (group.groupType.value === 'UNIT') {
                    $scope.childUnits.push(group);
                }
            }
        });
    });

    $scope.submit = function () {
        var groupId = 0;
        $scope.request.id = null;
        $scope.successMessage = null;
        $scope.errorMessage = null;
        $scope.loading = true;

        var formOk = true;

        if (typeof $scope.request.unit === 'undefined') {
            $scope.errorMessage = 'Please select a unit to join';
            formOk = false;
        } else {
            groupId = $scope.request.unit;
        }

        if (UtilService.validateEmail($scope.request.email)) {
            $scope.errorMessage = 'Invalid format for email';
            formOk = false;
        }

        if (typeof $scope.request.specialty !== 'undefined') {
            for (var i = 0; i < $scope.specialties.length; i++) {
                if ($scope.specialties[i].id === $scope.request.specialty) {
                    $scope.request.specialty = $scope.specialties[i].id;
                    break;
                }
            }
        }

        if (!UtilService.validationDateNoFuture($scope.request.selectedDay,
                                        $scope.request.selectedMonth,
                                        $scope.request.selectedYear)) {
            $scope.errorMessage = 'Please enter a valid date (and not in the future)';
            formOk = false;
        } else {
            $scope.request.dateOfBirth = $scope.request.selectedDay +
                '-' + $scope.request.selectedMonth +
                '-' +  $scope.request.selectedYear;
        }

        if (formOk) {
            RequestService.create(groupId, $scope.request).then(function () {
                $scope.successMessage = 'The join request has been saved';
                $scope.loading = false;
                $scope.completed = true;
            }, function (result) {
                $scope.errorMessage = 'The join request has not been submitted ' + result.data;
                $scope.loading = false;
            });
        } else {
            $scope.loading = false;
        }
    };

    $scope.refreshUnits = function() {
        $scope.units = [];
        if (typeof $scope.request.specialty !== 'undefined') {
            $scope.childUnits.forEach(function(unit) {
                if (_.findWhere(unit.parentGroups, {id: $scope.request.specialty})) {
                    $scope.units.push(unit);
                }
            });
        }
    };

    $scope.reCaptchaCallback = function(response) {
        $scope.request.captcha = response;
        $timeout(function() {
            $scope.$apply();
        });
    };

    $scope.getReCaptchaPublicKey = function() {
        return ENV.reCaptchaPublicKey;
    };
}]);
