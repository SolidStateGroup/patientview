'use strict';

angular.module('patientviewApp').controller('ConversationDetailsCtrl', ['$scope', 'ConversationService',
function ($scope, ConversationService) {

    $scope.selectGroup = function(conversation, groupId) {
        $scope.modalLoading = true;
        $scope.recipientsExist = false;

        ConversationService.getRecipients($scope.loggedInUser.id, groupId).then(function (recipientOptions) {

            var element = document.getElementById("conversation-add-recipient");
            if (element !== null) {
                element.parentNode.removeChild(element);
            }

            var select = document.createElement('select');
            select.setAttribute('class', 'form-control recipient-select');
            select.setAttribute('id', 'conversation-add-recipient');
            select.innerHTML = recipientOptions;
            document.getElementById('recipient-select-container').appendChild(select);

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
