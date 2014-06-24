'use strict';

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

// reset staff password modal instance controller
var ResetStaffPasswordModalInstanceCtrl = ['$scope', '$modalInstance','user','UserService',
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

        $scope.loading = true;

        // TODO: hardcoded for now to first group of user or -1
        var groupId = -1;
        if ($rootScope.loggedInUser.groupRoles.length > 0) {
            groupId = $rootScope.loggedInUser.groupRoles[0].group.id;

        }

        GroupService.get(groupId).then(function(allGroups) {

            $scope.allGroups = [];
            $scope.allGroups.push(allGroups);

            GroupService.getUsersByType(groupId,'staff').then(function(staffUsers) {
            //UserService.getStaffByGroup(groupId).then(function(staffUsers) {
                $scope.list = staffUsers;
                $scope.currentPage = 1; //current page
                $scope.entryLimit = 10; //max no of items to display in a page
                $scope.totalItems = $scope.list.length;
                delete $scope.loading;
            });

            /*RoleService.getByType('staff').then(function(allRoles) {
                $scope.allRoles = allRoles;
            });*/

            SecurityService.getSecurityRolesByUser($rootScope.loggedInUser.id).then(function(allRoles) {
                $scope.allRoles = allRoles;
            });

            FeatureService.getAll().then(function(allFeatures) {
                //$scope.allFeatures = allFeatures;
                $scope.allFeatures = [];
                for (var i=0;i<allFeatures.length;i++){
                    $scope.allFeatures.push({'feature':allFeatures[i]});
                }
            });
        });
    };

    // Opened for edit
    $scope.opened = function (user, $event) {

        $scope.successMessage = '';

        if ($event) {
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

                    //childMenu.append(dropDownMenuToAdd);
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
        else {
            user.groups = [];
        }

        // create list of available features (all - users)
        user.availableFeatures = _.clone($scope.allFeatures);
        if (user.userFeatures) {
            for (var j = 0; j < user.userFeatures.length; j++) {
                //user.availableFeatures = _.without(user.availableFeatures, _.findWhere(user.availableFeatures, {feature.id: user.userFeatures[j].feature.id}));
                for (var k = 0; k < user.availableFeatures.length; k++) {
                    if (user.userFeatures[j].feature.id === user.availableFeatures[k].feature.id) {
                        user.availableFeatures.splice(k, 1);
                    }
                }
            }
        } else {
            user.userFeatures = [];
        }

        $scope.editUser = _.clone(user);

        if (user.availableGroups[0]) {
            $scope.groupToAdd = user.availableGroups[0].id;
        }
        if (user.availableFeatures[0]) {
            $scope.FeatureToAdd = user.availableFeatures[0].id;
        }

        $scope.editUser.selectedRole = '';
    };

    // Save from edit
    $scope.save = function (editUserForm, user, index) {
        UserService.save(user).then(function() {
            editUserForm.$setPristine(true);
            $scope.list[index] = _.clone(user);
            $scope.successMessage = 'User saved';
        });
    };

    // add group to current group, remove from allowed
    $scope.addGroup = function (form, user, groupId) {
        if(_.findWhere(user.availableGroups, {id: groupId}) && _.findWhere($scope.allRoles, {id: user.selectedRole})) {
            user.availableGroups = _.without(user.availableGroups, _.findWhere(user.availableGroups, {id: groupId}));
            var newGroup = _.findWhere($scope.allGroups, {id: groupId});
            newGroup.role = _.findWhere($scope.allRoles, {id: user.selectedRole});
            user.groups.push(newGroup);
            user.selectedRole = '';

            if (user.availableGroups[0]) {
                $scope.groupToAdd = user.availableGroups[0].id;
            }

            // for REST compatibility
            user.groupRoles = [];
            for(var i=0;i<user.groups.length;i++) {
                var group = user.groups[i];
                user.groupRoles.push({'group': group, 'role': group.role});
            }

            form.$setDirty(true);
        }
    };

    // remove group from current groups, add to allowed groups
    $scope.removeGroup = function (form, user, group) {
        user.groups = _.without(user.groups, _.findWhere(user.groups, {id: group.id}));
        user.availableGroups.push(group);

        if (user.availableGroups[0]) {
            $scope.groupToAdd = user.availableGroups[0].id;
        }

        // for REST compatibility
        user.groupRoles = [];
        for(var i=0;i<user.groups.length;i++) {
            var tempGroup = user.groups[i];
            user.groupRoles.push({'group': tempGroup, 'role': tempGroup.role});
        }

        form.$setDirty(true);
    };

    // add feature to current feature, remove from allowed
    $scope.addFeature = function (form, user, featureId) {
        for (var j = 0; j < user.availableFeatures.length; j++) {
            if (user.availableFeatures[j].feature.id === featureId) {
                user.userFeatures.push(user.availableFeatures[j]);
                user.availableFeatures.splice(j, 1);
            }
        }
        form.$setDirty(true);
    };

    // remove feature from current features, add to allowed features
    $scope.removeFeature = function (form, user, feature) {
        for (var j = 0; j < user.userFeatures.length; j++) {
            if (user.userFeatures[j].feature.id === feature.feature.id) {
                user.availableFeatures.push(user.userFeatures[j]);
                user.userFeatures.splice(j, 1);
            }
        }
        form.$setDirty(true);
    };

    // delete user
    $scope.deleteUser = function (userId, $event) {

        $scope.successMessage = '';

        // workaround for cloned object not capturing ng-click properties
        var eventUserId = $event.currentTarget.dataset.userid;

        UserService.get(eventUserId).then(function(user) {
            var modalInstance = $modal.open({
                templateUrl: 'views/partials/deleteStaffModal.html',
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
                templateUrl: 'views/partials/resetStaffPasswordModal.html',
                controller: ResetStaffPasswordModalInstanceCtrl,
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

    $scope.init();
}]);


