'use strict';

angular.module('patientviewApp').controller('ResultsDetailCtrl',['$scope', '$routeParams', '$location',
    'ObservationHeadingService', 'ObservationService', '$modal', '$timeout',
function ($scope, $routeParams, $location, ObservationHeadingService, ObservationService, $modal, $timeout) {

    $scope.init = function() {
        $scope.loading = true;

        // if query parameters not set redirect to results
        if ($routeParams.code === undefined) {
            $location.path('/results');
        }

        // handle single result type from query parameter, todo: multiple result types
        var code = $routeParams.code;

        if (code instanceof Array) {
            code = $scope.code[0];
        }

        $scope.selectedCode = code;
        $scope.getObservations(code);
        $scope.getAvailableObservationHeadings(code, $scope.loggedInUser.id);
    };

    $scope.getAvailableObservationHeadings = function(code, userId) {
        ObservationHeadingService.getAvailableObservationHeadings(userId).then(function(observationHeadings) {
            $scope.observationHeadings = observationHeadings;
            $scope.observationHeading = $scope.findObservationHeadingByCode(code);
            $scope.selectedCode = $scope.observationHeading.code;
        }, function() {
            alert('Error retrieving result types');
        });
    };

    $scope.initialiseChart = function() {
        // using highstocks
        $('.chart-content-panel').show();

        var data = [];

        var minValue = Number.MAX_VALUE;
        var maxValue = Number.MIN_VALUE;

        for (var i = $scope.observations.length -1; i >= 0; i--) {

            var observation = $scope.observations[i];

            var row = [];
            row[0] = observation.applies;
            row[1] = parseFloat(observation.value);
            data.push(row);

            // get min/max values for y-axis
            if (observation.value > maxValue) {
                maxValue = observation.value;
            }

            if (observation.value < minValue) {
                minValue = observation.value;
            }
        }

        $('#chart_div').highcharts('StockChart', {
            rangeSelector : {
                buttons: [{
                    type: 'month',
                    count: 1,
                    text: '1m'
                }, {
                    type: 'month',
                    count: 3,
                    text: '3m'
                }, {
                    type: 'year',
                    count: 1,
                    text: '1y'
                }, {
                    type: 'year',
                    count: 3,
                    text: '3y'
                }, {
                    type: 'all',
                    text: 'All'
                }],

                selected: 3
            },

            credits : {
                enabled: false
            },

            title : {
                text: ''
            },

            navigator: {
                enabled: true
            },

            series : [{
                name : $scope.selectedObservation.name,
                data : data,
                tooltip: {
                    valueDecimals: 2
                }
            }],

            chart: {
                events: {
                    zoomType: 'x',
                    redraw: function (event) {
                        var minDate = event.target.xAxis[0].min;
                        var maxDate = event.target.xAxis[0].max;
                        $scope.showHideObservationsInTable(minDate, maxDate);
                    }
                }
            }
        });
        $scope.setRangeInDays(9999);

        $scope.chartLoading = false;
    };

    $scope.getObservations = function(code) {
        $scope.loading = true;
        $scope.chartLoading = true;
        ObservationService.getByCode($scope.loggedInUser.id, code).then(function(observations) {
            if (observations.length) {
                $scope.observations = _.sortBy(observations, 'applies').reverse();
                $scope.selectedObservation = $scope.observations[0];

                // dont show or deal with chart if result comment type
                //if ($scope.selectedCode !== 'resultcomment') {
                    $scope.initialiseChart();
                //}
            } else {
                delete $scope.observations;
                delete $scope.selectedObservation;
            }
            $scope.loading = false;
        }, function() {
            alert('Error retrieving results');
            $scope.loading = false;
        });
    };

    $scope.getResultIcon = function(value) {
        if (value === undefined) {
            return null;
        }
        if (value === 0) {
            return null;
        }

        if (value < 0) {
            return 'icon-result-down';
        }
        return 'icon-result-up';
    };

    $scope.removeMinus = function(value) {
        if (value !== undefined) {
            value = Math.abs(value);

            // now round to at most 2 dp
            value = +(Math.round(value + "e+2")  + "e-2");

            return value;
        } else {
            return null;
        }
    };

    $scope.findObservationHeadingByCode = function(code) {
        for (var i=0;i<$scope.observationHeadings.length;i++) {
            if ($scope.observationHeadings[i].code === code) {
                return $scope.observationHeadings[i];
            }
        }
    };

    $scope.changeObservationHeading = function(code) {
        $('.chart-content-panel').hide();
        $scope.observationHeading = $scope.findObservationHeadingByCode(code);
        $scope.selectedCode = $scope.observationHeading.code;
        $scope.getObservations(code);
    };

    $scope.observationClicked = function (observation) {
        $scope.selectedObservation = observation;
    };

    $scope.getValueChanged = function(observation) {
        if (observation !== undefined && $scope.selectedCode !== 'resultcomment') {
            var index = $scope.tableObservationsKey[observation.applies];
            if ($scope.tableObservations[index + 1]) {
                return $scope.tableObservations[index].value - $scope.tableObservations[index + 1].value;
            }
        }
        return null;
    };

    $scope.setRangeInDays = function (days) {
        $scope.range = days;
        var now = new Date();
        now = new Date(now.getTime() + 86400000);
        var start = new Date(now.getTime() - days * 86400000);
        $scope.showHideObservationsInTable(start, now);
    };

    $scope.showHideObservationsInTable = function(start, end) {
        $scope.tableObservations = false;
        $scope.tableObservations = [];
        $scope.tableObservationsKey = [];

        for (var i=0;i<$scope.observations.length;i++) {
            if (start <= $scope.observations[i].applies && end >= $scope.observations[i].applies) {
                $scope.tableObservations.push($scope.observations[i]);
                $scope.tableObservationsKey[$scope.observations[i].applies] = $scope.tableObservations.length - 1;
            }
        }

        $timeout(function() {
            $scope.$apply();
        });
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
        }, function () {
            // closed
        });
    };

    $scope.init();
}]);
