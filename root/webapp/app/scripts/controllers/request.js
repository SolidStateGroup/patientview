'use strict';

angular.module('patientviewApp').controller('RequestCtrl', ['GroupService', 'RequestService',
    'StaticDataService', '$scope', '$rootScope', 'UtilService', 'ENV', '$timeout', '$routeParams', '$location',
function (GroupService, RequestService, StaticDataService, $scope, $rootScope, UtilService, ENV, $timeout,
          $routeParams, $location) {

    var init = function() {
        $scope.request = {};
        $scope.pw = '';
        $scope.months = UtilService.generateMonths();
        $scope.years = UtilService.generateYears();
        $scope.days = UtilService.generateDays();
        $scope.request.selectedYear = '';
        $scope.request.selectedMonth = '';
        $scope.request.selectedDay = '';
        $scope.request.showunits=true;
        
        // get type of request from route parameters, if none then assume JOIN_REQUEST
        if ($routeParams.type !== undefined) {
            if ($routeParams.type === 'FORGOT_LOGIN') {
                $scope.request.type = 'FORGOT_LOGIN';
            } else {
                $location.path('/');
            }
        } else {
            $scope.request.type = 'JOIN_REQUEST';
        }
    };

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
                // need to show GP on Forgot Password request
            } else if ($scope.request.type = 'FORGOT_LOGIN' && group.id == '8'){
                $scope.specialties.push(group);
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

        // For GP id is speciality
        if ($scope.request.specialty == '8') {
            groupId = $scope.request.specialty;
        } else {
            // otherwise unit id
            if (typeof $scope.request.unit === 'undefined') {
                $scope.errorMessage = 'Please select a unit';
                formOk = false;
            } else {
                groupId = $scope.request.unit;
            }
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
                $scope.successMessage = 'Your request has been submitted';
                $scope.loading = false;
                $scope.completed = true;
            }, function (result) {
                $scope.errorMessage = 'Your request has not been submitted ' + result.data;
                $scope.loading = false;
            });
        } else {
            $scope.loading = false;
        }
    };

    $scope.refreshUnits = function() {
        $scope.units = [];
        $scope.request.showunits=false;
        $scope.request.unit = null;
        if (typeof $scope.request.specialty !== 'undefined') {

            // Hide Units if GP speciality selected
            if ($scope.request.specialty == '8') {
                $scope.request.showunits = false;
                $scope.units = [];
            } else {
                $scope.request.showunits = true;
                $scope.childUnits.forEach(function (unit) {
                    if (_.findWhere(unit.parentGroups, {id: $scope.request.specialty})) {
                        $scope.units.push(unit);
                    }
                });
            }
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
    
    init();
}]);
