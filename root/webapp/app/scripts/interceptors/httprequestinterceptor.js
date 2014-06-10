'use strict';

angular.module('patientviewApp').factory('HttpRequestInterceptor', ['$q','$rootScope',function ($q, $rootScope) {
    return {
        'request': function(config) {
            if (angular.isDefined($rootScope.authToken)) {
                config.headers['X-Auth-Token'] = $rootScope.authToken;
            }
            return config || $q.when(config);
        }
    };
}]);
