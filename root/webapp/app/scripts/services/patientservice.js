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
        },
        // Get IBD patient management given user, group, identifier id
        getPatientManagement: function (userId, groupId, identifierId) {
            var deferred = $q.defer();
            // GET /patientmanagement/{userId}/group/{groupId}/identifier/{identifierId}
            Restangular.one('patientmanagement', userId).one('group', groupId).one('identifier', identifierId)
                .get().then(function(successResult) {
                    deferred.resolve(successResult);
                }, function(failureResult) {
                    deferred.reject(failureResult);
                });
            return deferred.promise;
        },
        // Get list of IBD patient management lookup types, for use in UI
        getPatientManagementLookupTypes: function () {
            var deferred = $q.defer();
            // GET /patientmanagement/lookuptypes
            Restangular.all('patientmanagement/lookuptypes').getList().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Get list of IBD patient management diagnoses (limited), for use in UI
        getPatientManagementDiagnoses: function () {
            var deferred = $q.defer();
            // GET /patientmanagement/diagnoses
            Restangular.all('patientmanagement/diagnoses').getList().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Save IBD patient management given user, group, identifier id
        savePatientManagement: function (userId, groupId, identifierId, patientManagement) {
            var deferred = $q.defer();
            // POST /patientmanagement/{userId}/group/{groupId}/identifier/{identifierId}
            Restangular.one('patientmanagement', userId).one('group', groupId).one('identifier', identifierId)
                .customPOST(patientManagement).then(function(successResult) {
                    deferred.resolve(successResult);
                }, function(failureResult) {
                    deferred.reject(failureResult);
                });
            return deferred.promise;
        },
        // Save surgeries (Encounters) in IBD patient management given user, group, identifier id
        savePatientManagementSurgeries: function (userId, groupId, identifierId, patientManagement) {
            var deferred = $q.defer();
            // POST /patientmanagement/{userId}/group/{groupId}/identifier/{identifierId}
            Restangular.one('patientmanagement', userId).one('group', groupId).one('identifier', identifierId)
                .one('surgeries').customPOST(patientManagement).then(function(successResult) {
                    deferred.resolve(successResult);
                }, function(failureResult) {
                    deferred.reject(failureResult);
                });
            return deferred.promise;
        },
        // Validate IBD patient management
        validatePatientManagement: function (patientManagement) {
            var deferred = $q.defer();
            // POST /patientmanagement/validate
            Restangular.one('patientmanagement/validate').customPOST(patientManagement).then(function(successResult) {
                    deferred.resolve(successResult);
                }, function(failureResult) {
                    deferred.reject(failureResult);
                });
            return deferred.promise;
        }
    };
}]);
