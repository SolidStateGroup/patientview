'use strict';

angular.module('patientviewApp').factory('ConversationService', ['$q', 'Restangular', 'UtilService', function ($q, Restangular, UtilService) {
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
        getAll: function (user, pageSize, offset) {
            var deferred = $q.defer();
            Restangular.one('user', user.id).all('conversations').getList().then(function(successResult) {
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
            Restangular.one('conversation', conversation.id).all('messages').post(message).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
