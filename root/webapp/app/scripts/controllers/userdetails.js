'use strict';

angular.module('patientviewApp').controller('UserDetailsCtrl', ['$scope', 'UserService', 'IdentifierService',
function ($scope, UserService, IdentifierService) {
    var i;

    // add group to current group, remove from allowed
    $scope.addGroupRole = function (form, user, groupId, roleId) {
        if ($scope.editMode) {
            UserService.addGroupRole(user, groupId, roleId).then(function () {
                // update accordion header with data from GET
                UserService.get(user.id).then(function (successResult) {
                    for(i=0;i<$scope.pagedItems.length;i++) {
                        if($scope.pagedItems[i].id === user.id) {
                            var headerDetails = $scope.pagedItems[i];
                            headerDetails.groupRoles = successResult.groupRoles;
                        }
                    }
                    user.groupRoles = successResult.groupRoles;
                }, function () {
                    alert('Error updating header (saved successfully)');
                });

                form.$setDirty(true);
            }, function () {
                alert('Error adding group role, may already exist');
            });
        } else {
            var groupRole = {};

            for (i=0;i<$scope.allGroups.length;i++){
                if ($scope.allGroups[i].id === groupId) {
                    groupRole.group = $scope.allGroups[i];
                }
            }

            for (i=0;i<$scope.allowedRoles.length;i++){
                if ($scope.allowedRoles[i].id === roleId) {
                    groupRole.role = $scope.allowedRoles[i];
                }
            }

            var canAddGroupRole = true;

            for (i=0;i<user.groupRoles.length;i++) {
                var existingGroupRole = user.groupRoles[i];
                if (groupRole.group.id === existingGroupRole.group.id &&
                    groupRole.role.id === existingGroupRole.role.id) {
                    canAddGroupRole = false
                }
            }

            if (canAddGroupRole) {
                groupRole.id = Math.floor(Math.random() * (9999)) -10000;
                user.groupRoles.push(groupRole);
                form.$setDirty(true)
            } else {
                alert("Group and Role already exist and cannot be added again")
            }
        }
    };

    // remove group from current groups, add to allowed groups
    $scope.removeGroupRole = function (form, user, groupRole) {
        if ($scope.editMode) {
            UserService.deleteGroupRole(user, groupRole.group.id, groupRole.role.id).then(function () {
                UserService.get(user.id).then(function (successResult) {
                    for(i=0;i<$scope.pagedItems.length;i++) {
                        if($scope.pagedItems[i].id === user.id) {
                            var headerDetails = $scope.pagedItems[i];
                            headerDetails.groupRoles = successResult.groupRoles;
                        }
                    }
                    user.groupRoles = successResult.groupRoles;
                }, function () {
                    alert('Error loading data (but saved successfully)');
                    $scope.getItems();
                });
            }, function () {
                alert('Error deleting group role');
            });
        } else {
            user.groupRoles = _.without(user.groupRoles, _.findWhere(user.groupRoles, {id: groupRole.id}));
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
                    for(var i=0;i<$scope.pagedItems.length;i++) {
                        if($scope.pagedItems[i].id === user.id) {
                            var headerDetails = $scope.pagedItems[i];
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
                    for(var i=0;i<$scope.pagedItems.length;i++) {
                        if($scope.pagedItems[i].id === user.id) {
                            var headerDetails = $scope.pagedItems[i];
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

        if (txtNhsNumber.length === 10) {
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

            if (identifier.identifierType.value === 'NHS_NUMBER' && user.dummy !== true) {
                valid = $scope.validateNHSNumber(identifier.identifier);
                errorMessage = 'Invalid NHS Number, please check format';
            }

            if (valid) {
                if ($scope.editMode) {
                    UserService.getIdentifierByValue(identifier.identifier).then(function () {
                        alert('Identifier already exists for another patient, please choose another');
                    }, function () {
                        delete identifier.id;
                        UserService.addIdentifier(user, identifier).then(function (successResult) {
                            // added identifier
                            identifier.id = successResult.id;
                            user.identifiers.push(_.clone(identifier));
                            identifier.identifier = '';
                            form.$setDirty(true);

                            // update accordion header with data from GET
                            UserService.get(user.id).then(function (successResult) {
                                for (var i = 0; i < $scope.pagedItems.length; i++) {
                                    if ($scope.pagedItems[i].id === user.id) {
                                        var headerDetails = $scope.pagedItems[i];
                                        headerDetails.identifiers = successResult.identifiers;
                                    }
                                }
                            }, function () {
                                alert('Error updating header (saved successfully)');
                            });

                        }, function () {
                            alert('Error adding identifier');
                        });
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
                for(var i=0;i<$scope.pagedItems.length;i++) {
                    if($scope.pagedItems[i].id === user.id) {
                        var headerDetails = $scope.pagedItems[i];
                        headerDetails.identifiers = successResult.identifiers;
                    }
                }
            }, function () {
                // failure
                alert('Error updating header (saved successfully)');
            });
        }, function(failureResult) {
            if (failureResult.status === 409) {
                alert(failureResult.data)
            } else {
                alert('There has been an error saving');
            }
        });
    };

    $scope.removeIdentifier = function (form, user, identifier) {
        if ($scope.editMode) {
            IdentifierService.remove(identifier).then(function () {
                // deleted identifier
                for (i = 0; i < user.identifiers.length; i++) {
                    if (user.identifiers[i].id === identifier.id) {
                        user.identifiers.splice(i, 1);
                    }
                }
                form.$setDirty(true);

                // update accordion header with data from GET
                UserService.get(user.id).then(function (successResult) {
                    for(var i=0;i<$scope.pagedItems.length;i++) {
                        if($scope.pagedItems[i].id === user.id) {
                            var headerDetails = $scope.pagedItems[i];
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
