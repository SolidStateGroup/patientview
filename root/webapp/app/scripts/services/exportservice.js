'use strict';

angular.module('patientviewApp').factory('ExportService', ['$q', 'Restangular', function ($q, Restangular) {
    return {
        buildUrl : function(userId, to, from, results){
            return

        },
        // result table
        download: function(userId, resultsParamaters) {
            var deferred = $q.defer();
            //GET /user/{userId}/export/download
            Restangular.one('user', userId).one('code').one('export', 'download').get(resultsParamaters)
                .then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
