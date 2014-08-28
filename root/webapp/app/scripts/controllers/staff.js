'use strict';

// todo: consider controllers in separate files

// new staff modal instance controller
var NewStaffModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'permissions', 'newUser', 'allGroups', 'allowedRoles', 'allFeatures', 'identifierTypes', 'UserService',
function ($scope, $rootScope, $modalInstance, permissions, newUser, allGroups, allowedRoles, allFeatures, identifierTypes, UserService) {
    $scope.permissions = permissions;
    $scope.editUser = newUser;
    $scope.allGroups = allGroups;
    $scope.allowedRoles = allowedRoles;
    $scope.identifierTypes = identifierTypes;
    $scope.editMode = false;

    // set initial group and feature (avoid blank option)
    if ($scope.editUser.availableGroups && $scope.editUser.availableGroups.length > 0) {
        $scope.editUser.groupToAdd = $scope.editUser.availableGroups[0].id;
    }
    if ($scope.editUser.availableFeatures && $scope.editUser.availableFeatures.length > 0) {
        $scope.editUser.featureToAdd = $scope.editUser.availableFeatures[0].feature.id;
    }

    // click Create New button
    $scope.new = function () {
        var i;

        UserService.new($scope.editUser).then(function(result) {
            // successfully created new staff user
            $scope.editUser = result;
            $scope.editUser.isNewUser = true;
            $modalInstance.close($scope.editUser);
        }, function(result) {
            if (result.status === 409) {
                // 409 = CONFLICT, means staff already exists, provide UI to edit existing staff group roles
                $scope.warningMessage = 'A staff member with this username or email already exists, you can add them to your group if required.';
                $scope.editUser = result.data;
                $scope.existingUser = true;
                $scope.editMode = true;
                $scope.pagedItems = [];

                // get staff existing group/roles from groupRoles
                $scope.editUser.groups = [];
                for(i=0; i<$scope.editUser.groupRoles.length; i++) {
                    var groupRole = $scope.editUser.groupRoles[i];
                    var group = groupRole.group;
                    group.role = groupRole.role;
                    $scope.editUser.groups.push(group);
                }

                // set available groups so user can add another group/role to the staff members existing group roles if required
                $scope.editUser.availableGroups = $scope.allGroups;
                for (i=0; i<$scope.editUser.groups.length; i++) {
                    $scope.editUser.availableGroups = _.without($scope.editUser.availableGroups, _.findWhere($scope.editUser.availableGroups, {id: $scope.editUser.groups[i].id}));
                }

                // set available staff roles
                $scope.editUser.roles = $scope.allowedRoles;

            } else {
                // Other errors treated as standard errors
                $scope.errorMessage = 'There was an error: ' + result.data;
            }
        });
    };

    // click Update Existing button, (after finding staff already exists)
    $scope.edit = function () {
        UserService.save($scope.editUser).then(function(result) {
            // successfully saved existing user
            $scope.editUser.isNewUser = false;
            $modalInstance.close($scope.editUser);
        }, function(result) {
            $scope.errorMessage = 'There was an error: ' + result.data;
        });
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
}];

