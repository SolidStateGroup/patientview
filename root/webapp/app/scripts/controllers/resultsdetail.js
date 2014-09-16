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
        chart1.type = 'LineChart';
        chart1.data = [
            ['date', 'Result']
        ];

        for (var i = 0; i < $scope.observations.length; i++) {
            var row = [];
            row[0] = new Date($scope.observations[i].applies);
            row[1] = $scope.observations[i].value;
            chart1.data.push(row);
        }

        // get most recent statistics of user locked and inactive
        chart1.data = new google.visualization.arrayToDataTable(chart1.data);

        chart1.options = {
            'title': null,
            'isStacked': 'true',
            'fill': 20,
            'displayExactValues': true,
            'vAxis': {
                'title': null,
                'pointSize': 5,
                'gridlines': {
                    'count': 10,
                    'color': '#ffffff'
                },
                'viewWindow': {
                    'min': 0
                }
            },
            'hAxis': {
                'title': null
            },
            'chartArea': {
                left: '7%',
                top: '7%',
                width: '80%',
                height: '85%'
            }
        };

        chart1.formatters = {};
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

    $scope.init();
}]);
