'use strict';

angular.module('patientviewApp').factory('ReviewService', ['$q', 'Restangular',
    function ($q, Restangular) {
        return {
            getPublicReviews: function () {
                var deferred = $q.defer();
                // GET /public/news?page=0&size=5
                Restangular.one('public').one('reviews').get().then(function (successResult) {
                    deferred.resolve(successResult);
                }, function (failureResult) {
                    deferred.reject(failureResult);
                });
                return deferred.promise;
            }
        };
    }]);
