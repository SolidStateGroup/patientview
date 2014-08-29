'use strict';

angular.module('patientviewApp').factory('ConversationService', ['$q', 'Restangular', function ($q, Restangular) {
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
            var deferred = $q.defer();
            // GET /user/{userId}/conversations/unreadcount
            Restangular.one('user', userId).one('conversations/unreadcount').get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getAll: function (user, page, size) {
            var deferred = $q.defer();
            // GET /user/{userId}/conversations?page=0&size=5
            // returns page
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
        new: function (user, conversation) {
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
