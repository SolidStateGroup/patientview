'use strict';

angular.module('patientviewApp').controller('JoinRequestCtrl', ['GroupService', 'JoinRequestService',
    'StaticDataService', '$scope', '$rootScope', 'UtilService','ENV','$timeout',
function (GroupService,JoinRequestService,StaticDataService,$scope,$rootScope,UtilService,ENV,$timeout) {

    $scope.joinRequest = {};
    $scope.pw = '';
    $scope.months = UtilService.generateMonths();
    $scope.years = UtilService.generateYears();
    $scope.days = UtilService.generateDays();
    $scope.joinRequest.selectedYear = '';
    $scope.joinRequest.selectedMonth = '';
    $scope.joinRequest.selectedDay = '';

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
        $scope.joinRequest.id = null;
        $scope.successMessage = null;
        $scope.errorMessage = null;
        $scope.loading = true;

        var formOk = true;

        if (typeof $scope.joinRequest.unit === 'undefined') {
            $scope.errorMessage = 'Please select a unit to join';
            formOk = false;
        } else {
            groupId = $scope.joinRequest.unit;
        }

        if (UtilService.validateEmail($scope.joinRequest.email)) {
            $scope.errorMessage = 'Invalid format for email';
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

        if (!UtilService.validationDateNoFuture($scope.joinRequest.selectedDay,
                                        $scope.joinRequest.selectedMonth,
                                        $scope.joinRequest.selectedYear)) {
            $scope.errorMessage = 'Please enter a valid date (and not in the future)';
            formOk = false;
        } else {
            $scope.joinRequest.dateOfBirth = $scope.joinRequest.selectedDay +
                '-' + $scope.joinRequest.selectedMonth +
                '-' +  $scope.joinRequest.selectedYear;
        }

        if (formOk) {
            JoinRequestService.create(groupId, $scope.joinRequest).then(function () {
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
        if (typeof $scope.joinRequest.specialty !== 'undefined') {
            $scope.childUnits.forEach(function(unit) {
                if (_.findWhere(unit.parentGroups, {id: $scope.joinRequest.specialty})) {
                    $scope.units.push(unit);
                }
            });
        }
    };

    $scope.reCaptchaCallback = function(response) {
        $scope.joinRequest.captcha = response;
        $timeout(function() {
            $scope.$apply();
        });
    };

    $scope.getReCaptchaPublicKey = function() {
        return ENV.reCaptchaPublicKey;
    };
}]);
