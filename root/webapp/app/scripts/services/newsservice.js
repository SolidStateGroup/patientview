'use strict';

angular.module('patientviewApp').factory('NewsService', ['$q', 'Restangular', 'UtilService', function ($q, Restangular, UtilService) {
    return {
        get: function (newsId) {
            var deferred = $q.defer();
            Restangular.one('news', newsId).get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getByUser: function (userId, page, size) {
            var deferred = $q.defer();
            // GET /user/{userId}/newss?page=0&size=5
            // returns page
            Restangular.one('user', userId).one('news').get({'page': page, 'size': size}).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        new: function (news) {
            var deferred = $q.defer();
            Restangular.all('news').post(news).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        delete: function (news) {
            var deferred = $q.defer();
            // DELETE /news/{newsId}
            Restangular.one('news', news.id).remove().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
