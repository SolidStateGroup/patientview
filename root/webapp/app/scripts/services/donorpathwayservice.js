'use strict';

angular.module('patientviewApp').factory('DonorPathwayService', ['$q', 'Restangular', function ($q, Restangular) {
    var userId;

    return {
        setUserId(id) {
          userId = id;
        },
        getUserId() {
          return userId;
        },
        /*getByUserIdAndClass: function (userId, fhirClass) {
            var deferred = $q.defer();
            // GET /user/{userId}/documents/{fhirClass}
            Restangular.one('user', userId).one('documents', fhirClass).get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }*/
    };
}]);
