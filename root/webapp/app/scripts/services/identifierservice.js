'use strict';

angular.module('patientviewApp').factory('IdentifierService', ['$q', 'Restangular', 'UtilService', function ($q, Restangular, UtilService) {
    return {
        get: function (identifierId) {
            var deferred = $q.defer();
            Restangular.one('identifier', identifierId).get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // save identifier
        save: function (identifier) {
            identifier = UtilService.cleanObject(identifier, 'identifier');
            var deferred = $q.defer();
            Restangular.all('identifier').customPUT(identifier).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Remove a single identifier based on identifierId
        remove: function (identifier) {
            var deferred = $q.defer();
            // DELETE /identifier/{identifierId}
            Restangular.one('identifier', identifier.id).remove().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
