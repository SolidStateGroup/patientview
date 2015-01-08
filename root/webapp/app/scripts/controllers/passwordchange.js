'use strict';

angular.module('patientviewApp').controller('PasswordChangeCtrl', ['RouteService','UserService', 'AuthService',
    '$scope', '$rootScope', 'UtilService', 'localStorageService','$interval','$location',
    function (RouteService,UserService,AuthService,$scope,$rootScope,UtilService,localStorageService,$interval,$location) {

    $scope.userdetails = $rootScope.loggedInUser;

    $scope.savePassword = function () {
        $scope.successMessage = null;
        $scope.passwordErrorMessage = null;
        if ($scope.pw !== $scope.userdetails.confirmPassword) {
            $scope.passwordErrorMessage = 'The passwords do not match';
        } else {

            $scope.loading = true;

            AuthService.login({'username': $scope.userdetails.username, 'password': $scope.userdetails.currentPassword})
                .then(function (authToken) {

                // set the password
                $scope.userdetails.password = $scope.pw;

                // set the authtoken
                $rootScope.authToken = authToken;
                localStorageService.set('authToken', authToken);

                UserService.changePassword($scope.userdetails).then(function () {

                    // successfully changed user password
                    $scope.successMessage = 'The password has been changed';

                    AuthService.getUserInformation(authToken).then(function (userInformation) {

                        // get user information (securityroles, userGroups), store in session
                        var user = userInformation.user;
                        delete userInformation.user;
                        user.userInformation = userInformation;

                        $rootScope.loggedInUser = user;
                        localStorageService.set('loggedInUser', user);
                        $rootScope.routes = userInformation.routes;
                        localStorageService.set('routes', userInformation.routes);

                        $scope.loading = false;

                        /*$interval(function(){
                            $location.path("/dashboard");
                        },3000);*/

                    }, function(result) {
                        if (result.data) {
                            $scope.passwordErrorMessage = ' - ' + result.data;
                        } else {
                            $scope.passwordErrorMessage = ' ';
                        }
                        $scope.loading = false;
                    });
                }, function () {
                    $scope.passwordErrorMessage = '- There was an error';
                    $scope.loading = false;
                });

            }, function (result) {
                if (result.data) {
                    $scope.passwordErrorMessage = ' - Current password incorrect';
                } else {
                    $scope.passwordErrorMessage = ' ';
                }
                $scope.loading = false;
            });
        }
    };
}]);
