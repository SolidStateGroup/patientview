'use strict';

angular.module('patientviewApp').controller('GroupDetailsCtrl', ['$scope', 'GroupService', 'LinkService', 'LocationService', 'ContactPointService',
function ($scope, GroupService, LinkService, LocationService, ContactPointService) {

    $scope.hasAdminEmail = function(group) {
        if (group !== undefined) {
            if (group.contactPoints) {
                for (var i = 0; i < group.contactPoints.length; i++) {
                    if (group.contactPoints[i].contactPointType.value === 'PV_ADMIN_EMAIL') {
                        return true;
                    }
                }
                return false;
            }
        }
    };

    $scope.addLink = function (form, group, link) {
        link.displayOrder = group.links.length +1;

        // only do POST if in edit mode, otherwise just add to object
        if ($scope.editMode) {
            GroupService.addLink(group, link).then(function (successResult) {
                // added link
                link.id = successResult.id;
                group.links.push(_.clone(link));
                delete link.id;
                delete link.link;
                delete link.name;
            }, function () {
                alert('Error saving link');
            });
        } else {
            link.id = Math.floor(Math.random() * (9999)) -10000;
            group.links.push(_.clone(link));
            delete link.id;
            delete link.link;
            delete link.name;
            form.$setDirty(true);
        }
    };

    $scope.updateLink = function (event, form, group, link) {
        link.saved = false;

        // try and save link
        LinkService.save(link).then(function () {
            link.saved = true;
        }, function() {
            alert('Error saving link');
        });
    };

    $scope.removeLink = function (form, group, link) {
        // only do DELETE if in edit mode, otherwise just remove from object
        if ($scope.editMode) {
            LinkService.remove(link).then(function () {
                // deleted link
                for (var j = 0; j < group.links.length; j++) {
                    if (group.links[j].id === link.id) {
                        group.links.splice(j, 1);
                    }
                }
            }, function () {
                alert('Error deleting link');
            });
        } else {
            for (var j = 0; j < group.links.length; j++) {
                if (group.links[j].id === link.id) {
                    group.links.splice(j, 1);
                }
            }
            form.$setDirty(true);
        }
    };
    
    $scope.addLocation = function (form, group, location) {
        // only do POST if in edit mode, otherwise just add to object
        if ($scope.editMode) {
            GroupService.addLocation(group, location).then(function (successResult) {
                // added location
                location.id = successResult.id;
                group.locations.push(_.clone(location));
                group.newLocation = {};
                group.newLocation.label = 'Additional Location';
            }, function () {
                alert('Error saving location');
            });
        } else {
            location.id = Math.floor(Math.random() * (9999)) -10000;
            group.locations.push(_.clone(location));
            group.newLocation = {};
            group.newLocation.label = 'Additional Location';
            form.$setDirty(true);
        }
    };

    $scope.updateLocation = function (event, form, group, location) {
        location.saved = false;

        // try and save location
        LocationService.save(location).then(function () {
            location.saved = true;
        }, function() {
            alert('Error saving location');
        });
    };

    $scope.removeLocation = function (form, group, location) {
        // only do DELETE if in edit mode, otherwise just remove from object
        if ($scope.editMode) {
            LocationService.remove(location).then(function () {
                // deleted location
                for (var j = 0; j < group.locations.length; j++) {
                    if (group.locations[j].id === location.id) {
                        group.locations.splice(j, 1);
                    }
                }
            }, function () {
                alert('Error deleting location');
            });
        } else {
            for (var j = 0; j < group.locations.length; j++) {
                if (group.locations[j].id === location.id) {
                    group.locations.splice(j, 1);
                }
            }
            form.$setDirty(true);
        }
    };

    $scope.addFeature = function (form, group, featureId) {
        // only do POST if in edit mode, otherwise just add to object
        if ($scope.editMode) {
            GroupService.addFeature(group, featureId).then(function () {
                // added feature
                for (var j = 0; j < group.availableFeatures.length; j++) {
                    if (group.availableFeatures[j].feature.id === featureId) {
                        group.groupFeatures.push(group.availableFeatures[j]);
                        group.availableFeatures.splice(j, 1);
                    }
                }

                // update accordion header with data from GET
                GroupService.get(group.id).then(function (successResult) {
                    for(var i=0;i<$scope.pagedItems.length;i++) {
                        if($scope.pagedItems[i].id === group.id) {
                            var headerDetails = $scope.pagedItems[i];
                            headerDetails.groupFeatures = successResult.groupFeatures;
                        }
                    }
                }, function () {
                    alert('Error updating header (saved successfully)');
                });
            }, function () {
                alert('Error saving feature');
            });
        } else {
            for (var j = 0; j < group.availableFeatures.length; j++) {
                if (group.availableFeatures[j].feature.id === featureId) {
                    group.groupFeatures.push(group.availableFeatures[j]);
                    group.availableFeatures.splice(j, 1);
                }
            }
            form.$setDirty(true);
        }
    };

    $scope.removeFeature = function (form, group, feature) {
        // only do DELETE if in edit mode, otherwise just remove from object
        if ($scope.editMode) {
            GroupService.deleteFeature(group, feature.feature).then(function () {
                // deleted feature
                for (var j = 0; j < group.groupFeatures.length; j++) {
                    if (group.groupFeatures[j].feature.id === feature.feature.id) {
                        group.availableFeatures.push(group.groupFeatures[j]);
                        group.groupFeatures.splice(j, 1);
                    }
                }

                // update accordion header with data from GET
                GroupService.get(group.id).then(function (successResult) {
                    for(var i=0;i<$scope.pagedItems.length;i++) {
                        if($scope.pagedItems[i].id === group.id) {
                            var headerDetails = $scope.pagedItems[i];
                            headerDetails.groupFeatures = successResult.groupFeatures;
                        }
                    }
                }, function () {
                    alert('Error updating header (saved successfully)');
                });
            }, function () {
                // failure
                alert('Error deleting feature');
            });
        } else {
            for (var j = 0; j < group.groupFeatures.length; j++) {
                if (group.groupFeatures[j].feature.id === feature.feature.id) {
                    group.availableFeatures.push(group.groupFeatures[j]);
                    group.groupFeatures.splice(j, 1);
                }
            }

            form.$setDirty(true);
        }
    };

    $scope.addParentGroup = function (form, group, parentGroupId) {
        // only do POST if in edit mode, otherwise just add to object
        if ($scope.editMode) {
            GroupService.addParentGroup(group, parentGroupId).then(function () {
                // added parentGroup
                for (var j = 0; j < group.availableParentGroups.length; j++) {
                    if (group.availableParentGroups[j].id === parentGroupId) {
                        group.parentGroups.push(group.availableParentGroups[j]);
                        group.availableParentGroups.splice(j, 1);
                    }
                }

                // update accordion header with data from GET
                GroupService.get(group.id).then(function (successResult) {
                    for(var i=0;i<$scope.pagedItems.length;i++) {
                        if($scope.pagedItems[i].id === successResult.id) {
                            var headerDetails = $scope.pagedItems[i];
                            headerDetails.parentGroups = successResult.parentGroups;
                        }
                    }
                }, function () {
                    alert('Error updating header (saved successfully)');
                });
            }, function () {
                alert('Error saving parentGroup');
            });
        } else {
            for (var j = 0; j < group.availableParentGroups.length; j++) {
                if (group.availableParentGroups[j].id === parentGroupId) {
                    group.parentGroups.push(group.availableParentGroups[j]);
                    group.availableParentGroups.splice(j, 1);
                }
            }
            form.$setDirty(true);
        }
    };

    $scope.removeParentGroup = function (form, group, parentGroup) {
        // only do DELETE if in edit mode, otherwise just remove from object
        if ($scope.editMode) {
            GroupService.deleteParentGroup(group, parentGroup).then(function () {
                // deleted parentGroup
                for (var j = 0; j < group.parentGroups.length; j++) {
                    if (group.parentGroups[j].id === parentGroup.id) {
                        group.availableParentGroups.push(group.parentGroups[j]);
                        group.parentGroups.splice(j, 1);
                    }
                }

                // update accordion header with data from GET
                GroupService.get(group.id).then(function (successResult) {
                    for(var i=0;i<$scope.pagedItems.length;i++) {
                        if($scope.pagedItems[i].id === successResult.id) {
                            var headerDetails = $scope.pagedItems[i];
                            headerDetails.parentGroups = successResult.parentGroups;
                        }
                    }
                }, function () {
                    alert('Error updating header (saved successfully)');
                });
            }, function () {
                alert('Error deleting parentGroup');
            });
        } else {
            for (var j = 0; j < group.parentGroups.length; j++) {
                if (group.parentGroups[j].id === parentGroup.id) {
                    group.availableParentGroups.push(group.parentGroups[j]);
                    group.parentGroups.splice(j, 1);
                }
            }

            form.$setDirty(true);
        }
    };

    $scope.addChildGroup = function (form, group, childGroupId) {
        // only do POST if in edit mode, otherwise just add to object
        if ($scope.editMode) {
            GroupService.addChildGroup(group, childGroupId).then(function () {
                // added childGroup
                for (var j = 0; j < group.availableChildGroups.length; j++) {
                    if (group.availableChildGroups[j].id === childGroupId) {
                        group.childGroups.push(group.availableChildGroups[j]);
                        group.availableChildGroups.splice(j, 1);
                    }
                }

                // update accordion header for child group with data from GET
                GroupService.get(childGroupId).then(function (successResult) {
                    for(var i=0;i<$scope.pagedItems.length;i++) {
                        if($scope.pagedItems[i].id === successResult.id) {
                            var headerDetails = $scope.pagedItems[i];
                            headerDetails.parentGroups = successResult.parentGroups;
                        }
                    }
                }, function () {
                    alert('Error updating header (saved successfully)');
                });
            }, function () {
                alert('Error saving childGroup');
            });
        } else {
            for (var j = 0; j < group.availableChildGroups.length; j++) {
                if (group.availableChildGroups[j].id === childGroupId) {
                    group.childGroups.push(group.availableChildGroups[j]);
                    group.availableChildGroups.splice(j, 1);
                }
            }
            form.$setDirty(true);
        }
    };

    $scope.removeChildGroup = function (form, group, childGroup) {
        // only do DELETE if in edit mode, otherwise just remove from object
        if ($scope.editMode) {
            GroupService.deleteChildGroup(group, childGroup).then(function () {
                // deleted childGroup
                for (var j = 0; j < group.childGroups.length; j++) {
                    if (group.childGroups[j].id === childGroup.id) {
                        group.availableChildGroups.push(group.childGroups[j]);
                        group.childGroups.splice(j, 1);
                    }
                }

                // update accordion header with data from GET
                GroupService.get(childGroup.id).then(function (successResult) {
                    for(var i=0;i<$scope.pagedItems.length;i++) {
                        if($scope.pagedItems[i].id === successResult.id) {
                            var headerDetails = $scope.pagedItems[i];
                            headerDetails.parentGroups = successResult.parentGroups;
                        }
                    }
                }, function () {
                    alert('Error updating header (saved successfully)');
                });
            }, function () {
                alert('Error deleting childGroup');
            });
        } else {
            for (var j = 0; j < group.childGroups.length; j++) {
                if (group.childGroups[j].id === childGroup.id) {
                    group.availableChildGroups.push(group.childGroups[j]);
                    group.childGroups.splice(j, 1);
                }
            }

            form.$setDirty(true);
        }
    };

    $scope.addContactPoint = function (form, group, contactPoint) {

        // only do POST if in edit mode, otherwise just add to object
        if ($scope.editMode) {

            contactPoint.contactPointType = _.findWhere($scope.contactPointTypes, {id: contactPoint.contactPointTypeId});

            GroupService.addContactPoint(group, contactPoint).then(function (successResult) {
                contactPoint.id = successResult.id;
                group.contactPoints.push(_.clone(contactPoint));
                delete contactPoint.id;
                delete contactPoint.contactPointType;
                delete contactPoint.content;
            }, function () {
                alert('Error saving contactPoint');
            });
        } else {
            contactPoint.id = (new Date).getTime() * -1;
            contactPoint.contactPointType = _.findWhere($scope.contactPointTypes, {id: contactPoint.contactPointTypeId});
            group.contactPoints.push(_.clone(contactPoint));
            delete contactPoint.id;
            delete contactPoint.contactPointType;
            delete contactPoint.content;
            form.$setDirty(true);
        }
    };

    $scope.updateContactPoint = function (event, form, group, contactPoint) {
        contactPoint.saved = false;
        contactPoint.contactPointType = _.findWhere($scope.contactPointTypes, {id: contactPoint.contactPointType.id});

        // try and save contactPoint
        ContactPointService.save(contactPoint).then(function () {
            contactPoint.saved = true;
        }, function() {
            alert('Error saving contactPoint');
        });
    };

    $scope.removeContactPoint = function (form, group, contactPoint) {

        // if deleting PV_ADMIN_EMAIL, check at least one remains
        var adminEmailCount = 0;
        if (group.contactPoints) {
            for (var i = 0; i < group.contactPoints.length; i++) {
                if (group.contactPoints[i].contactPointType.value === 'PV_ADMIN_EMAIL') {
                    adminEmailCount += 1;
                }
            }
        }

        if (contactPoint.contactPointType.value === 'PV_ADMIN_EMAIL' && adminEmailCount <= 1) {
            alert('Cannot delete only remaining PatientView Admin Email');
        } else {
            // only do DELETE if in edit mode, otherwise just remove from object
            if ($scope.editMode) {
                ContactPointService.remove(contactPoint).then(function () {
                    // deleted contactPoint
                    for (var j = 0; j < group.contactPoints.length; j++) {
                        if (group.contactPoints[j].id === contactPoint.id) {
                            group.contactPoints.splice(j, 1);
                        }
                    }
                }, function () {
                    alert('Error deleting contactPoint');
                });
            } else {
                for (var j = 0; j < group.contactPoints.length; j++) {
                    if (group.contactPoints[j].id === contactPoint.id) {
                        group.contactPoints.splice(j, 1);
                    }
                }
                form.$setDirty(true);
            }
        }
    };
}]);
