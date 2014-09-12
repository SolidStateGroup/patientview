'use strict';

angular.module('patientviewApp').factory('ObservationHeadingService', ['$q', 'Restangular', 'UtilService', function ($q, Restangular, UtilService) {
    return {
        getAll: function (getParameters) {
            var deferred = $q.defer();
            // GET /observationheading?page=0&size=5&sortDirection=ASC&sortField=code
            Restangular.one('observationheading').get(getParameters).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        get: function (observationheadingId) {
            var deferred = $q.defer();
            Restangular.one('observationheading', observationheadingId).get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // create new observation heading
        create: function (observationHeading) {
            var deferred = $q.defer();
            Restangular.all('observationheading').post(observationHeading).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // save observation heading
        save: function (inputObservationHeading) {
            var deferred = $q.defer();
            var observationHeading = UtilService.cleanObject(inputObservationHeading, 'observationHeading');
            Restangular.all('observationheading').customPUT(observationHeading).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