// delete staff modal instance controller
var DeleteStaffModalInstanceCtrl = ['$scope', '$modalInstance','user','UserService',
function ($scope, $modalInstance, user, UserService) {
    $scope.user = user;
    $scope.ok = function () {
        UserService.delete(user).then(function() {
            // successfully deleted user
            $modalInstance.close();
        }, function() {
            // error
            $scope.errorMessage = 'There was an error';
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
angular.module('patientviewApp').controller('StaffCtrl',['$rootScope', '$scope', '$compile', '$modal', '$timeout', 'UserService', 'GroupService', 'RoleService', 'FeatureService', 'SecurityService', 'StaticDataService',
    function ($rootScope, $scope, $compile, $modal, $timeout, UserService, GroupService, RoleService, FeatureService, SecurityService, StaticDataService) {

    $scope.itemsPerPage = 20;
    $scope.currentPage = 0;
    $scope.filterText = '';
    $scope.sortField = '';
    $scope.sortDirection = '';
    $scope.initFinished = false;

    var tempFilterText = '';
    var filterTextTimeout;

    // watches
    // update page on user typed search text
    $scope.$watch('searchText', function (value) {
        if (value != undefined) {
            if (filterTextTimeout) $timeout.cancel(filterTextTimeout);
            $scope.currentPage = 0;

            tempFilterText = value;
            filterTextTimeout = $timeout(function () {
                $scope.filterText = tempFilterText;
                $scope.getItems();
            }, 1000); // delay 1000 ms
        }
    });

    // update page when currentPage is changed
    $scope.$watch("currentPage", function(value) {
        if ($scope.initFinished == true) {
            $scope.currentPage = value;
            $scope.getItems();
        }
    });

    // filter users by group
    $scope.selectedGroup = [];
    $scope.setSelectedGroup = function () {
        var id = this.group.id;
        if (_.contains($scope.selectedGroup, id)) {
            $scope.selectedGroup = _.without($scope.selectedGroup, id);
        } else {
            $scope.selectedGroup.push(id);
        }
        $scope.getItems();
    };
    $scope.isGroupChecked = function (id) {
        if (_.contains($scope.selectedGroup, id)) {
            return 'glyphicon glyphicon-ok pull-right';
        }
        return false;
    };
    $scope.removeAllSelectedGroup = function () {
        $scope.selectedGroup = [];
        $scope.getItems();
    };
        
    // filter users by role
    $scope.selectedRole = [];
    $scope.setSelectedRole = function () {
        var id = this.role.id;
        if (_.contains($scope.selectedRole, id)) {
            $scope.selectedRole = _.without($scope.selectedRole, id);
        } else {
            $scope.selectedRole.push(id);
        }
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
        $scope.getItems();
    };

    $scope.pageCount = function() {
        return Math.ceil($scope.total/$scope.itemsPerPage);
    };

    $scope.range = function() {
        var rangeSize = 10;
        var ret = [];
        var start;

        if ($scope.currentPage < 10) {
            start = 0;
        } else {
            start = $scope.currentPage;
        }

        if ( start > $scope.pageCount()-rangeSize ) {
            start = $scope.pageCount()-rangeSize;
        }

        for (var i=start; i<start+rangeSize; i++) {
            if (i > -1) {
                ret.push(i);
            }
        }

        return ret;
    };

    $scope.setPage = function(n) {
        if (n > -1 && n < $scope.totalPages) {
            $scope.currentPage = n;
        }
    };

    $scope.prevPage = function() {
        if ($scope.currentPage > 0) {
            $scope.currentPage--;
        }
    };

    $scope.prevPageDisabled = function() {
        return $scope.currentPage === 0 ? "hidden" : "";
    };

    $scope.nextPage = function() {
        if ($scope.currentPage < $scope.totalPages - 1) {
            $scope.currentPage++;
        }
    };

    $scope.nextPageDisabled = function() {
        return $scope.currentPage === $scope.pageCount() - 1 ? "disabled" : "";
    };

    $scope.sortBy = function(sortField) {
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

    // Get staff based on current user selected filters etc
    $scope.getItems = function () {
        $scope.loading = true;

        var getParameters = {};
        getParameters.page = $scope.currentPage;
        getParameters.size = $scope.itemsPerPage;
        getParameters.filterText = $scope.filterText;
        getParameters.sortField = $scope.sortField;
        getParameters.sortDirection = $scope.sortDirection;

        if ($scope.selectedGroup.length > 0) {
            getParameters.groupIds = $scope.selectedGroup;
        } else {
            getParameters.groupIds = $scope.groupIds;
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
        });
    };

    // Init
    $scope.init = function () {
        $('body').click(function () {
            $('.child-menu').remove();
        });

        var i, role, group;
        $scope.loading = true;
        $scope.allGroups = [];
        $scope.allRoles = [];
        $scope.roleIds = [];
        $scope.groupIds = [];

        // TODO: set permissions for ui
        $scope.permissions = {};
        // used in html when checking for user group membership by id only (e.g. to show/hide delete on staff GroupRole)
        // A unit admin cannot remove staff from groups to which the unit admin is not assigned.
        $scope.permissions.allGroupsIds = [];

        GroupService.getGroupsForUser($scope.loggedInUser.id).then(function (groups) {
            $scope.initFinished = false;
            groups = groups.content;
            // sort groups by name
            groups = _.sortBy(groups, 'name' );

            // show error if user is not a member of any groups
            if (groups.length !== 0) {

                // set groups that can be chosen in UI, only show users from visible groups (assuming all users are in generic which is visible==false)
                for (i = 0; i < groups.length; i++) {
                    group = groups[i];
                    if (group.visible === true) {
                        $scope.allGroups.push(group);
                        $scope.groupIds.push(group.id);
                        $scope.permissions.allGroupsIds[group.id] = group.id;
                    }
                }

                // get staff type roles
                RoleService.getByType('STAFF').then(function(roles) {

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
                    SecurityService.getSecurityRolesByUser($rootScope.loggedInUser.id).then(function (roles) {
                        // filter by roleId found previously as STAFF
                        var allowedRoles = [];
                        for (i = 0; i < roles.length; i++) {
                            if ($scope.roleIds.indexOf(roles[i].id) != -1) {
                                allowedRoles.push(roles[i]);
                            }
                        }
                        $scope.allowedRoles = allowedRoles;
                    });

                    // get list of features available when user is adding a new Feature to staff members
                    FeatureService.getAllStaffFeatures().then(function (allFeatures) {
                        $scope.allFeatures = [];
                        for (var i = 0; i < allFeatures.length; i++) {
                            $scope.allFeatures.push({'feature': allFeatures[i]});
                        }
                    });

                    // only applies to patients
                    $scope.identifierTypes = [];

                    $scope.initFinished = true;
                    $scope.getItems();
                });


            } else {
                // no groups found
                delete $scope.loading;
                $scope.fatalErrorMessage = 'No user groups found, cannot retrieve staff';
            }
        }, function () {
            // error retrieving groups
            delete $scope.loading;
            $scope.fatalErrorMessage = 'Error retrieving user groups, cannot retrieve staff';
        });


    };

    // Opened for edit
    $scope.opened = function (openedUser) {
        $scope.successMessage = '';
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

            // now using lightweight group list, do GET on id to get full group and populate editGroup
            UserService.get(openedUser.id).then(function (user) {

                $scope.editing = true;
                user.roles = $scope.allowedRoles;

                // for REST compatibility, convert staff member groupRoles to objects suitable for UI
                user.groups = [];
                for (var h = 0; h < user.groupRoles.length; h++) {
                    if (user.groupRoles[h].role.name !== 'MEMBER') {
                        var groupRole = user.groupRoles[h];
                        var group = groupRole.group;
                        group.role = groupRole.role;
                        user.groups.push(group);
                    }
                }

                // create list of available groups (all - staff members existing groups)
                user.availableGroups = $scope.allGroups;
                if (user.groups) {
                    for (var i = 0; i < user.groups.length; i++) {
                        user.availableGroups = _.without(user.availableGroups, _.findWhere(user.availableGroups, {id: user.groups[i].id}));
                    }
                }
                else {
                    user.groups = [];
                }

                // create list of available features (all - staff members existing features)
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

                // set the staff member being edited to a clone of the existing staff member (so only updated in UI on save)
                $scope.editUser = _.clone(user);

                // set initial group and feature (avoid blank <select> option)
                if ($scope.editUser.availableGroups[0]) {
                    $scope.editUser.groupToAdd = $scope.editUser.availableGroups[0].id;
                }
                if ($scope.editUser.availableFeatures[0]) {
                    $scope.editUser.featureToAdd = $scope.editUser.availableFeatures[0].feature.id;
                }
            });
        }
    };

    // Save from edit
    $scope.save = function (editUserForm, user, index) {
        UserService.save(user).then(function() {
            // successfully saved user
            editUserForm.$setPristine(true);
            $scope.saved = true;

            // update accordion header for group with data from GET
            UserService.get(user.id).then(function (successResult) {
                for(var i=0;i<$scope.pagedItems.length;i++) {
                    if($scope.pagedItems[i].id == successResult.id) {
                        var headerDetails = $scope.pagedItems[i];
                        headerDetails.forename = successResult.forename;
                        headerDetails.surname = successResult.surname;
                        headerDetails.email = successResult.email;
                    }
                }
            }, function () {
                // failure
                alert('Error updating header (saved successfully)');
            });

            $scope.successMessage = 'User saved';
        });
    };

    // handle opening modal (Angular UI Modal http://angular-ui.github.io/bootstrap/)
    $scope.openModalNewStaff = function (size) {
        // close any open edit panels
        for (var i = 0; i < $scope.pagedItems.length; i++) {
            $scope.pagedItems[i].showEdit = false;
        }
        // clear messages
        $scope.errorMessage = '';
        $scope.warningMessage = '';
        $scope.successMessage = '';
        $scope.userCreated = '';

        // create new user with list of available roles, groups and features
        $scope.editUser = {};
        $scope.editUser.roles = $scope.allowedRoles;
        $scope.editUser.availableGroups = $scope.allGroups;
        $scope.editUser.groups = [];
        $scope.editUser.availableFeatures = _.clone($scope.allFeatures);
        $scope.editUser.userFeatures = [];
        $scope.editUser.selectedRole = '';
        $scope.editUser.identifiers = [];

        // open modal and pass in required objects for use in modal scope
        var modalInstance = $modal.open({
            templateUrl: 'newStaffModal.html',
            controller: NewStaffModalInstanceCtrl,
            size: size,
            resolve: {
                permissions: function(){
                    return $scope.permissions;
                },
                newUser: function(){
                    return $scope.editUser;
                },
                allGroups: function(){
                    return $scope.allGroups;
                },
                allowedRoles: function(){
                    return $scope.allowedRoles;
                },
                allFeatures: function(){
                    return $scope.allFeatures;
                },
                identifierTypes: function(){
                    return $scope.identifierTypes;
                },
                UserService: function(){
                    return UserService;
                },
                SecurityService: function(){
                    return SecurityService;
                }
            }
        });

        // handle modal close (via button click)
        modalInstance.result.then(function (user) {
            // check if staff member is newly created
            if (user.isNewUser) {
                // is a new staff member, add to end of list and show username and password
                $scope.currentPage = 0;
                $scope.getItems();
                $scope.editUser = '';
                $scope.successMessage = 'User successfully created ' +
                    'with username: "' + user.username + '" ' +
                    'and password: "' + user.password + '"';
                $scope.userCreated = true;
            } else {
                // is an already existing staff member, likely updated group roles
                var index = null;
                for (var i = 0; i < $scope.pagedItems.length; i++) {
                    if (user.id === $scope.pagedItems[i].id) {
                        index = i;
                    }
                }

                if (index !== null) {
                    // user already in list of users shown, update object
                    $scope.pagedItems[index] = _.clone(user);
                } else {
                    // user wasn't already present in list
                    $scope.currentPage = 0;
                    $scope.getItems();
                }

                $scope.successMessage = 'User successfully updated with username: "' + user.username + '"';
            }
        }, function () {
            // close
            $scope.getItems();
        });
    };

    // delete user
    $scope.deleteUser = function (userId) {
        $scope.successMessage = '';

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
                // ok, delete from list
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
                $scope.successMessage = 'Password reset, new password is: ' + successResult.password;
            }, function () {
                // closed
            });
        });
    };

    // send verification email
    $scope.sendVerificationEmail = function (userId) {
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

    $scope.closeStatistics = function () {

    };

    $scope.init();
}]);
