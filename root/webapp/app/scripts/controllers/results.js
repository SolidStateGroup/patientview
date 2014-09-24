'use strict';

// observation heading information modal instance controller
var ObservationHeadingInfoModalInstanceCtrl = ['$scope','$modalInstance','result',
    function ($scope, $modalInstance, result) {
        $scope.result = result;
        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }];

angular.module('patientviewApp').controller('ResultsCtrl', ['$scope', '$modal', 'ObservationService',
function ($scope, $modal, ObservationService) {

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

    $scope.changeGroup = function(groupId) {
        for (var i=0;i<$scope.summary.length;i++) {
            if (groupId === $scope.summary[i].group.id) {
                $scope.groupIndex = i;
                $scope.currentPage = 1;
                $scope.group = $scope.summary[$scope.groupIndex].group;
                $scope.panels = $scope.summary[$scope.groupIndex].panels;
                $scope.panel = $scope.panels[$scope.currentPage];
            }
        }
    };

    $scope.changePanel = function(panelId) {
        $scope.panel = $scope.panels[panelId];
        $scope.currentPage = panelId;
    };

    $scope.getResultIcon = function(result) {
        if (result.valueChange === 0) {
            return null;
        }

        if (result.valueChange < 0) {
            return 'icon-result-down';
        }
        return 'icon-result-up';
    };

    $scope.removeMinus = function(value) {
        value = Math.abs(value);

        // now round to at most 2 dp
        value = +(Math.round(value + "e+2")  + "e-2");

        return value;
    };

    $scope.openObservationHeadingInformation = function (result) {

        var modalInstance = $modal.open({
            templateUrl: 'views/partials/observationHeadingInfoModal.html',
            controller: ObservationHeadingInfoModalInstanceCtrl,
            size: 'sm',
            resolve: {
                result: function(){
                    return result;
                }
            }
        });

        modalInstance.result.then(function () {
            // ok (not used)
        }, function () {
            // closed
        });
    };

    $scope.getPanelResultTitles = function(panel) {
        var text = "", i;
        for (i=0;i<panel.length;i++) {
            text += panel[i].heading;
            if (i !== panel.length-1) {
                text+= ", ";
            }
        }

        return text;
    };

    $scope.init();
}]);
