'use strict';

angular.module('patientviewApp').factory('HttpResponseInterceptor', ['$q', '$rootScope', '$location',
function ($q, $rootScope, $location) {
    return {
        'responseError': function(rejection) {
            var status = rejection.status;

            if (status === 401) {
                // http UNAUTHORIZED, log out user
                $rootScope.logout();
            }
            return $q.reject(rejection);
        }
    };
}]);
