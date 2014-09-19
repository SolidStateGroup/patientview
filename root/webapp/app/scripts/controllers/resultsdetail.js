'use strict';

angular.module('patientviewApp').controller('ResultsDetailCtrl',['$scope', '$routeParams', '$location', 'ObservationHeadingService', 'ObservationService',
function ($scope, $routeParams, $location, ObservationHeadingService, ObservationService) {

    $scope.init = function() {
        var i;
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

        $scope.getObservationHeadings(code);
        $scope.getObservations(code);
    };

    $scope.getObservationHeadings = function(code) {
        ObservationHeadingService.getAll().then(function(observationHeadings) {
            $scope.observationHeadings = observationHeadings.content;
            $scope.observationHeading = $scope.findObservationHeadingByCode(code);
            $scope.selectedCode = $scope.observationHeading.code;
        }, function() {
            alert('Error retrieving results');
        });
    };

    $scope.initialiseChart = function() {
        var chart1 = {};
        //chart1.type = 'LineChart';
        chart1.type = 'AnnotationChart';

        chart1.data = [
            ['date', 'Result']
        ];

        var minValue = Number.MAX_VALUE;
        var maxValue = Number.MIN_VALUE;

        for (var i = 0; i < $scope.observations.length; i++) {

            var observation = $scope.observations[i];

            var row = [];
            row[0] = new Date(observation.applies);
            row[1] = observation.value;
            chart1.data.push(row);

            // get min/max values for y-axis
            if (observation.value > maxValue) {
                maxValue = observation.value;
            }

            if (observation.value < minValue) {
                minValue = observation.value;
            }
        }

        // get most recent statistics of user locked and inactive
        $scope.chartData = chart1.data;
        $scope.chartDataTable = new google.visualization.arrayToDataTable(chart1.data);
        chart1.data = $scope.chartDataTable;

        if ($scope.observationHeading.minGraph) {
            if (minValue > $scope.observationHeading.minGraph) {
                minValue = $scope.observationHeading.minGraph;
            }
        }

        if ($scope.observationHeading.maxGraph) {
            if (maxValue < $scope.observationHeading.maxGraph) {
                maxValue = $scope.observationHeading.maxGraph;
            }
        }

        chart1.options = {
            min: minValue,
            max: maxValue,
            displayZoomButtons: false
        };

        /*chart1.options = {
            'title': null,
            'isStacked': 'true',
            'fill': 20,
            'displayExactValues': true,
            'vAxis': {
                baseline: 0,
                viewWindow: {min: minValue, max: maxValue},
                title: null,
                'pointSize': 5,
                'gridlines': {
                    'count': 10,
                    'color': '#ffffff'
                }
            },
            'hAxis': {
                'title': null
            },
            'chartArea': {
                left: '7%',
                top: '7%',
                width: '90%',
                height: '85%'
            },
            'legend': {position: 'none'}
        };

        chart1.formatters = {};*/
        $scope.chart = chart1;
        $scope.chartLoading = false;
    };

    $scope.getObservations = function(code) {
        $scope.loading = true;
        $scope.chartLoading = true;
        ObservationService.getByCode($scope.loggedInUser.id, code).then(function(observations) {
            if (observations.length) {
                $scope.observations = observations;
                $scope.selectedObservation = observations[0];
                $scope.initialiseChart();
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

    $scope.findObservationHeadingByCode = function(code) {
        for (var i=0;i<$scope.observationHeadings.length;i++) {
            if ($scope.observationHeadings[i].code === code) {
                return $scope.observationHeadings[i];
            }
        }
    };

    $scope.changeObservationHeading = function(code) {
        $scope.observationHeading = $scope.findObservationHeadingByCode(code);
        $scope.selectedCode = $scope.observationHeading.code;
        $scope.getObservations(code);
    };

    $scope.observationClicked = function (observation) {
        $scope.selectedObservation = observation;

    };

    $scope.graphClicked = function () {
        var selection = $scope.chartWrapper.getChart().getSelection();
        var range = $scope.chartWrapper.getChart().getVisibleChartRange();
        var startIndex, startFound = false;

        for(var i=$scope.observations.length-1;i>0;i--) {
            if (!startFound) {
                if ($scope.observations[i].applies >= range.start.getTime()) {
                    startIndex = $scope.observations.length - i - 1;
                    startFound = true;
                }
            }
        }

        var index = $scope.observations.length - startIndex - 1 - selection[0].row;
        $scope.selectedObservation = $scope.observations[index];
    };

    $scope.readyHandler = function (chartWrapper) {

        if (!$scope.chartReady) {
            $scope.chartWrapper = chartWrapper;
            var start = new Date().getTime();
            start -= (31557600000 * 3);
            $scope.chartWrapper.getChart().setVisibleChartRange(new Date(start), new Date());
            $scope.chartReady = true;
        }
    };

    $scope.init();
}]);
