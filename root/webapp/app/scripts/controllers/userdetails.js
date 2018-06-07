'use strict';

angular.module('patientviewApp').controller('UserDetailsCtrl', ['$scope', '$rootScope', 'UserService',
    'IdentifierService', 'localStorageService',
function ($scope, $rootScope, UserService, IdentifierService, localStorageService) {
    var i;

    // add group to current group, remove from allowed
    $scope.addGroupRole = function (form, user, groupId, roleId) {
        if (groupId > 0 && roleId > 0) {
            if ($scope.editMode) {
                UserService.addGroupRole(user, groupId, roleId).then(function () {
                    // update accordion header with data from GET
                    UserService.get(user.id).then(function (successResult) {
                        for (i = 0; i < $scope.pagedItems.length; i++) {
                            if ($scope.pagedItems[i].id === user.id) {
                                var headerDetails = $scope.pagedItems[i];
                                headerDetails.groupRoles = successResult.groupRoles;
                            }
                        }
                        user.groupRoles = successResult.groupRoles;
                    }, function () {
                        alert('Error updating header (saved successfully)');
                    });
                }, function () {
                    alert('Error adding group role, may already exist');
                });
            } else {
                var groupRole = {};

                for (i = 0; i < $scope.allGroups.length; i++) {
                    if ($scope.allGroups[i].id === groupId) {
                        groupRole.group = $scope.allGroups[i];
                    }
                }

                for (i = 0; i < $scope.allowedRoles.length; i++) {
                    if ($scope.allowedRoles[i].id === roleId) {
                        groupRole.role = $scope.allowedRoles[i];
                    }
                }

                var canAddGroupRole = true;

                for (i = 0; i < user.groupRoles.length; i++) {
                    var existingGroupRole = user.groupRoles[i];
                    if (groupRole.group.id === existingGroupRole.group.id &&
                        groupRole.role.id === existingGroupRole.role.id) {
                        canAddGroupRole = false;
                    }
                }

                if (canAddGroupRole) {
                    groupRole.id = Math.floor(Math.random() * (9999)) - 10000;
                    user.groupRoles.push(groupRole);
                    form.$setDirty(true)
                } else {
                    alert('Group and Role already exist and cannot be added again');
                }
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
                    // saved user, but no longer have rights to view
                });
            }, function () {
                alert('Error deleting group role');
            });
        } else {
            user.groupRoles = _.without(user.groupRoles, _.findWhere(user.groupRoles, {id: groupRole.id}));
            form.$setDirty(true);
        }
    };

    // add feature to current feature, remove from allowed, add to current logged in user if required
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

                // update accordion header with data from GET
                UserService.get(user.id).then(function (successResult) {
                    for(var i=0;i<$scope.pagedItems.length;i++) {
                        if($scope.pagedItems[i].id === user.id) {
                            var headerDetails = $scope.pagedItems[i];
                            headerDetails.userFeatures = successResult.userFeatures;
                        }
                    }
                    if (user.id == $rootScope.loggedInUser.id) {
                        if (localStorageService.isSupported) {
                            var localStorageUser = localStorageService.get('loggedInUser');
                            localStorageUser.userFeatures = successResult.userFeatures;
                            localStorageService.set('loggedInUser', localStorageUser);
                        }
                        $rootScope.loggedInUser.userFeatures = successResult.userFeatures;                        
                    }
                }, function () {
                    alert('Error updating header (saved successfully)');
                });
            }, function () {
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

                // update accordion header with data from GET
                UserService.get(user.id).then(function (successResult) {
                    for(var i=0;i<$scope.pagedItems.length;i++) {
                        if($scope.pagedItems[i].id === user.id) {
                            var headerDetails = $scope.pagedItems[i];
                            headerDetails.userFeatures = successResult.userFeatures;
                        }
                    }
                    if (user.id == $rootScope.loggedInUser.id) {
                        if (localStorageService.isSupported) {
                            var localStorageUser = localStorageService.get('loggedInUser');
                            localStorageUser.userFeatures = successResult.userFeatures;
                            localStorageService.set('loggedInUser', localStorageUser);
                        }
                        $rootScope.loggedInUser.userFeatures = successResult.userFeatures;
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

    // user should have only one type of each identifier type
    var identifierTypeExists = function(user, identifierType) {
        for (var i=0;i<user.identifiers.length;i++) {
            if (user.identifiers[i].identifierType.value === identifierType.value) {
                return true;
            }
        }

        return false;
    };

    $scope.addIdentifier = function (form, user, identifier) {
        var newIdentifier = _.clone(identifier);

        if (newIdentifier.identifierType !== undefined) {
            newIdentifier.identifierType = _.findWhere($scope.identifierTypes, {id: newIdentifier.identifierType});

            if (!identifierTypeExists(user, newIdentifier.identifierType)) {

                UserService.validateIdentifier(user.id, newIdentifier, user.dummy).then(function () {
                    if ($scope.editMode) {
                        delete newIdentifier.id;
                        UserService.addIdentifier(user, newIdentifier).then(function () {

                            // added identifier
                            delete identifier.id;
                            delete identifier.identifier;

                            // update accordion header with data from GET
                            UserService.get(user.id).then(function (successResult) {
                                for (var i = 0; i < $scope.pagedItems.length; i++) {
                                    if ($scope.pagedItems[i].id === user.id) {
                                        var headerDetails = $scope.pagedItems[i];
                                        headerDetails.identifiers = successResult.identifiers;
                                    }
                                }
                                user.identifiers = successResult.identifiers;
                            }, function () {
                                alert('Error updating header (saved successfully)');
                            });

                        }, function (failureResult) {
                            if (failureResult.status === 409) {
                                alert(failureResult.data);
                            } else {
                                alert('There has been an error saving');
                            }
                        });
                    } else {
                        identifier.id = Math.floor(Math.random() * (9999)) - 10000;
                        user.identifiers.push(newIdentifier);
                        identifier.identifier = '';
                        form.$setDirty(true);
                    }
                }, function (failure) {
                    alert(failure.data);
                });
            } else {
                alert('An Identifier of this type already exists for this user');
            }
        }
    };

    $scope.updateIdentifier = function (event, form, user, identifier) {
        identifier.saved = false;

        // try and save identifier
        UserService.validateIdentifier(user.id, identifier, user.dummy).then(function () {
            IdentifierService.save(identifier).then(function () {
                // saved identifier
                identifier.saved = true;

                // update accordion header with data from GET
                UserService.get(user.id).then(function (successResult) {
                    for(var i=0;i<$scope.pagedItems.length;i++) {
                        if($scope.pagedItems[i].id === user.id) {
                            var headerDetails = $scope.pagedItems[i];
                            headerDetails.identifiers = successResult.identifiers;
                        }
                    }
                    user.identifiers = successResult.identifiers;
                }, function () {
                    alert('Error updating header (saved successfully)');
                });
            }, function(failureResult) {
                if (failureResult.status === 409) {
                    alert(failureResult.data);
                } else {
                    alert('There has been an error saving');
                }
            });
        }, function (failure) {
            alert(failure.data);
        });
    };

    $scope.removeIdentifier = function (form, user, identifier) {
        if ($scope.editMode) {

            // must always have at least one identifier
            if (user.identifiers.length < 2) {
                alert('Must have at least one identifier.\n' +
                    'If you have made a mistake, please add the correct identifier before deleting this one.');
            } else {
                IdentifierService.remove(identifier).then(function () {

                    // update accordion header with data from GET
                    UserService.get(user.id).then(function (successResult) {
                        for (var i = 0; i < $scope.pagedItems.length; i++) {
                            if ($scope.pagedItems[i].id === user.id) {
                                var headerDetails = $scope.pagedItems[i];
                                headerDetails.identifiers = successResult.identifiers;
                            }
                        }
                        user.identifiers = successResult.identifiers;
                    }, function () {
                        alert('Error updating header (saved successfully)');
                    });
                }, function (failure) {
                    alert('Error deleting identifier: ' + failure.data);
                });
            }
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
