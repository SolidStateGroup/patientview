'use strict';

// find existing staff modal instance controller
var FindExistingStaffModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'permissions', 'allGroups', 'allowedRoles', 'UserService',
function ($scope, $rootScope, $modalInstance, permissions, allGroups, allowedRoles, UserService) {
    $scope.permissions = permissions;
    $scope.allGroups = allGroups;
    $scope.allowedRoles = allowedRoles;
    $scope.editMode = false;
    $scope.editUser = {};

    // click Find by username button
    $scope.findByUsername = function () {
        UserService.findByUsername($('#username').val()).then(function(result) {
            showUserOnScreen(result, "username");
        }, function () {
            $scope.warningMessage = 'No staff member exists with this username';
        });
    };

    // click Find by email button
    $scope.findByEmail = function () {
        UserService.findByEmail($('#email').val()).then(function(result) {
            showUserOnScreen(result, "email");
        }, function () {
            $scope.warningMessage = 'No staff member exists with this email';
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

// delete staff modal instance controller
var DeleteStaffModalInstanceCtrl = ['$scope', '$modalInstance','user','UserService',
function ($scope, $modalInstance, user, UserService) {
    $scope.user = user;
    $scope.successMessage = '';
    $scope.errorMessage = '';
    $scope.ok = function () {
        UserService.remove(user).then(function() {
            // successfully deleted user
            $modalInstance.close();
        }, function(failure) {
            // error
            $scope.errorMessage = 'There was an error: ' + failure;
        });
    };
    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
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

// Staff controller
angular.module('patientviewApp').controller('StaffCtrl',['$rootScope', '$scope', '$compile', '$modal', '$timeout', 
    '$routeParams', 'UserService',
    function ($rootScope, $scope, $compile, $modal, $timeout, $routeParams, UserService) {

    $scope.itemsPerPage = 10;
    $scope.currentPage = 0;
    $scope.sortField = 'surname';
    $scope.sortDirection = 'ASC';
    $scope.initFinished = false;
    $scope.searchItems = {};
    $scope.selectedRole = [];
    $scope.selectedGroup = [];

    // multi search
    $scope.search = function() {
        delete $scope.successMessage;
        $scope.searchItems.searchUsername = $('#search-username').val();
        $scope.searchItems.searchForename = $('#search-forename').val();
        $scope.searchItems.searchSurname = $('#search-surname').val();
        $scope.searchItems.searchEmail = $('#search-email').val();
        $scope.currentPage = 0;
        $scope.getItems();
    };

    // update page when currentPage is changed
    $scope.$watch('currentPage', function(value) {
        delete $scope.successMessage;
        if ($scope.initFinished === true) {
            $scope.currentPage = value;
            $scope.getItems();
        }
    });

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
    $scope.isGroupChecked = function (id) {
        if (_.contains($scope.selectedGroup, id)) {
            return 'glyphicon glyphicon-ok pull-right';
        }
        return false;
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
    $scope.removeSelectedGroup = function (group) {
        delete $scope.successMessage;
        $scope.selectedGroup.splice($scope.selectedGroup.indexOf(group.id), 1);
        $scope.currentPage = 0;
        $scope.getItems();
    };

    // filter users by role
    $scope.setSelectedRole = function () {
        var id = this.role.id;
        if (_.contains($scope.selectedRole, id)) {
            $scope.selectedRole = _.without($scope.selectedRole, id);
        } else {
            $scope.selectedRole.push(id);
        }
        $scope.currentPage = 0;
        $scope.getItems();
    };
    $scope.isRoleChecked = function (id) {
        if (_.contains($scope.selectedRole, id)) {
            return 'glyphicon glyphicon-ok pull-right';
        }
        return false;
    };
    $scope.removeAllSelectedRole = function () {
        $scope.selectedRole = [];
        $scope.currentPage = 0;
        $scope.getItems();
    };
    $scope.removeSelectedRole = function (role) {
        $scope.selectedRole.splice($scope.selectedRole.indexOf(role.id), 1);
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

    // Get users based on current user selected filters etc
    $scope.getItems = function () {
        $scope.loading = true;

        var getParameters = {};
        getParameters.page = $scope.currentPage;
        getParameters.size = $scope.itemsPerPage;
        getParameters.sortField = $scope.sortField;
        getParameters.sortDirection = $scope.sortDirection;

        // multi search
        getParameters.searchUsername = $scope.searchItems.searchUsername;
        getParameters.searchForename = $scope.searchItems.searchForename;
        getParameters.searchSurname = $scope.searchItems.searchSurname;
        getParameters.searchIdentifier = $scope.searchItems.searchIdentifier;
        getParameters.searchEmail = $scope.searchItems.searchEmail;

        // for filtering users by status (e.g. locked, active, inactive)
        getParameters.statusFilter = $scope.statusFilter;

        if ($scope.selectedGroup.length > 0) {
            getParameters.groupIds = $scope.selectedGroup;
        } else {
            getParameters.groupIds = [0];
        }
        if ($scope.selectedRole.length > 0) {
            getParameters.roleIds = $scope.selectedRole;
        } else {
            getParameters.roleIds = $scope.roleIds;
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

        $('body').click(function () {
            $('.child-menu').remove();
        });

        var i, role, group;
        $scope.loadingMessage = 'Loading Staff';
        $scope.loading = true;
        $scope.allGroups = [];
        $scope.allRoles = [];
        $scope.roleIds = [];
        $scope.groupMap = [];

        $scope.permissions = {};
        // used in html when checking for user group membership by id only (e.g. to show/hide delete on staff GroupRole)
        // A unit admin cannot remove staff from groups to which the unit admin is not assigned.
        $scope.permissions.allGroupsIds = [];

        // check if user is GLOBAL_ADMIN or SPECIALTY_ADMIN or UNIT_ADMIN
        $scope.permissions.isSuperAdmin = UserService.checkRoleExists('GLOBAL_ADMIN', $scope.loggedInUser);
        $scope.permissions.isSpecialtyAdmin = UserService.checkRoleExists('SPECIALTY_ADMIN', $scope.loggedInUser);
        $scope.permissions.isUnitAdmin = UserService.checkRoleExists('UNIT_ADMIN', $scope.loggedInUser);

        // only allow GLOBAL_ADMIN or SPECIALTY_ADMIN ...
        if ($scope.permissions.isSuperAdmin || $scope.permissions.isSpecialtyAdmin) {
            // to delete group membership in edit UI
            $scope.permissions.canDeleteGroupRolesDuringEdit = true;
        }

        // only allow GLOBAL_ADMIN or SPECIALTY_ADMIN or UNIT_ADMIN ...
        if ($scope.permissions.isSuperAdmin || $scope.permissions.isSpecialtyAdmin || $scope.permissions.isUnitAdmin) {
            // to see the option to delete patients in menu
            $scope.permissions.showDeleteMenuOption = true;
            // can reset passwords
            $scope.permissions.canResetPasswords = true;
            // can send verification emails
            $scope.permissions.canSendVerificationEmails = true;
        }

        var groups = $scope.loggedInUser.userInformation.userGroups;

        // show error if user is not a member of any groups
        if (groups.length !== 0) {
            $scope.filterUnitGroups = [];
            $scope.filterDiseaseGroupGroups = [];
            $scope.filterSpecialtyGroups = [];

            // set groups that can be chosen in UI, only show users from visible groups (assuming all users are in generic which is visible==false)
            for (i = 0; i < groups.length; i++) {
                group = groups[i];

                // global admin can see all groups
                if ($scope.permissions.isSuperAdmin) {
                    group.visible = true;
                }

                if (group.visible === true) {
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
                    } else if (group.groupType.value === 'DISEASE_GROUP') {
                        $scope.showDiseaseGroupFilter = true;
                        $scope.filterDiseaseGroupGroups.push(minimalGroup);
                    } else if (group.groupType.value === 'SPECIALTY') {
                        $scope.showSpecialtyFilter = true;
                        $scope.filterSpecialtyGroups.push(minimalGroup);
                    }
                }
            }

            // get staff type roles
            var roles = $scope.loggedInUser.userInformation.staffRoles;
            // set roles that can be chosen in UI, only show visible roles
            for (i = 0; i < roles.length; i++) {
                role = roles[i];
                if (role.visible === true) {
                    $scope.allRoles.push(role);
                    $scope.roleIds.push(role.id);
                }
            }

            // get list of roles available when user is adding a new Group & Role to staff member
            // e.g. unit admins cannot add specialty admin roles to staff members
            roles = $scope.loggedInUser.userInformation.securityRoles;
            // filter by roleId found previously as STAFF
            var allowedRoles = [];
            for (i = 0; i < roles.length; i++) {
                if ($scope.roleIds.indexOf(roles[i].id) != -1) {
                    allowedRoles.push(roles[i]);
                }
            }
            $scope.allowedRoles = allowedRoles;

            // get list of features available when user is adding a new Feature to staff members
            var allFeatures = $scope.loggedInUser.userInformation.staffFeatures;
            $scope.allFeatures = [];
            for (i = 0; i < allFeatures.length; i++) {
                $scope.allFeatures.push({'feature': allFeatures[i]});
            }

            // only applies to patients
            $scope.identifierTypes = [];

            $scope.initFinished = true;
        } else {
            // no groups found
            delete $scope.loading;
            $scope.fatalErrorMessage = 'No user groups found, cannot retrieve staff';
        }
    };

    // Opened for edit
    $scope.opened = function (openedUser) {
        $scope.successMessage = '';
        $scope.printSuccessMessage = false;
        $scope.editUser = '';
        $scope.editMode = true;
        $scope.saved = '';

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
                            } else if (user.availableFeatures[k].feature.name === 'CENTRAL_SUPPORT_CONTACT') {
                                // hide 'CENTRAL_SUPPORT_CONTACT'
                                user.availableFeatures.splice(k, 1);                                
                            }
                        }
                    }
                } else {
                    user.userFeatures = [];
                }

                // global admin can see all group roles of globaladmin
                if ($scope.permissions.isSuperAdmin) {
                    for(var i = 0; i < user.groupRoles.length; i++) {
                        if (user.groupRoles[i].role.name === 'GLOBAL_ADMIN') {
                            user.groupRoles[i].group.visible = true;
                        }
                    }
                }

                // set the user being edited to a clone of the existing user (so only updated in UI on save)
                $scope.editUser = _.clone(user);
                openedUser.editLoading = false;
            }, function(failureResult) {
                openedUser.showEdit = false;
                openedUser.editLoading = false;
                alert('Cannot open staff member: ' + failureResult.data);
            });
        }
    };

    // Save from edit
    $scope.save = function (editUserForm, user) {
        delete $scope.successMessage;
        
        UserService.save(user).then(function() {
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
    };

    // handle opening modal for finding existing staff by email
    $scope.openModalFindExistingStaff = function (size) {
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
            templateUrl: 'findExistingStaffModal.html',
            controller: FindExistingStaffModalInstanceCtrl,
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

    // delete user
    $scope.deleteUser = function (userId) {
        $scope.successMessage = '';
        $scope.printSuccessMessage = false;
        // close any open edit panels
        $('.panel-collapse.in').collapse('hide');

        UserService.get(userId).then(function(user) {
            var modalInstance = $modal.open({
                templateUrl: 'deleteStaffModal.html',
                controller: DeleteStaffModalInstanceCtrl,
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
                // closed, refresh list
                $scope.currentPage = 0;
                $scope.getItems();
                $scope.successMessage = 'User successfully deleted';
            }, function () {
                // closed
            });
        });
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

    $scope.removeStatusFilter = function() {
        delete $scope.statusFilter;
        $scope.getItems();
    };

    $scope.init();
}]);
