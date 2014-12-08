'use strict';

angular.module('patientviewApp').factory('LetterService', ['$q', 'Restangular', function ($q, Restangular) {
    return {
        getByUserId: function (userId) {
            var deferred = $q.defer();
            // GET /user/{userId}/letters
            Restangular.one('user', userId).one('letters').get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        remove: function (userId, date) {
            var deferred = $q.defer();
            // DELETE /user/{userId}/letters/{date}
            Restangular.one('user', userId).one('letters', date).remove().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
