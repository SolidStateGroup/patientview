'use strict';

angular.module('patientviewApp').controller('SymptomScoresCtrl',['$scope', '$routeParams', '$location',
    'SymptomScoreService', '$modal', '$timeout', '$filter',
function ($scope, $routeParams, $location, SymptomScoreService, $modal, $timeout, $filter) {

    $scope.init = function() {
        $scope.loading = true;
        $scope.getSymptomScores();
    };

    $scope.initialiseChart = function() {
        Highcharts.setOptions({
            global: {
                useUTC: false
            }
        });
        // using highstocks
        $('.chart-content-panel').show();

        var data = [];

        var minValue = Number.MAX_VALUE;
        var maxValue = Number.MIN_VALUE;

        for (var i = $scope.symptomScores.length -1; i >= 0; i--) {

            var symptomScore = $scope.symptomScores[i];

            var row = [];
            row[0] = symptomScore.dateTaken;
            row[1] = parseFloat(symptomScore.score);

            // don't display textual results on graph
            if (!isNaN(row[1])) {
                data.push(row);

                // get min/max values for y-axis
                if (symptomScore.score > maxValue) {
                    maxValue = symptomScore.score;
                }

                if (symptomScore.score < minValue) {
                    minValue = symptomScore.score;
                }
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
                name : null,
                data : data,
                tooltip: {
                    valueDecimals: 1
                }
            }],

            chart: {
                events: {
                    zoomType: 'x',
                    redraw: function (event) {
                        var minDate = event.target.xAxis[0].min;
                        var maxDate = event.target.xAxis[0].max;
                        $scope.showHideInTable(minDate, maxDate);
                    }
                }
            },
            xAxis: {
                minTickInterval: 864000000,
                type: 'datetime',
                dateTimeLabelFormats: {
                    millisecond: '%H:%M:%S.%L<br/>%Y',
                    second: '%H:%M:%S',
                    minute: '%H:%M',
                    hour: '%H:%M',
                    day: '%e. %b. %Y',
                    week: '%e. %b. %Y',
                    month: '%e. %b. %Y',
                    year: '%e. %b. %Y'
                },
                text: 'ESEMPIO'
            },
            tooltip: {
                minTickInterval: 864000000,
                type: 'datetime',
                dateTimeLabelFormats: {
                    millisecond: '%H:%M:%S.%L<br/>%Y',
                    second: '%H:%M:%S',
                    minute: '%H:%M',
                    hour: '%H:%M',
                    day: '%e. %b. %Y',
                    week: '%e. %b. %Y',
                    month: '%e. %b. %Y',
                    year: '%e. %b. %Y'
                },
                text: 'ESEMPIO'
            }
        });
        $scope.setRangeInDays(9999);

        $scope.chartLoading = false;
    };

    $scope.getSymptomScores = function() {
        $scope.loading = true;
        $scope.chartLoading = true;
        SymptomScoreService.getByUser($scope.loggedInUser.id).then(function(symptomScores) {
            if (symptomScores.length) {
                $scope.symptomScores = _.sortBy(symptomScores, 'dateTaken').reverse();
                $scope.initialiseChart();
            } else {
                delete $scope.symptomScores;
            }
            $scope.loading = false;
        }, function() {
            alert('Error retrieving symptom scores');
            $scope.loading = false;
        });
    };

    $scope.setRangeInDays = function (days) {
        $scope.range = days;
        var now = new Date();
        now = new Date(now.getTime() + 86400000);
        var start = new Date(now.getTime() - days * 86400000);
        $scope.showHideObservationsInTable(start, now);
    };

    $scope.showHideInTable = function(start, end) {
        $scope.tableSymptomScores = [];
        $scope.tableSymptomScoresKey = [];

        for (var i=0;i<$scope.symptomScores.length;i++) {
            var symptomScore = $scope.symptomScores[i];
            if (start <= symptomScore.dateTaken && end >= symptomScore.dateTaken) {
                symptomScore.dateTakenFormatted = $filter('date')(symptomScore.dateTaken, 'dd-MMM-yyyy HH:mm');
                symptomScore.dateTakenFormatted = symptomScore.dateTakenFormatted.replace(' 00:00', '');
                $scope.tableSymptomScores.push(symptomScore);
                $scope.tableSymptomScoresKey[symptomScore.dateTaken] = $scope.tableSymptomScores.length - 1;
            }
        }

        $timeout(function() {
            $scope.$apply();
        });
    };

    $scope.init();
}]);
