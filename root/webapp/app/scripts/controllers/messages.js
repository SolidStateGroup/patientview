'use strict';


// new conversation modal instance controller
var NewConversationModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'newConversation', 'ConversationService', 'recipients',
    function ($scope, $rootScope, $modalInstance, newConversation, ConversationService, recipients) {
        var i;
        $scope.newConversation = newConversation;
        newConversation.availableRecipients = _.clone(recipients);
        newConversation.allRecipients = [];

        for (i = 0; i < recipients.length; i++) {
            newConversation.allRecipients[recipients[i].id] = recipients[i];
        }

        $scope.ok = function () {
            // build correct conversation from newConversation
            var conversation = {};
            conversation.type = "MESSAGE";
            conversation.title = newConversation.title;
            conversation.messages = [];
            conversation.open = true;

            // build message
            var message = {};
            message.user = $scope.loggedInUser;
            message.message = newConversation.message;
            message.type = "MESSAGE";
            conversation.messages[0] = message;

            // add conversation users from list of users (temp anonymous = false)
            var conversationUsers = [];
            for (i=0;i<newConversation.recipients.length;i++) {
                conversationUsers[i] = {};
                conversationUsers[i].user = {};
                conversationUsers[i].user.id = newConversation.recipients[i].id;
                conversationUsers[i].anonymous = false;
            }

            // add logged in user to list of conversation users
            var conversationUser = {};
            conversationUser.user = {};
            conversationUser.user.id = $scope.loggedInUser.id;
            conversationUser.anonymous = false;
            conversationUsers.push(conversationUser);

            conversation.conversationUsers = conversationUsers;

            ConversationService.new($scope.loggedInUser, conversation).then(function() {
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
            $modalInstance.dismiss('cancel');
        };
    }];

// pagination following http://fdietz.github.io/recipes-with-angular-js/common-user-interface-patterns/paginating-through-server-side-data.html
angular.module('patientviewApp').controller('MessagesCtrl',['$scope', '$modal', '$q', 'ConversationService', 'GroupService', 'RoleService', 'UserService', '$sce',
    function ($scope, $modal, $q, ConversationService, GroupService, RoleService, UserService, $sce) {

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
        return $scope.currentPage === 0 ? "hidden" : "";
    };

    $scope.nextPage = function() {
        if ($scope.currentPage < $scope.totalPages - 1) {
            $scope.currentPage++;
        }
    };

    $scope.nextPageDisabled = function() {
        if ($scope.totalPages > 0) {
            return $scope.currentPage === $scope.totalPages - 1 ? "hidden" : "";
        } else {
            return "hidden";
        }
    };

    // get page of data every time currentPage is changed
    $scope.$watch("currentPage", function(newValue, oldValue) {
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
        ConversationService.addMessage($scope.loggedInUser, conversation, conversation.addMessageContent).then(function() {
            conversation.addMessageContent = '';

            ConversationService.get(conversation.id).then(function(successResult) {
                for(var i =0; i<$scope.pagedItems.length;i++) {
                    if($scope.pagedItems[i].id == successResult.id) {
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
        ConversationService.addMessage($scope.loggedInUser, conversation, conversation.quickReplyContent).then(function() {
            conversation.quickReplyContent = '';
            conversation.quickReplyOpen = false;

            ConversationService.get(conversation.id).then(function(successResult) {
                for(var i =0; i<$scope.pagedItems.length;i++) {
                    if($scope.pagedItems[i].id == successResult.id) {
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
        var i;
        $scope.errorMessage = '';
        $scope.newConversation = {};
        $scope.newConversation.recipients = [];
        var roleIds = [], groupIds = [];

        // populate list of allowed recipients
        GroupService.getGroupsForUser($scope.loggedInUser.id).then(function (groups) {
            // get logged in user's groups
            for (i = 0; i < groups.length; i++) {
                var group = groups[i];
                if (group.visible === true) {
                    groupIds.push(group.id);
                }
            }

            // todo: how to deal with patients sending messages
            RoleService.getByType('STAFF').then(function(roles) {
                // get roles for recipients
                for (i = 0; i < roles.length; i++) {
                    var role = roles[i];
                    if (role.visible === true) {
                        roleIds.push(role.id);
                    }
                }

                // now have user's groups and list of roles, get all users
                UserService.getByGroupsAndRoles(groupIds, roleIds).then(function (users) {

                    // open modal
                    var modalInstance = $modal.open({
                        templateUrl: 'newConversationModal.html',
                        controller: NewConversationModalInstanceCtrl,
                        size: size,
                        resolve: {
                            recipients: function(){
                                return users;
                            },
                            newConversation: function(){
                                return $scope.newConversation;
                            },
                            ConversationService: function(){
                                return ConversationService;
                            }
                        }
                    });

                    modalInstance.result.then(function () {
                        $scope.loading = true;
                        ConversationService.getAll($scope.loggedInUser, $scope.currentPage, $scope.itemsPerPage).then(function(page) {
                            $scope.pagedItems = page.content;
                            $scope.total = page.totalElements;
                            $scope.totalPages = page.totalPages;
                            $scope.loading = false;
                            $scope.successMessage = 'Conversation successfully created';
                        }, function() {
                            $scope.loading = false;
                            // error
                        });
                    }, function () {
                        // cancel
                        $scope.editConversation = '';
                    });

                }, function () {
                    // error retrieving users
                    alert('Error loading possible message recipients [3]');
                });
            }, function () {
                // error retrieving roles
                alert('Error loading possible message recipients [2]');
            });
        }, function () {
            // error retrieving groups
            alert('Error loading possible message recipients [1]');
        });
    };
    
}]);
