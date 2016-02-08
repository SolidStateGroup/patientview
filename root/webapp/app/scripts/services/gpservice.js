'use strict';
angular.module('patientviewApp').factory('GpService', ['$q', 'Restangular', function ($q, Restangular) {
    return {
        updateMasterTable: function () {
            var deferred = $q.defer();
            Restangular.all('gp/updatemastertable').post().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        validateDetails: function(details) {
            var deferred = $q.defer();
            Restangular.all('gp/validatedetails').customPOST(details).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
