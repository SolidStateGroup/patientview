'use strict';

// observation heading information modal instance controller
var ObservationHeadingInfoModalInstanceCtrl = ['$scope','$modalInstance','result',
    function ($scope, $modalInstance, result) {
        $scope.result = result;
        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }];

angular.module('patientviewApp').controller('ResultsTableCtrl', ['$scope', '$modal', 'ObservationService', 'ObservationHeadingService',
function ($scope, $modal, ObservationService, ObservationHeadingService) {

    $scope.init = function() {
        var i;
        $scope.itemsPerPage = 20;
        $scope.currentPage = 0;
        $scope.loading = true;
        $scope.observationHeadingCodes = [];

        ObservationHeadingService.getAvailableObservationHeadings($scope.loggedInUser.id)
            .then(function(observationHeadings) {
                if (observationHeadings.length > 0) {
                    $scope.observationHeadings = observationHeadings;
                    $scope.selectedCode = $scope.observationHeadings[0].code;
                }
                $scope.loading = false;
        }, function() {
            alert('Error retrieving result types');
        });

    };

    $scope.includeObservationHeading = function(code) {
        if ($.inArray(code, $scope.observationHeadingCodes) === -1) {
            $scope.observationHeadingCodes.push(code);
        }

        getObservations();
    };

    var getObservations = function () {
        $scope.loading = true;

        var offset = $scope.currentPage * $scope.itemsPerPage;

        ObservationService.getByCodes($scope.loggedInUser.id, $scope.observationHeadingCodes, $scope.itemsPerPage, offset)
            .then(function(observationsPage) {
            var data = observationsPage.data;
            $scope.total = observationsPage.totalElements;
            $scope.totalPages = observationsPage.totalPages;

            // convert from map to something suitable for display

            $scope.loading = false;
        }, function() {
            alert('Error retrieving results');
            $scope.loading = false;
        });
    };

    $scope.openObservationHeadingInformation = function (result) {

        var modalInstance = $modal.open({
            templateUrl: 'views/partials/observationHeadingInfoModal.html',
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

    // pagination
    $scope.pageCount = function() {
        return Math.ceil($scope.total/$scope.itemsPerPage);
    };
    $scope.range = function() {
        var rangeSize = 10;
        var ret = [];
        var start;

        if ($scope.currentPage < 10) {
            start = 0;
        } else {
            start = $scope.currentPage;
        }

        if ( start > $scope.pageCount()-rangeSize ) {
            start = $scope.pageCount()-rangeSize;
        }

        for (var i=start; i<start+rangeSize; i++) {
            if (i > -1) {
                ret.push(i);
            }
        }
        return ret;
    };
    $scope.setPage = function(n) {
        if (n > -1 && n < $scope.totalPages) {
            $scope.currentPage = n;
        }
    };
    $scope.prevPage = function() {
        if ($scope.currentPage > 0) {
            $scope.currentPage--;
        }
    };
    $scope.prevPageDisabled = function() {
        return $scope.currentPage === 0 ? 'hidden' : '';
    };
    $scope.nextPage = function() {
        if ($scope.currentPage < $scope.totalPages - 1) {
            $scope.currentPage++;
        }
    };
    $scope.nextPageDisabled = function() {
        return $scope.currentPage === $scope.pageCount() - 1 ? 'disabled' : '';
    };

    $scope.init();
}]);
