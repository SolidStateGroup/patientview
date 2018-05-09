'use strict';
angular.module('patientviewApp').controller('ConversationsCtrl',['$scope', '$rootScope', '$modal', '$q', '$filter',
    'ConversationService', 'GroupService', 'UserService', 'AuthService', 'UtilService', '$location',
    'localStorageService', '$cookies',
function ($scope, $rootScope, $modal, $q, $filter, ConversationService, GroupService, UserService, AuthService,
          UtilService, $location, localStorageService, $cookies) {

    var init = function() {
        $scope.formatBytes = UtilService.formatBytes;

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
        if (ConversationService.userHasMessagingFeature()) {
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

            ConversationService.getAll($scope.loggedInUser, getParameters).then(function (page) {

                // add archived property if present as a label for this user
                for (var i = 0; i < page.content.length; i++) {
                    page.content[i] = setArchivedStatus(page.content[i]);
                    page.content[i].unread = hasUnreadMessages(page.content[i]);
                }

                $scope.pagedItems = page.content;
                $scope.hasMessagingFeature = true;
                $scope.total = page.totalElements;
                $scope.totalPages = page.totalPages;
                $scope.loading = false;
            }, function () {
                $scope.loading = false;
                alert("Could not get conversations");
            });
        }else{
            $scope.hasMessagingFeature = false;
        }
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

    $scope.viewMyMedia = function (fromMessage) {
        var modalInstance = $modal.open({
            templateUrl: 'views/modal/viewMyMedia.html',
            controller: ViewMyMediaModalInstanceCtrl,
            size: 'lg',
            resolve: {
                myMedia: function(){
                    return {};
                },
                message: function () {
                    return fromMessage;
                },
            }
        });

        modalInstance.result.then(function () {
            // ok (not used)
        }, function () {
            // closed
        });
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
                $rootScope.setUnreadConversationCount();
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
                $rootScope.setUnreadConversationCount();
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

    // view patient
    $scope.viewUser = function (userId) {
        $scope.loadingMessage = 'Viewing Patient';
        $scope.loading = true;
        $scope.successMessage = '';
        $scope.printSuccessMessage = false;
        $rootScope.switchingUser = true;
        var currentToken = $rootScope.authToken;

        AuthService.switchUser(userId, null).then(function(authToken) {

            $rootScope.previousAuthToken = currentToken;
            localStorageService.set('previousAuthToken', currentToken);

            $rootScope.previousLoggedInUser = $scope.loggedInUser;
            localStorageService.set('previousLoggedInUser', $scope.loggedInUser);

            $rootScope.previousLocation = '/conversations';
            localStorageService.set('previousLocation', '/conversations');

            $rootScope.authToken = authToken;
            $cookies.authToken = authToken;
            localStorageService.set('authToken', authToken);

            // get user information, store in session
            AuthService.getUserInformation({'token' : authToken}).then(function (userInformation) {

                var user = userInformation.user;
                delete userInformation.user;
                user.userInformation = userInformation;

                $rootScope.loggedInUser = user;
                localStorageService.set('loggedInUser', user);

                $rootScope.routes = userInformation.routes;
                localStorageService.set('routes', userInformation.routes);

                $scope.loading = false;
                $location.path('/dashboard');
                delete $rootScope.switchingUser;

            }, function() {
                alert('Error receiving user information');
                $scope.loading = false;
            });

        }, function() {
            alert('Cannot view patient');
            $scope.loading = false;
            delete $rootScope.switchingUser;
        });
    };

    // show With: text next to recipients
    $scope.showWithText = function(conversationUsers) {
        if (conversationUsers.length > 2) {
            return true;
        }

        return true;
    };

    $scope.userHasMessagingFeature = function() {
        return ConversationService.userHasMessagingFeature();
    };

    init();
}]);
