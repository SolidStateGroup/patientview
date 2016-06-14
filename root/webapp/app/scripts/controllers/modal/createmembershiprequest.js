'use strict';
var CreateMembershipRequestModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'permissions', 'user',
    'GroupService', 'ConversationService', '$filter',
function ($scope, $rootScope, $modalInstance, permissions, user, GroupService, ConversationService, $filter) {

    var init = function() {
        if (ConversationService.userHasMessagingFeature()) {
            $scope.newConversation = {};
            $scope.newConversation.patient = user;

            $scope.permissions = permissions;
            $scope.showForm = true;
            $scope.modalLoading = true;
            $scope.loadingMessage = 'Loading Groups';

            // get public groups, only include groups of type UNIT or DISEASE_GROUP
            GroupService.getAllPublic().then(function(groups) {
                $scope.conversationGroups = [];
                groups.forEach(function(group) {
                    if (group.visibleToJoin) {
                        if (group.groupType.value === 'DISEASE_GROUP' || group.groupType.value === 'UNIT') {
                            $scope.conversationGroups.push(group);
                        }
                    }
                });
                $scope.modalLoading = false;
            });
        } else {
            $scope.warningMessage = 'You must have the Messaging feature added to your account prior to sending a ' +
                'membership request. This may be done on the Staff screen, clicking Edit on your account and adding ' +
                'the Messaging feature. Please contact the PatientView support team if assistance is needed.';
        }
    };

    var groupHasMessagingFeature = function(group) {
        for (var i=0; i<group.groupFeatures.length; i++) {
            if (group.groupFeatures[i].feature.name === 'MESSAGING') {
                return true;                
            }            
        }
        return false;
    };

    $scope.selectGroup = function(group) {
        delete $scope.errorMessage;
        delete $scope.newConversation.recipients;
        $scope.recipientsExist = false;
        
        if (groupHasMessagingFeature(group)) {
            $scope.modalLoading = true;
            $scope.loadingMessage = 'Loading Recipients';
            $scope.recipientsExist = false;

            ConversationService.getGroupRecipientsByFeature(group.id, 'DEFAULT_MESSAGING_CONTACT')
            .then(function (recipients) {
                if (recipients.length) {
                    $scope.newConversation.recipients = recipients;
                    $scope.newConversation.readOnlyMessage = 
                        'This user has requested that they be added to your group "'
                        + $scope.newConversation.selectedGroup.name
                        + '". I have seen an appropriate request/consent document. Thank you!';
                    $scope.recipientsExist = true;
                } else {
                    delete $scope.newConversation.recipients;
                    $scope.recipientsExist = false;
                    $scope.errorMessage = 'No default contact exists for this group. Please contact the PatientView support team for assistance.';
                }
                $scope.modalLoading = false;
            }, function (failureResult) {
                if (failureResult.status === 404) {
                    $scope.modalLoading = false;
                } else {
                    $scope.modalLoading = false;
                    $scope.errorMessage = 'Error loading message recipients';
                }
            });
        } else {
            $scope.recipientsExist = false;
            $scope.errorMessage = 'Group does not have messaging enabled. Please contact the PatientView support team for assistance.'
        }
    };

    $scope.createMembershipRequest = function() {
        $scope.modalLoading = true;
        $scope.showForm = false;
        $scope.loadingMessage = 'Sending Membership Request';

        // build correct conversation from newConversation
        var conversation = {}, i;
        conversation.type = 'MEMBERSHIP_REQUEST';
        conversation.title = 'Membership Request';
        conversation.messages = [];
        conversation.open = true;

        // build message
        var user = $scope.newConversation.patient;
        var userDetailsText = user.forename + ' ' + user.surname;
        if (user.dateOfBirth !== null && user.dateOfBirth.length) {
            userDetailsText += ' (date of birth: ' + $filter('date')(user.dateOfBirth, 'dd-MMM-yyyy') + ')'
        }
        userDetailsText += ', identifier(s) ';
        for (i=0; i<user.identifiers.length; i++) {
            userDetailsText += user.identifiers[i].identifier + ' ';      
        }
        
        var messageText = userDetailsText + '<br/>' + $scope.newConversation.readOnlyMessage;

        if ($scope.newConversation.additionalComments !== undefined 
            && $scope.newConversation.additionalComments.length) {
            messageText = messageText + '<br/>Additional Comments: ' + $scope.newConversation.additionalComments;
        }
        
        // add userId and groupId for audit purposes
        conversation.userId = user.id;
        conversation.groupId = $scope.newConversation.selectedGroup.id;

        var message = {};
        message.user = {};
        message.user.id = $scope.loggedInUser.id;
        message.message = messageText;
        message.type = 'MESSAGE';
        conversation.messages[0] = message;

        // add conversation users from list of users (temp anonymous = false)
        var conversationUsers = [];
        for (i=0; i<$scope.newConversation.recipients.length; i++) {
            conversationUsers[i] = {};
            conversationUsers[i].user = {};
            conversationUsers[i].user.id = $scope.newConversation.recipients[i].id;
            conversationUsers[i].anonymous = false;
        }

        // add logged in user to list of conversation users
        var conversationUser = {};
        conversationUser.user = {};
        conversationUser.user.id = $scope.loggedInUser.id;
        conversationUser.anonymous = false;
        conversationUsers.push(conversationUser);

        conversation.conversationUsers = conversationUsers;

        ConversationService.create($scope.loggedInUser, conversation).then(function() {
            $scope.successMessage = 'A local administrator for the selected group has been contacted with this request. You'
                + ' can track and follow up this request under your Messages menu.';
            $scope.modalLoading = false;
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
    
    init();
}];
