'use strict';

angular.module('patientviewApp').factory('ConversationService', ['$http', '$q', 'Restangular', 'UserService', '$rootScope',
function ($http, $q, Restangular, UserService, $rootScope) {
    return {
        addConversationUser: function (conversationId, userId) {
            var deferred = $q.defer();
            // POST /conversation/{conversationId}/conversationuser/{userId}
            Restangular.one('conversation', conversationId).one('conversationuser', userId).post()
                .then(function(successResult) {
                    deferred.resolve(successResult);
                }, function(failureResult) {
                    deferred.reject(failureResult);
                });
            return deferred.promise;
        },
        addConversationUserLabel: function (userId, conversationId, conversationLabel) {
            var deferred = $q.defer();
            // POST /user/{userId}/conversations/{conversationId}/conversationlabel/{conversationLabel}
            Restangular.one('user', userId).one('conversations', conversationId)
                .one('conversationlabel', conversationLabel).post().then(function(successResult) {
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
            // POST /conversation/{conversationId}/messages
            Restangular.one('conversation', conversation.id).all('messages').post(message)
                .then(function(successResult) {
                    deferred.resolve(successResult);
                }, function(failureResult) {
                    deferred.reject(failureResult);
                });
            return deferred.promise;
        },
        addMessageReadReceipt: function (messageId, userId) {
            var deferred = $q.defer();
            // PUT /message/{messageId}/readreceipt/{userId}
            Restangular.one('message', messageId).one('readreceipt', userId).put().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        create: function (user, conversation) {
            var deferred = $q.defer();
            // POST /user/{userId}/conversations
            Restangular.one('user', user.id).all('conversations').post(conversation).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        get: function (conversationId) {
            var deferred = $q.defer();
            // GET /conversation/{conversationId}
            Restangular.one('conversation', conversationId).get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getAll: function (user, getParameters) {
            var deferred = $q.defer();
            // GET /user/{userId}/conversations?page=0&size=5
            Restangular.one('user', user.id).one('conversations').get(getParameters).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        getGroupRecipientsByFeature: function (groupId, featureName) {
            var deferred = $q.defer();
            // GET /group/{groupId}/recipientsbyfeature/{featureName}
            Restangular.one('group', groupId).one('recipientsbyfeature', featureName).get()
                .then(function(successResult) {
                    deferred.resolve(successResult);
                }, function(failureResult) {
                    deferred.reject(failureResult);
                });
            return deferred.promise;
        },
        getRecipients: function (userId, groupId) {
            var deferred = $q.defer();
            // GET /user/{userId}/conversations/recipientsfast?groupId=123
            Restangular.one('user', userId).one('conversations/recipientsfast')
                .get({'groupId' : groupId}).then(function(successResult) {
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
        memberOfGroup: function(group) {
            var i;
            // try from group roles
            for (i=0; i<$rootScope.loggedInUser.groupRoles.length; i++) {
                if ($rootScope.loggedInUser.groupRoles[i].group.id === group.id) {
                    return true;
                }
            }
            // try from user groups (for SPECIALTY_ADMIN)
            for (i=0; i<$rootScope.loggedInUser.userInformation.userGroups.length; i++) {
                if ($rootScope.loggedInUser.userInformation.userGroups[i].id === group.id) {
                    return true;
                }
            }
            return false;
        },
        removeConversationUser: function (conversationId, userId) {
            var deferred = $q.defer();
            // DELETE /conversation/{conversationId}/conversationuser/{userId}
            Restangular.one('conversation', conversationId).one('conversationuser', userId).remove()
                .then(function(successResult) {
                    deferred.resolve(successResult);
                }, function(failureResult) {
                    deferred.reject(failureResult);
                });
            return deferred.promise;
        },
        removeConversationUserLabel: function (userId, conversationId, conversationLabel) {
            var deferred = $q.defer();
            // DELETE /user/{userId}/conversations/{conversationId}/conversationlabel/{conversationLabel}
            Restangular.one('user', userId).one('conversations', conversationId)
                .one('conversationlabel', conversationLabel).remove().then(function(successResult) {
                    deferred.resolve(successResult);
                }, function(failureResult) {
                    deferred.reject(failureResult);
                });
            return deferred.promise;
        },
        userHasMessagingFeature: function() {
            // GLOBAL_ADMIN and PATIENT both always have messaging enabled
            if (UserService.checkRoleExists('GLOBAL_ADMIN', $rootScope.loggedInUser)
                || UserService.checkRoleExists('PATIENT', $rootScope.loggedInUser) ) {
                return true;
            }

            var messagingFeatures = ['MESSAGING', 'DEFAULT_MESSAGING_CONTACT', 'UNIT_TECHNICAL_CONTACT',
                'PATIENT_SUPPORT_CONTACT', 'CENTRAL_SUPPORT_CONTACT'];

            // although IBD_SCORING_ALERTS is a messaging feature, you must have MESSAGING or another enabled as well

            for (var i = 0; i < $rootScope.loggedInUser.userInformation.userFeatures.length; i++) {
                var feature = $rootScope.loggedInUser.userInformation.userFeatures[i];
                if (messagingFeatures.indexOf(feature.name) > -1) {
                    return true;
                }
            }

            return false;
        }
    };
}]);
