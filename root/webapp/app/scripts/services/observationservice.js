'use strict';

angular.module('patientviewApp').factory('ObservationService', ['$q', 'Restangular', function ($q, Restangular) {
    return {
        getObservation: function (uuid, typeName) {
            var deferred = $q.defer();
            Restangular.one('patient',uuid).getList('observations', {type: typeName}).then(function(res) {
                deferred.resolve(res);
            });
            return deferred.promise;
        },
        getResultTypes: function (uuid) {
            var deferred = $q.defer();
            Restangular.one('patient',uuid).getList('resulttypes').then(function(res) {
                deferred.resolve(res);
            });
            return deferred.promise;
        },
        getAll: function (userId) {
            var deferred = $q.defer();
            // GET /user/{userId}/observations
            Restangular.one('user', userId).one('observations').get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getByCode: function (userId, code) {
            var deferred = $q.defer();
            // GET /user/{userId}/observations/{code}
            Restangular.one('user', userId).one('observations').one(code).get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getSummary: function (userId) {
            var deferred = $q.defer();
            // GET /user/{userId}/observations/summary
            Restangular.one('user', userId).one('observations').one('summary').get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
