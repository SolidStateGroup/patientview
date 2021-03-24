'use strict';

// Patient controller
angular.module('patientviewApp').controller('PatientsCtrl',['$rootScope', '$scope', '$compile', '$modal', '$timeout', 
    '$location', '$routeParams', 'UserService', 'GroupService', 'RoleService', 'FeatureService', 'StaticDataService',
    'AuthService', 'localStorageService', 'UtilService', '$route', 'ConversationService', '$cookies',
    'DiagnosisService', 'CodeService', 'PatientService', 'Mixins', 'AlertService',
    function ($rootScope, $scope, $compile, $modal, $timeout, $location, $routeParams, UserService, GroupService,
        RoleService, FeatureService, StaticDataService, AuthService, localStorageService, UtilService, $route,
        ConversationService, $cookies, DiagnosisService, CodeService, PatientService, Mixins, AlertService) {

    // mixins for filters and shared code
    angular.extend($scope, Mixins);

    $scope.itemsPerPage = 10;
    $scope.currentPage = 0;
    $scope.sortField = 'surname';
    $scope.sortDirection = 'ASC';
    $scope.initFinished = false;
    $scope.selectedGroup = [];

    $scope.canAddDiagnosis = function () {
        // only Cardiol specialty
        if ($scope.editUser) {
            for (var i=0; i<$scope.editUser.groupRoles.length; i++) {
                if ($scope.editUser.groupRoles[i].group.code === 'Cardiol') {
                    return true;
                }
            }
        }

        return false;
    };

    $scope.addDiagnosis = function (userId, selectedDiagnosis) {
        if (selectedDiagnosis !== undefined && selectedDiagnosis) {
            var diag = JSON.parse(selectedDiagnosis);
            DiagnosisService.addStaffEntered(userId, diag.code).then(function () {
                $scope.getUser($scope.editUser);
            }, function () {
                alert('Failed to add ' + diag.description + ' diagnosis with code "' + diag.code + '"');
            })
        }
    };

    // delete user
    $scope.deleteUser = function (userId) {
        $scope.successMessage = '';
        $scope.errorMessage = '';
        $scope.printSuccessMessage = false;
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

    // Get users based on current user selected filters etc
    $scope.getItems = function () {

        $scope.getAlertsForUser();
        $scope.loading = true;

        var getParameters = {};
        getParameters.page = $scope.currentPage;
        getParameters.size = $scope.itemsPerPage;
        getParameters.sortField = $scope.sortField;
        getParameters.sortDirection = $scope.sortDirection;
        getParameters.roleIds = $scope.roleIds;

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

        // Get alerts for patient delete
        $scope.getAlertsForUser = function () {
            $scope.deletedPatientAlert = [];
            $scope.failedPatientAlert = [];

            // successfully deleted patients alerts
            AlertService.getAlerts($scope.loggedInUser.id, 'PATIENT_DELETED')
                .then(function (deletedPatientAlert) {
                    $scope.deletedPatientAlert = deletedPatientAlert;
                }, function () {
                    alert('Error getting Deleted patients alerts');
                });

            // failed to delete patients alerts
            AlertService.getAlerts($scope.loggedInUser.id, 'PATIENT_DELETE_FAILED')
                .then(function (failedPatientAlert) {
                    $scope.failedPatientAlert = failedPatientAlert;
                }, function () {
                    alert('Error getting Deleted patients alerts');
                });
        };

        $scope.dismissAlert = function (alertId) {
            AlertService.removeAlert($scope.loggedInUser.id, alertId).then(function () {
                $scope.getAlertsForUser();
            }, function () {
                alert('Error removing alert');
            });
        };

    $scope.downloadList = function(){
        if ($scope.total >= 5000) {
            return alert("You are currently viewing more than 5000 patients. \n\nPlease filter further to reduce the output.");
        }

        if (window.confirm("You are about to start downloading this list. \n\nWarning: By continuing you acknowledge that the computer you are using complies with your organisation's security policy")) {

            var groupId = 0;
            if ($scope.selectedGroup.length > 0) {
                groupId = $scope.selectedGroup;
            }

            var status = "";
            if (typeof $scope.statusFilter != 'undefined') {
                status = $scope.statusFilter;
            }


            var downloadUrl = "../api/export/patients/download?token=";
            downloadUrl += $scope.authToken;
            downloadUrl += "&sortField="+ $scope.sortField || "";
            downloadUrl += "&sortDirection=" + $scope.sortDirection || "";
            downloadUrl += "&roleIds=" + $scope.roleIds.join(', ') || "";
            downloadUrl += "&searchUsername=" +  $('#search-username').val()  || "";
            downloadUrl += "&searchForename="+ $('#search-forename').val()  || "";
            downloadUrl += "&searchSurname=" + $('#search-surname').val()  || "";
            downloadUrl += "&searchIdentifier=" + $('#search-identifier').val() || "";
            downloadUrl += "&searchEmail=" +  $('#search-email').val() || "";
            downloadUrl += "&statusFilter=" + status;
            downloadUrl += "&groupIds=" + groupId;

            window.open(downloadUrl, "Your download is now starting.");
        }

    };

    $scope.getUser = function(openedUser) {
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

            // set date of birth dropdowns
            if (user.dateOfBirth != null) {
                user.selectedYear = user.dateOfBirth.split('-')[0].toString();
                user.selectedMonth = user.dateOfBirth.split('-')[1].toString();
                user.selectedDay = user.dateOfBirth.split('-')[2].toString();
            } else {
                user.selectedYear = '';
                user.selectedMonth = '';
                user.selectedDay = '';
            }

            // set the user being edited to a clone of the existing user (so only updated in UI on save)
            $scope.editUser = _.clone(user);

            // set role to first
            $scope.editUser.selectedRole = $scope.allowedRoles[0].id;

            // set latest staff entered diagnosis if present
            if ($scope.canAddDiagnosis()) {
                // get diagnosis codes if not already retrieved for previous patient, note: selectize needs to happen
                // after all data is loaded
                if ($scope.diagnosisCodes) {
                    // get staff entered diagnosis (condition)
                    DiagnosisService.getStaffEntered(openedUser.id).then(function (conditions) {
                        if (conditions.length) {
                            var latest = conditions[0];
                            for (var i = 0; i < conditions.length; i++) {
                                if (conditions[i].date > latest.date) {
                                    latest = conditions[i];
                                }
                            }
                            if (latest.status === 'confirmed') {
                                $scope.editUser.staffEnteredDiagnosis = latest;
                            }
                        }

                        $timeout(function() {
                            $('#select-diagnosis-' + $scope.editUser.id).selectize({
                                sortField: 'text'
                            });
                        });
                    }, function () {
                        alert('Error retrieving staff entered condition information');
                    });
                } else {
                    // don't already have diagnosisCodes in scope, retrieve
                    CodeService.getAllDiagnosisCodes().then(function (codes) {
                        $scope.diagnosisCodes = codes;

                        // get staff entered diagnosis (condition)
                        DiagnosisService.getStaffEntered(openedUser.id).then(function (conditions) {
                            if (conditions.length) {
                                var latest = conditions[0];
                                for (var i = 0; i < conditions.length; i++) {
                                    if (conditions[i].date > latest.date) {
                                        latest = conditions[i];
                                    }
                                }
                                if (latest.status === 'confirmed') {
                                    $scope.editUser.staffEnteredDiagnosis = latest;
                                }
                            }

                            $timeout(function () {
                                $('#select-diagnosis-' + $scope.editUser.id).selectize({
                                    sortField: 'text'
                                });
                            });
                        }, function () {
                            alert('Error retrieving staff entered condition information');
                        });
                    }, function () {
                    });
                }
            }

            // timeout required to send broadcast after everything else done
            $timeout(function() {
                getPatientManagement(user);
            });

            openedUser.editLoading = false;
        }, function(failureResult) {
            openedUser.showEdit = false;
            openedUser.editLoading = false;
            alert('Cannot open patient: ' + failureResult.data);
        });
    };

    var getPatientManagement = function (user) {
        // get patient management information based on group with IBD_PATIENT_MANAGEMENT feature
        var patientManagementGroupId = null;
        var patientManagementIdentifierId = null;
        var i, j;

        for (i = 0; i < user.groupRoles.length; i++) {
            if (patientManagementGroupId == null) {
                var group = user.groupRoles[i].group;
                if (group.groupFeatures != null && group.groupFeatures != undefined) {
                    for (j = 0; j < group.groupFeatures.length; j++) {
                        if (group.groupFeatures[j].feature.name === 'IBD_PATIENT_MANAGEMENT') {
                            patientManagementGroupId = group.id;
                        }
                    }
                }
            }
        }

        // based on first identifier
        for (i = 0; i < user.identifiers.length; i++) {
            if (patientManagementIdentifierId == null) {
                patientManagementIdentifierId = user.identifiers[i].id;
            }
        }

        if (patientManagementGroupId !== null && patientManagementIdentifierId !== null) {
            PatientService.getPatientManagement(user.id, patientManagementGroupId, patientManagementIdentifierId)
                .then(function (patientManagement) {
                    if (patientManagement !== undefined && patientManagement !== null) {
                        $scope.patientManagement = patientManagement;
                    } else {
                        $scope.patientManagement = {};
                    }

                    $scope.patientManagement.groupId = patientManagementGroupId;
                    $scope.patientManagement.identifierId = patientManagementIdentifierId;
                    $scope.patientManagement.userId = user.id;

                    $timeout(function() {
                        $scope.$broadcast('patientManagementInit', {});
                    });
                }, function () {
                    alert('Error retrieving patient management information');
                });
        }
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

        $scope.months = UtilService.generateMonths();
        $scope.years = UtilService.generateYears();
        $scope.days = UtilService.generateDays();

        $('body').click(function () {
            $('.child-menu').remove();
        });

        var i, role, group;
        $scope.loadingMessage = 'Loading Patients';
        $scope.loading = true;
        $scope.allGroups = [];
        $scope.allRoles = [];
        $scope.roleIds = [];
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
            $scope.permissions.canDeleteGroupRolesDuringEdit = true;
            // to see the option to permanently delete patients
            $scope.permissions.canDeleteUsers = true;
        }

        if ($scope.permissions.isSuperAdmin || $scope.permissions.isSpecialtyAdmin || $scope.permissions.isUnitAdmin) {
            $scope.permissions.showExportButton = true;
        }

        // only allow GLOBAL_ADMIN or SPECIALTY_ADMIN or UNIT_ADMIN ...
        if ($scope.permissions.isSuperAdmin || $scope.permissions.isSpecialtyAdmin || $scope.permissions.isUnitAdmin) {
            $scope.permissions.showDeleteMenuOption = true;
            $scope.permissions.showMembershipRequestMenuOption = true;
            $scope.permissions.canCreatePatients = true;
            $scope.permissions.canEditPatients = true;
            $scope.permissions.canResetPasswords = true;
            $scope.permissions.canSendVerificationEmails = true;
        }

        // STAFF_ADMIN, DISEASE_GROUP_ADMIN can only view
        if (!($scope.permissions.isSuperAdmin || $scope.permissions.isSpecialtyAdmin
            || $scope.permissions.isUnitAdmin)) {
            $scope.permissions.canViewPatients = true;
        }

        $scope.permissions.messagingEnabled = ConversationService.userHasMessagingFeature();

        // get patient type roles
        var roles = $scope.loggedInUser.userInformation.patientRoles;

        // handle back button to patients from dashboard
        if (roles === null) {
            $rootScope.switchUserBack();
            $timeout(function(){
                $route.reload();
            }, 3000);
        } else {

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
                            || group.groupType.value === 'GENERAL_PRACTICE') {
                            $scope.showOtherGroupFilter = true;
                            $scope.filterOtherGroups.push(minimalGroup);
                        } else if (group.groupType.value === 'SPECIALTY') {
                            $scope.showSpecialtyFilter = true;
                            $scope.filterSpecialtyGroups.push(minimalGroup);
                        }
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

                $scope.initFinished = true;
            } else {
                // no groups found
                delete $scope.loading;
                $scope.fatalErrorMessage = 'No user groups found, cannot retrieve patients';
            }
        }
    };

    // Opened for edit
    $scope.opened = function (openedUser) {
        $scope.successMessage = '';
        $scope.printSuccessMessage = false;
        $scope.editUser = '';
        $scope.editMode = true;
        $scope.saved = '';
        delete $scope.patientManagement;

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
            $scope.getUser(openedUser)
        }
    };

    // handle opening modal for creating membership request
    $scope.openModalCreateMembershipRequest = function (user) {
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
            templateUrl: 'createMembershipRequestModal.html',
            controller: CreateMembershipRequestModalInstanceCtrl,
            size: 'lg',
            backdrop: 'static',
            resolve: {
                ConversationService : function() {
                    return ConversationService;
                },
                GroupService: function() {
                    return GroupService;
                },
                permissions: function() {
                    return $scope.permissions;
                },
                user: function() {
                    return user;
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

    // handle opening modal for finding existing patient by identifier value
    $scope.openModalFindExistingPatient = function (size) {
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
            templateUrl: 'findExistingPatientModal.html',
            controller: FindExistingPatientModalInstanceCtrl,
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
                identifierTypes: function(){
                    return $scope.identifierTypes;
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

    // open modal for new conversation
    $scope.openModalNewConversation = function (size, user) {
        delete $scope.errorMessage;
        delete $scope.successMessage;

        $scope.conversationUser = user;

        // open modal
        var modalInstance = $modal.open({
            templateUrl: 'newConversationModal.html',
            controller: NewPatientConversationModalInstanceCtrl,
            size: size,
            backdrop: 'static',
            resolve: {
                ConversationService: function () {
                    return ConversationService;
                },
                GroupService: function () {
                    return GroupService;
                },
                ConversationUser: function(){
                    return user;
                },
                UtilService: function(){
                    return UtilService;
                }
            }
        });

        modalInstance.result.then(function () {
            $scope.successMessage = 'Successfully sent message';
        }, function () {
            $scope.editConversation = '';
        });
    };

    $scope.removeDiagnosis = function () {
        DiagnosisService.removeStaffEntered($scope.editUser.id).then(function() {
            $scope.getUser($scope.editUser);
        }, function() {
            alert('Failed to remove diagnosis.');
        })
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

    // Save from edit
    $scope.save = function (editUserForm, user) {
        delete $scope.successMessage;

        if ((user.selectedDay != '' || user.selectedMonth != '' || user.selectedYear != '')
            && !UtilService.validationDateNoFuture(user.selectedDay, user.selectedMonth, user.selectedYear)
            && !(user.selectedDay == '' && user.selectedMonth == '' && user.selectedYear == '')) {
            alert('Please enter a valid date of birth (and not in the future)');
        } else {
            user.dateOfBirth = null;
            UserService.save(user).then(function () {
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
                            headerDetails.dateOfBirth = successResult.dateOfBirth;
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
        }
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

    // view patient
    $scope.viewUser = function (userId) {
        $scope.loadingMessage = 'Viewing Patient';
        $scope.loading = true;
        $scope.successMessage = '';
        $scope.printSuccessMessage = false;
        $rootScope.switchingUser = true;
        var currentToken = $rootScope.authToken;

        AuthService.switchUser(userId, null).then(function(authToken) {

            $rootScope.previousAuthToken = currentToken;
            localStorageService.set('previousAuthToken', currentToken);

            $rootScope.previousLoggedInUser = $scope.loggedInUser;
            localStorageService.set('previousLoggedInUser', $scope.loggedInUser);

            $rootScope.previousLocation = '/patients';
            localStorageService.set('previousLocation', '/patients');

            $rootScope.authToken = authToken;
            $cookies.authToken = authToken;
            localStorageService.set('authToken', authToken);

            // get user information, store in session
            AuthService.getUserInformation({'token' : authToken}).then(function (userInformation) {

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
                alert('Error receiving user information');
                $scope.loading = false;
            });

        }, function() {
            alert('Cannot view patient');
            $scope.loading = false;
            delete $rootScope.switchingUser;
        });
    };

    // update page when currentPage is changed
    $scope.$watch('currentPage', function(value) {
        delete $scope.successMessage;
        if ($scope.initFinished === true) {
            $scope.currentPage = value;
            $scope.getItems();
        }
    });

    $scope.init();
}]);
