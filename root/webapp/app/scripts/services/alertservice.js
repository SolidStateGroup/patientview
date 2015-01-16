'use strict';

angular.module('patientviewApp').factory('AlertService', ['$q', 'Restangular', function ($q, Restangular) {
    return {
        addAlert: function(userId, alert) {
            var deferred = $q.defer();
            // POST /user/{userId}/alert
            Restangular.one('user', userId).post('alert', alert).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        removeAlert: function(userId, alertId) {
            var deferred = $q.defer();
            // DELETE /user/{userId}/alerts/{alertId}
            Restangular.one('user', userId).one('alerts', alertId).remove().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        updateAlert: function(userId, alert) {
            var deferred = $q.defer();
            // PUT /user/{userId}/alert
            Restangular.one('user', userId).customPUT(alert, 'alert').then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getAlerts: function(userId, alertType) {
            var deferred = $q.defer();
            // GET /user/{userId}/alerts/{alertType}
            Restangular.one('user', userId).one('alerts', alertType).get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
