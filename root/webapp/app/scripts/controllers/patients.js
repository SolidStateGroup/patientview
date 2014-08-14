'use strict';

// todo: consider controllers in separate files

// new patient modal instance controller
var NewPatientModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'permissions', 'newUser', 'allGroups', 'allowedRoles', 'allFeatures', 'identifierTypes', 'UserService',
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
                // successfully created new patient user
                $scope.editUser = result;
                $scope.editUser.isNewUser = true;
                $modalInstance.close($scope.editUser);
            }, function(result) {
                if (result.status === 409) {
                    // 409 = CONFLICT, means patient already exists, provide UI to edit existing patient group roles
                    $scope.warningMessage = 'A patient member with this username or email already exists, you can add them to your group if required.';
                    $scope.editUser = result.data;
                    $scope.existingUser = true;

                    // get patient existing group/roles from groupRoles
                    $scope.editUser.groups = [];
                    for(i=0; i<$scope.editUser.groupRoles.length; i++) {
                        var groupRole = $scope.editUser.groupRoles[i];
                        var group = groupRole.group;
                        group.role = groupRole.role;
                        $scope.editUser.groups.push(group);
                    }

                    // set available groups so user can add another group/role to the patient members existing group roles if required
                    $scope.editUser.availableGroups = $scope.allGroups;
                    for (i=0; i<$scope.editUser.groups.length; i++) {
                        $scope.editUser.availableGroups = _.without($scope.editUser.availableGroups, _.findWhere($scope.editUser.availableGroups, {id: $scope.editUser.groups[i].id}));
                    }

                    // set available patient roles
                    $scope.editUser.roles = $scope.allowedRoles;

                } else {
                    // Other errors treated as standard errors
                    $scope.errorMessage = 'There was an error: ' + result.data;
                }
            });
        };

        // click Update Existing button, (after finding patient already exists)
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

                if (groupRoleGroupCode != 'Generic' && groupRoleGroupType != 'SPECIALTY') {
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
            for (j=0;j<user.groupRoles[i].group.groupFeatures;j++) {
                var feature = user.groupRoles[i].group.groupFeatures[j];
                if (feature.name = 'KEEP_ALL_DATA') {
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
                for (i = 0; i < user.groupRoles.length; i++) {
                    var groupRole = user.groupRoles[i];
                    promises.push(UserService.deleteGroupRole(user, groupRole.group.id, groupRole.role.id));
                }
            } else {
                promises.push(UserService.delete(user));
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
                $scope.errorMessage = 'There was an error';
            });
        };

        // delete patient permanently
        $scope.delete = function () {
            UserService.delete(user).then(function() {
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
angular.module('patientviewApp').controller('PatientsCtrl',['$rootScope', '$scope', '$compile', '$modal', '$timeout', 'UserService', 'GroupService', 'RoleService', 'FeatureService', 'SecurityService', 'StaticDataService',
    function ($rootScope, $scope, $compile, $modal, $timeout, UserService, GroupService, RoleService, FeatureService, SecurityService, StaticDataService) {

        // filter users by group
        $scope.selectedGroup = [];
        $scope.setSelectedGroup = function () {
            var id = this.group.id;
            if (_.contains($scope.selectedGroup, id)) {
                $scope.selectedGroup = _.without($scope.selectedGroup, id);
            } else {
                $scope.selectedGroup.push(id);
            }
            return false;
        };
        $scope.isGroupChecked = function (id) {
            if (_.contains($scope.selectedGroup, id)) {
                return 'glyphicon glyphicon-ok pull-right';
            }
            return false;
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
            return false;
        };
        $scope.isRoleChecked = function (id) {
            if (_.contains($scope.selectedRole, id)) {
                return 'glyphicon glyphicon-ok pull-right';
            }
            return false;
        };

        // TODO: server side pagination
        // pagination, sorting, basic filter    
        $scope.setPage = function(pageNo) {
            $scope.currentPage = pageNo;
        };
        $scope.filter = function() {
            $timeout(function() {
                $scope.filteredItems = $scope.filtered.length;
            }, 10);
        };
        $scope.sortBy = function(predicate) {
            $scope.predicate = predicate;
            $scope.reverse = !$scope.reverse;
        };

        // Init
        $scope.init = function () {
            $('body').click(function () {
                $('.child-menu').remove();
            });

            var i, group, role, groupIds = [], patientRoleIds = [];
            $scope.loading = true;
            $scope.allGroups = [];
            $scope.allRoles = [];

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
            }

            // get patient type roles
            RoleService.getByType('PATIENT').then(function(roles) {

                // set roles that can be chosen in UI, only show visible roles
                for (i = 0; i < roles.length; i++) {
                    role = roles[i];
                    if (role.visible === true) {
                        $scope.allRoles.push(role);
                        patientRoleIds.push(role.id);
                    }
                }

                // get logged in user's groups
                GroupService.getGroupsForUser($scope.loggedInUser.id).then(function (groups) {

                    // sort groups by name
                    groups = _.sortBy(groups, 'name' );

                    // show error if user is not a member of any groups
                    if (groups.length !== 0) {

                        // set groups that can be chosen in UI, only show users from visible groups (assuming all users are in generic which is visible==false)
                        for (i = 0; i < groups.length; i++) {
                            group = groups[i];
                            if (group.visible === true) {
                                $scope.allGroups.push(group);
                                groupIds.push(group.id);
                                $scope.permissions.allGroupsIds[group.id] = group.id;
                            }
                        }

                        // get patient users by list of patient roles and list of logged in user's groups
                        UserService.getByGroupsAndRoles(groupIds, patientRoleIds).then(function (users) {
                            $scope.list = users;
                            $scope.currentPage = 1; //current page
                            $scope.entryLimit = 20; //max no of items to display in a page
                            $scope.totalItems = $scope.list.length;
                            delete $scope.loading;
                        });

                        // get list of roles available when user is adding a new Group & Role to patient member
                        // e.g. unit admins cannot add specialty admin roles to patient members
                        SecurityService.getSecurityRolesByUser($rootScope.loggedInUser.id).then(function (roles) {
                            // filter by roleId found previously as PATIENT
                            var allowedRoles = [];
                            for (i = 0; i < roles.length; i++) {
                                if (patientRoleIds.indexOf(roles[i].id) != -1) {
                                    allowedRoles.push(roles[i]);
                                }
                            }
                            $scope.allowedRoles = allowedRoles;
                        });

                        // get list of features available when user is adding a new Feature to patient members
                        FeatureService.getAllPatientFeatures().then(function (allFeatures) {
                            $scope.allFeatures = [];
                            for (var i = 0; i < allFeatures.length; i++) {
                                $scope.allFeatures.push({'feature': allFeatures[i]});
                            }
                        });

                        // get list of identifier types when user adding identifiers to patient members
                        $scope.identifierTypes = [];
                        StaticDataService.getLookupsByType('IDENTIFIER').then(function(identifierTypes) {
                            if (identifierTypes.length > 0) {
                                $scope.identifierTypes = identifierTypes;
                            }
                        });

                    } else {
                        // no groups found
                        delete $scope.loading;
                        $scope.fatalErrorMessage = 'No user groups found, cannot retrieve patient';
                    }
                }, function () {
                    // error retrieving groups
                    delete $scope.loading;
                    $scope.fatalErrorMessage = 'Error retrieving user groups, cannot retrieve patient';
                });
            });
        };

        // Opened for edit
        $scope.opened = function (openedUser, $event, status) {
            $scope.successMessage = '';
            $scope.editUser = '';
            $scope.editMode = true;
            $scope.saved = '';

            // TODO: handle accordion and bootstrap dropdowns correctly without workaround
            if ($event) {
                // workaround for angular accordion and bootstrap dropdowns (clone and activate ng-click)
                if ($event.target.className.indexOf('dropdown-toggle') !== -1) {
                    if ($('#' + $event.target.id).parent().children('.child-menu').length > 0) {
                        $('#' + $event.target.id).parent().children('.child-menu').remove();
                        $event.stopPropagation();
                    } else {
                        // close any open edit panels
                        $('.panel-collapse.in').collapse('hide');
                        $('.child-menu').remove();
                        $event.stopPropagation();
                        var childMenu = $('<div class="child-menu"></div>');
                        var dropDownMenuToAdd = $('#' + $event.target.id + '-menu').clone().attr('id', '').show();

                        // manually remove Send Verification Email link if already verified
                        if (openedUser.emailVerified) {
                            $(dropDownMenuToAdd).find('#li-email-verified-' + openedUser.id).hide();
                        } else {
                            $(dropDownMenuToAdd).find('#li-email-verified-' + openedUser.id).show();
                        }

                        // http://stackoverflow.com/questions/16949299/getting-ngclick-to-work-on-dynamic-fields
                        // http://stackoverflow.com/questions/12202067/angularjs-ng-click-broken-inside-a-popover
                        var compiledElement = $compile(dropDownMenuToAdd)($scope);
                        $(childMenu).append(compiledElement);
                        $('#' + $event.target.id).parent().append(childMenu);
                    }
                }
                if ($event.target.className.indexOf('dropdown-menu-accordion-item') !== -1) {
                    $event.stopPropagation();
                }
            }

            // do not load if already opened (status.open == true)
            if (!status || status.open === false) {

                // now using lightweight group list, do GET on id to get full group and populate editGroup
                UserService.get(openedUser.id).then(function (user) {

                    $scope.editing = true;
                    user.roles = $scope.allowedRoles;

                    // for REST compatibility, convert patient member groupRoles to objects suitable for UI
                    user.groups = [];
                    for (var h = 0; h < user.groupRoles.length; h++) {
                        var groupRole = user.groupRoles[h];
                        var group = groupRole.group;
                        group.role = groupRole.role;
                        user.groups.push(group);
                    }

                    // create list of available groups (all - patient members existing groups)
                    user.availableGroups = $scope.allGroups;
                    if (user.groups) {
                        for (var i = 0; i < user.groups.length; i++) {
                            user.availableGroups = _.without(user.availableGroups, _.findWhere(user.availableGroups, {id: user.groups[i].id}));
                        }
                    }
                    else {
                        user.groups = [];
                    }

                    // create list of available features (all - patient members existing features)
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

                    // set the patient member being edited to a clone of the existing patient member (so only updated in UI on save)
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
                for(var i=0;i<$scope.list.length;i++) {
                    if($scope.list[i].id == successResult.id) {
                        var headerDetails = $scope.list[i];
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
        $scope.openModalNewPatient = function (size) {
            // close any open edit panels
            $('.panel-collapse.in').collapse('hide');
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
                    SecurityService: function(){
                        return SecurityService;
                    }
                }
            });

            // handle modal close (via button click)
            modalInstance.result.then(function (user) {
                // check if patient member is newly created
                if (user.isNewUser) {
                    // is a new patient member, add to end of list and show username and password
                    $scope.list.push(user);
                    $scope.editUser = user;
                    $scope.successMessage = 'User successfully created ' +
                        'with username: "' + user.username + '" ' +
                        'and password: "' + user.password + '"';
                    $scope.userCreated = true;
                } else {
                    // is an already existing patient member, likely updated group roles
                    var index = null;
                    for (var i = 0; i < $scope.list.length; i++) {
                        if (user.id === $scope.list[i].id) {
                            index = i;
                        }
                    }

                    if (index !== null) {
                        // user already in list of users shown, update object
                        $scope.list[index] = _.clone(user);
                    } else {
                        // user wasn't already present in list, add to end
                        $scope.list.push(user);
                    }

                    $scope.successMessage = 'User successfully updated with username: "' + user.username + '"';
                }
            }, function () {
                // cancel
                $scope.editUser = '';
            });
        };

        // delete user
        $scope.deleteUser = function (userId, $event) {
            $scope.successMessage = '';
            // close any open edit panels
            $('.panel-collapse.in').collapse('hide');

            // workaround for cloned object not capturing ng-click properties
            var eventUserId = $event.currentTarget.dataset.userid;

            UserService.get(eventUserId).then(function(user) {
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
                    $scope.init();
                }, function () {
                    // closed
                    $scope.init();
                });
            });
        };

        // view patient
        $scope.viewUser = function (userId, $event) {
            $scope.successMessage = '';

            // workaround for cloned object not capturing ng-click properties
            var eventUserId = $event.currentTarget.dataset.userid;

            UserService.get(eventUserId).then(function(user) {
                alert('TODO: view patient "' + user.forename + ' ' + user.surname + '" ');
            });
        };

        // reset user password
        $scope.resetUserPassword = function (userId, $event) {
            $scope.successMessage = '';

            // workaround for cloned object not capturing ng-click properties
            var eventUserId = $event.currentTarget.dataset.userid;

            UserService.get(eventUserId).then(function(user) {
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
                    // ok
                    $scope.successMessage = 'Password reset, new password is: ' + successResult.password;
                }, function () {
                    // closed
                });
            });
        };

        // send verification email
        $scope.sendVerificationEmail = function (userId, $event) {

            $scope.successMessage = '';

            // workaround for cloned object not capturing ng-click properties
            var eventUserId = $event.currentTarget.dataset.userid;

            UserService.get(eventUserId).then(function(user) {
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
                    // ok
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


