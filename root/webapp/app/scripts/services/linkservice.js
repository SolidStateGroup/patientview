'use strict';

angular.module('patientviewApp').factory('LinkService', ['$q', 'Restangular', function ($q, Restangular) {
    return {
        getAll: function () {
            var deferred = $q.defer();
            Restangular.all('link').getList().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        get: function (linkId) {
            var deferred = $q.defer();
            Restangular.one('link', linkId).get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // save link
        save: function (link) {
            var deferred = $q.defer();
            Restangular.all('link').customPUT(link).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Remove a single link based on userId
        delete: function (link) {
            var deferred = $q.defer();
            // DELETE /link/{linkId}
            Restangular.one('link', link.id).remove().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
