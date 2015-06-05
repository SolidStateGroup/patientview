'use strict';

angular.module('patientviewApp').factory('SymptomScoreService', ['$q', 'Restangular',
function ($q, Restangular) {
    return {
        getSymptomScore: function (userId, symptomScoreId) {
            var deferred = $q.defer();
            // GET /user/{userId}/symptomscores/{symptomScoreId}
            Restangular.one('user', userId).one('symptomscores', symptomScoreId).get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getByUser: function (userId) {
            var deferred = $q.defer();
            // GET /user/{userId}/symptomscores
            Restangular.one('user', userId).one('symptomscores').getList().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
