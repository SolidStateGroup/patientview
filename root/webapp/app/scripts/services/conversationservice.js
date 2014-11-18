'use strict';

angular.module('patientviewApp').factory('ConversationService', ['$http', '$q', 'Restangular', '$rootScope',
function ($http, $q, Restangular, $rootScope) {
    return {
        get: function (conversationId) {
            var deferred = $q.defer();
            Restangular.one('conversation', conversationId).get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getUnreadConversationCount: function (userId) {
            /*var deferred = $q.defer();
            // GET /user/{userId}/conversations/unreadcount
            Restangular.one('user', userId).one('conversations/unreadcount').get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;*/

            // temporary testing non-restangular for performance
            // GET /user/{userId}/conversations/unreadcount
            var deferred = $q.defer();
            $http.get($rootScope.apiEndpoint + '/user/' + userId + '/conversations/unreadcount').success(function(successResult){
                deferred.resolve(successResult);
            }).error(function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getRecipients: function (userId, groupId) {
            var deferred = $q.defer();
            // GET /user/{userId}/conversations/recipients?groupId=123
            Restangular.one('user', userId).one('conversations/recipients')
                .get({'groupId' : groupId}).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getAll: function (user, page, size) {
            var deferred = $q.defer();
            // GET /user/{userId}/conversations?page=0&size=5
            Restangular.one('user', user.id).one('conversations').get({'page': page, 'size': size})
                .then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        addMessage: function (user, conversation, messageContent) {
            var message = {};
            message.user = {};
            message.user.id = user.id;
            message.message = messageContent;
            message.type = 'MESSAGE';

            var deferred = $q.defer();
            Restangular.one('conversation', conversation.id).all('messages').post(message)
                .then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        create: function (user, conversation) {
            var deferred = $q.defer();
            Restangular.one('user', user.id).all('conversations').post(conversation).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        addMessageReadReceipt: function (messageId, userId) {
            var deferred = $q.defer();
            Restangular.one('message', messageId).one('readreceipt', userId).put().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
