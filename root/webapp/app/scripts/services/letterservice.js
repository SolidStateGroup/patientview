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
        }
    };
}]);
