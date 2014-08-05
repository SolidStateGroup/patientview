'use strict';

angular.module('patientviewApp').controller('UserDetailsCtrl', ['$scope', 'UserService', 'IdentifierService', function ($scope, UserService, IdentifierService) {
    var i, j;

    // add group to current group, remove from allowed
    $scope.addGroup = function (form, user, groupId) {
        if(_.findWhere(user.availableGroups, {id: groupId}) && _.findWhere($scope.allowedRoles, {id: user.selectedRole})) {
            var newGroup = _.findWhere($scope.allGroups, {id: groupId});
            newGroup.role = _.findWhere($scope.allowedRoles, {id: user.selectedRole});

            if ($scope.editMode) {
                UserService.addGroupRole(user, newGroup.id, newGroup.role.id).then(function () {
                    user.groups.push(newGroup);
                    user.availableGroups = _.without(user.availableGroups, _.findWhere(user.availableGroups, {id: groupId}));

                    // for REST compatibility
                    user.groupRoles = [];
                    for (i = 0; i < user.groups.length; i++) {
                        var group = user.groups[i];
                        user.groupRoles.push({'group': group, 'role': group.role});
                    }

                    if (user.availableGroups && user.availableGroups.length > 0) {
                        user.groupToAdd = user.availableGroups[0].id;
                    }

                    // update accordion header with data from GET
                    UserService.get(user.id).then(function (successResult) {
                        for(i=0;i<$scope.list.length;i++) {
                            if($scope.list[i].id == user.id) {
                                var headerDetails = $scope.list[i];
                                headerDetails.groupRoles = successResult.groupRoles;
                            }
                        }
                    }, function () {
                        // failure
                        alert('Error updating header (saved successfully)');
                    });

                    form.$setDirty(true);
                }, function () {
                    // failure
                    alert('Error adding group role');
                });
            } else {
                user.groups.push(newGroup);
                user.availableGroups = _.without(user.availableGroups, _.findWhere(user.availableGroups, {id: groupId}));

                // for REST compatibility
                user.groupRoles = [];
                for (i = 0; i < user.groups.length; i++) {
                    var group = user.groups[i];
                    user.groupRoles.push({'group': group, 'role': group.role});
                }

                if (user.availableGroups && user.availableGroups.length > 0) {
                    user.groupToAdd = user.availableGroups[0].id;
                }

                form.$setDirty(true);
            }
        }
    };

    // remove group from current groups, add to allowed groups
    $scope.removeGroup = function (form, user, group) {
        if ($scope.editMode) {
            UserService.deleteGroupRole(user, group.id, group.role.id).then(function () {

                user.groups = _.without(user.groups, _.findWhere(user.groups, {id: group.id}));
                user.availableGroups.push(group);
                user.availableGroups = _.sortBy(user.availableGroups, 'name');

                if (user.availableGroups && user.availableGroups.length > 0) {
                    $scope.groupToAdd = user.availableGroups[0].id;
                }

                // for REST compatibility
                user.groupRoles = [];
                for (i = 0; i < user.groups.length; i++) {
                    var tempGroup = user.groups[i];
                    user.groupRoles.push({'group': tempGroup, 'role': tempGroup.role});
                }

                form.$setDirty(true);

                // update accordion header with data from GET
                UserService.get(user.id).then(function (successResult) {
                    for(i=0;i<$scope.list.length;i++) {
                        if($scope.list[i].id == user.id) {
                            var headerDetails = $scope.list[i];
                            headerDetails.groupRoles = successResult.groupRoles;
                        }
                    }
                }, function () {
                    // failure
                    alert('Error updating header (saved successfully)');
                });
            }, function () {
                // failure
                alert('Error deleting group role');
            });
        } else {
            user.groups = _.without(user.groups, _.findWhere(user.groups, {id: group.id}));
            user.availableGroups.push(group);
            user.availableGroups = _.sortBy(user.availableGroups, 'name');

            if (user.availableGroups && user.availableGroups.length > 0) {
                $scope.groupToAdd = user.availableGroups[0].id;
            }

            // for REST compatibility
            user.groupRoles = [];
            for (i = 0; i < user.groups.length; i++) {
                var tempGroup = user.groups[i];
                user.groupRoles.push({'group': tempGroup, 'role': tempGroup.role});
            }

            form.$setDirty(true);
        }
    };

    // add feature to current feature, remove from allowed
    $scope.addFeature = function (form, user, featureId) {
        if ($scope.editMode) {
            UserService.addFeature(user, featureId).then(function () {
                for (i = 0; i < user.availableFeatures.length; i++) {
                    if (user.availableFeatures[i].feature.id === featureId) {
                        user.userFeatures.push(user.availableFeatures[i]);
                        user.availableFeatures.splice(i, 1);
                    }
                }

                if ($scope.editUser.availableFeatures && $scope.editUser.availableFeatures.length > 0) {
                    $scope.editUser.featureToAdd = $scope.editUser.availableFeatures[0].feature.id;
                }

                form.$setDirty(true);

                // update accordion header with data from GET
                UserService.get(user.id).then(function (successResult) {
                    for(var i=0;i<$scope.list.length;i++) {
                        if($scope.list[i].id == user.id) {
                            var headerDetails = $scope.list[i];
                            headerDetails.userFeatures = successResult.userFeatures;
                        }
                    }
                }, function () {
                    // failure
                    alert('Error updating header (saved successfully)');
                });
            }, function () {
                // failure
                alert('Error saving feature');
            });
        } else {
            for (i = 0; i < user.availableFeatures.length; i++) {
                if (user.availableFeatures[i].feature.id === featureId) {
                    user.userFeatures.push(user.availableFeatures[i]);
                    user.availableFeatures.splice(i, 1);
                }
            }

            if ($scope.editUser.availableFeatures && $scope.editUser.availableFeatures.length > 0) {
                $scope.editUser.featureToAdd = $scope.editUser.availableFeatures[0].feature.id;
            }

            form.$setDirty(true);
        }
    };

    // remove feature from current features, add to allowed features
    $scope.removeFeature = function (form, user, feature) {
        if ($scope.editMode) {
            UserService.deleteFeature(user, feature.feature).then(function () {

                for (i = 0; i < user.userFeatures.length; i++) {
                    if (user.userFeatures[i].feature.id === feature.feature.id) {
                        user.availableFeatures.push(user.userFeatures[i]);
                        user.userFeatures.splice(i, 1);
                    }
                }

                if ($scope.editUser.availableFeatures && $scope.editUser.availableFeatures.length > 0) {
                    $scope.editUser.featureToAdd = $scope.editUser.availableFeatures[0].feature.id;
                }

                form.$setDirty(true);

                // update accordion header with data from GET
                UserService.get(user.id).then(function (successResult) {
                    for(var i=0;i<$scope.list.length;i++) {
                        if($scope.list[i].id == user.id) {
                            var headerDetails = $scope.list[i];
                            headerDetails.userFeatures = successResult.userFeatures;
                        }
                    }
                }, function () {
                    // failure
                    alert('Error updating header (saved successfully)');
                });
            }, function () {
                // failure
                alert('Error deleting feature');
            });
        } else {
            for (i = 0; i < user.userFeatures.length; i++) {
                if (user.userFeatures[i].feature.id === feature.feature.id) {
                    user.availableFeatures.push(user.userFeatures[i]);
                    user.userFeatures.splice(i, 1);
                }
            }

            if ($scope.editUser.availableFeatures && $scope.editUser.availableFeatures.length > 0) {
                $scope.editUser.featureToAdd = $scope.editUser.availableFeatures[0].feature.id;
            }

            form.$setDirty(true);
        }
    };

    $scope.validateNHSNumber = function(txtNhsNumber) {
        var isValid = false;

        if (txtNhsNumber.length == 10) {
            var total = 0, i;
            for (i = 0; i <= 8; i++) {
                var digit = txtNhsNumber.substr(i, 1);
                var factor = 10 - i;
                total += (digit * factor);
            }

            var checkDigit = (11 - (total % 11));
            if (checkDigit == 11) { checkDigit = 0; }
            if (checkDigit == txtNhsNumber.substr(9, 1)) { isValid = true; }
        }

        return isValid;
    };

    $scope.addIdentifier = function (form, user, identifier) {

        if (identifier.identifierType !== undefined) {
            identifier.identifierType = _.findWhere($scope.identifierTypes, {id: identifier.identifierType});

            // validate NHS_NUMBER
            var valid = true, errorMessage = '';

            if (identifier.identifierType.value === 'NHS_NUMBER' && user.dummy != true) {
                valid = $scope.validateNHSNumber(identifier.identifier);
                errorMessage = 'Invalid NHS Number, please check format';
            }

            if (valid) {
                if ($scope.editMode) {
                    delete identifier.id;
                    UserService.addIdentifier(user, identifier).then(function (successResult) {
                        // added identifier
                        identifier.id = successResult.id;
                        user.identifiers.push(_.clone(identifier));
                        identifier.identifier = '';
                        form.$setDirty(true);

                        // update accordion header with data from GET
                        UserService.get(user.id).then(function (successResult) {
                            for(var i=0;i<$scope.list.length;i++) {
                                if($scope.list[i].id == user.id) {
                                    var headerDetails = $scope.list[i];
                                    headerDetails.identifiers = successResult.identifiers;
                                }
                            }
                        }, function () {
                            // failure
                            alert('Error updating header (saved successfully)');
                        });

                    }, function () {
                        // failure
                        alert('Error adding identifier');
                    });
                } else {
                    identifier.id = Math.floor(Math.random() * (9999)) - 10000;
                    user.identifiers.push(_.clone(identifier));
                    identifier.identifier = '';
                    form.$setDirty(true);
                }
            } else {
                identifier.identifierType = identifier.identifierType.id;
                alert(errorMessage);
            }
        }
    };

    $scope.updateIdentifier = function (event, form, user, identifier) {
        identifier.saved = false;

        // try and save identifier
        IdentifierService.save(identifier).then(function () {
            // saved identifier
            identifier.saved = true;
            form.$setDirty(true);

            // update accordion header with data from GET
            UserService.get(user.id).then(function (successResult) {
                for(var i=0;i<$scope.list.length;i++) {
                    if($scope.list[i].id == user.id) {
                        var headerDetails = $scope.list[i];
                        headerDetails.identifiers = successResult.identifiers;
                    }
                }
            }, function () {
                // failure
                alert('Error updating header (saved successfully)');
            });
        }, function() {
            // failure
            alert('Error saving identifier');
        });
    };

    $scope.removeIdentifier = function (form, user, identifier) {
        if ($scope.editMode) {
            IdentifierService.delete(identifier).then(function () {
                // deleted identifier
                for (i = 0; i < user.identifiers.length; i++) {
                    if (user.identifiers[i].id === identifier.id) {
                        user.identifiers.splice(i, 1);
                    }
                }
                form.$setDirty(true);

                // update accordion header with data from GET
                UserService.get(user.id).then(function (successResult) {
                    for(var i=0;i<$scope.list.length;i++) {
                        if($scope.list[i].id == user.id) {
                            var headerDetails = $scope.list[i];
                            headerDetails.identifiers = successResult.identifiers;
                        }
                    }
                }, function () {
                    // failure
                    alert('Error updating header (saved successfully)');
                });
            }, function () {
                // failure
                alert('Error deleting identifier');
            });
        } else {
            for (i = 0; i < user.identifiers.length; i++) {
                if (user.identifiers[i].id === identifier.id) {
                    user.identifiers.splice(i, 1);
                }
            }
            form.$setDirty(true);
        }
    };
}]);
