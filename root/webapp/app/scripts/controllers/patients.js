'use strict';

// todo: consider controllers in separate files

// new patient modal instance controller
var NewPatientModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'permissions', 'newUser', 'allGroups', 'allowedRoles', 'allFeatures', 'identifierTypes', 'UserService', 'UtilService',
function ($scope, $rootScope, $modalInstance, permissions, newUser, allGroups, allowedRoles, allFeatures, identifierTypes, UserService, UtilService) {
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
    $scope.create = function () {
        var i;

        // generate password
        var password = UtilService.generatePassword();
        $scope.editUser.password = password;

        UserService.create($scope.editUser).then(function(userId) {
            // successfully created new user
            UserService.get(userId).then(function(result) {
                result.isNewUser = true;
                result.password = password;
                $modalInstance.close(result);
            }, function() {
                alert('Cannot get user (has been created)');
            });
        }, function(result) {
            if (result.status === 409) {
                // 409 = CONFLICT, means user already exists, provide UI to edit existing user group roles
                // but only if user has rights to GET user

                UserService.get(result.data).then(function(result) {
                    $scope.warningMessage = 'A patient member with this username or email already exists. Add them to your group if required, then close this window. You can then edit their details normally as they will appear in the refreshed list.';
                    $scope.editUser = result;
                    $scope.existingUser = true;
                    $scope.editMode = true;
                    $scope.pagedItems = [];

                    // get user existing group/roles from groupRoles
                    $scope.editUser.groups = [];
                    for (i = 0; i < $scope.editUser.groupRoles.length; i++) {
                        var groupRole = $scope.editUser.groupRoles[i];
                        var group = groupRole.group;
                        group.role = groupRole.role;
                        $scope.editUser.groups.push(group);
                    }

                    // set available groups so user can add another group/role to the users existing group roles if required
                    $scope.editUser.availableGroups = $scope.allGroups;
                    for (i = 0; i < $scope.editUser.groups.length; i++) {
                        $scope.editUser.availableGroups = _.without($scope.editUser.availableGroups, _.findWhere($scope.editUser.availableGroups, {id: $scope.editUser.groups[i].id}));
                    }

                    // set available user roles
                    $scope.editUser.roles = $scope.allowedRoles;
                }, function () {
                    $scope.warningMessage = 'This username is restricted, please try another';
                });
            } else {
                // Other errors treated as standard errors
                $scope.errorMessage = 'There was an error: ' + result.data;
            }
        });
    };

    // click Update Existing button, (after finding user already exists)
    $scope.edit = function () {
        UserService.save($scope.editUser).then(function() {
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

// delete patient modal instance controller
var DeletePatientModalInstanceCtrl = ['$scope', '$modalInstance','permissions','user','UserService','allGroups','allRoles','$q',
    function ($scope, $modalInstance, permissions, user, UserService, allGroups, allRoles, $q) {
        var i, j, inMyGroups = false, notMyGroupCount = 0;
        $scope.successMessage = $scope.errorMessage = '';
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

        // remove from my units
        $scope.removeFromMyGroups = function () {
            var promises = [];
            // remove group roles from user where group is my unit with multiple deleteGroupRole
            for (i=0;i<allGroups.length;i++) {
                for (j=0;j<allRoles.length;j++) {
                    promises.push(UserService.deleteGroupRole(user, allGroups[i].id, allRoles[j].id));
                }
            }
            $q.all(promises).then(function () {
                $scope.successMessage = 'Patient has been removed from your groups.';
                $scope.user.canRemoveFromMyGroups = false;
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
                // successfully deleted user
                $scope.successMessage = 'Patient has been permanently deleted.';
                $scope.user.canRemoveFromMyGroups = false;
                $scope.user.canRemoveFromAllGroups = false;
                $scope.user.canDelete = false;
            }, function() {
                // error
                $scope.errorMessage = 'There was an error';
            });
        };

        $scope.cancel = function () {
            //$modalInstance.dismiss('cancel');
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
angular.module('patientviewApp').controller('PatientsCtrl',['$rootScope', '$scope', '$compile', '$modal', '$timeout', '$location',
    'UserService', 'GroupService', 'RoleService', 'FeatureService', 'StaticDataService', 'AuthService', 'localStorageService',
    'UtilService',
    function ($rootScope, $scope, $compile, $modal, $timeout, $location, UserService, GroupService, RoleService, FeatureService,
              StaticDataService, AuthService, localStorageService, UtilService) {

    $scope.itemsPerPage = 20;
    $scope.currentPage = 0;
    $scope.filterText = '';
    $scope.sortField = 'forename';
    $scope.sortDirection = 'ASC';
    $scope.initFinished = false;

    var tempFilterText = '';
    var filterTextTimeout;

    // watches
    // update page on user typed search text
    $scope.$watch('searchText', function (value) {
        if (value !== undefined) {
            if (filterTextTimeout) {
                $timeout.cancel(filterTextTimeout);
            }
            $scope.currentPage = 0;

            tempFilterText = value;
            filterTextTimeout = $timeout(function () {
                $scope.filterText = tempFilterText;
                $scope.getItems();
            }, 1000); // delay 1000 ms
        }
    });

    // update page when currentPage is changed
    $scope.$watch('currentPage', function(value) {
        if ($scope.initFinished === true) {
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
        return $scope.currentPage === 0 ? 'hidden' : '';
    };

    $scope.nextPage = function() {
        if ($scope.currentPage < $scope.totalPages - 1) {
            $scope.currentPage++;
        }
    };

    $scope.nextPageDisabled = function() {
        return $scope.currentPage === $scope.pageCount() - 1 ? 'disabled' : '';
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

    // Get users based on current user selected filters etc
    $scope.getItems = function () {
        $scope.loadingMessage = 'Loading Patients';
        $scope.loading = true;

        var getParameters = {};
        getParameters.page = $scope.currentPage;
        getParameters.size = $scope.itemsPerPage;
        getParameters.filterText = $scope.filterText;
        getParameters.sortField = $scope.sortField;
        getParameters.sortDirection = $scope.sortDirection;
        getParameters.roleIds = $scope.roleIds;

        if ($scope.selectedGroup.length > 0) {
            getParameters.groupIds = $scope.selectedGroup;
        } else {
            getParameters.groupIds = $scope.groupIds;
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
        $scope.loadingMessage = 'Loading Patients';
        $scope.loading = true;
        $scope.allGroups = [];
        $scope.allRoles = [];
        $scope.roleIds = [];
        $scope.groupIds = [];
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
            // to delete group membership in edit UI
            $scope.permissions.canDeleteGroupRolesDuringEdit = true;
            // to see the option to permanently delete patients
            $scope.permissions.canDeleteUsers = true;
        }

        // only allow GLOBAL_ADMIN or SPECIALTY_ADMIN or UNIT_ADMIN ...
        if ($scope.permissions.isSuperAdmin || $scope.permissions.isSpecialtyAdmin || $scope.permissions.isUnitAdmin) {
            // to see the option to delete patients in menu
            $scope.permissions.showDeleteMenuOption = true;
            // can add patients
            $scope.permissions.canCreatePatients = true;
            // can edit patients
            $scope.permissions.canEditPatients = true;
            // can reset passwords
            $scope.permissions.canResetPasswords = true;
            // can send verification emails
            $scope.permissions.canSendVerificationEmails = true;
        }

        // STAFF_ADMIN can only view
        if (!($scope.permissions.isSuperAdmin || $scope.permissions.isSpecialtyAdmin
            || $scope.permissions.isUnitAdmin)) {
            $scope.permissions.canViewPatients = true;
        }

        // get patient type roles
        var roles = $scope.loggedInUser.userInformation.patientRoles;

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
        $scope.initFinished = false;

        // show error if user is not a member of any groups
        if (groups.length !== 0) {

            // set groups that can be chosen in UI, only show users from visible groups (assuming all users are in generic which is visible==false)
            for (i = 0; i < groups.length; i++) {
                group = groups[i];
                if (group.visible === true) {
                    $scope.allGroups.push(group);
                    $scope.groupIds.push(group.id);
                    $scope.permissions.allGroupsIds[group.id] = group.id;
                    $scope.groupMap[group.id] = group;
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
            StaticDataService.getLookupsByType('IDENTIFIER').then(function(identifierTypes) {
                if (identifierTypes.length > 0) {
                    $scope.identifierTypes = identifierTypes;
                }
            });

            $scope.initFinished = true;
        } else {
            // no groups found
            delete $scope.loading;
            $scope.fatalErrorMessage = 'No user groups found, cannot retrieve patients';
        }
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
                            }
                        }
                    }
                } else {
                    user.userFeatures = [];
                }

                // set the user being edited to a clone of the existing user (so only updated in UI on save)
                $scope.editUser = _.clone(user);
                openedUser.editLoading = false;
            }, function(failureResult) {
                openedUser.showEdit = false;
                openedUser.editLoading = false;
                alert('Cannot open patient: ' + failureResult.data);
            });
        }
    };

    // Save from edit
    $scope.save = function (editUserForm, user) {
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
                alert("Cannot save User: " + failureResult.data);
            } else {
                alert("Cannot save User: " + failureResult.data);
            }
        });
    };

    // handle opening modal (Angular UI Modal http://angular-ui.github.io/bootstrap/)
    $scope.openModalNewPatient = function (size) {
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
        $scope.editUser.groupRoles = [];
        $scope.editUser.availableFeatures = _.clone($scope.allFeatures);
        $scope.editUser.userFeatures = [];
        $scope.editUser.selectedRole = '';
        $scope.editUser.identifiers = [];

        // open modal and pass in required objects for use in modal scope
        var modalInstance = $modal.open({
            templateUrl: 'newPatientModal.html',
            controller: NewPatientModalInstanceCtrl,
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
                UtilService: function(){
                    return UtilService;
                }
            }
        });

        // handle modal close (via button click)
        modalInstance.result.then(function (user) {
            // check if user is newly created
            if (user.isNewUser) {
                // is a new user
                $scope.successMessage = 'User successfully created ' +
                    'with username: "' + user.username + '" ' +
                    'and password: "' + user.password + '"';
                $scope.userCreated = true;
            } else {
                // is an already existing user, likely updated group roles
                $scope.successMessage = 'User successfully updated with username: "' + user.username + '"';
            }

            $scope.currentPage = 0;
            $scope.getItems();
            delete $scope.editUser;

        }, function () {
            $scope.getItems();
        });
    };

    // delete user
    $scope.deleteUser = function (userId) {
        $scope.successMessage = '';
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

    // view patient
    $scope.viewUser = function (userId) {
        $scope.loadingMessage = 'Viewing Patient';
        $scope.loading = true;
        $scope.successMessage = '';
        $rootScope.switchingUser = true;
        var currentToken = $rootScope.authToken;

        AuthService.switchUser(userId, null).then(function(authToken) {

            $rootScope.previousAuthToken = currentToken;
            localStorageService.set('previousAuthToken', currentToken);

            $rootScope.previousLoggedInUser = $scope.loggedInUser;
            localStorageService.set('previousLoggedInUser', $scope.loggedInUser);

            $rootScope.authToken = authToken;
            localStorageService.set('authToken', authToken);

            // get user information, store in session
            AuthService.getUserInformation(authToken).then(function (userInformation) {

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
                alert("Error receiving user information");
                $scope.loading = false;
            });

        }, function() {
            alert("Cannot view patient");
            $scope.loading = false;
            delete $rootScope.switchingUser;
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
                $scope.successMessage = 'Password reset for ' + user.forename + ' ' + user.surname
                    + ' (username ' + user.username + '), new password is: ' + successResult.password;
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
