'use strict';

// new patient modal instance controller
angular.module('patientviewApp').controller('NewUserCtrl', ['$scope', '$rootScope', '$location', 'UserService', 'UtilService', 'StaticDataService',
function ($scope, $rootScope, $location, UserService, UtilService, StaticDataService) {
    
    var init = function() {
        var addPatient = false;
        if ($location.url().indexOf("newpatient") > 0) {
            addPatient = true;
        }

        var i, role, group, roles, allFeatures;
        $scope.allGroups = [];
        $scope.allRoles = [];
        $scope.roleIds = [];
        $scope.groupMap = [];
        $scope.permissions = {};
        $scope.permissions.isSuperAdmin = UserService.checkRoleExists('GLOBAL_ADMIN', $scope.loggedInUser);
        var groups = $scope.loggedInUser.userInformation.userGroups;
        
        if (addPatient) {
            roles = $scope.loggedInUser.userInformation.patientRoles;
        } else {
            roles = $scope.loggedInUser.userInformation.staffRoles;
        }

        // set roles that can be chosen in UI, only show visible roles
        for (i = 0; i < roles.length; i++) {
            role = roles[i];
            if (role.visible === true) {
                $scope.allRoles.push(role);
                $scope.roleIds.push(role.id);
            }
        }

        // used in html when checking for user group membership by id only (e.g. to show/hide delete on patient GroupRole)
        // A unit admin cannot remove patient from groups to which the unit admin is not assigned.
        $scope.permissions.allGroupsIds = [];

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
            }
        }

        // get list of roles available when user is adding a new Group & Role to patient member
        // e.g. unit admins cannot add specialty admin roles to patient members
        roles = $scope.loggedInUser.userInformation.securityRoles;

        var allowedRoles = [];
        for (i = 0; i < roles.length; i++) {
            if ($scope.roleIds.indexOf(roles[i].id) != -1) {
                allowedRoles.push(roles[i]);
            }
        }
        $scope.allowedRoles = allowedRoles;

        // get list of features available when user is adding a new Feature
        if (addPatient) {
            allFeatures = $scope.loggedInUser.userInformation.patientFeatures;
        } else {
            allFeatures = $scope.loggedInUser.userInformation.staffFeatures;
        }
        
        $scope.allFeatures = [];
        for (i = 0; i < allFeatures.length; i++) {
            if (allFeatures[i].name !== 'CENTRAL_SUPPORT_CONTACT') {
                $scope.allFeatures.push({'feature': allFeatures[i]});
            }
        }

        if (addPatient) {
            // get list of identifier types when user adding identifiers to patient members
            $scope.identifierTypes = [];
            StaticDataService.getLookupsByType('IDENTIFIER').then(function (identifierTypes) {
                if (identifierTypes.length > 0) {
                    var noHospitalNumber = [];
                    for (i = 0; i < identifierTypes.length; i++) {
                        if (identifierTypes[i].value !== 'HOSPITAL_NUMBER') {
                            noHospitalNumber.push(identifierTypes[i]);
                        }
                    }
                    $scope.identifierTypes = noHospitalNumber;
                }
            });
        }
        
        clearForm();  
        $scope.showForm = true;
    };

    // check username is not already in use
    $scope.checkUsername = function () {
        UserService.checkUsernameExists($scope.editUser.username).then(function (usernameExists) {
            $scope.editUser.usernameChecked = true;
            if (usernameExists === 'true') {
                $scope.editUser.usernameExists = true;
            } else {
                $scope.editUser.usernameExists = false;
            }
        }, function (errorResult) {
            alert("Error: " + errorResult.data);
        });
    };

    // clear username check when username is changed
    $scope.$watch('editUser.username', function () {
        $scope.editUser.usernameChecked = false;
    });

    // click Create New button
    $scope.create = function () {
        // check date of birth if entered
        if (($scope.editUser.selectedDay != '' || $scope.editUser.selectedMonth != '' || $scope.editUser.selectedYear != '')
            && !UtilService.validationDateNoFuture($scope.editUser.selectedDay, $scope.editUser.selectedMonth, $scope.editUser.selectedYear)) {
            $scope.errorMessage = 'Please enter a valid date of birth (and not in the future)';
        } else {
            // generate password
            var password = UtilService.generatePassword();
            $scope.editUser.password = password;

            UserService.create($scope.editUser).then(function(userId) {
                UserService.get(userId).then(function(result) {
                    result.isNewUser = true;
                    result.password = password;
                    $scope.printSuccessMessage = true;
                    $scope.successMessage = 'User successfully created ' +
                        'with username: ' + $scope.editUser.username + ' ' +
                        'and password: ' + password;
                    clearForm();
                    $scope.showForm = false;
                }, function() {
                    alert('Cannot get user (has been created)');
                });
            }, function(result) {
                if (result.status === 409) {
                    // 409 = CONFLICT, means user already exists
                    $scope.warningMessage = 'A patient with this username or email already exists. Please choose an alternative or search for an existing patient if you want to add them to your group';
                } else {
                    // Other errors treated as standard errors
                    $scope.errorMessage = 'There was an error: ' + result.data;
                }
            });
        }
    };
    
    $scope.showFormUI = function() {
        $scope.showForm = true;    
        delete $scope.successMessage;
    };
    
    var clearForm = function() {        
        delete $scope.errorMessage;
        delete $scope.warningMessage;

        $scope.editUser = {};
        $scope.editUser.groupRoles = [];
        $scope.editUser.availableFeatures = _.clone($scope.allFeatures);
        $scope.editUser.userFeatures = [];
        $scope.editUser.selectedRole = '';
        $scope.editUser.identifiers = [];

        // date of birth
        $scope.months = UtilService.generateMonths();
        $scope.years = UtilService.generateYears();
        $scope.days = UtilService.generateDays();
        $scope.editUser.selectedYear = '';
        $scope.editUser.selectedMonth = '';
        $scope.editUser.selectedDay = '';

        // set initial group and feature (avoid blank option)
        if ($scope.editUser.availableGroups && $scope.editUser.availableGroups.length > 0) {
            $scope.editUser.groupToAdd = $scope.editUser.availableGroups[0].id;
        }
        if ($scope.editUser.availableFeatures && $scope.editUser.availableFeatures.length > 0) {
            $scope.editUser.featureToAdd = $scope.editUser.availableFeatures[0].feature.id;
        }

        // set role to first
        $scope.editUser.selectedRole = $scope.allowedRoles[0].id;
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
    
    init();
}]);