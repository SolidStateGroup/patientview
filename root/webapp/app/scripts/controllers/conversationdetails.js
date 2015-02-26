'use strict';

angular.module('patientviewApp').controller('ConversationDetailsCtrl', ['$scope', '$timeout', 'ConversationService',
function ($scope, $timeout, ConversationService) {

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
                        var userDescription = this.getItem(userId)[0].innerHTML;
                        if (userDescription !== undefined) {
                            addRecipient(userId, userDescription);
                            this.setValue("");
                        }
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
    
    var addRecipient = function (userId, userDescription) {
        var found = false;
        
        // check not already added
        for (var i = 0; i < $scope.newConversation.recipients.length; i++) {
            // need to cast string to number using == not === for id
            if ($scope.newConversation.recipients[i].id == userId) {
                found = true;
            }
        }

        if (!found && userId !== '') {
            var recipient = {};
            recipient.id = userId;
            recipient.description = userDescription;
            $scope.newConversation.recipients.push(recipient);
        }

        $timeout(function() {
            $scope.$apply();
        });
    };

    $scope.removeRecipient = function (form, conversation, user) {
        for (var i = 0; i < conversation.recipients.length; i++) {
            if (conversation.recipients[i].id === user.id) {
                conversation.recipients.splice(i, 1);
            }
        }
    };
}]);
