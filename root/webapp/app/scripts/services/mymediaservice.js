'use strict';

angular.module('patientviewApp').factory('MyMediaService', ['$q', 'Restangular', 'UtilService',
    function ($q, Restangular, UtilService) {
        return {
            getByUser: function (userId, limitResults, page, size) {
                var deferred = $q.defer();

                // GET /user/{userId}/news?page=0&size=5
                Restangular.one('user', userId).one('mymedia').get(
                    {
                        'sortDirection' : 'DESC',
                        'sortField' : 'created',
                        'page': page,
                        'limitResults': limitResults,
                        'size': size
                    }).then(function (successResult) {
                        deferred.resolve(successResult);
                    }, function (failureResult) {
                        deferred.reject(failureResult);
                    });
                return deferred.promise;
            },
             // remove a news link
             removeMedia: function (userId, myMediaId) {
                var deferred = $q.defer();
                // DELETE /user/{userId}/mymedia/{myMediaId}
                Restangular.one('user', userId).one('mymedia', myMediaId).remove().then(function (successResult) {
                    deferred.resolve(successResult);
                }, function (failureResult) {
                    deferred.reject(failureResult);
                });
                return deferred.promise;
            },
        };
    }]);
