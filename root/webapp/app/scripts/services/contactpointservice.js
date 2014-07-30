'use strict';

angular.module('patientviewApp').factory('ContactPointService', ['$q', 'Restangular', 'UtilService', function ($q, Restangular, UtilService) {
    return {
        getAll: function () {
            var deferred = $q.defer();
            Restangular.all('contactPoint').getList().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        get: function (contactPointId) {
            var deferred = $q.defer();
            Restangular.one('contactPoint', contactPointId).get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // save contactPoint
        save: function (contactPoint) {

            contactPoint = UtilService.cleanObject(contactPoint, 'contactPoint');

            var deferred = $q.defer();
            Restangular.all('contactPoint').customPUT(contactPoint).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Remove a single contactPoint based on contactPointId
        delete: function (contactPoint) {
            var deferred = $q.defer();
            // DELETE /contactPoint/{contactPointId}
            Restangular.one('contactPoint', contactPoint.id).remove().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
