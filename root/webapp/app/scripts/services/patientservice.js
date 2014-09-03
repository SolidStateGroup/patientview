'use strict';

angular.module('patientviewApp').factory('PatientService', ['$q', 'Restangular',
function ($q, Restangular) {
    return {
        // Get a list of FHIR patient records based on user id
        get: function (userId) {
            var deferred = $q.defer();
            // GET /patient/{userId}
            Restangular.one('patient', userId).get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
