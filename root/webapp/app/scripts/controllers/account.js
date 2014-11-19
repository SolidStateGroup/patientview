'use strict';

angular.module('patientviewApp').controller('AccountCtrl', ['localStorageService', 'UserService', 'AuthService', '$scope', '$rootScope', 'UtilService',
    function (localStorageService, UserService, AuthService, $scope, $rootScope, UtilService) {

    $scope.pw ='';

    if ($rootScope.loggedInUser == null) {
        $rootScope.logout();
    }

    UserService.get($rootScope.loggedInUser.id).then(function(data) {
        $scope.userdetails = data;
        $scope.userdetails.confirmEmail = $scope.userdetails.email;
    });

    $scope.saveSettings = function () {
        // If the email field has been changed validate emails
        $scope.successMessage = null;
        $scope.errorMessage = null;
        if (!$scope.userdetails.confirmEmail) {
            $scope.errorMessage = 'Please confirm the email address';
        } else {
            // Email equal and correct
            if (($scope.userdetails.confirmEmail === $scope.userdetails.email)) {
                if (UtilService.validateEmail($scope.userdetails.email)) {
                    $scope.errorMessage = 'Invalid format for email';
                } else {
                    UserService.saveOwnSettings($scope.loggedInUser.id, $scope.userdetails).then(function () {
                        $scope.successMessage = 'The settings have been saved';
                        AuthService.getUserInformation($scope.loggedInUser.userInformation.token)
                            .then(function (userInformation) {

                            // get user information, store in session
                            var user = userInformation.user;
                            delete userInformation.user;
                            user.userInformation = userInformation;

                            $rootScope.loggedInUser = user;
                            localStorageService.set('loggedInUser', user);

                        }, function(result) {
                            if (result.data) {
                                $scope.errorMessage = result.data;
                            } else {
                                delete $scope.errorMessage;
                            }
                            $scope.loading = false;
                        });

                    }, function (result) {
                        $scope.errorMessage = 'The settings have not been saved ' + result.data;
                    });
                }
            } else {
                $scope.errorMessage = 'The emails do not match';
            }
        }
    };

    $scope.savePassword = function () {
        $scope.successMessage = null;
        $scope.passwordErrorMessage = null;
        if ($scope.pw !== $scope.userdetails.confirmPassword) {
            $scope.passwordErrorMessage = 'The passwords do not match';
        } else {
            AuthService.login({'username': $scope.userdetails.username, 'password': $scope.userdetails.currentPassword}).then(function () {
                // set the password
                $scope.userdetails.password =  $scope.pw;

                UserService.changePassword($scope.userdetails).then(function () {
                    // successfully changed user password
                    $scope.successMessage = 'The password has been saved';
                }, function () {
                    $scope.passwordErrorMessage = 'There was an error';
                });

            }, function (result) {
                if (result.data) {
                    $scope.passwordErrorMessage = 'Current password incorrect';
                } else {
                    $scope.passwordErrorMessage = ' ';
                }
            });
        }
    };
}]);