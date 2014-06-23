'use strict';

angular.module('patientviewApp').factory('AuthService', ['$q', 'Restangular',
    function ($q, Restangular) {
    return {
        login: function (usernameAndPassword) {
            var deferred = $q.defer();
            Restangular.all('auth/login').post(usernameAndPassword).then(function(res) {
                deferred.resolve(res);
            }, function(res) {
                deferred.reject(res);
            });
            return deferred.promise;
        },
        logout: function (token) {
            var deferred = $q.defer();
            Restangular.all('auth/logout').customDELETE(token).then(function(successResult) {
                deferred.resolve(successResult);
            }, function (failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
