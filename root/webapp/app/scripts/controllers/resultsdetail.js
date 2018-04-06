'use strict';

angular.module('patientviewApp').controller('ResultsDetailCtrl', ['$scope', '$routeParams', '$location',
    'ObservationHeadingService', 'ObservationService', '$modal', '$timeout', '$filter', '$q',
    function ($scope, $routeParams, $location, ObservationHeadingService, ObservationService,
              $modal, $timeout, $filter, $q) {

        $scope.init = function () {
            $scope.loading = true;

            // return parameter (currentPage on results page)
            $scope.r = $routeParams.r;

            // if query parameters not set redirect to results
            if ($routeParams.code === undefined) {
                $location.path('/results');
            }

            $scope.codes = [];
            $scope.observations = [];

            $scope.xAxisRange = 0;
            $scope.vscale = 0;
            $scope.lastvscale = 0;
            // handle single result type from query parameter
            var code = $routeParams.code;

            if (code instanceof Array) {
                $scope.codes = code;
            } else {
                $scope.codes.push(code);
            }

            $scope.getAvailableObservationHeadings($scope.codes[0], $scope.loggedInUser.id);
        };

        $scope.compareResults = function (codeToCompare) {
            // first code in list is most important, don't remove
            var codes = [];
            codes.push($scope.codes[0]);
            codes.push(codeToCompare);
            $scope.codes = codes;

            $scope.loading = true;
            $scope.chartLoading = true;
            $scope.getObservations();
        };

        $scope.getAvailableObservationHeadings = function (code, userId) {
            ObservationHeadingService.getAvailableObservationHeadings(userId).then(function (observationHeadings) {
                $scope.observationHeadings = observationHeadings;
                $scope.observationHeading = $scope.findObservationHeadingByCode(code);
                $scope.selectedCode = code;

                $scope.getObservations();
            }, function () {
                alert('Error retrieving result types');
            });
        };

        $scope.hasBloodPressure = function () {
            var hasBpsys = false;
            var hasBpdia = false;
            if ($scope.observationHeadings) {
                for (var i = 0; i < $scope.observationHeadings.length; i++) {
                    if ($scope.observationHeadings[i].code === 'bpdia') {
                        hasBpdia = true;
                    }
                    if ($scope.observationHeadings[i].code === 'bpsys') {
                        hasBpsys = true;
                    }
                }
            }

            return hasBpdia && hasBpsys;
        };

        $scope.initialiseChart = function () {
            Highcharts.setOptions({
                global: {
                    useUTC: false
                }
            });
            // using highstocks
            $('.chart-content-panel').show();

            var i;
            var data = [];
            var maxValue = [];
            var minValue = [];
            var firstObservations = [];
            var series = [];
            var yAxis = [];
            var yAxisData = {
                scrollbar: {
                    enabled: true,
                    showFull: false
                }
            };
            var legend = {};

            // special options for blood pressure
            var sameScale = (_.contains($scope.codes, "bpsys") && _.contains($scope.codes, "bpdia"));

            $scope.codes.forEach(function (code, index) {
                if ($scope.observations[code] !== undefined) {
                    var observeHeading = $scope.findObservationHeadingByCode(code);

                    minValue[code] = observeHeading.minGraph
                        ? parseFloat(observeHeading.minGraph) : Number.MAX_VALUE;
                    maxValue[code] = observeHeading.maxGraph
                        ? parseFloat(observeHeading.maxGraph) : Number.MIN_VALUE;

                    data[code] = [];

                    for (i = $scope.observations[code].length - 1; i >= 0; i--) {
                        var observation = $scope.observations[code][i];

                        if (i == $scope.observations[code].length - 1) {
                            firstObservations[code] = observation;
                        }

                        var row = [];
                        row[0] = observation.applies;
                        row[1] = parseFloat(observation.value);

                        // don't display textual results on graph
                        if (!isNaN(row[1])) {

                            //skip invalid results
                            if (observeHeading.maxGraph && row[1]>observeHeading.maxGraph) {
                                continue;
                            } else if (observeHeading.minGraph && row[1] < observeHeading.minGraph) {
                                continue;
                            }

                            data[code].push(row);

                            // get min/max values for y-axis
                            if (observation.value > maxValue[code]) {
                                maxValue[code] = row[1];
                            }

                            if (observation.value < minValue[code]) {
                                minValue[code] = row[1];
                            }
                        }
                    }
                }

                if (data[code]) {
                    if ($scope.codes.length > 1 && !sameScale) {
                        yAxisData.title = {
                            text: firstObservations[code].name
                        };
                    }
                    yAxisData.labels = {
                        format: '{value}'
                    };

                    yAxis.push(yAxisData);

                    var seriesData = {};
                    seriesData.name = firstObservations[code].name;
                    seriesData.tooltip = {
                        valueDecimals: firstObservations[code].decimalPlaces
                    };

                    if (sameScale) {
                        seriesData.yAxis = 0;
                    } else {
                        seriesData.yAxis = index;
                    }

                    seriesData.data = data[code];

                    series.push(seriesData);
                }
            });

            legend.enabled = $scope.codes.length > 1;

            var chart = $('#chart_div').highcharts('StockChart', {
                rangeSelector: {
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
                credits: {
                    enabled: false
                },
                legend: legend,
                title: {
                    text: ''
                },
                navigator: {
                    enabled: true
                },
                series: series,
                chart: {
                    followTouchMove: false,
                    ignoreHiddenSeries: true,
                    zoomType: 'xy',
                    panning: true,
                    panKey: 'shift'
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
                        year: '%e. %b. %Y',

                    },
                    events: {
                        setExtremes: function (e) {
                            //zoom y reset
                            $scope.vscale = 0;
                            $scope.$broadcast('rzSliderForceRender')
                        }
                    },
                    text: 'ESEMPIO',
                    ordinal: false
                },
                yAxis:yAxis,
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

        $scope.getObservations = function () {
            $scope.loading = true;
            $scope.chartLoading = true;
            var promises = [];
            var obs = [];
            var selectedObs;

            $scope.codes.forEach(function (code, index) {
                promises.push(ObservationService.getByCode($scope.loggedInUser.id, code).then(function (observations) {
                    if (observations.length) {
                        obs[code] = _.sortBy(observations, 'applies').reverse();

                        if (index == 0) {
                            selectedObs = obs[code][0];
                        }
                    } else {
                        delete obs[code];
                        //delete $scope.selectedObservation;
                    }

                }, function () {
                    alert('Error retrieving results');
                    $scope.loading = false;
                }));
            });

            $q.all(promises).then(function () {
                $scope.observations = obs;
                $scope.selectedObservation = selectedObs;
                $scope.loading = false;
                $scope.initialiseChart();
            });
        };

        $scope.getResultIcon = function (value) {
            if (value === undefined || value === 0 || value === null || isNaN(value)) {
                return null;
            }

            if (value < 0) {
                return 'icon-result-down';
            }
            return 'icon-result-up';
        };

        $scope.removeMinus = function (value) {
            if (value !== undefined) {
                value = Math.abs(value);

                // now round to at most 2 dp
                value = +(Math.round(value + 'e+2') + 'e-2');

                return value;
            } else {
                return null;
            }
        };

        $scope.findObservationHeadingByCode = function (code) {
            for (var i = 0; i < $scope.observationHeadings.length; i++) {
                if ($scope.observationHeadings[i].code === code) {
                    return $scope.observationHeadings[i];
                }
            }
        };

        $scope.changeObservationHeading = function (code) {
            $('.chart-content-panel').hide();
            delete $scope.compareCode;
            $scope.codes = [];
            $scope.codes.push(code);

            $scope.observationHeading = $scope.findObservationHeadingByCode(code);
            $scope.selectedCode = $scope.observationHeading.code;
            $scope.getObservations(code);
        };

        $scope.observationClicked = function (observation) {
            $scope.selectedObservation = observation;
        };

        $scope.getValueChanged = function (observation) {
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

        $scope.showHideObservationsInTable = function (start, end) {
            $scope.tableObservations = false;
            $scope.tableObservations = [];
            $scope.tableObservationsKey = [];

            if ($scope.observations[$scope.selectedCode] !== undefined) {
                for (var i = 0; i < $scope.observations[$scope.selectedCode].length; i++) {
                    var observation = $scope.observations[$scope.selectedCode][i];
                    if (start <= observation.applies && end >= observation.applies) {
                        observation.appliesFormatted = $filter('date')(observation.applies, 'dd-MMM-yyyy HH:mm');
                        observation.appliesFormatted = observation.appliesFormatted.replace(' 00:00', '');
                        $scope.tableObservations.push(observation);
                        $scope.tableObservationsKey[observation.applies] = $scope.tableObservations.length - 1;
                    }
                }
            }

            $timeout(function () {
                $scope.$apply();
            });
        };

        $scope.openObservationHeadingInformation = function (result) {
            if (/Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)) {
                var modalsize = 'md';
            } else {
                modalsize = 'sm';
            }

            var modalInstance = $modal.open({
                templateUrl: 'views/modal/observationHeadingInfoModal.html',
                controller: ObservationHeadingInfoModalInstanceCtrl,
                size: modalsize,
                resolve: {
                    result: function () {
                        return result;
                    }
                }
            });

            modalInstance.result.then(function () {
            }, function () {
                // closed
            });
        };

        $scope.sliderChanged = function () {
            if (this.debouncedSliderChanged)
                clearTimeout(this.debouncedSliderChanged);
            this.debouncedSliderChanged = setTimeout(function () {
                if ($scope.lastvscale != $scope.vscale) {
                    $scope.lastvscale = $scope.vscale;
                    var value = $scope.vscale / 100;
                    var chart = $('#chart_div').highcharts();
                    //reduce % each side
                    if (chart) {
                        var min = chart.yAxis[0].dataMin;
                        var max = chart.yAxis[0].dataMax;
                        var range = (max - min) / 2;
                        var diff = range * value;
                        chart.yAxis[0].setExtremes(min + diff, max - diff)
                        console.log(value, chart);
                        console.log("chart")
                    } else {
                        console.log("ok")
                    }
                }

            }, 200)


        };

        $scope.sliderOptions = {
            floor: 0,
            ceil: 100,
            onChange:$scope.sliderChanged,
            hideLimitLabels:true,
            hidePointerLabels:true
        };

        $scope.extendXAxis = function () {
            var months = parseInt($scope.xAxisRange);
            var charts = $('#chart_div').highcharts();
            var min = charts.xAxis[0].min;
            var maxDate = new Date(charts.xAxis[0].dataMax);
            var newMaxDate = new Date(new Date(maxDate).setMonth(maxDate.getMonth() + months));

            console.log("X Axis newMax " + newMaxDate);
            charts.xAxis[0].setExtremes(min, newMaxDate.getTime());
        };

        $scope.init();
    }]);
