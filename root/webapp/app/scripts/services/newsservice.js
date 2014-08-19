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
        getPublicNews: function (page, size) {
            var deferred = $q.defer();
            // GET /news/public?page=0&size=5
            // returns page
            Restangular.one('news').one('public').get({'page': page, 'size': size}).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getNewsLinksFromGroupsRoles: function (groups, roles) {
            var i, newsLink, newsLinks = [];

            for (i=0;i<groups.length;i++) {
                newsLink = {};
                newsLink.group = {};
                newsLink.group.id = groups[i].id;
                newsLinks.push(newsLink);
            }

            for (i=0;i<roles.length;i++) {
                newsLink = {};
                newsLink.role = {};
                newsLink.role.id = roles[i].id;
                newsLinks.push(newsLink);
            }

            return newsLinks;
        },
        new: function (newsItem) {
            var i;

            //newsItem.newsLinks = this.getNewsLinksFromGroupsRoles(newsItem.groups, newsItem.roles);
            newsItem = UtilService.cleanObject(newsItem, 'newsItem');
            for (i=0;i<newsItem.newsLinks.length;i++) {
                newsItem.newsLinks[i].group = UtilService.cleanObject(newsItem.newsLinks[i].group, 'group');
                newsItem.newsLinks[i].role = UtilService.cleanObject(newsItem.newsLinks[i].role, 'role');
            }

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
        // Add new group and role to news links
        addGroupAndRole: function (newsItemId, groupId, roleId) {
            var deferred = $q.defer();
            // PUT /group/{groupId}/role/{roleId}/news/{newsId}
            Restangular.one('group',groupId).one('role', roleId).one('news', newsItemId).put().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // remove a news link
        removeNewsLink: function (newsItemId, newsLinkId) {
            var deferred = $q.defer();
            // DELETE /news/{newsItemId}/newslinks/{newsLinkId}
            Restangular.one('news',newsItemId).one('newslinks', newsLinkId).remove().then(function(successResult) {
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
