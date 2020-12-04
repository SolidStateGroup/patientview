'use strict';
var ChangeConversationRecipientsModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', '$timeout',
    'GroupService', 'ConversationService', 'conversation',
    function ($scope, $rootScope, $modalInstance, $timeout, GroupService, ConversationService, conversation) {

        var init = function() {
            delete $scope.errorMessage;
            $scope.editConversation = conversation;
            $scope.conversationGroups = [];

            GroupService.getMessagingGroupsForUser($scope.loggedInUser.id).then(function(successResult) {
                for (var i = 0; i < successResult.length; i++) {
                    if (successResult[i].code !== 'Generic') {
                        if (ConversationService.memberOfGroup(successResult[i])) {
                            successResult[i].name = successResult[i].name + ' (member)';
                        }
                        $scope.conversationGroups.push(successResult[i]);
                    }
                }
            }, function(failResult) {
                $scope.errorMessage = failResult.data;
            });
        };

        $scope.cancel = function () {
            delete $scope.errorMessage;
            $modalInstance.dismiss('cancel');
        };

        $scope.selectGroup = function(conversation, groupId) {
            $scope.modalLoading = true;
            $scope.recipientsExist = false;
            delete $scope.errorMessage;

            ConversationService.getRecipients($scope.loggedInUser.id, groupId).then(function (recipientOptions) {
                var element = document.getElementById('conversation-add-recipient');
                if (element !== null) {
                    element.parentNode.removeChild(element);
                }

                $('#recipient-select-container').html(recipientOptions);

                $('#select-recipient').selectize({
                    sortField: 'text',
                    onChange: function(userId) {
                        if (userId.length) {
                            addConversationUser(userId);
                        }
                    }
                });

                $scope.recipientsExist = true;
                $scope.modalLoading = false;
            }, function (failureResult) {
                if (failureResult.status === 404) {
                    $scope.modalLoading = false;
                } else {
                    $scope.modalLoading = false;
                    alert('Error loading message recipients');
                }
            });
        };

        var addConversationUser = function (userId) {
            delete $scope.errorMessage;
            var found = false;
            var conversation = $scope.editConversation;

            // check not already added
            for (var i = 0; i < conversation.conversationUsers.length; i++) {
                // need to cast string to number using == not === for id
                if (conversation.conversationUsers[i].user.id == userId) {
                    found = true;
                }
            }

            if (!found && userId !== '') {
                ConversationService.addConversationUser(conversation.id, userId).then(function() {
                    ConversationService.get(conversation.id).then(function(successResult) {
                        $scope.editConversation = successResult;
                    }, function() {
                        alert('Error getting conversation');
                    });
                }, function(err) {
                    if (err.data) {
                        $scope.errorMessage = ' - ' + err.data;
                    } else {
                        $scope.errorMessage = ' ';
                    }
                });
            }

            $timeout(function() {
                $scope.$apply();
            });
        };
        
        $scope.addConversationUser = function (conversation) {
            delete $scope.errorMessage;
            var userId = $('#conversation-add-recipient option').filter(':selected').val();
            var found = false;

            // check not already added
            for (var i = 0; i < conversation.conversationUsers.length; i++) {
                // need to cast string to number using == not === for id
                if (conversation.conversationUsers[i].user.id == userId) {
                    found = true;
                }
            }

            if (!found && userId !== '') {
                ConversationService.addConversationUser(conversation.id, userId).then(function() {
                    ConversationService.get(conversation.id).then(function(successResult) {
                        $scope.editConversation = successResult;
                    }, function() {
                        alert('Error getting conversation');
                    });
                }, function() {
                    alert('Error adding conversation user');
                });
            }
        };

        $scope.removeConversationUser = function (conversationId, userId) {
            delete $scope.errorMessage;
            ConversationService.removeConversationUser(conversationId, userId).then(function() {
                if (userId !== $scope.loggedInUser.id) {
                    ConversationService.get(conversation.id).then(function(successResult) {
                        $scope.editConversation = successResult;
                    }, function() {
                        alert('Error getting conversation');
                    });
                } else {
                    // have removed own user from conversation
                    $scope.removedSelfFromConversation = true;
                }
            }, function() {
                alert('Error removing conversation user');
            });
        };

        init();
    }];
