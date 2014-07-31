'use strict';

angular.module('patientviewApp').factory('ContactPointService', ['$q', 'Restangular', 'UtilService', function ($q, Restangular, UtilService) {
    return {
        // save contactPoint
        save: function (contactPoint) {
            contactPoint = UtilService.cleanObject(contactPoint, 'contactPoint');
            contactPoint.contactPointType = UtilService.cleanObject(contactPoint.contactPointType, 'contactPointType');
            var deferred = $q.defer();
            Restangular.all('contactpoint').customPUT(contactPoint).then(function(successResult) {
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
            Restangular.one('contactpoint', contactPoint.id).remove().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
