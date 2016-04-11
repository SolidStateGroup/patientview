'use strict';

// new conversation modal instance controller
var NewPatientConversationModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'GroupService', 'RoleService', 'ConversationUser',
    'UserService', 'ConversationService', 'UtilService',
    function ($scope, $rootScope, $modalInstance, GroupService, RoleService, ConversationUser, UserService, ConversationService, UtilService) {
        var i;

        var init = function() {
            delete $scope.errorMessage;
            $scope.newConversation = {};
            $scope.singlePatientConversation = true;
            $scope.newConversation.recipients = [];
            $scope.conversationGroups = [];

            var recipient = {};
            recipient.id = ConversationUser.id;
            var dob = new Date(ConversationUser.dateOfBirth);
            recipient.description = ConversationUser.surname + ", " + ConversationUser.forename +
                " (" + dob.getDate() + "-"+UtilService.getMonthTextShort(dob.getMonth()) + "-" + dob.getFullYear() + ")";
            $scope.newConversation.recipients.push(recipient);

            GroupService.getMessagingGroupsForUser($scope.loggedInUser.id).then(function(successResult) {
                var myGroups = [];
                var supportGroups = [];
                var otherGroups = [];

                // custom ordering
                for (var i = 0; i < successResult.length; i++) {
                    var group = successResult[i];
                    if (group.code !== 'Generic') {
                        if (group.groupType.value === 'CENTRAL_SUPPORT') {
                            supportGroups.push(group)
                        } else if (ConversationService.memberOfGroup(group)) {
                            group.groupType.value = 'MY_GROUP';
                            group.name = group.name + ' (' + group.groupType.description + ')';
                            group.groupType.description = 'My Groups';
                            myGroups.push(group);
                        } else {
                            group.groupType.value = 'OTHER_GROUP';
                            group.name = group.name + ' (' + group.groupType.description + ')';
                            group.groupType.description = 'Other Groups';
                            otherGroups.push(group);
                        }
                    }
                }

                $scope.conversationGroups = $scope.conversationGroups.concat(myGroups);
                $scope.conversationGroups = $scope.conversationGroups.concat(supportGroups);
                $scope.conversationGroups = $scope.conversationGroups.concat(otherGroups);
            }, function(failResult) {
                $scope.errorMessage = failResult.data;
            });
        };

        $scope.ok = function () {
            delete $scope.errorMessage;
            $scope.sendingMessage = true;

            // build correct conversation from newConversation
            var conversation = {};
            conversation.type = 'MESSAGE';
            conversation.title = $scope.newConversation.title;
            conversation.messages = [];
            conversation.open = true;

            // build message
            var message = {};
            message.user = {};
            message.user.id = $scope.loggedInUser.id;
            message.message = $scope.newConversation.message;
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
            delete $scope.errorMessage;
            $modalInstance.dismiss('cancel');
        };

        init();
    }];

// create membership request modal instance controller
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



// find existing patient modal instance controller
var FindExistingPatientModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'permissions', 'allGroups', 'allowedRoles', 'identifierTypes', 'UserService',
function ($scope, $rootScope, $modalInstance, permissions, allGroups, allowedRoles, identifierTypes, UserService) {
    $scope.permissions = permissions;
    $scope.allGroups = allGroups;
    $scope.allowedRoles = allowedRoles;
    $scope.identifierTypes = identifierTypes;
    $scope.editMode = false;
    $scope.editUser = {};

    // click Find by username button
    $scope.findByUsername = function () {
        UserService.findByUsername($('#username').val()).then(function(result) {
            showUserOnScreen(result, "username");
        }, function () {
            $scope.warningMessage = 'No patient exists with this username';
        });
    };

    // click Find by username button
    $scope.findByIdentifier = function () {
        UserService.findByIdentifier($('#identifier').val()).then(function(result) {
            showUserOnScreen(result, "identifier");
        }, function () {
            $scope.warningMessage = 'No patient exists with this identifier';
        });
    };

    var showUserOnScreen = function (result, searchType) {
        $scope.editUser = result;
        $scope.existingUser = true;
        $scope.editMode = true;
        $scope.warningMessage = 'A user with this '
            + searchType
            + ' already exists. Add them to your group if required, then close this window. '
            + 'You can then edit their details normally as they will appear in the refreshed list.';
        $scope.pagedItems = [];
        var i;

        // get user existing group/roles from groupRoles
        $scope.editUser.groups = [];
        for (i = 0; i < $scope.editUser.groupRoles.length; i++) {
            var groupRole = $scope.editUser.groupRoles[i];

            // global admin can see all group roles of globaladmin
            if ($scope.permissions.isSuperAdmin) {
                if (groupRole.role.name === 'GLOBAL_ADMIN') {
                    groupRole.group.visible = true;
                }
            }

            var group = groupRole.group;
            group.role = groupRole.role;
            $scope.editUser.groups.push(group);
        }

        // global admin can see all group roles of globaladmin
        if ($scope.permissions.isSuperAdmin) {
            for(i = 0; i < $scope.editUser.groupRoles.length; i++) {
                if ($scope.editUser.groupRoles[i].role.name === 'GLOBAL_ADMIN') {
                    $scope.editUser.groupRoles[i].group.visible = true;
                }
            }
        }

        // set available groups so user can add another group/role to the users existing group roles if required
        $scope.editUser.availableGroups = $scope.allGroups;
        for (i = 0; i < $scope.editUser.groups.length; i++) {
            $scope.editUser.availableGroups = _.without($scope.editUser.availableGroups,
                _.findWhere($scope.editUser.availableGroups, {id: $scope.editUser.groups[i].id}));
        }

        // set available user roles
        $scope.editUser.roles = $scope.allowedRoles;
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
}];

