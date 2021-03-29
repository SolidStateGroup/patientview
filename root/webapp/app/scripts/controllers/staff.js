'use strict';

angular.module('patientviewApp').controller('StaffCtrl',['$rootScope', '$scope', '$compile', '$modal', '$timeout', 
    '$routeParams', 'UserService', 'Mixins',
    function ($rootScope, $scope, $compile, $modal, $timeout, $routeParams, UserService, Mixins) {

    // mixins for filters and shared code
    angular.extend($scope, Mixins);

    $scope.itemsPerPage = 10;
    $scope.currentPage = 0;
    $scope.sortField = 'surname';
    $scope.sortDirection = 'ASC';
    $scope.initFinished = false;
    $scope.selectedRole = [];
    $scope.selectedGroup = [];

    // update page when currentPage is changed
    $scope.$watch('currentPage', function(value) {
        delete $scope.successMessage;
        if ($scope.initFinished === true) {
            $scope.currentPage = value;
            $scope.getItems();
        }
    });

    // Get users based on current user selected filters etc
    $scope.getItems = function () {
        $scope.loading = true;
        var getParameters = {};
        getParameters.page = $scope.currentPage;
        getParameters.size = $scope.itemsPerPage;
        getParameters.sortField = $scope.sortField;
        getParameters.sortDirection = $scope.sortDirection;

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
                            || group.groupType.value === 'CENTRAL_SUPPORT'
                            || group.groupType.value === 'GENERAL_PRACTICE') {
                        $scope.showOtherGroupFilter = true;
                        $scope.filterOtherGroups.push(minimalGroup);
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

                // global admin can see all Roles, inc hidden IMPORTER role
                if ($scope.permissions.isSuperAdmin) {
                    role.visible = true;
                }

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
                    if ($scope.permissions.isSuperAdmin) {
                        roles[i].visible = true;
                    }
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
                // update email verified flag in case email changed
                $scope.editUser.emailVerified = successResult.emailVerified;
                for (var i = 0; i < $scope.pagedItems.length; i++) {
                    if ($scope.pagedItems[i].id === successResult.id) {
                        var headerDetails = $scope.pagedItems[i];
                        headerDetails.forename = successResult.forename;
                        headerDetails.surname = successResult.surname;
                        headerDetails.email = successResult.email;
                        headerDetails.emailVerified = successResult.emailVerified;
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
                templateUrl: 'views/modal/resetPasswordModal.html',
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
                templateUrl: 'views/modal/sendVerificationEmailModal.html',
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

    $scope.init();
}]);
