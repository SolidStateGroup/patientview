'use strict';

angular.module('patientviewApp').controller('ConversationDetailsCtrl', ['$scope', 'ConversationService',
function ($scope, ConversationService) {

    $scope.selectGroup = function(conversation, groupId) {
        $scope.modalLoading = true;

        ConversationService.getRecipients($scope.loggedInUser.id, groupId).then(function (recipientMap) {
            conversation.availableRecipients = [];
            conversation.recipientMap = {};

            var restangularObjects
                = ['route','reqParams','fromServer','parentResource','restangularCollection','singleOne'];

            for (var key in recipientMap) {
                if (recipientMap.hasOwnProperty(key) && typeof(recipientMap[key]) !== 'function'
                    && !(restangularObjects.indexOf(key) > -1)) {

                    if (recipientMap[key].length) {
                        var element = {};
                        element.description = key;
                        conversation.availableRecipients.push(element);
                    }

                    var temp = [];
                    for (var i = 0; i < recipientMap[key].length ; i++) {
                        conversation.availableRecipients.push(recipientMap[key][i]);
                        temp.push(recipientMap[key][i]);
                    }
                    conversation.recipientMap[key] = temp;
                }
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

        var found = false;
        var i;

        // check not already added
        for (i = 0; i < conversation.recipients.length; i++) {
            // need to cast string to number using == not === for id
            if (conversation.recipients[i].id == userId) {
                found = true;
            }
        }

        if (!found) {
            for (i = 0; i < conversation.availableRecipients.length; i++) {
                if (conversation.availableRecipients[i].id == userId) {
                    conversation.recipients.push(conversation.availableRecipients[i]);
                }
            }
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
