'use strict';
var DeletePatientModalInstanceCtrl = ['$scope', '$modalInstance','permissions','user','UserService','allGroups',
    'allRoles','$q',
    function ($scope, $modalInstance, permissions, user, UserService, allGroups, allRoles, $q) {
        var i, j, inMyGroups = false, notMyGroupCount = 0;
        $scope.successMessage = '';
        $scope.errorMessage = '';
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

        // remove from my groups
        $scope.removeFromMyGroups = function () {
            var promises = [];
            var groupsToRemove = [];
            var userNonSpecialtyGroups = [];

            // get userNonSpecialtyGroups
            for (j=0;j<user.groupRoles.length;j++) {
                var groupRoleGroupCode = user.groupRoles[j].group.code;
                var groupRoleGroupType = user.groupRoles[j].group.groupType.value;

                if (groupRoleGroupCode !== 'Generic' && groupRoleGroupType !== 'SPECIALTY') {
                    userNonSpecialtyGroups.push(user.groupRoles[j].group);
                }
            }

            // find intersection of userNonSpecialtyGroups and allGroups
            for (i = 0; i< allGroups.length; i++) {
                for (j = 0; j < userNonSpecialtyGroups.length; j++) {
                    if (allGroups[i].id === userNonSpecialtyGroups[j].id) {
                        groupsToRemove.push(allGroups[i]);
                    }
                }
            }

            // remove group roles from user where group is my unit with multiple deleteGroupRole
            for (i=0;i<groupsToRemove.length;i++) {
                for (j=0;j<allRoles.length;j++) {
                    promises.push(UserService.deleteGroupRole(user, groupsToRemove[i].id, allRoles[j].id));
                }
            }
            $q.all(promises).then(function () {
                $scope.user.canRemoveFromMyGroups = false;
                $scope.user.removedFromGroups = true;
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
                    $scope.successMessage += ' and requested deletion is processing now. A notification will appear on your Patients page once this has completed.';
                    $scope.user.canDelete = false;
                }
            }, function() {
                $scope.errorMessage = 'There was an error.';
            });
        };

        // delete patient permanently
        $scope.remove = function () {
            UserService.remove(user).then(function() {
                $scope.successMessage = 'The requested deletion is processing now. A notification will appear on your Patients page once this has completed.';
                $scope.user.canRemoveFromMyGroups = false;
                $scope.user.canRemoveFromAllGroups = false;
                $scope.user.canDelete = false;
            }, function(error) {
                $scope.errorMessage = 'There was an error';
            });
        };

        $scope.cancel = function () {
            $modalInstance.close();
        };
    }];
