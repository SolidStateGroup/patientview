'use strict';

angular.module('patientviewApp').controller('PasswordChangeCtrl', ['RouteService','UserService', 'AuthService',
    '$scope', '$rootScope', '$cookies', 'UtilService', 'localStorageService',
    function (RouteService,UserService,AuthService,$scope,$rootScope,$cookies,UtilService,localStorageService) {

    $scope.savePassword = function () {
        $scope.successMessage = null;
        $scope.passwordErrorMessage = null;
        if ($scope.pw !== $scope.confirmPassword) {
            $scope.passwordErrorMessage = 'The passwords do not match';
        } else {
            $scope.loading = true;

            UserService.changePassword($rootScope.loggedInUser.id, $scope.pw).then(function () {
                AuthService.getUserInformation({'token' : $rootScope.authToken}).then(function (userInformation) {
                    var user = userInformation.user;
                    delete userInformation.user;
                    user.userInformation = userInformation;

                    $rootScope.loggedInUser = user;
                    localStorageService.set('loggedInUser', user);

                    if (userInformation.mustSetSecretWord) {
                        // remove messaging link if present
                        $rootScope.loggedInUser.userInformation.groupMessagingEnabled = false;

                        // clear all routes other than set secret word
                        $rootScope.routes = [];
                        $rootScope.routes.push(RouteService.getSetSecretWordRoute());
                        localStorageService.set('routes', $rootScope.routes);
                    } else {
                        $rootScope.routes = userInformation.routes;
                        localStorageService.set('routes', userInformation.routes);
                    }

                    $scope.successMessage = 'The password has been changed';
                    $scope.loading = false;
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
        }
    };
}]);
