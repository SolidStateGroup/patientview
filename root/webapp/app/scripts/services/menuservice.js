'use strict';

angular.module('patientviewApp').factory('MenuService', ['$q', 'Restangular', function ($q, Restangular) {
    return {
        getDefault: function() {
            var defaultMenu = {
                'routes': [{
                    'url': '/',
                    'templateUrl': 'views/main.html',
                    'controller': 'MainCtrl',
                    'title': 'Home',
                    'menu': '1'
                },{
                    'url': '/login',
                    'templateUrl': 'views/login.html',
                    'controller': 'LoginCtrl',
                    'title': 'Login',
                    'menu': '0'
                },{
                    'url': '/logout',
                    'templateUrl': 'views/logout.html',
                    'controller': 'LogoutCtrl',
                    'title': 'Log Out',
                    'menu': '0'
                }]
            };

            return defaultMenu;
        },
        getMenu: function (uuid) {
            var deferred = $q.defer();
            Restangular.one('user', uuid).customGET('menus').then(function (res) {
                deferred.resolve(res);
            });
            return deferred.promise;
        }
    };
}]);
