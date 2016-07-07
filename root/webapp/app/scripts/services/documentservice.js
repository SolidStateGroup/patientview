'use strict';

angular.module('patientviewApp').factory('DocumentService', ['$q', 'Restangular', function ($q, Restangular) {
    return {
        getByUserIdAndClass: function (userId, fhirClass) {
            var deferred = $q.defer();
            // GET /user/{userId}/documents/{fhirClass}
            Restangular.one('user', userId).one('documents', fhirClass).get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
