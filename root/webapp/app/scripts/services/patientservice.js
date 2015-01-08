'use strict';

angular.module('patientviewApp').factory('PatientService', ['$q', 'Restangular',
function ($q, Restangular) {
    return {
        // Get a list of FHIR patient records based on user id
        get: function (userId, groupIds) {
            var deferred = $q.defer();
            var getParameters = {};
            getParameters.groupId = groupIds;
            // GET /patient/{userId}?groupId=1
            Restangular.one('patient').customGET(userId, getParameters).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
