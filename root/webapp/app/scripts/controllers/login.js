'use strict';

angular.module('patientviewApp').controller('LoginCtrl', ['localStorageService','$scope', '$rootScope','$routeParams','$location','AuthService','RouteService',
    function (localStorageService, $scope, $rootScope, $routeParams, $location, AuthService, RouteService) {

    $scope.login = function() {
        $scope.errorMessage = '';
        $scope.loading = true;

        // workaround for https://github.com/angular/angular.js/issues/1460
        $scope.username = $('#username').val();
        $scope.password = $('#password').val();

        AuthService.login({'username': $scope.username, 'password': $scope.password}).then(function (authToken) {

            $rootScope.authToken = authToken;
            localStorageService.set('authToken', authToken);

            AuthService.getUserInformation(authToken).then(function (userInformation) {

                // get user information (securityroles, userGroups), store in session
                var user = userInformation.user;
                user.securityRoles = userInformation.securityRoles;
                user.userGroups = userInformation.userGroups;
                user.staffRoles = userInformation.staffRoles;
                user.patientRoles = userInformation.patientRoles;
                user.groupFeatures = userInformation.groupFeatures;
                user.staffFeatures = userInformation.staffFeatures;
                user.patientFeatures = userInformation.patientFeatures;

                $rootScope.loggedInUser = user;
                localStorageService.set('loggedInUser', user);

                $scope.loading = false;

                if (user.changePassword) {
                    $rootScope.routes = [];
                    $rootScope.routes.push(RouteService.getChangePasswordRoute());
                    $location.path('/changepassword');
                    localStorageService.set('routes', $rootScope.routes);
                } else {
                    $rootScope.routes = userInformation.routes;
                    localStorageService.set('routes', userInformation.routes);
                    $location.path('/dashboard');
                }

            }, function(result) {
                if (result.data) {
                    $scope.errorMessage = ' - ' + result.data;
                } else {
                    $scope.errorMessage = ' ';
                }
                $scope.loading = false;
            });
        }, function(result) {
            if (result.data) {
                $scope.errorMessage = ' - ' + result.data;
            } else {
                $scope.errorMessage = ' ';
            }
            $scope.loading = false;
        });
    };

    var init = function() {
        if ($rootScope.authToken) {
            $location.path('/dashboard');
        }
    };

    init();
}]);
