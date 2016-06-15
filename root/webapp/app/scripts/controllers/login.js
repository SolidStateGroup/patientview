'use strict';

angular.module('patientviewApp').controller('LoginCtrl', ['localStorageService', '$scope', '$rootScope', '$routeParams',
    '$location', '$cookies', 'AuthService', 'RouteService', 'UtilService',
    function (localStorageService, $scope, $rootScope, $routeParams, $location, $cookies, AuthService, RouteService, UtilService) {

        var getUserInformation = function(userToken) {
            AuthService.getUserInformation(userToken).then(function (userInformation) {
                var authToken = userInformation.token;
                $rootScope.authToken = authToken;
                localStorageService.set('authToken', authToken);
                $cookies.authToken = authToken;

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

                        if (userInformation.mustSetSecretWord) {
                            $location.path('/setsecretword');
                        } else {
                            $location.path('/dashboard');
                        }
                    }
                } else {
                    // error getting routes
                    alert('Error retrieving routes, please contact PatientView support');
                    $location.path('/logout');
                }

                $rootScope.startTimers();
            }, function (result) {
                if (result.data) {
                    $scope.errorMessage = ' - ' + result.data;
                } else {
                    $scope.errorMessage = ' ';
                }
                $scope.loading = false;
            });
        };

        $scope.login = function () {
            delete $scope.timeout;
            $scope.errorMessage = '';
            $scope.showGpLoginMessage = false;
            $scope.loading = true;
            $scope.loadingMessage = 'Logging In';

            // workaround for https://github.com/angular/angular.js/issues/1460
            $scope.username = $('#username').val();
            $scope.password = $('#password').val();

            // completely clear local data
            $rootScope.removeAllClientData();

            AuthService.login({'username': $scope.username, 'password': $scope.password}).then(function (userToken) {
                delete $scope.checkSecretWord;

                if (userToken.checkSecretWord) {
                    $scope.checkSecretWord = true;
                    $scope.secretWordIndexes = userToken.secretWordIndexes;
                    $scope.secretWordChoices = {};
                    $scope.secretWordToken = userToken.secretWordToken;
                    $scope.alphabet = UtilService.generateAlphabet();
                    $scope.loading = false;
                } else {
                    $scope.loadingMessage = 'Loading Your Information';
                    getUserInformation(userToken);
                }
            }, function (result) {
                if ($scope.keyPressCount < 3 &&
                    $scope.username.length > 1 &&
                    $scope.password.length > 1 &&
                    result.data == "Incorrect username or password") {
                    $scope.keyPressCount = 0;
                } else{
                    $scope.keyPressCount = 3;
                }

                if (result.data) {
                    $scope.errorMessage = ' - ' + result.data;
                } else {
                    $scope.errorMessage = ' ';
                }

                var username = $scope.username;

                if (username.length >= 3
                    && username.substring(username.length - 3, username.length).toUpperCase() == '-GP') {
                    //$scope.showGpLoginMessage = true;
                }

                $scope.loading = false;
            });
        };

        $scope.loginWithSecretWord = function () {
            delete $scope.errorMessage;
            $scope.loading = true;
            $scope.loadingMessage = 'Logging In';

            var userToken = {};
            userToken.secretWordToken = $scope.secretWordToken;
            userToken.secretWordChoices = $scope.secretWordChoices;

            getUserInformation(userToken);
        };

        var init = function () {
            $scope.keyPressCount = 0;

            if ($rootScope.authToken) {
                $location.path('/dashboard');
            }

            $(document).keypress(function (e) {
                //Log that a key has been pressed
                $scope.keyPressCount++;
            });

            $scope.timeout = $routeParams.timeout;
        };

        init();
    }]);
