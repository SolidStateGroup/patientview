'use strict';

angular.module('patientviewApp').controller('PasswordChangeCtrl', ['RouteService','UserService', 'AuthService', '$scope', '$rootScope', 'UtilService', 'localStorageService',
    function (RouteService,UserService,AuthService,$scope,$rootScope,UtilService, localStorageService) {

    $scope.userdetails = $rootScope.loggedInUser;

    $scope.savePassword = function () {
        $scope.successMessage = null;
        $scope.passwordErrorMessage = null;
        if ($scope.pw !== $scope.userdetails.confirmPassword) {
            $scope.passwordErrorMessage = 'The passwords do not match';
        } else {

            AuthService.login({'username': $scope.userdetails.username, 'password': $scope.userdetails.currentPassword}).then(function () {

                // set the password
                $scope.userdetails.password = $scope.pw;

                UserService.changePassword($scope.userdetails).then(function () {
                    // successfully changed user password
                    $scope.successMessage = 'The password has been changed';

                    RouteService.getRoutes($scope.userdetails.id).then(function (data) {
                            $rootScope.routes = data;
                            localStorageService.set('routes', data);
                    });

                }, function () {
                    // error
                    $scope.passwordErrorMessage = '- There was an error';
                });

            }, function (result) {
                if (result.data) {
                    $scope.passwordErrorMessage = ' - Current password incorrect';
                } else {
                    $scope.passwordErrorMessage = ' ';
                }
            });
        }

    };


}]);

