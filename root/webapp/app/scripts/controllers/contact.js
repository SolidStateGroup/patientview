'use strict';


// contact unit modal instance controller
var ContactUnitModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'ConversationService',
function ($scope, $rootScope, $modalInstance, ConversationService) {
    var i;
    $scope.conversation = {};
    $scope.conversation.recipients = [];
    $scope.modalLoading = true;
    var featureTypes = ['MESSAGING'];

    ConversationService.getRecipients($scope.loggedInUser.id, featureTypes).then(function (recipients) {
        $scope.conversation.availableRecipients = _.clone(recipients);
        $scope.conversation.allRecipients = [];

        for (i = 0; i < recipients.length; i++) {
            $scope.conversation.allRecipients[recipients[i].id] = recipients[i];
        }

        $scope.recipientToAdd = recipients[0].id;

        $scope.modalLoading = false;
    }, function () {
        alert('Error loading message recipients');
    });

    $scope.ok = function () {
        // build correct conversation from conversation
        var conversation = {};
        conversation.type = 'MESSAGE';
        conversation.title = $scope.conversation.title;
        conversation.messages = [];
        conversation.open = true;

        // build message
        var message = {};
        message.user = $scope.loggedInUser;
        message.message = $scope.conversation.message;
        message.type = 'MESSAGE';
        conversation.messages[0] = message;

        // add conversation users from list of users (temp anonymous = false)
        var conversationUsers = [];
        for (i=0; i<$scope.conversation.recipients.length; i++) {
            conversationUsers[i] = {};
            conversationUsers[i].user = {};
            conversationUsers[i].user.id = $scope.conversation.recipients[i].id;
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


angular.module('patientviewApp').controller('ContactCtrl',['$scope', '$modal', 'GroupService', 'ConversationService',
function ($scope, $modal, GroupService, ConversationService) {

    $scope.init = function() {
        $scope.loading = true;
        GroupService.getGroupsForUserAllDetails($scope.loggedInUser.id).then(function(page) {
            $scope.groups = page.content;
            $scope.loading = false;
        }, function () {
            alert('Error retrieving groups');
            $scope.loading = false;
        });
    };

    // open modal for contacting unit
    $scope.openModalContactUnit = function (size) {
        $scope.errorMessage = '';

        // open modal
        var modalInstance = $modal.open({
            templateUrl: 'contactUnitModal.html',
            controller: ContactUnitModalInstanceCtrl,
            size: size,
            resolve: {
                ConversationService: function () {
                    return ConversationService;
                }
            }
        });

        modalInstance.result.then(function () {
            $scope.successMessage = 'Contacted Unit';
        }, function () {
            $scope.conversation = '';
        });

    };

    $scope.init();
}]);
