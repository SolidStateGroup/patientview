'use strict';

angular.module('patientviewApp').controller('ResultsCtrl', ['$scope', 'ObservationService',
function ($scope, ObservationService) {

    $scope.init = function() {

        var i;
        $scope.initFinished = false;
        $scope.loading = true;

        ObservationService.getSummary($scope.loggedInUser.id).then(function(summary) {

            if (summary.length) {
                $scope.groupIndex = 0;
                $scope.currentPage = 1;

                $scope.summary = summary;
                $scope.group = summary[$scope.groupIndex].group;
                $scope.panels = summary[$scope.groupIndex].panels;
                $scope.panel = $scope.panels[$scope.currentPage];

                // set up group switcher
                $scope.groups = [];
                for (i=0;i<summary.length;i++) {
                    $scope.groups.push(summary[i].group);
                }

                $scope.selectedGroup = summary[0].group.id;
                $scope.initFinished = true;
                $scope.loading = false;
            }

        }, function () {
            alert('Cannot get results summary');
        })
    };

    $scope.changePanel = function(panelId) {
        $scope.panel = $scope.panels[panelId];
        $scope.currentPage = panelId;
    };

    $scope.getResultIcon = function(result) {
        if (result.valueChange === 0) {
            return null;
        }

        if (result.valueChange === -1) {
            return 'icon-result-down';
        }
        return 'icon-result-up';
    };

    $scope.removeMinus = function(value) {
        value = Math.abs(value);
        return value;
    };

    $scope.init();
}]);
