'use strict';

angular.module('patientviewApp').factory('ResearchService', ['$q', 'Restangular', 'UtilService',
    function ($q, Restangular, UtilService) {
        return {
            get: function (researchId) {
                var deferred = $q.defer();
                Restangular.one('news', newsId).get().then(function (successResult) {
                    deferred.resolve(successResult);
                }, function (failureResult) {
                    deferred.reject(failureResult);
                });
                return deferred.promise;
            },
            getByUser: function (userId, newsType, limitResults, page, size) {
                var deferred = $q.defer();

                // GET /user/{userId}/news?page=0&size=5
                Restangular.one('user', userId).one('research').get(
                    {
                        'page': page,
                        'newsType': newsType,
                        'limitResults': limitResults,
                        'size': size
                    }).then(function (successResult) {
                        deferred.resolve(successResult);
                    }, function (failureResult) {
                        deferred.reject(failureResult);
                    });
                return deferred.promise;
            },
            getAll: function (userId) {
                var deferred = $q.defer();

                // GET /research
                Restangular.one('user', userId).one('research').get(
                    {
                        'page': page,
                        'newsType': newsType,
                        'limitResults': limitResults,
                        'size': size
                    }).then(function (successResult) {
                    deferred.resolve(successResult);
                }, function (failureResult) {
                    deferred.reject(failureResult);
                });
                return deferred.promise;
            },
            create: function (newsItem) {
                var i;

                newsItem = UtilService.cleanObject(newsItem, 'newsItem');
                for (i = 0; i < newsItem.newsLinks.length; i++) {
                    newsItem.newsLinks[i].group = UtilService.cleanObject(newsItem.newsLinks[i].group, 'group');
                    newsItem.newsLinks[i].role = UtilService.cleanObject(newsItem.newsLinks[i].role, 'role');
                }

                var deferred = $q.defer();
                Restangular.all('news').post(newsItem).then(function (successResult) {
                    deferred.resolve(successResult);
                }, function (failureResult) {
                    deferred.reject(failureResult);
                });
                return deferred.promise;
            },
            // save code
            save: function (newsItem) {
                var deferred = $q.defer();
                newsItem = UtilService.cleanObject(newsItem, 'newsItem');

                Restangular.all('news').customPUT(newsItem).then(function (successResult) {
                    deferred.resolve(successResult);
                }, function (failureResult) {
                    deferred.reject(failureResult);
                });
                return deferred.promise;
            },
            remove: function (news) {
                var deferred = $q.defer();
                // DELETE /news/{newsId}
                Restangular.one('news', news.id).remove().then(function (successResult) {
                    deferred.resolve(successResult);
                }, function (failureResult) {
                    deferred.reject(failureResult);
                });
                return deferred.promise;
            },
            // Add new group and role to news links
            addGroupAndRole: function (newsItemId, groupId, roleId) {
                var deferred = $q.defer();
                // PUT /group/{groupId}/role/{roleId}/news/{newsId}
                Restangular.one('group', groupId).one('role', roleId).one('news', newsItemId).put().then(function (successResult) {
                    deferred.resolve(successResult);
                }, function (failureResult) {
                    deferred.reject(failureResult);
                });
                return deferred.promise;
            },
            // remove a news link
            removeNewsLink: function (newsItemId, newsLinkId) {
                var deferred = $q.defer();
                // DELETE /news/{newsItemId}/newslinks/{newsLinkId}
                Restangular.one('news', newsItemId).one('newslinks', newsLinkId).remove().then(function (successResult) {
                    deferred.resolve(successResult);
                }, function (failureResult) {
                    deferred.reject(failureResult);
                });
                return deferred.promise;
            },
            // Add new role to news links
            addRole: function (newsId, roleId) {
                var deferred = $q.defer();
                // PUT /news/{newsId}/role/{roleId}
                Restangular.one('news', newsId).one('role', roleId).put().then(function (successResult) {
                    deferred.resolve(successResult);
                }, function (failureResult) {
                    deferred.reject(failureResult);
                });
                return deferred.promise;
            }
        };
    }]);
