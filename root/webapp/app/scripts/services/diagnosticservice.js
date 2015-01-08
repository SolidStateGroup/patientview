'use strict';

angular.module('patientviewApp').factory('DiagnosticService', ['$q', 'Restangular', function ($q, Restangular) {
    return {
        getByUserId: function (userId) {
            var deferred = $q.defer();
            // GET /user/{userId}/diagnostics
            Restangular.one('user', userId).one('diagnostics').get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
