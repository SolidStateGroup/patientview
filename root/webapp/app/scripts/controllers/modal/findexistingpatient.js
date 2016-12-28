'use strict';
var FindExistingPatientModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'permissions', 'allGroups',
    'allowedRoles', 'identifierTypes', 'UserService', 'UtilService',
function ($scope, $rootScope, $modalInstance, permissions, allGroups, allowedRoles, identifierTypes, UserService,
    UtilService) {
    $scope.permissions = permissions;
    $scope.allGroups = allGroups;
    $scope.allowedRoles = allowedRoles;
    $scope.identifierTypes = identifierTypes;
    $scope.editMode = false;
    $scope.editUser = {};

    // for date of birth check
    $scope.years = UtilService.generateYears();
    $scope.months = UtilService.generateMonths();
    $scope.days = UtilService.generateDays();
    $scope.dobCheck = { 'selectedYear': $scope.years[0], 'selectedMonth': $scope.months[0], 'selectedDay': $scope.days[0] };

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

    // click Find by email button
    $scope.findByEmail = function () {
        UserService.findByEmail($('#email').val()).then(function(result) {
            showUserOnScreen(result, "email");
        }, function () {
            $scope.warningMessage = 'No patient exists with this email address';
        });
    };

    var showUserOnScreen = function (result, searchType) {
        $scope.editUser = result;
        $scope.existingUser = true;
        $scope.editMode = true;
        $scope.searchType = searchType;
        delete $scope.warningMessage;
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

    $scope.validateDateOfBirth = function(dobCheck) {
        $scope.passedDobCheck = true;
    }
}];
