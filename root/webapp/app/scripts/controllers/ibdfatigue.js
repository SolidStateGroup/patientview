'use strict';

angular.module('patientviewApp').controller('IbdFatigueCtrl',['$scope', '$routeParams', '$location',
    'SurveyResponseService', 'SurveyService', 'ObservationService', 'ObservationHeadingService', '$modal', '$timeout',
    '$filter', 'UtilService',
function ($scope, $routeParams, $location, SurveyResponseService, SurveyService, ObservationService,
          ObservationHeadingService, $modal, $timeout, $filter, UtilService) {

    $scope.init = function() {
        $scope.loading = true;
        $scope.surveyType = 'IBD_FATIGUE';

        $scope.plotLines = [{
            color: '#FF8A8A',
            width: 105,
            value: 110
        },{
            color: '#FFB347',
            width: 70,
            value: 60
        }, {
            color: '#77DD77',
            width: 70,
            value: 20
        }];

        $scope.max = 140;
        getSurveyResponses();
    };

    $scope.initialiseChart = function() {
        Highcharts.setOptions({
            global: {
                useUTC: false
            }
        });
        // using highstocks
        $('.chart-content-panel').show();

        var data1 = [];

        for (var i = $scope.surveyResponses.length -1; i >= 0; i--) {

            var surveyResponse = $scope.surveyResponses[i];
            var score1 = surveyResponse.surveyResponseScores[0] ? surveyResponse.surveyResponseScores[0].score : null;

            var row = [];
            row[0] = surveyResponse.date;
            row[1] = parseFloat(score1);

            // don't display textual results on graph
            if (!isNaN(row[1])) {
                data1.push(row);
            }
        }

        $('#chart_IBD_FATIGUE').highcharts('StockChart', {
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
                name : 'Fatigue Questions',
                data : data1,
                color: '#585858',
                tooltip: {
                    valueDecimals: 1
                },
                marker : {
                    enabled : true,
                    radius : 2
                },
                yAxis: 0
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
                text: 'ESEMPIO',
                ordinal: false
            },
            yAxis: [{
                offset: 20,
                plotLines: $scope.plotLines,
                min: 0,
                max: $scope.max,
                floor: 0,
                endOnTick: false,
                title : {
                    text: 'Fatigue Questions',
                    style: {
                        color: '#585858'
                    }
                }
            }],
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

    var getSurveyResponses = function() {
        $scope.loading = true;
        $scope.chartLoading = true;
        SurveyResponseService.getByUserAndSurveyType($scope.loggedInUser.id, $scope.surveyType)
        .then(function(surveyResponses) {
            if (surveyResponses.length) {
                $scope.surveyResponses = _.sortBy(surveyResponses, 'date').reverse();
                $scope.initialiseChart();
            } else {
                delete $scope.surveyResponses;
            }
            $scope.loading = false;
        }, function() {
            alert('Error retrieving responses');
            $scope.loading = false;
        });
    };

    $scope.setRangeInDays = function (days) {
        $scope.range = days;
        var now = new Date();
        now = new Date(now.getTime() + 86400000);
        var start = new Date(now.getTime() - days * 86400000);
        $scope.showHideInTable(start, now);
    };

    $scope.showHideInTable = function(start, end) {
        $scope.tableSurveyResponses = false;
        $scope.tableSurveyResponses = [];
        $scope.tableSurveyResponsesKey = [];

        for (var i=0;i<$scope.surveyResponses.length;i++) {
            var surveyResponse = $scope.surveyResponses[i];
            if (start <= surveyResponse.date && end >= surveyResponse.date) {
                surveyResponse.dateFormatted = $filter('date')(surveyResponse.date, 'dd-MMM-yyyy HH:mm');
                surveyResponse.dateFormatted = surveyResponse.dateFormatted.replace(' 00:00', '');
                $scope.tableSurveyResponses.push(surveyResponse);
                $scope.tableSurveyResponsesKey[surveyResponse.date] = $scope.tableSurveyResponses.length - 1;
            }
        }

        $timeout(function() {
            $scope.$apply();
        });
    };

    $scope.openModalEnterSurveyResponses = function () {
        // open modal and pass in required objects for use in modal scope
        var modalInstance = $modal.open({
            templateUrl: 'views/partials/surveyResponseDetailNew.html',
            controller: SurveyResponseDetailNewModalInstanceCtrl,
            size: 'lg',
            backdrop: 'static',
            resolve: {
                SurveyService: function(){
                    return SurveyService;
                },
                SurveyResponseService: function(){
                    return SurveyResponseService;
                },
                surveyType: function(){
                    return $scope.surveyType;
                },
                UtilService: function(){
                    return UtilService;
                }
            }
        });

        // handle modal close (via button click)
        modalInstance.result.then(function () {
            getSurveyResponses();
        }, function () {
            // close button, do nothing
        });
    };

    $scope.openModalSurveyResponseDetail = function (surveyResponseId) {
        // open modal and pass in required objects for use in modal scope
        var modalInstance = $modal.open({
            templateUrl: 'views/modal/surveyResponseDetailModal.html',
            controller: SurveyResponseDetailModalInstanceCtrl,
            size: 'lg',
            backdrop: 'static',
            resolve: {
                SurveyResponseService: function(){
                    return SurveyResponseService;
                },
                surveyResponseId: function(){
                    return surveyResponseId;
                }
            }
        });

        // handle modal close (via button click)
        modalInstance.result.then(function () {
            // no ok button, do nothing
        }, function () {
            // close button, do nothing
        });
    };

    $scope.init();
}]);
