'use strict';

angular.module('patientviewApp').controller('SymptomScoresCtrl',['$scope', '$routeParams', '$location',
    'SurveyResponseService', 'SurveyService', 'ObservationService', 'ObservationHeadingService', '$modal',
    '$timeout', '$filter', 'UtilService',
function ($scope, $routeParams, $location, SurveyResponseService, SurveyService, ObservationService,
          ObservationHeadingService,  $modal, $timeout, $filter, UtilService) {

    $scope.init = function() {
        $scope.loading = true;
        if ($scope.$parent.patientDetails[0].myIbd && $scope.$parent.patientDetails[0].myIbd.primaryDiagnosis) {
            var primaryDiagnosis = $scope.$parent.patientDetails[0].myIbd.primaryDiagnosis;
            if (primaryDiagnosis.code === 'Crohn\'s Disease') {
                $scope.surveyType = 'CROHNS_SYMPTOM_SCORE';

                $scope.plotLines = [{
                    color: '#FF8A8A',
                    width: 112,
                    value: 23
                }, {
                    color: '#FFB347',
                    width: 96,
                    value: 10
                }, {
                    color: '#77DD77',
                    width: 35,
                    value: 2
                }];

                $scope.max = 30;

                getSurveyResponses();
            } else if (primaryDiagnosis.code === 'Ulcerative Colitis'
                    || primaryDiagnosis.code === 'IBD - Unclassified (IBDU)') {
                $scope.surveyType = 'COLITIS_SYMPTOM_SCORE';

                $scope.plotLines = [{
                    color: '#FF8A8A',
                    width: 90,
                    value: 13
                },{
                    color: '#FFB347',
                    width: 90,
                    value: 7
                }, {
                    color: '#77DD77',
                    width: 60,
                    value: 2
                }];

                $scope.max = 16;

                getSurveyResponses();
            } else if (primaryDiagnosis.code === 'Heart Failure') {
                $scope.surveyType = 'HEART_SYMPTOM_SCORE';

                $scope.plotLines = [{
                    color: '#77DD77',
                    width: 80,
                    value: 20
                },{
                    color: '#FFB347',
                    width: 80,
                    value: 12
                }, {
                    color: '#FF8A8A',
                    width: 80,
                    value: 4
                }];

                $scope.max = 25;

                getSurveyResponses();
            } else {
                $scope.unknownPrimaryDiagnosis = true;
                $scope.loading = false;
            }
        }
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

        for (var i = $scope.surveyResponses.length - 1; i >= 0; i--) {
            var surveyResponse = $scope.surveyResponses[i];
            var score = surveyResponse.surveyResponseScores[0].score;
            var row = [];
            row[0] = surveyResponse.date;
            row[1] = parseFloat(score);

            // don't display textual results on graph
            if (!isNaN(row[1])) {
                data.push(row);
            }
        }

        $('#chart_'.concat($scope.surveyType)).highcharts('StockChart', {
            rangeSelector: {
                buttons: [
                    {
                        type: 'month',
                        count: 1,
                        text: '1m'
                    },
                    {
                        type: 'month',
                        count: 3,
                        text: '3m'
                    },
                    {
                        type: 'year',
                        count: 1,
                        text: '1y'
                    },
                    {
                        type: 'year',
                        count: 3,
                        text: '3y'
                    },
                    {
                        type: 'all',
                        text: 'All'
                    }
                ],

                selected: 3
            },

            credits: {
                enabled: false
            },
            title: {
                text: ''
            },
            navigator: {
                enabled: true
            },
            series: [
                {
                    name: 'Score',
                    data: data,
                    color: '#585858',
                    tooltip: {
                        valueDecimals: 1
                    },
                    marker: {
                        enabled: true,
                        radius: 2
                    }
                }
            ],
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
            yAxis: {
                plotLines: $scope.plotLines,
                min: 0,
                max: $scope.max,
                floor: 0,
                endOnTick: false
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
            controller: SurveyResponseDetailsNewModalInstanceCtrl,
            size: 'lg',
            backdrop: 'static',
            windowClass: 'survey-modal',
            resolve: {
                ObservationService: function(){
                    return ObservationService;
                },
                ObservationHeadingService: function(){
                    return ObservationHeadingService;
                },
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
            templateUrl: 'views/partials/surveyResponseDetailModal.html',
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
