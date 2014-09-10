'use strict';


// contact unit modal instance controller
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
    $scope.contactOptions.push({'id':3, 'staffFeature': 'UNIT_TECHNICAL_CONTACT', 'anonymous': false, 'description':'Comments about PatientView for the system administrators'});
    $scope.contactOptions.push({'id':4, 'staffFeature': 'PATIENT_SUPPORT_CONTACT', 'anonymous': false, 'description':'Other'});
    $scope.selectedContactOption = $scope.contactOptions[-1];
    $scope.modalLoading = false;

    $scope.ok = function () {
        // build correct conversation from conversation
        var conversation = {};
        conversation.type = 'CONTACT_UNIT';
        conversation.title = $scope.conversation.title;
        conversation.messages = [];
        conversation.open = true;

        // contact unit specific attributes
        conversation.staffFeature = $scope.conversation.staffFeature;
        conversation.groupId = $scope.group.id;

        // build message
        var message = {};
        message.user = $scope.loggedInUser;
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

    $scope.contactOptionChanged = function(value) {
        $scope.conversation.title = value.description;
        $scope.conversation.anonymous = value.anonymous;
        $scope.conversation.staffFeature = value.staffFeature;
    }
}];


angular.module('patientviewApp').controller('ContactCtrl',['$scope', '$modal', 'GroupService', 'ConversationService',
function ($scope, $modal, GroupService, ConversationService) {

    $scope.init = function() {
        $scope.loading = true;
        GroupService.getGroupsForUserAllDetails($scope.loggedInUser.id).then(function(page) {
            $scope.groups = page.content;

            // set checkboxes
            for (var i=0;i<$scope.groups.length;i++) {
                $scope.groups[i].selected = true;
            }

            $scope.loading = false;
        }, function () {
            alert('Error retrieving groups');
            $scope.loading = false;
        });
    };

    // open modal for contacting unit
    $scope.openModalContactUnit = function (group) {
        $scope.errorMessage = '';

        // open modal
        var modalInstance = $modal.open({
            templateUrl: 'contactUnitModal.html',
            controller: ContactUnitModalInstanceCtrl,
            resolve: {
                group: function () {
                    return group;
                },
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
