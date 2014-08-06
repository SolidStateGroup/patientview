'use strict';

angular.module('patientviewApp').factory('ConversationService', ['$q', 'Restangular', 'UtilService', function ($q, Restangular, UtilService) {
    return {
        getAll: function (user, pageSize, offset) {
            var deferred = $q.defer();
            Restangular.one('user', user.id).all('conversations').getList().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
