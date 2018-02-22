'use strict';

angular.module('patientviewApp').factory('ResearchService', ['$q', 'Restangular', 'UtilService',
    function ($q, Restangular, UtilService) {
        return {
            get: function (researchId) {
                var deferred = $q.defer();
                Restangular.one('research', researchId).get().then(function (successResult) {
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
            create: function (researchStudy) {
                var i;

                researchStudy = UtilService.cleanObject(researchStudy, 'researchStudy');
                //TODO add in clean criteria

                var deferred = $q.defer();
                researchStudy.criteria = [ { 'researchStudyCriterias' : { 'gender' : 'Male' , 'fromAge' : 0 }}];

                Restangular.all('research').post(researchStudy).then(function (successResult) {
                    deferred.resolve(successResult);
                }, function (failureResult) {
                    deferred.reject(failureResult);
                });
                return deferred.promise;
            },
            // save code
            save: function (researchStudy) {
                var deferred = $q.defer();
                researchStudy = UtilService.cleanObject(researchStudy, 'researchStudy');

                Restangular.all('news').customPUT(researchStudy).then(function (successResult) {
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
            addGroupAndRole: function (researchStudyId, groupId, roleId) {
                var deferred = $q.defer();
                // PUT /group/{groupId}/role/{roleId}/news/{newsId}
                Restangular.one('group', groupId).one('role', roleId).one('news', researchStudyId).put().then(function (successResult) {
                    deferred.resolve(successResult);
                }, function (failureResult) {
                    deferred.reject(failureResult);
                });
                return deferred.promise;
            },
            // remove a news link
            removeNewsLink: function (researchStudyId, newsLinkId) {
                var deferred = $q.defer();
                // DELETE /news/{researchStudyId}/newslinks/{newsLinkId}
                Restangular.one('news', researchStudyId).one('newslinks', newsLinkId).remove().then(function (successResult) {
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
