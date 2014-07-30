'use strict';

angular.module('patientviewApp').factory('JoinRequestService', ['$q', 'Restangular', 'UtilService', function ($q, Restangular, UtilService) {
    return {
        // save join request
        new: function (joinRequest) {

            joinRequest = UtilService.cleanObject(joinRequest, 'joinRequest');

            var deferred = $q.defer();
            Restangular.all('joinRequest').customPOST(joinRequest).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
