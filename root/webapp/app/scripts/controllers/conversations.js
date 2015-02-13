'use strict';

// new conversation modal instance controller
var NewConversationModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'GroupService', 'RoleService', 'UserService', 'ConversationService',
    function ($scope, $rootScope, $modalInstance, GroupService, RoleService, UserService, ConversationService) {
        var i;

        var init = function() {
            delete $scope.errorMessage;
            $scope.newConversation = {};
            $scope.newConversation.recipients = [];
            $scope.conversationGroups = [];

            GroupService.getMessagingGroupsForUser($scope.loggedInUser.id).then(function(successResult) {
                for (var i = 0; i < successResult.length; i++) {
                    if (successResult[i].code !== 'Generic') {
                        $scope.conversationGroups.push(successResult[i]);
                    }
                }
            }, function(failResult) {
                $scope.errorMessage = failResult.data;
            });
        };

        $scope.ok = function () {
            delete $scope.errorMessage;

            // build correct conversation from newConversation
            var conversation = {};
            conversation.type = 'MESSAGE';
            conversation.title = $scope.newConversation.title;
            conversation.messages = [];
            conversation.open = true;

            // build message
            var message = {};
            message.user = {};
            message.user.id = $scope.loggedInUser.id;
            message.message = $scope.newConversation.message;
            message.type = 'MESSAGE';
            conversation.messages[0] = message;

            // add conversation users from list of users (temp anonymous = false)
            var conversationUsers = [];
            for (i=0; i<$scope.newConversation.recipients.length; i++) {
                conversationUsers[i] = {};
                conversationUsers[i].user = {};
                conversationUsers[i].user.id = $scope.newConversation.recipients[i].id;
                conversationUsers[i].anonymous = false;
            }

            // add logged in user to list of conversation users
            var conversationUser = {};
            conversationUser.user = {};
            conversationUser.user.id = $scope.loggedInUser.id;
            conversationUser.anonymous = false;
            conversationUsers.push(conversationUser);

            conversation.conversationUsers = conversationUsers;

            ConversationService.create($scope.loggedInUser, conversation).then(function() {
                $modalInstance.close();
            }, function(result) {
                if (result.data) {
                    $scope.errorMessage = ' - ' + result.data;
                } else {
                    $scope.errorMessage = ' ';
                }
            });
        };

        $scope.cancel = function () {
            delete $scope.errorMessage;
            $modalInstance.dismiss('cancel');
        };

        init();
    }];

