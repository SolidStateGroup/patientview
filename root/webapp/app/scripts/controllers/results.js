'use strict';

angular.module('patientviewApp').controller('ResultsCtrl', ['$scope', '$modal', 'ObservationService', '$routeParams',
function ($scope, $modal, ObservationService, $routeParams) {

    $scope.init = function() {

        var i, j, k, panel, resultHeading;
        $scope.initFinished = false;
        $scope.loading = true;
        ObservationService.getSummary($scope.loggedInUser.id).then(function(summary) {

            if (summary.length) {
                // set property on results with most recent date
                var latestDate = 0;
                var summaryLatest = [];

                // find latest date
                for (i = 0; i < summary.length; i++) {
                    for (panel in summary[i].panels) {
                        if (summary[i].panels.hasOwnProperty(panel)) {
                            for (j = 0; j < panel.length; j++) {
                                for (k = 0; k < summary[i].panels[panel[j]].length; k++) {
                                    resultHeading = summary[i].panels[panel[j]][k];
                                    if (resultHeading.latestObservation != null
                                        && resultHeading.latestObservation != undefined
                                        && resultHeading.latestObservation.applies > latestDate) {
                                        latestDate = resultHeading.latestObservation.applies;
                                    }
                                }
                            }
                        }
                    }
                }

                var latestDateStr = moment(latestDate).format("DDMMYYYY");

                // set latest
                for (i = 0; i < summary.length; i++) {
                    summaryLatest[i] = [];
                    for (panel in summary[i].panels) {
                        if (summary[i].panels.hasOwnProperty(panel)) {
                            for (j = 0; j < panel.length; j++) {
                                for (k = 0; k < summary[i].panels[panel[j]].length; k++) {
                                    resultHeading = summary[i].panels[panel[j]][k];
                                    if (resultHeading.latestObservation != null
                                        && resultHeading.latestObservation != undefined
                                        && moment(resultHeading.latestObservation.applies).format("DDMMYYYY") == latestDateStr) {
                                        summaryLatest[i].push(resultHeading);
                                        resultHeading.isLatest = true;
                                    }
                                }
                            }
                        }
                    }

                    summary[i].panels[-1] = summaryLatest[i];
                }

                $scope.groupIndex = 0;
                $scope.summary = summary;
                $scope.group = summary[$scope.groupIndex].group;
                $scope.panels = summary[$scope.groupIndex].panels;

                // validate that r param points to a correct page
                if ($routeParams.r !== undefined) {
                    $scope.currentPage = $scope.panels[$routeParams.r] !== undefined ? $routeParams.r : 1;
                } else {
                    if (summary[$scope.groupIndex].panels[-1].length) {
                        $scope.currentPage = -1;
                    } else {
                        $scope.currentPage = 1;
                    }
                }

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
        });
    };

    $scope.changeGroup = function(groupId) {
        for (var i=0;i<$scope.summary.length;i++) {
            if (groupId === $scope.summary[i].group.id) {
                $scope.groupIndex = i;
                if ($scope.summary[$scope.groupIndex].panels[-1].length) {
                    $scope.currentPage = -1;
                } else {
                    $scope.currentPage = 1;
                }
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
        value = +(Math.round(value + 'e+2')  + 'e-2');

        return value;
    };

    $scope.openObservationHeadingInformation = function (result) {

        var modalInstance = $modal.open({
            templateUrl: 'views/modal/observationHeadingInfoModal.html',
            controller: ObservationHeadingInfoModalInstanceCtrl,
            size: 'sm',
            windowClass: 'results-modal',
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
    $scope.openExportToCSVModal = function () {

        var modalInstance = $modal.open({
            templateUrl: 'views/modal/exportToCSVModal.html',
            controller: "ExportInfoModalInstanceCtrl",
            size: 'sm',
            windowClass: 'results-modal',
            resolve: {
                result: function(){
                    return true;
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
        var text = '', i;
        var sortedResultSummaries = _.sortBy(panel, 'panelOrder');

        for (i=0;i<sortedResultSummaries.length;i++) {
            text += sortedResultSummaries[i].heading;
            if (i !== sortedResultSummaries.length-1) {
                text+= ', ';
            }
        }

        return text;
    };

    $scope.init();
}]);
