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
        },
        // add observation heading group (specialty specific order)
        addObservationHeadingGroup: function (observationHeadingId, groupId, panel, panelOrder) {
            var deferred = $q.defer();

            Restangular.one('observationheading', observationHeadingId)
                .one('group', groupId).one('panel', panel).one('panelorder', panelOrder).post()
                .then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // save observation heading group (specialty specific order)
        updateObservationHeadingGroup: function (observationHeadingGroup) {
            var deferred = $q.defer();

            Restangular.one('observationheadinggroup').customPUT(observationHeadingGroup).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // remove observation heading group (specialty specific order)
        removeObservationHeadingGroup: function (observationHeadingGroupId) {
            var deferred = $q.defer();

            Restangular.one('observationheadinggroup', observationHeadingGroupId)
                .remove()
                .then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
