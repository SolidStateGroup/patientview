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
    $scope.passedDobCheck = false;
    $scope.failedDobCheck = false;
    $scope.confirmAddGroupRole = false;
    $scope.addGroupRoleLoading = false;
    $scope.addGroupRoleDone = false;

    // for date of birth check
    $scope.years = UtilService.generateYears();
    $scope.months = UtilService.generateMonths();
    $scope.days = UtilService.generateDays();
    $scope.dobCheck = { 'year': $scope.years[0],
        'month': $scope.months[0], 'day': $scope.days[0] };

    $scope.addGroupRoleConfirmed = function() {
        $scope.addGroupRoleLoading = true;
        UserService.addGroupRole($scope.editUser, $scope.editUser.groupToAdd.id, $scope.editUser.selectedRole.id).then(function () {
            $scope.addGroupRoleLoading = false;
            $scope.addGroupRoleDone = true;
        }, function () {
            $scope.addGroupRoleLoading = false;
            alert('Error adding group role, may already exist');
        });
    };

    // click Find by username button
    $scope.findByUsername = function () {
        delete $scope.warningMessage;
        UserService.findByUsername($('#username').val()).then(function(result) {
            $scope.searchType = 'username';
            initDobCheck(result);
        }, function () {
            $scope.warningMessage = 'No patient exists with this username';
        });
    };

    // click Find by identifier button
    $scope.findByIdentifier = function () {
        delete $scope.warningMessage;
        UserService.findByIdentifier($('#identifier').val()).then(function(result) {
            $scope.searchType = 'identifier';
            initDobCheck(result);
        }, function () {
            $scope.warningMessage = 'No patient exists with this identifier';
        });
    };

    // click Find by email button
    $scope.findByEmail = function () {
        delete $scope.warningMessage;
        UserService.findByEmail($('#email').val()).then(function(result) {
            $scope.searchType = 'email';
            initDobCheck(result);
        }, function () {
            $scope.warningMessage = 'No patient exists with this email address';
        });
    };

    // find and validate patient
    $scope.findAndValidate = function () {
        var findParam = {}
        findParam.searchUsername = $('#by-username').val();
        findParam.searchIdentifier = $('#by-identifier').val();
        findParam.searchEmail = $('#by-email').val();

        // set date of birth if available
        if ($scope.dobCheck.day && $scope.dobCheck.month && $scope.dobCheck.year) {
            findParam.dateOfBirth = new Date($scope.dobCheck.year, $scope.dobCheck.month -1 , $scope.dobCheck.day);
        }

        delete $scope.warningMessage;
        console.log(findParam)
        UserService.findAndValidate(findParam).then(function (result) {

            $scope.passedDobCheck = true;
            $scope.existingUser = true;

            $scope.editUser = result;
            $scope.hasDob = true;
            showUserOnScreen();

        }, function () {
            $scope.warningMessage = 'No patient exists with given details';
        });
    };

    var showUserOnScreen = function () {
        $scope.editMode = true;
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

    var initDobCheck = function(user) {
        $scope.passedDobCheck = false;
        $scope.existingUser = true;

        // validate if user has a date of birth (all patients should)
        if (user.dateOfBirth === null || user.dateOfBirth === undefined) {
            $scope.hasDob = false;
        } else {
            // has date of birth, in format YYYY-MM-DD
            $scope.editUser = user;
            $scope.hasDob = true;
        }
    };

    $scope.validateDateOfBirth = function(dobCheck) {
        $scope.failedDobCheck = false;
        var year = $scope.editUser.dateOfBirth.split('-')[0].toString();
        var month = $scope.editUser.dateOfBirth.split('-')[1].toString();
        var day = $scope.editUser.dateOfBirth.split('-')[2].toString();

        $scope.passedDobCheck = (dobCheck.year === year && dobCheck.month === month && dobCheck.day === day);

        if (!$scope.passedDobCheck) {
            $scope.failedDobCheck = true;
        } else {
            showUserOnScreen();
        }
    }
}];
