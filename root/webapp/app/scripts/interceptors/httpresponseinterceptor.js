'use strict';

angular.module('patientviewApp').factory('HttpResponseInterceptor', ['$q', '$rootScope', '$location',
function ($q, $rootScope, $location) {
    return {
        'responseError': function(rejection) {
            var status = rejection.status;
            var config = rejection.config;
            var method = config.method;
            var url = config.url;

           // if (status === 401) {
           //        $location.path( '/login' );
           // } else {
           //     $rootScope.error = method + ' on ' + url + ' failed with status ' + status;
           // }
            return $q.reject(rejection);
        }
    };
}]);
