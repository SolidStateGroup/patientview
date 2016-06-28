'use strict';

angular.module('patientviewApp').factory('CodeExternalStandardService', ['$q', 'Restangular', 'UtilService', function ($q, Restangular, UtilService) {
    return {
        // save externalStandard
        save: function (externalStandard) {
            //externalStandard = UtilService.cleanObject(externalStandard, 'externalStandard');
            var deferred = $q.defer();
            // PUT /codeexternalstandards
            Restangular.all('codeexternalstandards').customPUT(externalStandard).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Remove a single CodeExternalStandard based on codeexternalStandardId
        remove: function (externalStandard) {
            var deferred = $q.defer();
            // DELETE /codeexternalstandards/{externalStandard.id}
            Restangular.one('codeexternalstandards', externalStandard.id).remove().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
