'use strict';


// new conversation modal instance controller
var NewConversationModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'newConversation', 'ConversationService',
    function ($scope, $rootScope, $modalInstance, newConversation, ConversationService) {
        var i;
        $scope.newConversation = newConversation;

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
            for (i=0;i<newConversation.users.length;i++) {
                conversationUsers[i] = {};
                conversationUsers[i].user = newConversation.users[i];
                conversationUsers[i].anonymous = false;
            }

            // add logged in user to list of conversation users
            var conversationUser = {};
            conversationUser.user = $scope.loggedInUser;
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
angular.module('patientviewApp').controller('MessagesCtrl',['$scope', '$modal', 'ConversationService',
    function ($scope, $modal, ConversationService) {

    $scope.itemsPerPage = 5;
    $scope.currentPage = 0;

    $scope.prevPage = function() {
        if ($scope.currentPage > 0) {
            $scope.currentPage--;
        }
    };

    $scope.prevPageDisabled = function() {
        return $scope.currentPage === 0 ? "disabled" : "";
    };

    $scope.nextPage = function() {
        if ($scope.currentPage < $scope.pageCount() - 1) {
            $scope.currentPage++;
        }
    };

    $scope.nextPageDisabled = function() {
        return $scope.currentPage === $scope.pageCount() - 1 ? "disabled" : "";
    };

    $scope.pageCount = function() {
        return Math.ceil($scope.total/$scope.itemsPerPage);
    };

    $scope.$watch("currentPage", function(newValue, oldValue) {
        $scope.loading = true;
        ConversationService.getAll($scope.loggedInUser, newValue*$scope.itemsPerPage, $scope.itemsPerPage).then(function(result) {
            $scope.pagedItems = result;
            $scope.loading = false;
        }, function() {
            $scope.loading = false;
            // error
        });
        //$scope.total = ConversationService.total();
    });

    $scope.init = function() {
    };

    $scope.init();

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
                // error
                alert('Error updating conversation (message added successfully)');
            });
        }, function() {
            // error
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
                // error
                alert('Error updating conversation (quick reply added successfully)');
            });
        }, function() {
            // error
            alert('Error adding message');
        });
    };


    // open modal for new conversation
    $scope.openModalNewConversation = function (size) {
        $scope.errorMessage = '';
        $scope.newConversation = {};
        $scope.newConversation.users = [];

        var modalInstance = $modal.open({
            templateUrl: 'newConversationModal.html',
            controller: NewConversationModalInstanceCtrl,
            size: size,
            resolve: {
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
            ConversationService.getAll($scope.loggedInUser, $scope.currentPage, $scope.itemsPerPage).then(function(result) {
                $scope.pagedItems = result;
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
    };
    
}]);
