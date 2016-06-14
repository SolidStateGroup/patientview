'use strict';

angular.module('patientviewApp').controller('ResultsTableCtrl', ['$scope', '$modal', '$filter', 'ObservationService', 'ObservationHeadingService',
function ($scope, $modal, $filter, ObservationService, ObservationHeadingService) {

    $scope.init = function() {
        var i;
        $scope.blankCode = 'blank';
        $scope.initFinished = false;
        $scope.itemsPerPage = 20;
        $scope.currentPage = 0;
        $scope.loading = true;
        $scope.observationHeadingCodes = [];
        $scope.observationHeadingMap = [];
        $scope.orderDirection = 'DESC';

        ObservationHeadingService.getAvailableObservationHeadings($scope.loggedInUser.id)
        .then(function(observationHeadings) {
            if (observationHeadings.length > 0) {
                var blankObservationHeading = {};
                blankObservationHeading.code = $scope.blankCode;
                blankObservationHeading.heading = ' Please Select..';
                blankObservationHeading.name = ' Please Select..';
                observationHeadings.push(blankObservationHeading);
                $scope.observationHeadings = observationHeadings;
                $scope.selectedCode = $scope.blankCode;
                for (i = 0; i < $scope.observationHeadings.length; i++) {
                    $scope.observationHeadingMap[$scope.observationHeadings[i].code] = $scope.observationHeadings[i];
                }
            }
            getSavedObservationHeadings();
            $scope.initFinished = true;
        }, function() {
            alert('Error retrieving result types');
        });

    };

    // update page when currentPage is changed
    $scope.$watch('currentPage', function(value) {
        if ($scope.initFinished === true) {
            $scope.currentPage = value;
            getObservations();
        }
    });

    $scope.changeOrderDirection = function() {
        if ($scope.orderDirection === 'ASC') {
            $scope.orderDirection = 'DESC';
        } else {
            $scope.orderDirection = 'ASC';
        }
        getObservations();
    };

    $scope.includeObservationHeading = function(code) {
        if ($.inArray(code, $scope.observationHeadingCodes) === -1
            && code !== $scope.blankCode) {
            $scope.observationHeadingCodes.push(code);
            getObservations();
            saveObservationHeadingSelection();
        }
        $scope.selectedCode = $scope.blankCode;
    };

    $scope.removeObservationHeading = function(code) {
        $scope.observationHeadingCodes.splice($scope.observationHeadingCodes.indexOf(code), 1);
        getObservations();
        saveObservationHeadingSelection();
        $scope.selectedCode = $scope.blankCode;
    };

    var saveObservationHeadingSelection = function() {
        ObservationHeadingService.saveObservationHeadingSelection($scope.loggedInUser.id, $scope.observationHeadingCodes)
        .then(function () {
        }, function () {
            alert('Could not save observation heading selection');
            $scope.loading = false;
        });
    };

    var getSavedObservationHeadings = function () {
        var i;
        $scope.loading = true;
        ObservationHeadingService.getSavedObservationHeadings($scope.loggedInUser.id)
            .then(function (observationHeadings) {
                for (i = 0; i < observationHeadings.length; i++) {
                    $scope.observationHeadingCodes.push(observationHeadings[i].code);
                }

                getObservations();
                $scope.loading = false;
            }, function () {
                alert('Error retrieving observation headings');
                $scope.loading = false;
            });
    };

    var getObservations = function () {
        $scope.loading = true;
        var i;
        var offset = $scope.currentPage * $scope.itemsPerPage;

        if ($scope.observationHeadingCodes.length) {
            ObservationService.getByCodes($scope.loggedInUser.id, $scope.observationHeadingCodes, $scope.itemsPerPage,
                offset, $scope.orderDirection).then(function (observationsPage) {
                var data = observationsPage.data;
                $scope.total = observationsPage.totalElements;
                $scope.totalPages = observationsPage.totalPages;

                var pagedItems = [];
                var count = 0;

                // convert from map to something suitable for display
                for (var key in data) {
                    var row = [];
                    var value = data[key];

                    row[0] = $filter('date')(key, 'dd-MMM-yyyy HH:mm').replace(' 00:00','');
                    for (i = 0; i < $scope.observationHeadingCodes.length; i++) {
                        var code = $scope.observationHeadingCodes[i].toUpperCase();
                        if (value[code] !== undefined) {
                            row[i + 1] = value[code];
                        } else {
                            row[i + 1] = null;
                        }
                    }
                    pagedItems[count] = row;
                    count = count + 1;
                }

                $scope.pagedItems = pagedItems;
                if ($scope.totalPages <= $scope.currentPage) {
                    $scope.currentPage = 0;
                }
                $scope.loading = false;
            }, function () {
                alert('Error retrieving results');
                $scope.loading = false;
            });
        } else {
            $scope.loading = false;
        }
    };

    $scope.openObservationHeadingInformation = function (code) {

        var modalInstance = $modal.open({
            templateUrl: 'views/partials/observationHeadingInfoModal.html',
            controller: ObservationHeadingInfoModalInstanceCtrl,
            size: 'sm',
            windowClass: 'results-modal',
            resolve: {
                result: function(){
                    return $scope.observationHeadingMap[code];
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
            templateUrl: 'views/partials/exportToCSVModal.html',
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
    
    $scope.getGroupsFromRow = function (row) {
        var groups = [];
        var out = [];
        for (var i=1; i<row.length; i++) {
            var resultList = row[i];
            if (resultList !== null) {
                for (var j = 0; j < resultList.length; j++) {
                    if (resultList[j].group.shortName !== null) {
                        groups[resultList[j].group.shortName] = resultList[j].group.shortName;
                    }
                }
            }
        }

        for (var key in groups) {
            if (groups.hasOwnProperty(key)) {
                out.push(key);
            }
        }

        return out;
    };

    $scope.init();
}]);
