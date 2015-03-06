'use strict';

// change conversation recipients modal instance controller
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
                }, function() {
                    alert('Error adding conversation user');
                });
            }

            $timeout(function() {
                $scope.$apply();
            });
        };
        
        $scope.addConversationUser = function (conversation) {
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

// new conversation modal instance controller
var NewConversationModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'GroupService', 'RoleService',
    'UserService', 'ConversationService',
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
            $scope.sendingMessage = true;

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
                delete $scope.sendingMessage;
            }, function(result) {
                if (result.data) {
                    $scope.errorMessage = ' - ' + result.data;
                } else {
                    $scope.errorMessage = ' ';
                }
                delete $scope.sendingMessage;
            });
        };

        $scope.cancel = function () {
            delete $scope.errorMessage;
            $modalInstance.dismiss('cancel');
        };

        init();
    }];

angular.module('patientviewApp').controller('ConversationsCtrl',['$scope', '$rootScope', '$modal', '$q', '$filter',
    'ConversationService', 'GroupService', 'UserService',
function ($scope, $rootScope, $modal, $q, $filter, ConversationService, GroupService, UserService) {

    var init = function() {
        $scope.itemsPerPage = 5;
        $scope.currentPage = 0;

        // folders (equivalent to labels, currently only INBOX, ARCHIVED)
        $scope.folders = [];
        $scope.folders.push({name:'INBOX', description:'Inbox'}, {name:'ARCHIVED', description:'Archived'});
        $scope.selectedFolder = $scope.folders[0].name;
    };

    // get page of data every time currentPage is changed
    $scope.$watch('currentPage', function() {
        getItems();
    });

    var getItems = function() {
        $scope.loading = true;
        var getParameters = {};
        getParameters.page = $scope.currentPage;
        getParameters.size = $scope.itemsPerPage;
        getParameters.filterText = $scope.filterText;
        if (!$scope.includeAllFoldersInSearch) {

            // labels passed as array, currently only one label at a time to emulate INBOX, ARCHIVED folders
            getParameters.conversationLabels = [];
            getParameters.conversationLabels.push($scope.selectedFolder);
        }

        ConversationService.getAll($scope.loggedInUser, getParameters).then(function(page) {

            // add archived property if present as a label for this user
            for (var i=0; i<page.content.length; i++) {
                page.content[i] = setArchivedStatus(page.content[i]);
                page.content[i].unread = hasUnreadMessages(page.content[i]);
            }

            $scope.pagedItems = page.content;
            $scope.total = page.totalElements;
            $scope.totalPages = page.totalPages;
            $scope.loading = false;
        }, function() {
            $scope.loading = false;
            alert("Could not get conversations");
        });
    };

    var setArchivedStatus = function(conversation) {
        var currentUserId = $scope.loggedInUser.id;
        for (var i=0; i<conversation.conversationUsers.length; i++) {
            var conversationUser = conversation.conversationUsers[i];
            if (conversationUser.user.id === currentUserId) {
                for (var j=0; j<conversationUser.conversationUserLabels.length; j++) {
                    if (conversationUser.conversationUserLabels[j].conversationLabel === 'ARCHIVED') {
                        conversation.archived = true;
                    }
                }
            }
        }
        return conversation;
    };

    var hasUnreadMessages = function(conversation) {
        var i, j, unread, unreadMessages = 0;
        for (i=0;i<conversation.messages.length;i++) {
            unread = true;
            var message = conversation.messages[i];
            
            for (j = 0; j < message.readReceipts.length; j++) {
                var readReceipt = message.readReceipts[j];
                if (readReceipt.user.id == $scope.loggedInUser.id) {
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
                //conversation.messages = successResult.messages;
                conversation = successResult;
                conversation.unread = hasUnreadMessages(conversation);
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
                    conversation.unread = false;
                    conversation.showMessages = true;
                    $rootScope.setUnreadConversationCount();
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
            conversation.successMessage = 'Successfully sent message';

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
            $scope.successMessage = 'Successfully sent message';

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
            var getParameters = {};
            getParameters.page = $scope.currentPage;
            getParameters.size = $scope.itemsPerPage;
            getParameters.filterText = $scope.filterText;
            if (!$scope.includeAllFoldersInSearch) {
                getParameters.conversationLabels = [];
                getParameters.conversationLabels.push($scope.selectedFolder);
            }

            ConversationService.getAll($scope.loggedInUser, getParameters).then(function(page) {
                // add archived property if present as a label for this user
                for (var i=0; i<page.content.length; i++) {
                    page.content[i] = setArchivedStatus(page.content[i]);
                }
                $scope.pagedItems = page.content;
                $scope.total = page.totalElements;
                $scope.totalPages = page.totalPages;
                $scope.successMessage = 'Successfully sent message';
                $scope.loading = false;
            }, function() {
                $scope.loading = false;
                alert("Could not get conversations");
            });
        }, function () {
            $scope.editConversation = '';
        });
    };

    // open modal for change conversation recipients
    $scope.openModalChangeConversationRecipients = function (conversation) {
        delete $scope.errorMessage;
        delete $scope.successMessage;

        // open modal
        var modalInstance = $modal.open({
            templateUrl: 'changeConversationRecipientsModal.html',
            controller: ChangeConversationRecipientsModalInstanceCtrl,
            size: 'lg',
            backdrop: 'static',
            resolve: {
                conversation: function () {
                    return conversation;
                },
                ConversationService: function () {
                    return ConversationService;
                },
                GroupService: function () {
                    return GroupService;
                }
            }
        });

        modalInstance.result.then(function () {
            // only has close button
        }, function () {
            getItems();
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
    };

    $scope.changeFolder = function(folder) {
        $scope.folder = folder;
        $scope.currentPage = 0;
        delete $scope.filterText;
        delete $scope.includeAllFoldersInSearch;
        getItems();
    };

    $scope.search = function() {
        if ($scope.filterText !== undefined && !$scope.filterText.length) {
            delete $scope.filterText;
        }
        $scope.currentPage = 0;
        getItems();
    };
    
    $scope.clearSearch = function() {
        delete $scope.filterText;
        delete $scope.includeAllFoldersInSearch;
        $scope.currentPage = 0;
        getItems();
    };

    // add ARCHIVE, remove INBOX
    $scope.archive = function(conversation) {
        ConversationService.removeConversationUserLabel($scope.loggedInUser.id, conversation.id, 'INBOX')
        .then(function() {
            ConversationService.addConversationUserLabel($scope.loggedInUser.id, conversation.id, 'ARCHIVED')
            .then(function() {
                $scope.currentPage = 0;
                getItems();
            }, function() {
                alert('Error archiving');
            });
        }, function() {
            alert('Error archiving');
        });
    };

    // remove ARCHIVE, add INBOX
    $scope.unArchive = function(conversation) {
        ConversationService.removeConversationUserLabel($scope.loggedInUser.id, conversation.id, 'ARCHIVED')
        .then(function() {
            ConversationService.addConversationUserLabel($scope.loggedInUser.id, conversation.id, 'INBOX')
            .then(function() {
                $scope.currentPage = 0;
                getItems();
            }, function() {
                alert('Error unarchiving');
            });
        }, function() {
            alert('Error unarchiving');
        });
    };

    $scope.printConversation = function(conversation) {
        // ie8 compatibility
        var printContent = $('<div>');
        var archived = '';
        var i;
        if (conversation.archived) {
            archived = '(archived)';
        }
        printContent.append('<h1 style="font-family: sans-serif;">PatientView Conversation ' + archived
            + '</h1><h2 style="font-family: sans-serif;">Subject: ' + conversation.title + '</h2>');
        printContent.append('<h4 style="font-family: sans-serif;">Between:</h4><ul>');
        for (i=0; i< conversation.conversationUsers.length; i++) {
            printContent.append('<li style="font-family: sans-serif;">'
                + conversation.conversationUsers[i].user.forename + ' '
                + conversation.conversationUsers[i].user.surname + '</li>');
        }

        for (i=0; i< conversation.messages.length; i++) {
            var message = conversation.messages[i];
            printContent.append('<hr><h4 style="font-family: sans-serif;">Message Date: '
                + $filter('date')(message.created, 'dd-MMM-yyyy HH:mm')
                + '</h4><h4 style="font-family: sans-serif;">From: '
                + message.user.forename + ' ' + message.user.surname + '</h4>');
            printContent.append('<p style="font-family: sans-serif;">' + message.message + '</p>');
        }

        var windowUrl = 'PatientView Conversation';
        var uniqueName = new Date();
        var windowName = 'Print' + uniqueName.getTime();
        var printWindow = window.open(windowUrl, windowName, 'left=50000,top=50000,width=0,height=0');
        printWindow.document.write(printContent.html());
        printWindow.document.close();
        printWindow.focus();
        printWindow.print();
        printWindow.close();
    };
    
    init();
}]);
