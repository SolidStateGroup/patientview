'use strict';

angular.module('patientviewApp').controller('LoginCtrl', ['localStorageService','$scope', '$rootScope','$routeParams','$location','AuthService','RouteService',
    function (localStorageService, $scope, $rootScope, $routeParams, $location, AuthService, RouteService) {

    $scope.login = function() {
        $scope.errorMessage = '';
        $scope.showGpLoginMessage = false;
        $scope.loading = true;
        $scope.loadingMessage = 'Logging In';

        // workaround for https://github.com/angular/angular.js/issues/1460
        $scope.username = $('#username').val();
        $scope.password = $('#password').val();

        AuthService.login({'username': $scope.username, 'password': $scope.password}).then(function (authToken) {

            $rootScope.authToken = authToken;
            localStorageService.set('authToken', authToken);
            $scope.loadingMessage = 'Loading Your Information';

            AuthService.getUserInformation(authToken).then(function (userInformation) {

                // get user information (securityroles, userGroups), store in session
                var user = userInformation.user;
                delete userInformation.user;
                user.userInformation = userInformation;

                $rootScope.loggedInUser = user;
                localStorageService.set('loggedInUser', user);
                $scope.loading = false;

                if (userInformation.routes !== undefined && userInformation.routes.length) {
                    if (user.changePassword) {
                        $rootScope.routes = [];
                        $rootScope.routes.push(RouteService.getChangePasswordRoute());
                        localStorageService.set('routes', $rootScope.routes);
                        
                        // manually call buildroute, ios fix
                        $rootScope.buildRoute();
                        
                        $location.path('/changepassword');
                    } else {
                        $rootScope.routes = userInformation.routes;
                        localStorageService.set('routes', userInformation.routes);
                        
                        // manually call buildroute, ios fix
                        $rootScope.buildRoute();
                        
                        $location.path('/dashboard');
                    }
                } else {
                    // error getting routes
                    alert('Error retrieving routes, please contact PatientView support');
                    $location.path('/logout');
                }

                $rootScope.startTimers();
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

            var username = $scope.username;

            if (username.length >= 3
                && username.substring(username.length - 3, username.length).toUpperCase() == '-GP') {
                $scope.showGpLoginMessage = true;
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
