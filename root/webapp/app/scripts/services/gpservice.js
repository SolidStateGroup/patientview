'use strict';
angular.module('patientviewApp').factory('GpService', ['$q', 'Restangular', function ($q, Restangular) {
    return {
        claim: function(details) {
            var deferred = $q.defer();
            // POST /gp/claim
            Restangular.all('gp/claim').customPOST(details).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        inviteGp: function(userId, patient) {
            var deferred = $q.defer();
            // POST /user/{userId}/gp/invite
            Restangular.one('user', userId).all('gp/invite').customPOST(patient).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        updateMasterTable: function () {
            var deferred = $q.defer();
            // POST /gp/updatemastertable
            Restangular.all('gp/updatemastertable').post().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        validateDetails: function(details) {
            var deferred = $q.defer();
            // POST /gp/validatedetails
            Restangular.all('gp/validatedetails').customPOST(details).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
