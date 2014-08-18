'use strict';

angular.module('patientviewApp').factory('JoinRequestService', ['$q', 'Restangular', 'UtilService', function ($q, Restangular, UtilService) {
    return {
        // save join request
        new: function (groupId, joinRequest) {

            joinRequest = UtilService.cleanObject(joinRequest, 'joinRequest');

            var deferred = $q.defer();
            Restangular.all('group/' + groupId + '/joinRequest').customPOST(joinRequest).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },

        // lookup values for the statuses
        getStatuses: function () {

                var deferred = $q.defer();
                Restangular.all('joinrequest/statuses').getList().then(function(successResult) {
                    deferred.resolve(successResult);
                },function(failureResult) {
                    deferred.reject(failureResult);
                });
                return deferred.promise;
        },

        // get the join request relating to a new user
        getByUser: function (userId) {
            var deferred = $q.defer();
            Restangular.one('user/' + userId).all('joinrequests').getList().then(function(successResult) {
                deferred.resolve(successResult);
            }, function (failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },

        // filter results by the type
        getByType: function (userId, status) {
            var deferred = $q.defer();
            var statuses = [];
            statuses.push(status);
            Restangular.one('user', userId).all('joinrequests').getList({'status': statuses}).then(function(successResult) {
                deferred.resolve(successResult);
            }, function (failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },

        // save an existing join request
        save: function (joinRequest) {
            // PUT /group
            joinRequest = UtilService.cleanObject(joinRequest, 'joinRequest');

            var deferred = $q.defer();
            Restangular.all('joinrequest').customPUT(joinRequest).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },

        getSubmittedJoinRequestCount: function (userId) {
            var deferred = $q.defer();
            // GET /user/{userId}/conversations/unreadcount
            Restangular.one('user', userId).one('joinrequests/count').get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };

}]);
