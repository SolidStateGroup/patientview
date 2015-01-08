'use strict';

angular.module('patientviewApp').factory('StaticDataService', ['$q', 'Restangular',
function ($q, Restangular) {
    return {
        // get a set of lookups by type string
        getLookupsByType: function (lookupType) {
            var deferred = $q.defer();
            Restangular.all('lookupType').one(lookupType).all('lookups').getList().then(function(successResult) {
                deferred.resolve(successResult);
            },function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // get a set of lookups by type string
        getLookupByTypeAndValue: function (lookupType, lookupValue) {
            var deferred = $q.defer();
            Restangular.all('lookupType').one(lookupType).all('lookups').one(lookupValue).get().then(function(successResult) {
                deferred.resolve(successResult);
            },function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
