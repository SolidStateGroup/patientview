'use strict';

angular.module('patientviewApp').factory('RequestService', ['$q', 'Restangular', 'UtilService', function ($q, Restangular, UtilService) {
    return {
        // save request
        create: function (groupId, request) {
            request = UtilService.cleanObject(request, 'request');
            request.groupId = groupId;

            // correctly format DOB
            request.dateOfBirth = request.dateOfBirth.split('-')[2] + '-'
                + request.dateOfBirth.split('-')[1] + '-' + request.dateOfBirth.split('-')[0];

            var deferred = $q.defer();
            // POST /public/request
            Restangular.all('public/request').customPOST(request).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },

        // complete relevant SUBMITTED requests
        complete: function () {
            var deferred = $q.defer();
            // POST /request/complete
            Restangular.all('request/complete').post().then(function(successResult) {
                deferred.resolve(successResult);
            }, function (failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },

        // get request by id
        get: function (requestId) {
            var deferred = $q.defer();
            // GET /request/{requestId}
            Restangular.one('request', requestId).get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function (failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },

        // get the request relating to a new user
        getByUser: function (userId, getParameters) {
            var deferred = $q.defer();
            // GET /user/{userId}/requests
            Restangular.one('user/' + userId).one('requests').get(getParameters).then(function(successResult) {
                deferred.resolve(successResult);
            }, function (failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },

        getSubmittedRequestCount: function (userId) {
            var deferred = $q.defer();
            // GET /user/{userId}/conversations/unreadcount
            Restangular.one('user', userId).one('requests/count').get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },

        // lookup values for the statuses
        getStatuses: function () {
            var deferred = $q.defer();
            Restangular.all('request/statuses').getList().then(function(successResult) {
                deferred.resolve(successResult);
            },function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },

        // save an existing request
        save: function (request) {
            // PUT /request
            request = UtilService.cleanObject(request, 'request');
            var deferred = $q.defer();
            Restangular.all('request').customPUT(request).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };

}]);
