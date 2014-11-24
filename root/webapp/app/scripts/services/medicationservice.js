'use strict';

angular.module('patientviewApp').factory('MedicationService', ['$q', 'Restangular', function ($q, Restangular) {
    return {
        getByUserId: function (userId) {
            var deferred = $q.defer();
            // GET /user/{userId}/medication
            Restangular.one('user', userId).one('medication').get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getGpMedicationStatus: function (userId) {
            var deferred = $q.defer();
            // GET /user/{userId}/gpmedicinesstatus
            Restangular.one('user', userId).one('gpmedicationstatus').get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        saveGpMedicationStatus: function (userId, gpMedicationStatus) {
            var deferred = $q.defer();
            // POST /user/{userId}/gpmedicinesstatus
            Restangular.one('user', userId).one('gpmedicationstatus').customPOST(gpMedicationStatus)
            .then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
