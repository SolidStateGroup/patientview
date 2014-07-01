'use strict';

angular.module('patientviewApp').controller('GroupDetailsCtrl', ['$scope', function ($scope) {
    $scope.addLink = function (form, group, link) {
        link.id = Math.floor(Math.random() * (9999)) -10000;
        link.displayOrder = group.links.length +1;
        group.links.push(_.clone(link));
        link.link = link.name = '';
        form.$setDirty(true);
    };

    $scope.removeLink = function (form, group, link) {
        for (var j = 0; j < group.links.length; j++) {
            if (group.links[j].id === link.id) {
                group.links.splice(j, 1);
            }
        }
        form.$setDirty(true);
    };

    // add feature to current feature, remove from allowed
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
}]);
