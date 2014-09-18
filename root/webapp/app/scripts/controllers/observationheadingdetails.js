'use strict';

angular.module('patientviewApp').controller('ObservationHeadingDetailsCtrl', ['$scope', 'ObservationHeadingService',
    function ($scope, ObservationHeadingService) {

    $scope.createSpecialtyOrder = function(observationHeadingGroup) {
        if ($scope.editMode) {
            ObservationHeadingService.addObservationHeadingGroup($scope.observationHeading.id, observationHeadingGroup.groupId,
                observationHeadingGroup.panel, observationHeadingGroup.panelOrder).then(function () {
                    ObservationHeadingService.get($scope.observationHeading.id).then(function (observationHeading) {
                        $scope.editObservationHeading.observationHeadingGroups = observationHeading.observationHeadingGroups;
                    });
                }, function () {
                    alert('Could not save specialty specific order');
                });
        } else {
            for (var i=0;i<$scope.groups.length;i++) {
                if ($scope.groups[i].id === observationHeadingGroup.groupId) {
                    observationHeadingGroup.group = _.clone($scope.groups[i]);
                }
            }
            observationHeadingGroup.id = Math.floor(Math.random() * (9999)) -10000;
            $scope.editObservationHeading.observationHeadingGroups.push(_.clone(observationHeadingGroup));
        }
    };

    $scope.updateSpecialtyOrder = function(observationHeadingGroup) {
        if ($scope.editMode) {
            var updateObservationHeadingGroup = {};
            updateObservationHeadingGroup.id = observationHeadingGroup.id;
            updateObservationHeadingGroup.groupId = observationHeadingGroup.group.id;
            updateObservationHeadingGroup.panel = observationHeadingGroup.panel;
            updateObservationHeadingGroup.panelOrder = observationHeadingGroup.panelOrder;
            updateObservationHeadingGroup.observationHeadingId = $scope.observationHeading.id;

            ObservationHeadingService.updateObservationHeadingGroup(updateObservationHeadingGroup).then(function () {
                observationHeadingGroup.saved = true;
            }, function () {
                alert('Could not save specialty specific order');
            });
        }
    };

    $scope.removeSpecialtyOrder = function(observationHeadingGroup) {
        if ($scope.editMode) {
            ObservationHeadingService.removeObservationHeadingGroup(observationHeadingGroup.id).then(function () {
                ObservationHeadingService.get($scope.observationHeading.id).then(function (observationHeading) {
                    $scope.editObservationHeading.observationHeadingGroups = observationHeading.observationHeadingGroups;
                });
            }, function () {
                alert('Could not save specialty specific order');
            });
        } else {
            for (var i=0;i<$scope.editObservationHeading.observationHeadingGroups.length;i++) {
                if ($scope.editObservationHeading.observationHeadingGroups[i].id === observationHeadingGroup.id) {
                    $scope.editObservationHeading.observationHeadingGroups.splice(i, 1);
                }
            }
        }
    };
}]);
