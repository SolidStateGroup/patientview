'use strict';

angular.module('patientviewApp').controller('GroupDetailsCtrl', ['$scope', 'GroupService', 'LinkService', 'LocationService', 'ContactPointService',
function ($scope, GroupService, LinkService, LocationService, ContactPointService) {

    $scope.addLink = function (form, group, link) {
        link.displayOrder = group.links.length +1;

        // only do POST if in edit mode, otherwise just add to object
        if ($scope.editMode) {
            GroupService.addLink(group, link).then(function (successResult) {
                // added link
                link.id = successResult.id;
                group.links.push(_.clone(link));
                link.link = link.name = '';
                form.$setDirty(true);
            }, function () {
                // failure
                alert('Error saving link');
            });
        } else {
            link.id = Math.floor(Math.random() * (9999)) -10000;
            group.links.push(_.clone(link));
            link.link = link.name = '';
            form.$setDirty(true);
        }
    };

    $scope.updateLink = function (event, form, group, link) {
        link.saved = false;

        // try and save link
        LinkService.save(link).then(function () {
            // saved link
            link.saved = true;
            form.$setDirty(true);
        }, function() {
            // failure
            alert('Error saving link');
        });
    };

    $scope.removeLink = function (form, group, link) {
        // only do DELETE if in edit mode, otherwise just remove from object
        if ($scope.editMode) {
            LinkService.delete(link).then(function () {
                // deleted link
                for (var j = 0; j < group.links.length; j++) {
                    if (group.links[j].id === link.id) {
                        group.links.splice(j, 1);
                    }
                }
                form.$setDirty(true);
            }, function () {
                // failure
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
                location.label = location.name = location.phone = location.address = location.web = location.email = '';
                form.$setDirty(true);
            }, function () {
                // failure
                alert('Error saving location');
            });
        } else {
            location.id = Math.floor(Math.random() * (9999)) -10000;
            group.locations.push(_.clone(location));
            location.label = location.name = location.phone = location.address = location.web = location.email = '';
            form.$setDirty(true);
        }
    };

    $scope.updateLocation = function (event, form, group, location) {
        location.saved = false;

        // try and save location
        LocationService.save(location).then(function () {
            // saved location
            location.saved = true;
            form.$setDirty(true);
        }, function() {
            // failure
            alert('Error saving location');
        });
    };

    $scope.removeLocation = function (form, group, location) {
        // only do DELETE if in edit mode, otherwise just remove from object
        if ($scope.editMode) {
            LocationService.delete(location).then(function () {
                // deleted location
                for (var j = 0; j < group.locations.length; j++) {
                    if (group.locations[j].id === location.id) {
                        group.locations.splice(j, 1);
                    }
                }
                form.$setDirty(true);
            }, function () {
                // failure
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
            GroupService.addFeature(group, featureId).then(function (successResult) {
                // added feature
                for (var j = 0; j < group.availableFeatures.length; j++) {
                    if (group.availableFeatures[j].feature.id === featureId) {
                        group.groupFeatures.push(group.availableFeatures[j]);
                        group.availableFeatures.splice(j, 1);
                    }
                }

                // update accordion header with data from GET
                GroupService.get(group.id).then(function (successResult) {
                    for(var i=0;i<$scope.list.length;i++) {
                        if($scope.list[i].id == group.id) {
                            var headerDetails = $scope.list[i];
                            headerDetails.groupFeatures = successResult.groupFeatures;
                        }
                    }
                }, function () {
                    // failure
                    alert('Error updating header (saved successfully)');
                });

                form.$setDirty(true);
            }, function () {
                // failure
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
                    for(var i=0;i<$scope.list.length;i++) {
                        if($scope.list[i].id == group.id) {
                            var headerDetails = $scope.list[i];
                            headerDetails.groupFeatures = successResult.groupFeatures;
                        }
                    }
                }, function () {
                    // failure
                    alert('Error updating header (saved successfully)');
                });

                form.$setDirty(true);
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
            GroupService.addParentGroup(group, parentGroupId).then(function (successResult) {
                // added parentGroup
                for (var j = 0; j < group.availableParentGroups.length; j++) {
                    if (group.availableParentGroups[j].id === parentGroupId) {
                        group.parentGroups.push(group.availableParentGroups[j]);
                        group.availableParentGroups.splice(j, 1);
                    }
                }

                // update accordion header with data from GET
                GroupService.get(group.id).then(function (successResult) {
                    for(var i=0;i<$scope.list.length;i++) {
                        if($scope.list[i].id == successResult.id) {
                            var headerDetails = $scope.list[i];
                            headerDetails.parentGroups = successResult.parentGroups;
                        }
                    }
                }, function () {
                    // failure
                    alert('Error updating header (saved successfully)');
                });

                form.$setDirty(true);
            }, function () {
                // failure
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
                    for(var i=0;i<$scope.list.length;i++) {
                        if($scope.list[i].id == successResult.id) {
                            var headerDetails = $scope.list[i];
                            headerDetails.parentGroups = successResult.parentGroups;
                        }
                    }
                }, function () {
                    // failure
                    alert('Error updating header (saved successfully)');
                });

                form.$setDirty(true);
            }, function () {
                // failure
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
            GroupService.addChildGroup(group, childGroupId).then(function (successResult) {
                // added childGroup
                for (var j = 0; j < group.availableChildGroups.length; j++) {
                    if (group.availableChildGroups[j].id === childGroupId) {
                        group.childGroups.push(group.availableChildGroups[j]);
                        group.availableChildGroups.splice(j, 1);
                    }
                }

                // update accordion header for child group with data from GET
                GroupService.get(childGroupId).then(function (successResult) {
                    for(var i=0;i<$scope.list.length;i++) {
                        if($scope.list[i].id == successResult.id) {
                            var headerDetails = $scope.list[i];
                            headerDetails.parentGroups = successResult.parentGroups;
                        }
                    }
                }, function () {
                    // failure
                    alert('Error updating header (saved successfully)');
                });

                form.$setDirty(true);
            }, function () {
                // failure
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
                    for(var i=0;i<$scope.list.length;i++) {
                        if($scope.list[i].id == successResult.id) {
                            var headerDetails = $scope.list[i];
                            headerDetails.parentGroups = successResult.parentGroups;
                        }
                    }
                }, function () {
                    // failure
                    alert('Error updating header (saved successfully)');
                });

                form.$setDirty(true);
            }, function () {
                // failure
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
                // added contactPoint
                contactPoint.id = successResult.id;
                group.contactPoints.push(_.clone(contactPoint));
                contactPoint.content = '';
                form.$setDirty(true);
            }, function () {
                // failure
                alert('Error saving contactPoint');
            });
        } else {
            contactPoint.id = (new Date).getTime() * -1;
            contactPoint.contactPointType = _.findWhere($scope.contactPointTypes, {id: contactPoint.contactPointTypeId});
            group.contactPoints.push(_.clone(contactPoint));
            contactPoint.content = '';
            form.$setDirty(true);
        }
    };

    $scope.updateContactPoint = function (event, form, group, contactPoint) {
        contactPoint.saved = false;
        contactPoint.contactPointType = _.findWhere($scope.contactPointTypes, {id: contactPoint.contactPointType.id});

        // try and save contactPoint
        ContactPointService.save(contactPoint).then(function () {
            // saved contactPoint
            contactPoint.saved = true;
            form.$setDirty(true);
        }, function() {
            // failure
            alert('Error saving contactPoint');
        });
    };

    $scope.removeContactPoint = function (form, group, contactPoint) {
        // only do DELETE if in edit mode, otherwise just remove from object
        if ($scope.editMode) {
            ContactPointService.delete(contactPoint).then(function () {
                // deleted contactPoint
                for (var j = 0; j < group.contactPoints.length; j++) {
                    if (group.contactPoints[j].id === contactPoint.id) {
                        group.contactPoints.splice(j, 1);
                    }
                }
                form.$setDirty(true);
            }, function () {
                // failure
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
    };
}]);
