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
            // GET /user/{userId}/news?page=0&size=5
            // returns page
            Restangular.one('user', userId).one('news').get({'page': page, 'size': size}).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        new: function (newsItem) {
            var i, newsLink, newsLinks = [];
            
            for (i=0;i<newsItem.groups.length;i++) {
                newsLink = {};
                newsLink.group = {};
                newsLink.group.id = newsItem.groups[i].id;
                newsLinks.push(newsLink);
            }
            
            for (i=0;i<newsItem.roles.length;i++) {
                newsLink = {};
                newsLink.role = {};
                newsLink.role.id = newsItem.roles[i].id;
                newsLinks.push(newsLink);
            }

            newsItem.newsLinks = newsLinks;

            newsItem = UtilService.cleanObject(newsItem, 'newsItem');

            var deferred = $q.defer();
            Restangular.all('news').post(newsItem).then(function(successResult) {
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
