'use strict';

angular.module('patientviewApp').factory('HttpResponseInterceptor', ['$q', '$rootScope', function ($q, $rootScope) {
    return {
        'responseError': function(rejection) {

            // http UNAUTHORIZED, log out user
            if (rejection.status === 401) {
                $rootScope.logout();
            }

            return $q.reject(rejection);
        }
    };
}]);
