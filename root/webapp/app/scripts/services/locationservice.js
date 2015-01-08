'use strict';

angular.module('patientviewApp').factory('LocationService', ['$q', 'Restangular', 'UtilService', function ($q, Restangular, UtilService) {
    return {
        getAll: function () {
            var deferred = $q.defer();
            Restangular.all('location').getList().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        get: function (locationId) {
            var deferred = $q.defer();
            Restangular.one('location', locationId).get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // save location
        save: function (location) {
            location = UtilService.cleanObject(location, 'location');
            var deferred = $q.defer();
            Restangular.all('location').customPUT(location).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Remove a single location based on locationId
        remove: function (location) {
            var deferred = $q.defer();
            // DELETE /location/{locationId}
            Restangular.one('location', location.id).remove().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
