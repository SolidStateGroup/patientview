'use strict';

angular.module('patientviewApp').controller('LoginCtrl', ['localStorageService','$scope', '$rootScope','$routeParams','$location','AuthService','RouteService',
    function (localStorageService, $scope, $rootScope, $routeParams, $location, AuthService, RouteService) {
    $scope.login = function() {
        $scope.errorMessage = '';

        // workaround for https://github.com/angular/angular.js/issues/1460
        $scope.username = $('#username').val();
        $scope.password = $('#password').val();

        AuthService.login({'username': $scope.username, 'password': $scope.password}).then(function (authenticationResult) {

            var authToken = authenticationResult.token;
            var user = authenticationResult.user;
            $rootScope.authToken = authToken;
            localStorageService.set('authToken', authToken);

            // get user details, store in session
            $rootScope.loggedInUser = user;
            localStorageService.set('loggedInUser', user);

            RouteService.getRoutes(user.id).then(function (data) {

                if (user.changePassword) {
                    $rootScope.routes = [];
                    $rootScope.routes.push(RouteService.getChangePasswordRoute());
                    $location.path('/changepassword');
                    localStorageService.set('routes', $rootScope.routes);
                } else {
                    $rootScope.routes = data;
                    localStorageService.set('routes', data);
                    $location.path('/dashboard');
                }
            });


        }, function(result) {
            if (result.data) {
                $scope.errorMessage = ' - ' + result.data;
            } else {
                $scope.errorMessage = ' ';
            }
        });
    };

    $scope.init = function() {
        if ($rootScope.authToken) {
            $location.path('/dashboard');
        }
    };

    $scope.init();
}]);
