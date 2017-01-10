'use strict';
var ContactUnitModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'ConversationService', 'group',
function ($scope, $rootScope, $modalInstance, ConversationService, group) {
    $scope.conversation = {};
    $scope.conversation.recipients = [];
    $scope.modalLoading = true;
    $scope.group = group;

    $scope.contactOptions = [];
    $scope.contactOptions.push({'id':0, 'staffFeature': 'PATIENT_SUPPORT_CONTACT', 'anonymous': false, 'description':'Missing or incorrect details in my record, such as results, diagnoses or contact details'});
    $scope.contactOptions.push({'id':1, 'staffFeature': 'PATIENT_SUPPORT_CONTACT', 'anonymous': false, 'description':'Feedback to my unit (public)'});
    $scope.contactOptions.push({'id':2, 'staffFeature': 'PATIENT_SUPPORT_CONTACT', 'anonymous': true, 'description':'Feedback to my unit (anonymous)'});
    $scope.contactOptions.push({'id':3, 'staffFeature': 'UNIT_TECHNICAL_CONTACT', 'anonymous': false, 'description':'Feedback on technical issues to your local PatientView administrator'});
    $scope.contactOptions.push({'id':4, 'staffFeature': 'PATIENT_SUPPORT_CONTACT', 'anonymous': false, 'description':'Other comments to your local PatientView administrator'});
    $scope.contactOptions.push({'id':5, 'staffFeature': 'CENTRAL_SUPPORT_CONTACT', 'anonymous': false, 'description':'Comments to central PatientView support (will be sent via email)'});
    $scope.selectedContactOption = $scope.contactOptions[-1];
    $scope.modalLoading = false;

    $scope.ok = function () {
        $scope.sendingMessage = true;
        
        // build correct conversation from conversation
        var conversation = {};
        conversation.type = 'CONTACT_UNIT';
        conversation.title = $scope.conversation.title;
        conversation.anonymous = $scope.conversation.anonymous;
        conversation.messages = [];
        conversation.open = true;

        // contact unit specific attributes
        conversation.staffFeature = $scope.conversation.staffFeature;
        conversation.groupId = $scope.group.id;

        // build message
        var message = {};
        message.user = {};
        message.user.id = $scope.loggedInUser.id;
        message.message = $scope.conversation.message;
        message.type = 'CONTACT_UNIT';
        conversation.messages[0] = message;

        // add logged in user to list of conversation users
        conversation.conversationUsers = [];
        var conversationUser = {};
        conversationUser.user = {};
        conversationUser.user.id = $scope.loggedInUser.id;
        conversationUser.anonymous = $scope.conversation.anonymous;
        conversation.conversationUsers.push(conversationUser);

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
        $modalInstance.dismiss('cancel');
    };

    $scope.contactOptionChanged = function(value) {
        $scope.conversation.title = value.description;
        $scope.conversation.anonymous = value.anonymous;
        $scope.conversation.staffFeature = value.staffFeature;
    };
}];
