'use strict';

angular.module('patientviewApp').controller('VerifyCtrl', ['$scope', '$routeParams', 'UserService',
function ($scope, $routeParams, UserService) {
    UserService.verify($routeParams.userId, $routeParams.verificationCode).then(function () {
        $scope.message = 'Verification Success, please log in';
    }, function () {
        // failure to verify
        $scope.message = 'Failed to verify';
    });
}]);
