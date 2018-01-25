'use strict';

angular.module('patientviewApp').controller('ForgottenPasswordCtrl', ['RouteService','AuthService', '$scope',
    'ENV', '$timeout',
function (RouteService, AuthService, $scope, ENV, $timeout) {
    $scope.credentials = {};

    $scope.submit = function () {
        AuthService.forgottenPassword($scope.credentials).then(function () {
            // successfully changed user password
            $scope.successMessage = 'Your new password has been sent to your email address. When you receive ' +
                'it you can use it to log on. After logging on you will be asked to change your password.';
        }, function (failure) {
            if (failure.status === 404) {
                $scope.errorMessage = 'Error: The account could not be found';
            } else {
                $scope.errorMessage = 'Error: ' + failure.data;
            }
        });
    };

    $scope.reCaptchaCallback = function(response) {
        $scope.credentials.captcha = response;
        $timeout(function() {
            $scope.$apply();
        });
    };

    $scope.getReCaptchaPublicKey = function() {
        return ENV.reCaptchaPublicKey;
    };

}]);
