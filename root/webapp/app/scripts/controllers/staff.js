'use strict';

// new staff modal instance controller
var NewStaffModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'newUser', 'allGroups', 'allRoles', 'allFeatures', 'UserService',
function ($scope, $rootScope, $modalInstance, newUser, allGroups, allRoles, allFeatures, UserService) {
    $scope.editUser = newUser;
    $scope.allGroups = allGroups;
    $scope.allRoles = allRoles;

    // set initial group and feature (avoid blank option)
    if ($scope.editUser.availableGroups && $scope.editUser.availableGroups.length > 0) {
        $scope.groupToAdd = $scope.editUser.availableGroups[0].id;
    }
    if ($scope.editUser.availableFeatures && $scope.editUser.availableFeatures.length > 0) {
        $scope.featureToAdd = $scope.editUser.availableFeatures[0].feature.id;
    }

    $scope.ok = function () {
        UserService.new($scope.editUser).then(function(result) {
            $scope.editUser = result;
            $modalInstance.close($scope.editUser);
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
}];

// delete staff modal instance controller
var DeleteStaffModalInstanceCtrl = ['$scope', '$modalInstance','user','UserService',
function ($scope, $modalInstance, user, UserService) {
    $scope.user = user;
    $scope.ok = function () {
        UserService.delete(user).then(function() {
            $modalInstance.close();
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
            $modalInstance.close(successResult);
        }, function() {
            // error
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
            $modalInstance.close();
        });
    };
    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
}];

angular.module('patientviewApp').controller('StaffCtrl',['$rootScope', '$scope', '$compile', '$modal', '$timeout', 'UserService', 'GroupService', 'RoleService', 'FeatureService', 'SecurityService',
    function ($rootScope, $scope, $compile, $modal, $timeout, UserService, GroupService, RoleService, FeatureService, SecurityService) {

    // filter by group
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
        
    // filter by role
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

        var i, groupIds = [], roleIds = [];
        $scope.loading = true;
        $scope.allGroups = [];

        // get staff roles
        RoleService.getByType("STAFF").then(function(roles)
        {
            for (i = 0; i < roles.length; i++) {
                roleIds.push(roles[i].id);
            }

            // get logged in user's groups
            GroupService.getGroupsForUser($scope.loggedInUser.id).then(function (groups) {
                $scope.allGroups = groups;
                for (i = 0; i < groups.length; i++) {
                    groupIds.push(groups[i].id);
                }

                // get users by staff roles and logged in user's groups
                UserService.getByGroupsAndRoles(groupIds, roleIds).then(function (users) {
                    $scope.list = users;
                    $scope.currentPage = 1; //current page
                    $scope.entryLimit = 20; //max no of items to display in a page
                    $scope.totalItems = $scope.list.length;
                    delete $scope.loading;
                });

                // get list of roles available when user is adding new Group & Role to staff member
                SecurityService.getSecurityRolesByUser($rootScope.loggedInUser.id).then(function (allRoles) {
                    $scope.allRoles = allRoles;
                });

                // get list of features available when user adding new Feature to staff member
                FeatureService.getAllStaffFeatures().then(function (allFeatures) {
                    $scope.allFeatures = [];
                    for (var i = 0; i < allFeatures.length; i++) {
                        $scope.allFeatures.push({'feature': allFeatures[i]});
                    }
                });
            });
        });
    };

    // Opened for edit
    $scope.opened = function (user, $event) {
        $scope.successMessage = '';

        if ($event) {
            // workaround for angular accordion and bootstrap dropdowns (clone and activate ng-click)
            if ($event.target.className.indexOf('dropdown-toggle') !== -1) {
                if ($('#' + $event.target.id).parent().children('.child-menu').length > 0) {
                    $('#' + $event.target.id).parent().children('.child-menu').remove();
                    $event.stopPropagation();
                } else {
                    $('.child-menu').remove();
                    $event.stopPropagation();
                    var childMenu = $('<div class="child-menu"></div>');
                    var dropDownMenuToAdd = $('#' + $event.target.id + '-menu').clone().attr('id', '').show();

                    // http://stackoverflow.com/questions/16949299/getting-ngclick-to-work-on-dynamic-fields
                    var compiledElement = $compile(dropDownMenuToAdd)($scope);
                    $(childMenu).append(compiledElement);
                    $('#' + $event.target.id).parent().append(childMenu);
                }
            }
            if ($event.target.className.indexOf('dropdown-menu-accordion-item') !== -1) {
                $event.stopPropagation();
            }
        }

        $scope.editing = true;
        user.roles = $scope.allRoles;

        // for REST compatibility
        user.groups = [];
        for(var h=0;h<user.groupRoles.length;h++) {
            var groupRole = user.groupRoles[h];
            var group = groupRole.group;
            group.role = groupRole.role;
            user.groups.push(group);
        }

        // create list of available groups (all - users)
        user.availableGroups = $scope.allGroups;
        if(user.groups) {
            for (var i = 0; i < user.groups.length; i++) {
                user.availableGroups = _.without(user.availableGroups, _.findWhere(user.availableGroups, {id: user.groups[i].id}));
            }
        }
        else { user.groups = []; }

        // create list of available features (all - users)
        user.availableFeatures = _.clone($scope.allFeatures);
        if (user.userFeatures) {
            for (var j = 0; j < user.userFeatures.length; j++) {
                for (var k = 0; k < user.availableFeatures.length; k++) {
                    if (user.userFeatures[j].feature.id === user.availableFeatures[k].feature.id) {
                        user.availableFeatures.splice(k, 1);
                    }
                }
            }
        } else { user.userFeatures = []; }

        $scope.editUser = _.clone(user);

        // set initial group and feature (avoid blank option)
        if ($scope.editUser.availableGroups[0]) {
            $scope.groupToAdd = $scope.editUser.availableGroups[0].id;
        }
        if ($scope.editUser.availableFeatures[0]) {
            $scope.featureToAdd = $scope.editUser.availableFeatures[0].feature.id;
        }
    };

    // Save from edit
    $scope.save = function (editUserForm, user, index) {
        UserService.save(user).then(function() {
            editUserForm.$setPristine(true);
            $scope.list[index] = _.clone(user);
            $scope.successMessage = 'User saved';
        });
    };

    $scope.openModalNewStaff = function (size) {
        $scope.errorMessage = '';
        $scope.successMessage = '';
        $scope.userCreated = '';
        // create new user with list of available roles, groups and features
        $scope.editUser = {};
        $scope.editUser.roles = $scope.allRoles;
        $scope.editUser.availableGroups = $scope.allGroups;
        $scope.editUser.groups = [];
        $scope.editUser.availableFeatures = _.clone($scope.allFeatures);
        $scope.editUser.userFeatures = [];
        $scope.editUser.selectedRole = '';

        var modalInstance = $modal.open({
            templateUrl: 'newStaffModal.html',
            controller: NewStaffModalInstanceCtrl,
            size: size,
            resolve: {
                newUser: function(){
                    return $scope.editUser;
                },
                allGroups: function(){
                    return $scope.allGroups;
                },
                allRoles: function(){
                    return $scope.allRoles;
                },
                allFeatures: function(){
                    return $scope.allFeatures;
                },
                UserService: function(){
                    return UserService;
                },
                SecurityService: function(){
                    return SecurityService;
                }
            }
        });

        modalInstance.result.then(function (user) {
            $scope.list.push(user);
            $scope.editUser = user;
            $scope.successMessage = 'User successfully created ' +
                'with username: "' + user.username + '" ' +
                'and password: "' + user.password + '"';
            $scope.userCreated = true;
            // ok (success)
        }, function () {
            // cancel
            $scope.editUser = '';
        });
    };

    // delete user
    $scope.deleteUser = function (userId, $event) {
        $scope.successMessage = '';

        // workaround for cloned object not capturing ng-click properties
        var eventUserId = $event.currentTarget.dataset.userid;

        UserService.get(eventUserId).then(function(user) {
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
                for(var l=0;l<$scope.list.length;l++) {
                    if ($scope.list[l].id.toString() === eventUserId) {
                        $scope.list = _.without($scope.list, $scope.list[l]);
                    }
                }
                $scope.successMessage = 'User successfully deleted';
            }, function () {
                // closed
            });
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