// delete patient modal instance controller
var DeletePatientModalInstanceCtrl = ['$scope', '$modalInstance','permissions','user','UserService','allGroups','allRoles','$q',
    function ($scope, $modalInstance, permissions, user, UserService, allGroups, allRoles, $q) {
        var i, j, inMyGroups = false, notMyGroupCount = 0;
        $scope.successMessage = '';
        $scope.errorMessage = '';
        $scope.user = user;

        // check if user can be removed from groups associated with logged in user
        $scope.user.canRemoveFromMyGroups = false;

        // check if user in other units (not specialties) but mine (not including Generic)
        for (i=0;i<allGroups.length;i++) {
            for (j=0;j<user.groupRoles.length;j++) {
                var groupRoleGroupCode = user.groupRoles[j].group.code;
                var groupRoleGroupType = user.groupRoles[j].group.groupType.value;

                if (groupRoleGroupCode !== 'Generic' && groupRoleGroupType !== 'SPECIALTY') {
                    if (allGroups[i].id === user.groupRoles[j].group.id) {
                        inMyGroups = true;
                    } else {
                        notMyGroupCount++;
                    }
                }
            }
        }

        // only allow removal from my group if a member of another group
        if (inMyGroups && (notMyGroupCount > 0)) {
            $scope.user.canRemoveFromMyGroups = true;
        }

        // check if any group in user's groupRoles has the KEEP_ALL_DATA feature, used during removeFromAllGroups to permanently delete
        $scope.user.keepData = false;

        for (i=0;i<user.groupRoles.length;i++) {
            for (j=0;j<user.groupRoles[i].group.groupFeatures.length;j++) {
                var feature = user.groupRoles[i].group.groupFeatures[j];
                if (feature.feature.name === 'KEEP_ALL_DATA') {
                    $scope.user.keepData = true;
                }
            }
        }

        // can be removed from all groups
        $scope.user.canRemoveFromAllGroups = true;

        // can delete permanently
        $scope.user.canDelete = permissions.canDeleteUsers;

        // remove from my groups
        $scope.removeFromMyGroups = function () {
            var promises = [];
            var groupsToRemove = [];
            var userNonSpecialtyGroups = [];

            // get userNonSpecialtyGroups
            for (j=0;j<user.groupRoles.length;j++) {
                var groupRoleGroupCode = user.groupRoles[j].group.code;
                var groupRoleGroupType = user.groupRoles[j].group.groupType.value;

                if (groupRoleGroupCode !== 'Generic' && groupRoleGroupType !== 'SPECIALTY') {
                    userNonSpecialtyGroups.push(user.groupRoles[j].group);
                }
            }

            // find intersection of userNonSpecialtyGroups and allGroups
            for (i = 0; i< allGroups.length; i++) {
                for (j = 0; j < userNonSpecialtyGroups.length; j++) {
                    if (allGroups[i].id === userNonSpecialtyGroups[j].id) {
                        groupsToRemove.push(allGroups[i]);
                    }
                }
            }

            // remove group roles from user where group is my unit with multiple deleteGroupRole
            for (i=0;i<groupsToRemove.length;i++) {
                for (j=0;j<allRoles.length;j++) {
                    promises.push(UserService.deleteGroupRole(user, groupsToRemove[i].id, allRoles[j].id));
                }
            }
            $q.all(promises).then(function () {
                $scope.user.canRemoveFromMyGroups = false;
                $scope.user.removedFromGroups = true;
            }, function() {
                $scope.errorMessage = 'There was an error';
            });
        };

        // remove from all units, then permanently delete if no Keep All Data feature available on units
        $scope.removeFromAllGroups = function () {
            var promises = [];

            // if keeping data remove group roles from user with multiple deleteGroupRole, otherwise delete permanently
            if ($scope.user.keepData) {
                promises.push(UserService.removeAllGroupRoles(user));
            } else {
                promises.push(UserService.remove(user));
            }

            $q.all(promises).then(function () {
                $scope.successMessage = 'Patient has been removed from all groups';
                $scope.user.canRemoveFromMyGroups = false;
                $scope.user.canRemoveFromAllGroups = false;

                if ($scope.user.keepData) {
                    $scope.successMessage += ' but data has not been permanently deleted.';
                } else {
                    $scope.successMessage += ' and data has been permanently deleted.';
                    $scope.user.canDelete = false;
                }
            }, function() {
                $scope.errorMessage = 'There was an error.';
            });
        };

        // delete patient permanently
        $scope.remove = function () {
            UserService.remove(user).then(function() {
                $scope.successMessage = 'Patient has been permanently deleted.';
                $scope.user.canRemoveFromMyGroups = false;
                $scope.user.canRemoveFromAllGroups = false;
                $scope.user.canDelete = false;
            }, function() {
                $scope.errorMessage = 'There was an error';
            });
        };

        $scope.cancel = function () {
            $modalInstance.close();
        };
    }];

