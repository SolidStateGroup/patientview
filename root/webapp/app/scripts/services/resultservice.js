'use strict';

angular.module('patientviewApp').factory('ResultService', ['$q', 'Restangular',
    function ($q, Restangular) {
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
                // GET /observation/user/{userId}
                Restangular.one('observation').one('user', userId).get().then(function(successResult) {
                    deferred.resolve(successResult);
                }, function(failureResult) {
                    deferred.reject(failureResult);
                });
                return deferred.promise;
            },
            getByCode: function (userId, code) {
                var deferred = $q.defer();
                // GET /observation/user/{userId}/{code}
                Restangular.one('observation').one('user', userId).one(code).get().then(function(successResult) {
                    deferred.resolve(successResult);
                }, function(failureResult) {
                    deferred.reject(failureResult);
                });
                return deferred.promise;
            },
            getSummary: function (userId) {
                var deferred = $q.defer();
                // GET /observation/user/{userId}/summary
                Restangular.one('observation').one('user', userId).one('summary').get().then(function(successResult) {
                    deferred.resolve(successResult);
                }, function(failureResult) {
                    deferred.reject(failureResult);
                });
                return deferred.promise;
            }
        };
    }]);
