'use strict';

angular.module('patientviewApp').controller('VerifyCtrl', ['$scope', '$routeParams', 'UserService',
function ($scope, $routeParams, UserService) {
    if ($routeParams.verificationCode !== undefined) {
        UserService.verify($routeParams.userId, $routeParams.verificationCode).then(function () {
            $scope.message = 'Verification successful, please Sign In using the button above';
        }, function () {
            $scope.message = 'Failed to verify email';
        });
    } else {
        $scope.message = 'Failed to verify email';
    }
}]);
