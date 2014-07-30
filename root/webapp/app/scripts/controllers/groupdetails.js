'use strict';

angular.module('patientviewApp').controller('GroupDetailsCtrl', ['$scope', 'GroupService', 'LinkService',
function ($scope, GroupService, LinkService) {

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
        location.id = Math.floor(Math.random() * (9999)) -10000;
        group.locations.push(_.clone(location));
        location.label = location.name = location.phone = location.address = location.web = location.email = '';
        form.$setDirty(true);
    };

    $scope.removeLocation = function (form, group, location) {
        for (var j = 0; j < group.locations.length; j++) {
            if (group.locations[j].id === location.id) {
                group.locations.splice(j, 1);
            }
        }
        form.$setDirty(true);
    };

    // add feature to current features, remove from allowed
    $scope.addFeature = function (form, group, featureId) {
        for (var j = 0; j < group.availableFeatures.length; j++) {
            if (group.availableFeatures[j].feature.id === featureId) {
                group.groupFeatures.push(group.availableFeatures[j]);
                group.availableFeatures.splice(j, 1);
            }
        }
        form.$setDirty(true);
    };

    // remove feature from current features, add to allowed features
    $scope.removeFeature = function (form, group, feature) {
        for (var j = 0; j < group.groupFeatures.length; j++) {
            if (group.groupFeatures[j].feature.id === feature.feature.id) {
                group.availableFeatures.push(group.groupFeatures[j]);
                group.groupFeatures.splice(j, 1);
            }
        }
        form.$setDirty(true);
    };

    // add parent group to group, remove from available
    $scope.addParentGroup = function (form, group, parentGroupId) {

        //var currentGroup = {};

        // set parent group for current group
        for (var j = 0; j < group.availableParentGroups.length; j++) {
            if (group.availableParentGroups[j].id === parentGroupId) {
                //currentGroup = group.availableParentGroups[j];
                group.parentGroups.push(group.availableParentGroups[j]);
                group.availableParentGroups.splice(j, 1);
            }
        }

        // add corresponding child group (this group) for the new parent
        /*for (var k = 0; k < $scope.allGroups; k++) {
            var group = $scope.allGroups[k];
            if (group.id === parentGroupId) {
                group.childGroups.push(currentGroup);
            }
        }*/

        form.$setDirty(true);
    };

    // remove parentGroup from current parentGroups, add to allowed parentGroups
    $scope.removeParentGroup = function (form, group, parentGroup) {
        for (var j = 0; j < group.parentGroups.length; j++) {
            if (group.parentGroups[j].id === parentGroup.id) {
                group.availableParentGroups.push(group.parentGroups[j]);
                group.parentGroups.splice(j, 1);
            }
        }
        form.$setDirty(true);
    };

    // add child group to group, remove from available
    $scope.addChildGroup = function (form, group, childGroupId) {

        // set child group for current group
        for (var j = 0; j < group.availableChildGroups.length; j++) {
            if (group.availableChildGroups[j].id === childGroupId) {
                //currentGroup = group.availableChildGroups[j];
                group.childGroups.push(group.availableChildGroups[j]);
                group.availableChildGroups.splice(j, 1);
            }
        }

        form.$setDirty(true);
    };

    // remove childGroup from current childGroups, add to allowed childGroups
    $scope.removeChildGroup = function (form, group, childGroup) {
        for (var j = 0; j < group.childGroups.length; j++) {
            if (group.childGroups[j].id === childGroup.id) {
                group.availableChildGroups.push(group.childGroups[j]);
                group.childGroups.splice(j, 1);
            }
        }
        form.$setDirty(true);
    };

    $scope.addContactPoint = function (form, group, contactPoint) {
        contactPoint.id = (new Date).getTime() * -1;
        contactPoint.contactPointType = _.findWhere($scope.contactPointTypes, {id: contactPoint.contactPointTypeId});
        group.contactPoints.push(_.clone(contactPoint));
        contactPoint.content = '';
        form.$setDirty(true);
    };

    $scope.removeContactPoint = function (form, group, contactPoint) {
        for (var j = 0; j < group.contactPoints.length; j++) {
            if (group.contactPoints[j].id === contactPoint.id) {
                group.contactPoints.splice(j, 1);
            }
        }
        form.$setDirty(true);
    };
    
}]);
