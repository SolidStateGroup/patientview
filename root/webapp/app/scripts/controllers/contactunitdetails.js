'use strict';

angular.module('patientviewApp').controller('ContactUnitDetailsCtrl', ['$scope', function ($scope) {

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
