'use strict';

angular.module('patientviewApp').factory('DiagnosisService', ['$q', 'Restangular', function ($q, Restangular) {
    return {
        addStaffEntered: function (userId, code) {
            var deferred = $q.defer();
            // POST /user/{userId}/diagnosis/{code}/staffentered
            Restangular.one('user', userId).one('diagnosis', code).one('staffentered').post().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        addPatientEntered: function (userId, code) {
            var deferred = $q.defer();
            // POST /user/{userId}/diagnosis/{code}/patiententered
            Restangular.one('user', userId).one('diagnosis', code).post('patiententered').then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        addMultiplePatientEntered: function (userId, codes) {
            var deferred = $q.defer();
            // POST /user/{userId}/diagnosis/patiententered
            Restangular.one('user', userId).one('diagnosis/patiententered').customPOST(codes).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getPatientEntered: function (userId) {
            var deferred = $q.defer();
            // GET /user/{userId}/diagnosis/patiententered
            Restangular.one('user', userId).one('diagnosis/patiententered').get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getStaffEntered: function (userId) {
            var deferred = $q.defer();
            // GET /user/{userId}/diagnosis/staffentered
            Restangular.one('user', userId).one('diagnosis/staffentered').get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        removePatientEntered: function (userId, code) {
            var deferred = $q.defer();
            // DELETE /user/{userId}/diagnosis/{code}/patiententered
            Restangular.one('user', userId).one('diagnosis', code).one('patiententered').remove().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        removeStaffEntered: function (userId) {
            var deferred = $q.defer();
            // DELETE /user/{userId}/diagnosis/staffentered
            Restangular.one('user', userId).one('diagnosis/staffentered').remove().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
