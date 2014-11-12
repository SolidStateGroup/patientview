'use strict';

angular.module('patientviewApp').factory('AuditService', ['$q', 'Restangular', 'UtilService', function ($q, Restangular, UtilService) {
    return {
        getAll: function (getParameters) {
            var deferred = $q.defer();
            // GET /audit?filterText=xxx&groupIds=123&page=0&size=20&sortDirection=DESC&sortField=date
            Restangular.one('audit').get(getParameters).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
