'use strict';

angular.module('patientviewApp').factory('ObservationHeadingService', ['$q', 'Restangular', 'UtilService', function ($q, Restangular, UtilService) {
    return {
        // result table
        getAvailableObservationHeadings: function(userId) {
            var deferred = $q.defer();
            // GET /user/{userId}/availableobservationheadings
            Restangular.one('user', userId).customGET('availableobservationheadings').then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getSavedObservationHeadings: function(userId) {
            var deferred = $q.defer();
            // GET /user/{userId}/savedobservationheadings
            Restangular.one('user', userId).customGET('savedobservationheadings').then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        saveObservationHeadingSelection: function(userId, codes) {
            var deferred = $q.defer();
            // GET /user/{userId}/saveobservationheadingselection
            Restangular.one('user', userId).post('saveobservationheadingselection', codes).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // result alerts
        getAvailableAlertObservationHeadings: function(userId) {
            var deferred = $q.defer();
            // GET /user/{userId}/availablealertobservationheadings
            Restangular.one('user', userId).customGET('availablealertobservationheadings').then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        addAlertObservationHeading: function(userId, alertObservationHeading) {
            var deferred = $q.defer();
            // POST /user/{userId}/alertobservationheading
            Restangular.one('user', userId).post('alertobservationheading', alertObservationHeading).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getAlertObservationHeadings: function(userId) {
            var deferred = $q.defer();
            // GET /user/{userId}/alertobservationheadings
            Restangular.one('user', userId).customGET('alertobservationheadings').then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
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
        getResultClusters: function () {
            var deferred = $q.defer();
            // GET /resultclusters
            Restangular.one('resultclusters').get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // create new observation heading
        create: function (observationHeading) {
            observationHeading = UtilService.cleanObject(observationHeading, 'observationHeading');

            for (var i=0;i<observationHeading.observationHeadingGroups.length;i++) {
                observationHeading.observationHeadingGroups[i]
                    = UtilService.cleanObject(observationHeading.observationHeadingGroups[i], 'observationHeadingGroup');
                delete observationHeading.observationHeadingGroups[i].id;
            }

            var deferred = $q.defer();
            Restangular.all('observationheading').post(observationHeading).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // save observation heading
        save: function (observationHeading) {
            var deferred = $q.defer();

            for(var i=0;i<observationHeading.observationHeadingGroups.length;i++) {
                observationHeading.observationHeadingGroups[i]
                    = UtilService.cleanObject(observationHeading.observationHeadingGroups[i], 'observationHeadingGroup');
            }

            Restangular.all('observationheading')
                .customPUT(UtilService.cleanObject(observationHeading, 'observationHeading'))
                .then(function(successResult) {
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
