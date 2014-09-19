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
        // now using standard google charts (not angular-google-chart)
        var chart = new google.visualization.AnnotationChart(document.querySelector('#chart_div'));
        var data = [
            ['date', 'Result']
        ];

        var minValue = Number.MAX_VALUE;
        var maxValue = Number.MIN_VALUE;

        for (var i = 0; i < $scope.observations.length; i++) {

            var observation = $scope.observations[i];

            var row = [];
            row[0] = new Date(observation.applies);
            row[1] = observation.value;
            data.push(row);

            // get min/max values for y-axis
            if (observation.value > maxValue) {
                maxValue = observation.value;
            }

            if (observation.value < minValue) {
                minValue = observation.value;
            }
        }

        data = new google.visualization.arrayToDataTable(data);

        var options = {
            min: minValue,
            max: maxValue,
            displayZoomButtons: false,
            annotationsWidth: '0'
        };

        chart.draw(data, options);

        google.visualization.events.addListener(chart, 'rangechange', function(e) {
            $scope.rangeChanged(e);
        });
        google.visualization.events.addListener(chart, 'select', function(e) {
            $scope.graphClicked();
        });

        $scope.chart = chart;
        $scope.setRangeInDays(1094.75);
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
        var selection = $scope.chart.getSelection();
        var range = $scope.chart.getVisibleChartRange();
        var startIndex, startFound = false;

        for(var i=$scope.tableObservations.length-1;i>0;i--) {
            if (!startFound) {
                if ($scope.tableObservations[i].applies >= range.start.getTime()) {
                    startIndex = $scope.tableObservations.length - i - 1;
                    startFound = true;
                }
            }
        }

        var index = $scope.tableObservations.length - startIndex - 1 - selection[0].row;
        $scope.selectedObservation = $scope.tableObservations[index];

        $timeout(function() {
            $scope.$apply();
        });
    };

    $scope.rangeChanged = function (range) {
        $scope.showHideObservationsInTable(range.start, range.end);
    };

    $scope.setRangeInDays = function (days) {
        $scope.range = days;
        var now = new Date();
        var start = new Date(now.getTime() - days * 86400000);
        $scope.chart.setVisibleChartRange(start, now);
        $scope.showHideObservationsInTable(start, now);
    };

    $scope.showHideObservationsInTable = function(start, end) {
        $scope.tableObservations = false;
        $scope.tableObservations = [];

        for (var i=0;i<$scope.observations.length;i++) {
            if (start.getTime() < $scope.observations[i].applies && end.getTime() > $scope.observations[i].applies) {
                $scope.tableObservations.push($scope.observations[i]);
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
