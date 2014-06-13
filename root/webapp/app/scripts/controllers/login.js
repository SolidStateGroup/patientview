'use strict';

angular.module('patientviewApp').controller('LoginCtrl', ['Restangular','$scope', '$rootScope','$cookieStore','$cookies','$routeParams','$location','AuthService','MenuService','UserService',
    function (Restangular, $scope, $rootScope, $cookieStore, $cookies, $routeParams, $location,AuthService,MenuService,UserService) {
    $scope.login = function() {

        if (!$rootScope.ieTestMode) {
            var loginObject = {'username': $scope.username, 'password': $scope.password};
            //AuthService.login($.param({username: $scope.username, password: $scope.password})).then(function (authenticationResult) {
            AuthService.login(loginObject).then(function (authenticationResult) {
               // authenticationResult = {"id": 1,"authToken": "10833ACBEF5E4E04162A815D394B271B"};
                var authToken = authenticationResult.authToken;
                var user = authenticationResult.user;
                $rootScope.authToken = authToken;
                $cookieStore.put('authToken', authToken);

                // get user details, store in session
                //UserService.get(authenticationResult.id).then(function (user) {
                    $rootScope.loggedInUser = user;
                    $cookieStore.put('loggedInUser', user);

                    // get user features, store in session
                    //UserService.getFeatures(authenticationResult.id).then(function (features) {
                    //    $rootScope.features = features;
                    //    $cookieStore.put('features', features);

                        MenuService.getMenu(user.id).then(function (data) {
                            $rootScope.menu = data.routes;
                            $cookieStore.put('menu', data.routes);
                            $location.path('/dashboard');
                        });
                    //});
                //});
            });
        } else {
            var authToken = '10833ACBEF5E4E04162A815D394B271B';
            var user = {
                'id': '1',
                'username': 'exampleuser1',
                'uuid': '6b22d403-9e5f-47fd-ad78-2b49910e68d7'
            };
            var features = [
                { 'name': 'SHARINGTHOUGHTS', 'friendlyName': 'Sharing Thoughts', 'notification': '10', 'href': '/sharingthoughts' },
                { 'name': 'MESSAGING', 'friendlyName': 'Messaging', 'notification': '2', 'href': '/messaging' }
            ];
            var menu = {
                'routes': [
                    {
                        'url': '/',
                        'templateUrl': 'views/main.html',
                        'controller': 'MainCtrl',
                        'title': 'PatientView2',
                        'menu': '0'
                    },{
                        'url': '/dashboard',
                        'templateUrl': 'views/dashboard.html',
                        'controller': 'DashboardCtrl',
                        'title': 'Home',
                        'menu': '1'
                    },{
                        'url': '/login',
                        'templateUrl': 'views/login.html',
                        'controller': 'LoginCtrl',
                        'title': 'Login',
                        'menu': '0'
                    },{
                        'url': '/messages',
                        'templateUrl': 'views/messages.html',
                        'controller': 'MessagesCtrl',
                        'title': 'Messages',
                        'menu': '2'
                    },{
                        'url': '/settings',
                        'templateUrl': 'views/settings.html',
                        'controller': 'SettingsCtrl',
                        'title': 'Settings',
                        'menu': '2'
                    },{
                        'url': '/feedback',
                        'templateUrl': 'views/feedback.html',
                        'controller': 'FeedbackCtrl',
                        'title': 'Feedback',
                        'menu': '2'
                    },{
                        'url': '/help',
                        'templateUrl': 'views/help.html',
                        'controller': 'HelpCtrl',
                        'title': 'Help',
                        'menu': '2'
                    },{
                        'url': '/logout',
                        'templateUrl': 'views/logout.html',
                        'controller': 'LogoutCtrl',
                        'title': 'Log Out',
                        'menu': '2'
                    },{
                        'url': '/mydetails',
                        'templateUrl': 'views/mydetails.html',
                        'controller': 'MydetailsCtrl',
                        'title': 'My Details',
                        'menu': '1'
                    },{
                        'url': '/results',
                        'templateUrl': 'views/results.html',
                        'controller': 'ResultsCtrl',
                        'title': 'Results',
                        'menu': '1'
                    },{
                        'url': '/resultsdetail',
                        'templateUrl': 'views/resultsdetail.html',
                        'controller': 'ResultsDetailCtrl',
                        'title': 'Results Detail',
                        'menu': '0'
                    },{
                        'url': '/medicines',
                        'templateUrl': 'views/medicines.html',
                        'controller': 'MedicinesCtrl',
                        'title': 'Medicines',
                        'menu': '1'
                    },{
                        'url': '/letters',
                        'templateUrl': 'views/letters.html',
                        'controller': 'LettersCtrl',
                        'title': 'Letters',
                        'menu': '1'
                    },{
                        'url': '/sharingthoughts',
                        'templateUrl': 'views/sharingthoughts.html',
                        'controller': 'SharingthoughtsCtrl',
                        'title': 'Sharing Thoughts',
                        'menu': '1'
                    },{
                        'url': '/contact',
                        'templateUrl': 'views/contact.html',
                        'controller': 'ContactCtrl',
                        'title': 'Contact',
                        'menu': '1'
                    }
                ],
                'default':'/'
            };

            $rootScope.authToken = authToken;
            $cookieStore.put('authToken', authToken);
            $rootScope.loggedInUser = user;
            $cookieStore.put('loggedInUser', user);
            $rootScope.features = features;
            $cookieStore.put('features', features);
            $rootScope.menu = menu.routes;
            $cookieStore.put('menu', menu.routes);
        }
    };

    $scope.init = function() {
        if ($rootScope.authToken) {
            $location.path('/dashboard');
        }
    };

    $scope.init();
}]);
