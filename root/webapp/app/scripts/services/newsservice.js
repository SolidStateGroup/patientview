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
        // save code
        save: function (newsItem) {
            var deferred = $q.defer();
            var newsItem = UtilService.cleanObject(newsItem, 'newsItem');

            Restangular.all('news').customPUT(newsItem).then(function(successResult) {
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
        },
        // Add new group to news links
        addGroup: function (newsId, groupId) {
            var deferred = $q.defer();
            // PUT /news/{newsId}/group/{groupId}
            Restangular.one('news', newsId).one('group',groupId).put().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Delete group from news links
        deleteGroup: function (newsId, groupId) {
            var deferred = $q.defer();
            // DELETE /news/{newsId}/group/{groupId}
            Restangular.one('news', newsId).one('group',groupId).remove().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Add new role to news links
        addRole: function (newsId, roleId) {
            var deferred = $q.defer();
            // PUT /news/{newsId}/role/{roleId}
            Restangular.one('news', newsId).one('role',roleId).put().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // Delete role from news links
        deleteRole: function (newsId, roleId) {
            var deferred = $q.defer();
            // DELETE /news/{newsId}/role/{roleId}
            Restangular.one('news', newsId).one('role',roleId).remove().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