// pagination following http://fdietz.github.io/recipes-with-angular-js/common-user-interface-patterns/paginating-through-server-side-data.html
angular.module('patientviewApp').controller('ConversationsCtrl',['$scope', '$modal', '$q', 'ConversationService', 'GroupService', 'UserService',
    function ($scope, $modal, $q, ConversationService, GroupService, UserService) {

    $scope.itemsPerPage = 5;
    $scope.currentPage = 0;

    $scope.range = function() {
        var rangeSize = 5;
        var ret = [];
        var start;

        start = 1;
        if ( start > $scope.totalPages-rangeSize ) {
            start = $scope.totalPages-rangeSize;
        }

        for (var i=start; i<start+rangeSize; i++) {
            if (i > -1) {
                ret.push(i);
            }
        }

        return ret;
    };

    $scope.setPage = function(n) {
        if (n > -1 && n < $scope.totalPages) {
            $scope.currentPage = n;
        }
    };

    $scope.prevPage = function() {
        if ($scope.currentPage > 0) {
            $scope.currentPage--;
        }
    };

    $scope.prevPageDisabled = function() {
        return $scope.currentPage === 0 ? 'hidden' : '';
    };

    $scope.nextPage = function() {
        if ($scope.currentPage < $scope.totalPages - 1) {
            $scope.currentPage++;
        }
    };

    $scope.nextPageDisabled = function() {
        if ($scope.totalPages > 0) {
            return $scope.currentPage === $scope.totalPages - 1 ? 'hidden' : '';
        } else {
            return 'hidden';
        }
    };

    // get page of data every time currentPage is changed
    $scope.$watch('currentPage', function(newValue) {
        $scope.loading = true;
        ConversationService.getAll($scope.loggedInUser, newValue, $scope.itemsPerPage).then(function(page) {
            $scope.pagedItems = page.content;
            $scope.total = page.totalElements;
            $scope.totalPages = page.totalPages;
            $scope.loading = false;
        }, function() {
            $scope.loading = false;
            // error
        });
    });

    $scope.hasUnreadMessages = function(conversation) {
        var i, j, unread, unreadMessages = 0;
        for (i=0;i<conversation.messages.length;i++) {
            unread = true;
            for (j=0;j<conversation.messages[i].readReceipts.length;j++) {
                var readReceipt = conversation.messages[i].readReceipts[j];
                if (readReceipt.user.id === $scope.loggedInUser.id) {
                    unread = false;
                }
            }
            if (unread) {
                unreadMessages++;
            }
        }

        return unreadMessages > 0;
    };

    $scope.viewMessages = function(conversation) {
        delete $scope.successMessage;
        delete $scope.errorMessage;
        delete conversation.successMessage;

        if (conversation.showMessages) {
            ConversationService.get(conversation.id).then(function(successResult) {
                conversation.messages = successResult.messages;
            }, function() {
                alert('Error getting conversation');
            });
            conversation.showMessages = false;
        } else {
            conversation.quickReplyOpen = false;
            var i, j;
            var unreadMessages = [], promises = [];

            ConversationService.get(conversation.id).then(function(successResult) {
                conversation.messages = successResult.messages;

                // add read receipt for all messages not currently read
                for (i = 0; i < conversation.messages.length; i++) {
                    var message = conversation.messages[i];
                    var receiptFound = false;
                    message.unread = true;

                    for (j = 0; j < message.readReceipts.length; j++) {
                        var readReceipt = message.readReceipts[j];
                        if (readReceipt.user.id === $scope.loggedInUser.id) {
                            receiptFound = true;
                            message.unread = false;
                        }
                    }

                    if (!receiptFound) {
                        unreadMessages.push(message.id);
                    }
                }

                // mark messages as read then get conversation fresh from GET, hides new notification
                for (i=0;i<unreadMessages.length;i++) {
                    promises.push(ConversationService.addMessageReadReceipt(unreadMessages[i], $scope.loggedInUser.id));
                }

                $q.all(promises).then(function () {
                    //conversation.unread = false;
                    conversation.showMessages = true;
                    $scope.setUnreadConversationCount();
                }, function() {
                    alert('Error adding read receipt for new messages');
                });
            }, function() {
                alert('Error getting conversation');
            });
        }
    };

    $scope.addMessage = function(conversation) {
        delete $scope.successMessage;
        delete $scope.errorMessage;
        delete conversation.successMessage;

        ConversationService.addMessage($scope.loggedInUser, conversation, conversation.addMessageContent)
            .then(function() {
            conversation.addMessageContent = '';
            conversation.successMessage = 'Successfully replied';

            ConversationService.get(conversation.id).then(function(successResult) {
                for(var i =0; i<$scope.pagedItems.length;i++) {
                    if($scope.pagedItems[i].id === successResult.id) {
                        $scope.pagedItems[i].messages = successResult.messages;
                    }
                }
            }, function() {
                alert('Error updating conversation (message added successfully)');
            });
        }, function() {
            alert('Error adding message');
        });
    };

    $scope.quickReply = function(conversation) {
        delete $scope.successMessage;
        delete $scope.errorMessage;

        ConversationService.addMessage($scope.loggedInUser, conversation, conversation.quickReplyContent)
            .then(function() {
            conversation.quickReplyContent = '';
            conversation.quickReplyOpen = false;
            $scope.successMessage = 'Successfully replied';

            ConversationService.get(conversation.id).then(function(successResult) {
                for(var i =0; i<$scope.pagedItems.length;i++) {
                    if($scope.pagedItems[i].id === successResult.id) {
                        $scope.pagedItems[i].messages = successResult.messages;
                    }
                }
            }, function() {
                alert('Error updating conversation (quick reply added successfully)');
            });
        }, function() {
            alert('Error adding message');
        });
    };

    // open modal for new conversation
    $scope.openModalNewConversation = function (size) {
        delete $scope.errorMessage;
        delete $scope.successMessage;

        // open modal
        var modalInstance = $modal.open({
            templateUrl: 'newConversationModal.html',
            controller: NewConversationModalInstanceCtrl,
            size: size,
            backdrop: 'static',
            resolve: {
                ConversationService: function () {
                    return ConversationService;
                },
                GroupService: function () {
                    return GroupService;
                }
            }
        });

        modalInstance.result.then(function () {
            $scope.loading = true;
            ConversationService.getAll($scope.loggedInUser, $scope.currentPage, $scope.itemsPerPage)
                .then(function (page) {
                $scope.pagedItems = page.content;
                $scope.total = page.totalElements;
                $scope.totalPages = page.totalPages;
                $scope.loading = false;
                $scope.successMessage = 'Successfully sent message';
            }, function () {
                $scope.loading = false;
                // error
            });
        }, function () {
            // cancel
            $scope.editConversation = '';
        });
    };

    $scope.userHasMessagingFeature = function() {

        // GLOBAL_ADMIN and PATIENT both always have messaging enabled
        if (UserService.checkRoleExists('GLOBAL_ADMIN', $scope.loggedInUser)
            || UserService.checkRoleExists('PATIENT', $scope.loggedInUser) ) {
            return true;
        }

        var messagingFeatures = ['MESSAGING', 'DEFAULT_MESSAGING_CONTACT'];

        for (var i = 0; i < $scope.loggedInUser.userInformation.userFeatures.length; i++) {
            var feature = $scope.loggedInUser.userInformation.userFeatures[i];
            if (messagingFeatures.indexOf(feature.name) > -1) {
                return true;
            }
        }

        return false;
    };
        
    $scope.getLastMessageUser = function(conversation) {
        var message = conversation.messages[conversation.messages.length-1];
        if (message != null) {
            return message.user;            
        }
    }
}]);
