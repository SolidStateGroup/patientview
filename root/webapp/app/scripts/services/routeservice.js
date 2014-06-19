'use strict';

angular.module('patientviewApp').factory('RouteService', ['$q', 'Restangular', function ($q, Restangular) {
    return {
        getDefault: function() {
            var defaultRoutes = {
                'routes': [{
                    'url': '/',
                    'templateUrl': 'views/main.html',
                    'controller': 'MainCtrl',
                    'title': 'Home',
                    'lookup': {
                        'id': 4,
                        'value': 'TOP',
                        'lookupType': {
                            'id': 2,
                            'type': 'MENU',
                            'description': 'Type of menu'
                        }
                    }
                },{
                    'url': '/login',
                    'templateUrl': 'views/login.html',
                    'controller': 'LoginCtrl',
                    'title': 'Login',
                    'lookup': {
                        'id': 5,
                        'value': 'NOT_DISPLAYED',
                        'lookupType': {
                            'id': 2,
                            'type': 'MENU',
                            'description': 'Type of menu'
                        }
                    }
                },{
                    'url': '/logout',
                    'templateUrl': 'views/logout.html',
                    'controller': 'LogoutCtrl',
                    'title': 'Log Out',
                    'lookup': {
                        'id': 5,
                        'value': 'NOT_DISPLAYED',
                        'lookupType': {
                            'id': 2,
                            'type': 'MENU',
                            'description': 'Type of menu'
                        }
                    }
                }]
            };

            return defaultRoutes;
        },
        getRoutes: function (uuid) {
            var deferred = $q.defer();
            Restangular.all('security').one('user', uuid).customGET('routes').then(function (res) {
                deferred.resolve(res);
            });
            return deferred.promise;
        }
    };
}]);
