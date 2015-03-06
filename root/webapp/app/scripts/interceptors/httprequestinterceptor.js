'use strict';

angular.module('patientviewApp').factory('HttpRequestInterceptor', ['$q','$rootScope', '$cookies', function ($q, $rootScope, $cookies) {
    return {
        'request': function(config) {
            if (angular.isDefined($rootScope.authToken)) {
                config.headers['X-Auth-Token'] = $rootScope.authToken;
            } else if (angular.isDefined($cookies.authToken)) {
                config.headers['X-Auth-Token'] = $cookies.authToken;
            }

            return config || $q.when(config);
        }
    };
}]);
