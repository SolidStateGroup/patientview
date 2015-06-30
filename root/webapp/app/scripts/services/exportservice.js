'use strict';

angular.module('patientviewApp').factory('ExportService', ['$q', 'Restangular', function ($q, Restangular) {
    return {
        buildUrl : function(userId, to, from, results){
            return

        },
        // result table
        download: function(userId) {
            var deferred = $q.defer();
            //GET /user/{userId}/export/download
            Restangular.one('user', userId).one('export', 'download').get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
