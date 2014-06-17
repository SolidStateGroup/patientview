'use strict';

angular.module('patientviewApp').controller('LoginCtrl', ['Restangular','localStorageService','$timeout','$scope', '$rootScope','$cookieStore','$cookies','$routeParams','$location','AuthService','RouteService',
    function (Restangular, localStorageService, $timeout, $scope, $rootScope, $cookieStore, $cookies, $routeParams, $location,AuthService,RouteService) {
    $scope.login = function() {
        $scope.errorMessage = '';
        AuthService.login({'username': $scope.username, 'password': $scope.password}).then(function (authenticationResult) {

            var authToken = authenticationResult.token;
            var user = authenticationResult.user;
            $rootScope.authToken = authToken;
            //$cookieStore.put('authToken', authToken);
            localStorageService.set('authToken', authToken);

            // get user details, store in session
            $rootScope.loggedInUser = user;
            //$cookieStore.put('loggedInUser', user);
            localStorageService.set('loggedInUser', user);

            RouteService.getRoutes(user.id).then(function (data) {
                $rootScope.routes = data;
                //$cookieStore.put('routes', data);
                localStorageService.set('routes', data);
                $location.path('/dashboard');
            });
        }, function(result) {
            if (result.data) {
                $scope.errorMessage = result.data;
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
