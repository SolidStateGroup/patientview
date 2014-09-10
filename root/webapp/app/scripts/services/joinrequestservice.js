'use strict';

angular.module('patientviewApp').factory('JoinRequestService', ['$q', 'Restangular', 'UtilService', function ($q, Restangular, UtilService) {
    return {
        // save join request
        new: function (groupId, joinRequest) {

            joinRequest = UtilService.cleanObject(joinRequest, 'joinRequest');

            // correctly format DOB
            joinRequest.dateOfBirth = joinRequest.dateOfBirth.split('-')[2] + '-'
                + joinRequest.dateOfBirth.split('-')[1] + '-' + joinRequest.dateOfBirth.split('-')[0];

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
        getByUser: function (userId, getParameters) {
            var deferred = $q.defer();
            Restangular.one('user/' + userId).one('joinrequests').get(getParameters).then(function(successResult) {
                deferred.resolve(successResult);
            }, function (failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },

        // get join request by id
        get: function (joinRequestId) {
            var deferred = $q.defer();
            Restangular.one('joinrequest',joinRequestId).get().then(function(successResult) {
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
