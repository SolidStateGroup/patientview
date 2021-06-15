'use strict';
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
        delete $scope.successMessage;
        delete $scope.errorMessage;
        delete $scope.warningMessage;
        $scope.editUser = result;
        $scope.existingUser = true;
        $scope.editMode = true;

        if (UserService.checkRoleExists('PATIENT', result)) {
            $scope.errorMessage = 'Please note a patient with these details already exists on PatientView. ' +
                'If this patient is also a member of staff, please create a separate user account by clicking ' +
                'the Create New button on the Staff page. This new account must contain a distinct username and email.';
        } else {
            $scope.warningMessage = 'A user with this '
                + searchType
                + ' already exists. Add them to your group if required, then close this window. '
                + 'You can then edit their details normally as they will appear in the refreshed list.';
        }

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

    // click Undelete button
    $scope.undelete = function (user) {
        delete $scope.successMessage;
        delete $scope.errorMessage;

        UserService.undelete(user).then(function() {
            $scope.successMessage = "Staff user has been undeleted";
            $scope.editUser.deleted = false;
        }, function (error) {
            $scope.errorMessage = error.data;
        });
    };
}];
