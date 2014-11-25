'use strict';

angular.module('patientviewApp').controller('ConversationDetailsCtrl', ['$scope', 'ConversationService',
function ($scope, ConversationService) {

    $scope.selectGroup = function(conversation, groupId) {
        $scope.modalLoading = true;
        $scope.recipientsExist = false;

        ConversationService.getRecipients($scope.loggedInUser.id, groupId).then(function (recipientOptions) {

            $("#conversation-add-recipient").remove();
            var conversationAddRecipient
                = $('<select>').addClass('form-control').addClass('recipient-select').attr("id","conversation-add-recipient");
            conversationAddRecipient.html(recipientOptions);
            $("#recipient-select-container").html(conversationAddRecipient);

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

    $scope.addRecipient = function (form, conversation) {

        var userId = $("#conversation-add-recipient option").filter(":selected").val();
        var userDescription = $("#conversation-add-recipient option").filter(":selected").text();
        var found = false;
        var i;

        // check not already added
        for (i = 0; i < conversation.recipients.length; i++) {
            // need to cast string to number using == not === for id
            if (conversation.recipients[i].id == userId) {
                found = true;
            }
        }

        if (!found && userId !== '') {
            var recipient = {};
            recipient.id = userId;
            recipient.description = userDescription;
            conversation.recipients.push(recipient);
        }
    };

    $scope.removeRecipient = function (form, conversation, user) {
        for (var i = 0; i < conversation.recipients.length; i++) {
            if (conversation.recipients[i].id === user.id) {
                conversation.recipients.splice(i, 1);
            }
        }
    };
}]);
