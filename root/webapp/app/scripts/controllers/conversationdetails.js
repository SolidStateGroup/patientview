'use strict';

angular.module('patientviewApp').controller('ConversationDetailsCtrl', ['$scope', 'ConversationService',
function ($scope, ConversationService) {

    $scope.selectGroup = function(conversation, groupId) {
        $scope.modalLoading = true;
        var featureTypes = ['MESSAGING','DEFAULT_MESSAGING_CONTACT'];

        // if patientMessagingFeatureTypes is set then restrict to this (PATIENT only, e.g. DEFAULT_MESSAGING_CONTACT)
        if ($scope.loggedInUser.userInformation.patientMessagingFeatureTypes) {
            featureTypes = $scope.loggedInUser.userInformation.patientMessagingFeatureTypes;
        }

        ConversationService.getRecipients($scope.loggedInUser.id, groupId, featureTypes).then(function (recipients) {
            conversation.availableRecipients = _.clone(recipients);
            conversation.allRecipients = [];

            for (var i = 0; i < recipients.length; i++) {
                conversation.allRecipients[recipients[i].id] = recipients[i];
            }

            if (recipients[0] !== undefined) {
                $scope.recipientToAdd = recipients[0].id;
            }

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

    $scope.addRecipient = function (form, conversation, userId) {
        for (var i = 0; i < conversation.availableRecipients.length; i++) {
            if (conversation.availableRecipients[i].id === userId) {
                conversation.recipients.push(conversation.allRecipients[userId]);
                conversation.availableRecipients.splice(i, 1);
            }
        }
    };

    $scope.removeRecipient = function (form, conversation, user) {
        for (var i = 0; i < conversation.recipients.length; i++) {
            if (conversation.recipients[i].id === user.id) {
                conversation.recipients.splice(i, 1);
                conversation.availableRecipients.push(conversation.allRecipients[user.id]);
            }
        }
    };
}]);
