'use strict';

angular.module('patientviewApp').factory('HttpRequestInterceptor', ['$q','$rootScope', '$cookies', function ($q, $rootScope, $cookies) {
    return {
        'request': function(config) {
            if (angular.isDefined($rootScope.authToken) && $rootScope.authToken !== null) {
                config.headers['X-Auth-Token'] = $rootScope.authToken;
            } else if (angular.isDefined($cookies.authToken) && $cookies.authToken !== null) {
                config.headers['X-Auth-Token'] = $cookies.authToken;
            }

            return config || $q.when(config);
        }
    };
}]);