// reset password modal instance controller
var ResetPasswordModalInstanceCtrl = ['$scope', '$modalInstance','user','UserService',
function ($scope, $modalInstance, user, UserService) {
    $scope.user = user;
    $scope.ok = function () {
        UserService.resetPassword(user).then(function(successResult) {
            // successfully reset user password
            $modalInstance.close(successResult);
        }, function() {
            // error
            $scope.errorMessage = 'There was an error';
        });
    };
    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
}];

// send verification email modal instance controller
var SendVerificationEmailModalInstanceCtrl = ['$scope', '$modalInstance','user','UserService',
function ($scope, $modalInstance, user, UserService) {
    $scope.user = user;
    $scope.ok = function () {
        UserService.sendVerificationEmail(user).then(function() {
            // successfully sent verification email
            $modalInstance.close();
        }, function(){
            // error
            $scope.errorMessage = 'There was an error';
        });
    };
    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
}];

// Patient controller
angular.module('patientviewApp').controller('PatientsCtrl',['$rootScope', '$scope', '$compile', '$modal', '$timeout', 
    '$location', '$routeParams', 'UserService', 'GroupService', 'RoleService', 'FeatureService', 'StaticDataService',
    'AuthService', 'localStorageService', 'UtilService', '$route', 'ConversationService', '$cookies',
    'DiagnosisService', 'CodeService', 'PatientService',
    function ($rootScope, $scope, $compile, $modal, $timeout, $location, $routeParams, UserService, GroupService,
        RoleService, FeatureService, StaticDataService, AuthService, localStorageService, UtilService, $route,
        ConversationService, $cookies, DiagnosisService, CodeService, PatientService) {

    $scope.itemsPerPage = 10;
    $scope.currentPage = 0;
    $scope.sortField = 'surname';
    $scope.sortDirection = 'ASC';
    $scope.initFinished = false;
    $scope.selectedGroup = [];

    $scope.canAddDiagnosis = function () {
        // only Cardiol specialty
        if ($scope.editUser) {
            for (var i=0; i<$scope.editUser.groupRoles.length; i++) {
                if ($scope.editUser.groupRoles[i].group.code === 'Cardiol') {
                    return true;
                }
            }
        }

        return false;
    };

    $scope.addDiagnosis = function (userId, selectedDiagnosis) {
        if (selectedDiagnosis !== undefined && selectedDiagnosis) {
            var diag = JSON.parse(selectedDiagnosis);
            DiagnosisService.add(userId, diag.code).then(function () {
                $scope.getUser($scope.editUser);
            }, function () {
                alert('Failed to add ' + diag.description + ' diagnosis with code "' + diag.code + '"');
            })
        }
    };

    // delete user
    $scope.deleteUser = function (userId) {
        $scope.successMessage = '';
        $scope.printSuccessMessage = false;
        // close any open edit panels
        $('.panel-collapse.in').collapse('hide');

        UserService.get(userId).then(function(user) {
            var modalInstance = $modal.open({
                templateUrl: 'deletePatientModal.html',
                controller: DeletePatientModalInstanceCtrl,
                resolve: {
                    permissions: function(){
                        return $scope.permissions;
                    },
                    user: function(){
                        return user;
                    },
                    UserService: function(){
                        return UserService;
                    },
                    allGroups: function(){
                        return $scope.allGroups;
                    },
                    allRoles: function(){
                        return $scope.allRoles;
                    }
                }
            });

            modalInstance.result.then(function () {
                // closed, refresh list
                $scope.currentPage = 0;
                $scope.getItems();
            }, function () {
                // closed
                $scope.currentPage = 0;
                $scope.getItems();
            });
        });
    };

    // Get users based on current user selected filters etc
    $scope.getItems = function () {
        $scope.loading = true;

        var getParameters = {};
        getParameters.page = $scope.currentPage;
        getParameters.size = $scope.itemsPerPage;
        getParameters.sortField = $scope.sortField;
        getParameters.sortDirection = $scope.sortDirection;
        getParameters.roleIds = $scope.roleIds;

        // multi search
        getParameters.searchUsername = $('#search-username').val();
        getParameters.searchForename = $('#search-forename').val();
        getParameters.searchSurname = $('#search-surname').val();
        getParameters.searchIdentifier = $('#search-identifier').val();
        getParameters.searchEmail = $('#search-email').val();

        // for filtering users by status (e.g. locked, active, inactive)
        getParameters.statusFilter = $scope.statusFilter;

        if ($scope.selectedGroup.length > 0) {
            getParameters.groupIds = $scope.selectedGroup;
        } else {
            getParameters.groupIds = [0];
        }

        // get staff users by list of staff roles and list of logged in user's groups
        UserService.getByGroupsAndRoles(getParameters).then(function (page) {
            $scope.pagedItems = page.content;
            $scope.total = page.totalElements;
            $scope.totalPages = page.totalPages;
            delete $scope.loading;
        }, function() {
            alert("Error retrieving users");
            delete $scope.loading;
        });
    };

    $scope.getUser = function(openedUser) {
        UserService.get(openedUser.id).then(function (user) {

            $scope.editing = true;
            user.roles = $scope.allowedRoles;

            // create list of available features (all - users existing features)
            user.availableFeatures = _.clone($scope.allFeatures);
            if (user.userFeatures) {
                for (var j = 0; j < user.userFeatures.length; j++) {
                    for (var k = 0; k < user.availableFeatures.length; k++) {
                        if (user.userFeatures[j].feature.id === user.availableFeatures[k].feature.id) {
                            user.availableFeatures.splice(k, 1);
                        }
                    }
                }
            } else {
                user.userFeatures = [];
            }

            // set date of birth dropdowns
            if (user.dateOfBirth != null) {
                user.selectedYear = user.dateOfBirth.split('-')[0].toString();
                user.selectedMonth = user.dateOfBirth.split('-')[1].toString();
                user.selectedDay = user.dateOfBirth.split('-')[2].toString();
            } else {
                user.selectedYear = '';
                user.selectedMonth = '';
                user.selectedDay = '';
            }

            // set the user being edited to a clone of the existing user (so only updated in UI on save)
            $scope.editUser = _.clone(user);

            // set role to first
            $scope.editUser.selectedRole = $scope.allowedRoles[0].id;

            // set latest staff entered diagnosis if present
            if ($scope.canAddDiagnosis()) {
                DiagnosisService.getStaffEntered(openedUser.id).then(function (conditions) {
                    if (conditions.length) {
                        var latest = conditions[0];
                        for (var i = 0; i < conditions.length; i++) {
                            if (conditions[i].date > latest.date) {
                                latest = conditions[i];
                            }
                        }
                        if (latest.status === 'confirmed') {
                            $scope.editUser.staffEnteredDiagnosis = latest;
                        }
                    }

                    $timeout(function() {
                        $('#select-diagnosis-' + $scope.editUser.id).selectize({
                            sortField: 'text'
                        });
                    });
                }, function () {
                    alert('Error retrieving staff entered condition information');
                });
            }

            // timeout required to send broadcast after everything else done
            $timeout(function() {
                getPatientManagement(user);
            });

            openedUser.editLoading = false;
        }, function(failureResult) {
            openedUser.showEdit = false;
            openedUser.editLoading = false;
            alert('Cannot open patient: ' + failureResult.data);
        });
    };

    var getPatientManagement = function (user) {
        // get patient management information based on group with IBD_PATIENT_MANAGEMENT feature
        var patientManagementGroupId = null;
        var patientManagementIdentifierId = null;
        var i, j;

        for (i = 0; i < user.groupRoles.length; i++) {
            if (patientManagementGroupId == null) {
                var group = user.groupRoles[i].group;
                if (group.groupFeatures != null && group.groupFeatures != undefined) {
                    for (j = 0; j < group.groupFeatures.length; j++) {
                        if (group.groupFeatures[j].feature.name === 'IBD_PATIENT_MANAGEMENT') {
                            patientManagementGroupId = group.id;
                        }
                    }
                }
            }
        }

        // based on first identifier
        for (i = 0; i < user.identifiers.length; i++) {
            if (patientManagementIdentifierId == null) {
                patientManagementIdentifierId = user.identifiers[i].id;
            }
        }

        if (patientManagementGroupId !== null && patientManagementIdentifierId !== null) {
            PatientService.getPatientManagement(user.id, patientManagementGroupId, patientManagementIdentifierId)
                .then(function (patientManagement) {
                    if (patientManagement !== undefined && patientManagement !== null) {
                        $scope.patientManagement = patientManagement;
                    } else {
                        $scope.patientManagement = {};
                    }

                    $scope.patientManagement.groupId = patientManagementGroupId;
                    $scope.patientManagement.identifierId = patientManagementIdentifierId;
                    $scope.patientManagement.userId = user.id;

                    $timeout(function() {
                        $scope.$broadcast('patientManagementInit', {});
                    });
                }, function () {
                    alert('Error retrieving patient management information');
                });
        }
    };

    // Init
    $scope.init = function () {
        $scope.initFinished = false;

        if ($routeParams.statusFilter !== undefined) {
            var allowedStatusFilters = ['ACTIVE', 'INACTIVE', 'LOCKED'];
            if (allowedStatusFilters.indexOf($routeParams.statusFilter.toUpperCase()) > -1) {
                $scope.statusFilter = $routeParams.statusFilter;
            }
        }

        if ($routeParams.groupId !== undefined && !isNaN($routeParams.groupId)) {
            $scope.selectedGroup.push(Number($routeParams.groupId));
        }

        $scope.months = UtilService.generateMonths();
        $scope.years = UtilService.generateYears();
        $scope.days = UtilService.generateDays();

        $('body').click(function () {
            $('.child-menu').remove();
        });

        var i, role, group;
        $scope.loadingMessage = 'Loading Patients';
        $scope.loading = true;
        $scope.allGroups = [];
        $scope.allRoles = [];
        $scope.roleIds = [];
        $scope.groupMap = [];

        $scope.permissions = {};
        // used in html when checking for user group membership by id only (e.g. to show/hide delete on patient GroupRole)
        // A unit admin cannot remove patient from groups to which the unit admin is not assigned.
        $scope.permissions.allGroupsIds = [];

        // check if user is GLOBAL_ADMIN or SPECIALTY_ADMIN or UNIT_ADMIN, todo: awaiting better security on users
        $scope.permissions.isSuperAdmin = UserService.checkRoleExists('GLOBAL_ADMIN', $scope.loggedInUser);
        $scope.permissions.isSpecialtyAdmin = UserService.checkRoleExists('SPECIALTY_ADMIN', $scope.loggedInUser);
        $scope.permissions.isUnitAdmin = UserService.checkRoleExists('UNIT_ADMIN', $scope.loggedInUser);

        // only allow GLOBAL_ADMIN or SPECIALTY_ADMIN ...
        if ($scope.permissions.isSuperAdmin || $scope.permissions.isSpecialtyAdmin) {
            $scope.permissions.canDeleteGroupRolesDuringEdit = true;
            // to see the option to permanently delete patients
            $scope.permissions.canDeleteUsers = true;
        }

        // only allow GLOBAL_ADMIN or SPECIALTY_ADMIN or UNIT_ADMIN ...
        if ($scope.permissions.isSuperAdmin || $scope.permissions.isSpecialtyAdmin || $scope.permissions.isUnitAdmin) {
            $scope.permissions.showDeleteMenuOption = true;
            $scope.permissions.showMembershipRequestMenuOption = true;
            $scope.permissions.canCreatePatients = true;
            $scope.permissions.canEditPatients = true;
            $scope.permissions.canResetPasswords = true;
            $scope.permissions.canSendVerificationEmails = true;
        }

        // STAFF_ADMIN, DISEASE_GROUP_ADMIN can only view
        if (!($scope.permissions.isSuperAdmin || $scope.permissions.isSpecialtyAdmin
            || $scope.permissions.isUnitAdmin)) {
            $scope.permissions.canViewPatients = true;
        }

        $scope.permissions.messagingEnabled = ConversationService.userHasMessagingFeature();

        // get diagnosis codes
        if ($scope.permissions.canEditPatients) {
            StaticDataService.getLookupsByType('CODE_TYPE').then(function(codeTypes) {
                if (codeTypes.length > 0) {
                    var arr = [];
                    for (var i=0; i<codeTypes.length; i++) {
                        if (codeTypes[i].value === 'DIAGNOSIS') {
                            arr.push(codeTypes[i].id);
                        }
                    }

                    var getParameters = {};
                    getParameters.codeTypes = arr;
                    getParameters.sortField = 'description';

                    CodeService.getAll(getParameters).then(function (page) {
                        $scope.diagnosisCodes = page.content;
                    }, function () {
                    });
                }
            });
        }

        // get patient type roles
        var roles = $scope.loggedInUser.userInformation.patientRoles;

        // handle back button to patients from dashboard
        if (roles === null) {
            $rootScope.switchUserBack();
            $timeout(function(){
                $route.reload();
            }, 3000);
        } else {

            // set roles that can be chosen in UI, only show visible roles
            for (i = 0; i < roles.length; i++) {
                role = roles[i];
                if (role.visible === true) {
                    $scope.allRoles.push(role);
                    $scope.roleIds.push(role.id);
                }
            }

            // get logged in user's groups
            var groups = $scope.loggedInUser.userInformation.userGroups;

            // show error if user is not a member of any groups
            if (groups.length !== 0) {
                $scope.filterUnitGroups = [];
                $scope.filterOtherGroups = [];
                $scope.filterSpecialtyGroups = [];

                // set groups that can be chosen in UI, only show users from visible groups (assuming all users are in generic which is visible==false)
                for (i = 0; i < groups.length; i++) {
                    group = groups[i];

                    // global admin can see all groups
                    if ($scope.permissions.isSuperAdmin) {
                        group.visible = true;
                    }

                    if (group.visible === true && group.code !== 'Generic') {
                        var minimalGroup = {};
                        minimalGroup.id = group.id;
                        minimalGroup.shortName = group.shortName;
                        minimalGroup.name = group.name;
                        minimalGroup.groupType = {};
                        minimalGroup.groupType.value = group.groupType.value;
                        minimalGroup.groupType.description = group.groupType.description;
                        $scope.allGroups.push(minimalGroup);
                        $scope.permissions.allGroupsIds[group.id] = group.id;
                        $scope.groupMap[group.id] = group;

                        if (group.groupType.value === 'UNIT') {
                            $scope.showUnitFilter = true;
                            $scope.filterUnitGroups.push(minimalGroup);
                        } else if (group.groupType.value === 'DISEASE_GROUP'
                            || group.groupType.value === 'GENERAL_PRACTICE') {
                            $scope.showOtherGroupFilter = true;
                            $scope.filterOtherGroups.push(minimalGroup);
                        } else if (group.groupType.value === 'SPECIALTY') {
                            $scope.showSpecialtyFilter = true;
                            $scope.filterSpecialtyGroups.push(minimalGroup);
                        }
                    }
                }

                // get list of roles available when user is adding a new Group & Role to patient member
                // e.g. unit admins cannot add specialty admin roles to patient members
                roles = $scope.loggedInUser.userInformation.securityRoles;
                // filter by roleId found previously as PATIENT
                var allowedRoles = [];
                for (i = 0; i < roles.length; i++) {
                    if ($scope.roleIds.indexOf(roles[i].id) != -1) {
                        allowedRoles.push(roles[i]);
                    }
                }
                $scope.allowedRoles = allowedRoles;

                // get list of features available when user is adding a new Feature to patient members
                var allFeatures = $scope.loggedInUser.userInformation.patientFeatures;
                $scope.allFeatures = [];
                for (i = 0; i < allFeatures.length; i++) {
                    $scope.allFeatures.push({'feature': allFeatures[i]});
                }

                // get list of identifier types when user adding identifiers to patient members
                $scope.identifierTypes = [];
                StaticDataService.getLookupsByType('IDENTIFIER').then(function (identifierTypes) {
                    if (identifierTypes.length > 0) {
                        var noHospitalNumber = [];
                        for (i = 0; i < identifierTypes.length; i++) {
                            if (identifierTypes[i].value !== 'HOSPITAL_NUMBER') {
                                noHospitalNumber.push(identifierTypes[i]);
                            }
                        }
                        $scope.identifierTypes = noHospitalNumber;
                    }
                });

                $scope.initFinished = true;
            } else {
                // no groups found
                delete $scope.loading;
                $scope.fatalErrorMessage = 'No user groups found, cannot retrieve patients';
            }
        }
    };

    $scope.isGroupChecked = function (id) {
        if (_.contains($scope.selectedGroup, id)) {
            return 'glyphicon glyphicon-ok pull-right';
        }
        return false;
    };

    // Opened for edit
    $scope.opened = function (openedUser) {
        $scope.successMessage = '';
        $scope.printSuccessMessage = false;
        $scope.editUser = '';
        $scope.editMode = true;
        $scope.saved = '';
        delete $scope.patientManagement;

        // do not load if already opened
        if (openedUser.showEdit) {
            $scope.editCode = '';
            openedUser.showEdit = false;
        } else {
            // close others
            for (var i = 0; i < $scope.pagedItems.length; i++) {
                $scope.pagedItems[i].showEdit = false;
            }

            $scope.editCode = '';
            openedUser.showEdit = true;
            openedUser.editLoading = true;

            // now using lightweight group list, do GET on id to get full group and populate editGroup
            $scope.getUser(openedUser)
        }
    };

    // handle opening modal for creating membership request
    $scope.openModalCreateMembershipRequest = function (user) {
        // close any open edit panels
        for (var i = 0; i < $scope.pagedItems.length; i++) {
            $scope.pagedItems[i].showEdit = false;
        }
        // clear messages
        $scope.errorMessage = '';
        $scope.warningMessage = '';
        $scope.successMessage = '';
        $scope.printSuccessMessage = false;

        // open modal and pass in required objects for use in modal scope
        var modalInstance = $modal.open({
            templateUrl: 'createMembershipRequestModal.html',
            controller: CreateMembershipRequestModalInstanceCtrl,
            size: 'lg',
            backdrop: 'static',
            resolve: {
                ConversationService : function() {
                    return ConversationService;
                },
                GroupService: function() {
                    return GroupService;
                },
                permissions: function() {
                    return $scope.permissions;
                },
                user: function() {
                    return user;
                }
            }
        });

        // handle modal close (via button click)
        modalInstance.result.then(function () {
            // no ok button, do nothing
        }, function () {
            $scope.getItems();
        });
    };

    // handle opening modal for finding existing patient by identifier value
    $scope.openModalFindExistingPatient = function (size) {
        // close any open edit panels
        for (var i = 0; i < $scope.pagedItems.length; i++) {
            $scope.pagedItems[i].showEdit = false;
        }
        // clear messages
        $scope.errorMessage = '';
        $scope.warningMessage = '';
        $scope.successMessage = '';
        $scope.printSuccessMessage = false;

        // open modal and pass in required objects for use in modal scope
        var modalInstance = $modal.open({
            templateUrl: 'findExistingPatientModal.html',
            controller: FindExistingPatientModalInstanceCtrl,
            size: size,
            backdrop: 'static',
            resolve: {
                permissions: function(){
                    return $scope.permissions;
                },
                allGroups: function(){
                    return $scope.allGroups;
                },
                allowedRoles: function(){
                    return $scope.allowedRoles;
                },
                identifierTypes: function(){
                    return $scope.identifierTypes;
                },
                UserService: function(){
                    return UserService;
                }
            }
        });

        // handle modal close (via button click)
        modalInstance.result.then(function () {
            // no ok button, do nothing
        }, function () {
            $scope.getItems();
        });
    };

    // open modal for new conversation
    $scope.openModalNewConversation = function (size, user) {
        delete $scope.errorMessage;
        delete $scope.successMessage;

        $scope.conversationUser = user;

        // open modal
        var modalInstance = $modal.open({
            templateUrl: 'newConversationModal.html',
            controller: NewPatientConversationModalInstanceCtrl,
            size: size,
            backdrop: 'static',
            resolve: {
                ConversationService: function () {
                    return ConversationService;
                },
                GroupService: function () {
                    return GroupService;
                },
                ConversationUser: function(){
                    return user;
                },
                UtilService: function(){
                    return UtilService;
                }
            }
        });

        modalInstance.result.then(function () {
            $scope.successMessage = 'Successfully sent message';
        }, function () {
            $scope.editConversation = '';
        });
    };

    $scope.printSuccessMessageCompat = function() {
        // ie8 compatibility
        var printContent = $('#success-message').clone();
        printContent.children('.print-success-message').remove();
        var windowUrl = 'PatientView';
        var uniqueName = new Date();
        var windowName = 'Print' + uniqueName.getTime();
        var printWindow = window.open(windowUrl, windowName, 'left=50000,top=50000,width=0,height=0');
        printWindow.document.write(printContent.html());
        printWindow.document.close();
        printWindow.focus();
        printWindow.print();
        printWindow.close();
    };

    $scope.removeAllSelectedGroup = function (groupType) {
        delete $scope.successMessage;
        var newSelectedGroupList = [];

        for (var i=0; i<$scope.selectedGroup.length; i++) {
            if ($scope.groupMap[$scope.selectedGroup[i]].groupType.value !== groupType) {
                newSelectedGroupList.push($scope.selectedGroup[i]);
            }
        }

        $scope.selectedGroup = newSelectedGroupList;
        $scope.currentPage = 0;
        $scope.getItems();
    };

    $scope.removeDiagnosis = function () {
        DiagnosisService.removeStaffEntered($scope.editUser.id).then(function() {
            $scope.getUser($scope.editUser);
        }, function() {
            alert('Failed to remove diagnosis.');
        })
    };

    $scope.removeSelectedGroup = function (group) {
        delete $scope.successMessage;
        $scope.selectedGroup.splice($scope.selectedGroup.indexOf(group.id), 1);
        $scope.currentPage = 0;
        $scope.getItems();
    };

    $scope.removeStatusFilter = function() {
        delete $scope.statusFilter;
        $scope.getItems();
    };

    // reset user password
    $scope.resetUserPassword = function (userId) {
        $scope.successMessage = '';
        $scope.printSuccessMessage = false;

        UserService.get(userId).then(function(user) {
            var modalInstance = $modal.open({
                templateUrl: 'views/partials/resetPasswordModal.html',
                controller: ResetPasswordModalInstanceCtrl,
                resolve: {
                    user: function(){
                        return user;
                    },
                    UserService: function(){
                        return UserService;
                    }
                }
            });

            modalInstance.result.then(function (successResult) {
                $scope.printSuccessMessage = true;
                $scope.successMessage = 'Password reset for ' + user.forename + ' ' + user.surname
                    + ' (username: ' + user.username + '), new password is: ' + successResult.password;
            }, function () {
                // closed
            });
        });
    };

    // Save from edit
    $scope.save = function (editUserForm, user) {
        delete $scope.successMessage;

        if ((user.selectedDay != '' || user.selectedMonth != '' || user.selectedYear != '')
            && !UtilService.validationDateNoFuture(user.selectedDay, user.selectedMonth, user.selectedYear)
            && !(user.selectedDay == '' && user.selectedMonth == '' && user.selectedYear == '')) {
            alert('Please enter a valid date of birth (and not in the future)');
        } else {
            user.dateOfBirth = null;
            UserService.save(user).then(function () {
                // successfully saved user
                editUserForm.$setPristine(true);
                $scope.saved = true;

                // update accordion header for group with data from GET
                UserService.get(user.id).then(function (successResult) {
                    for (var i = 0; i < $scope.pagedItems.length; i++) {
                        if ($scope.pagedItems[i].id === successResult.id) {
                            var headerDetails = $scope.pagedItems[i];
                            headerDetails.forename = successResult.forename;
                            headerDetails.surname = successResult.surname;
                            headerDetails.email = successResult.email;
                            headerDetails.dateOfBirth = successResult.dateOfBirth;
                        }
                    }
                }, function () {
                    alert('Error updating header (saved successfully)');
                });

                $scope.successMessage = 'User saved';
            }, function (failureResult) {
                if (failureResult.status === 409) {
                    // conflict (already exists)
                    alert('Cannot save User: ' + failureResult.data);
                } else {
                    alert('Cannot save User: ' + failureResult.data);
                }
            });
        }
    };

    // multi search
    $scope.search = function() {
        delete $scope.successMessage;
        $scope.currentPage = 0;
        $scope.getItems();
    };

    // send verification email
    $scope.sendVerificationEmail = function (userId) {
        $scope.printSuccessMessage = false;
        $scope.successMessage = '';

        UserService.get(userId).then(function(user) {
            var modalInstance = $modal.open({
                templateUrl: 'views/partials/sendVerificationEmailModal.html',
                controller: SendVerificationEmailModalInstanceCtrl,
                resolve: {
                    user: function(){
                        return user;
                    },
                    UserService: function(){
                        return UserService;
                    }
                }
            });

            modalInstance.result.then(function () {
                $scope.successMessage = 'Verification email has been sent';
            }, function () {
                // closed
            });
        });
    };

    // filter users by group
    $scope.setSelectedGroup = function () {
        delete $scope.successMessage;
        var id = this.group.id;
        if (_.contains($scope.selectedGroup, id)) {
            $scope.selectedGroup = _.without($scope.selectedGroup, id);
        } else {
            $scope.selectedGroup.push(id);
        }
        $scope.currentPage = 0;
        $scope.getItems();
    };

    $scope.sortBy = function(sortField) {
        delete $scope.successMessage;
        $scope.currentPage = 0;
        if ($scope.sortField !== sortField) {
            $scope.sortDirection = 'ASC';
            $scope.sortField = sortField;
        } else {
            if ($scope.sortDirection === 'ASC') {
                $scope.sortDirection = 'DESC';
            } else {
                $scope.sortDirection = 'ASC';
            }
        }

        $scope.getItems();
    };

    // view patient
    $scope.viewUser = function (userId) {
        $scope.loadingMessage = 'Viewing Patient';
        $scope.loading = true;
        $scope.successMessage = '';
        $scope.printSuccessMessage = false;
        $rootScope.switchingUser = true;
        var currentToken = $rootScope.authToken;

        AuthService.switchUser(userId, null).then(function(authToken) {

            $rootScope.previousAuthToken = currentToken;
            localStorageService.set('previousAuthToken', currentToken);

            $rootScope.previousLoggedInUser = $scope.loggedInUser;
            localStorageService.set('previousLoggedInUser', $scope.loggedInUser);

            $rootScope.previousLocation = '/patients';
            localStorageService.set('previousLocation', '/patients');

            $rootScope.authToken = authToken;
            $cookies.authToken = authToken;
            localStorageService.set('authToken', authToken);

            // get user information, store in session
            AuthService.getUserInformation({'token' : authToken}).then(function (userInformation) {

                var user = userInformation.user;
                delete userInformation.user;
                user.userInformation = userInformation;

                $rootScope.loggedInUser = user;
                localStorageService.set('loggedInUser', user);

                $rootScope.routes = userInformation.routes;
                localStorageService.set('routes', userInformation.routes);

                $scope.loading = false;
                $location.path('/dashboard');
                delete $rootScope.switchingUser;

            }, function() {
                alert('Error receiving user information');
                $scope.loading = false;
            });

        }, function() {
            alert('Cannot view patient');
            $scope.loading = false;
            delete $rootScope.switchingUser;
        });
    };

    // update page when currentPage is changed
    $scope.$watch('currentPage', function(value) {
        delete $scope.successMessage;
        if ($scope.initFinished === true) {
            $scope.currentPage = value;
            $scope.getItems();
        }
    });

    $scope.init();
}]);
