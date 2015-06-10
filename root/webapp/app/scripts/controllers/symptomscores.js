'use strict';

var SymptomScoreDetailModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'SymptomScoreService', 'symptomScoreId',
function ($scope, $rootScope, $modalInstance, SymptomScoreService, symptomScoreId) {

    var init = function() {
        $scope.loading = true;
        delete $scope.symptomScore;
        $scope.errorMessage = '';

        if (symptomScoreId == null) {
            $scope.errorMessage = 'Error retrieving symptom score';
            $scope.loading = false;
            return;
        }

        SymptomScoreService.getSymptomScore($scope.loggedInUser.id, symptomScoreId).then(function(result) {
            $scope.symptomScore = result;
            $scope.loading = false;
        }, function () {
            $scope.errorMessage = 'Error retrieving symptom score';
            $scope.loading = false;
        });
    };

    $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
    };

    init();
}];

var SymptomScoreDetailsNewModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'SurveyService',
    'SymptomScoreService', 'surveyType', 'UtilService',
function ($scope, $rootScope, $modalInstance, SurveyService, SymptomScoreService, surveyType, UtilService) {

    var init = function() {
        $scope.symptomScore = {};
        $scope.answers = [];
        $scope.days = UtilService.generateDays();
        $scope.months = UtilService.generateMonths();
        $scope.years = UtilService.generateYears2000();
        $scope.date = {};
        var i;

        var currentDate = new Date();
        for (i=0;i<$scope.days.length;i++) {
            if (parseInt($scope.days[i]) === currentDate.getDate()) {
                $scope.date.day = $scope.days[i];
            }
        }
        for (i=0;i<$scope.months.length;i++) {
            if (parseInt($scope.months[i]) === currentDate.getMonth() + 1) {
                $scope.date.month = $scope.months[i];
            }
        }
        for (i=0;i<$scope.years.length;i++) {
            if (parseInt($scope.years[i]) === currentDate.getFullYear()) {
                $scope.date.year = $scope.years[i];
            }
        }

        SurveyService.getByType(surveyType).then(function(result) {
            $scope.survey = result;
        }, function () {
            alert('error getting survey')
        });
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };

    $scope.save = function () {
        // build object to send to back end
        var symptomScore = {};
        symptomScore.user = {};
        symptomScore.user.id = $scope.loggedInUser.id;
        symptomScore.survey = {};
        symptomScore.survey.id = $scope.survey.id;
        symptomScore.questionAnswers = [];

        for (var i = 0; i < $scope.answers.length; i++) {
            var answer = $scope.answers[i];
            if (answer !== null) {
                var questionAnswer = {};
                questionAnswer.questionOption = {};
                questionAnswer.questionOption.id = i;
                questionAnswer.value = answer;
                symptomScore.questionAnswers.push(questionAnswer);
            }
        }

        SymptomScoreService.add(symptomScore.user.id, symptomScore).then(function() {
            $modalInstance.dismiss('ok');
        }, function () {
            alert('error getting survey')
        });
    };

    $scope.range = function(min, max) {
        var input = [];
        for (var i = min; i <= max; i += 1) {
            input.push(i);
        }
        return input;
    };

    init();
}];

angular.module('patientviewApp').controller('SymptomScoresCtrl',['$scope', '$routeParams', '$location',
    'SymptomScoreService', 'SurveyService', '$modal', '$timeout', '$filter', 'UtilService',
function ($scope, $routeParams, $location, SymptomScoreService, SurveyService, $modal, $timeout, $filter, UtilService) {

    $scope.init = function() {
        $scope.loading = true;
        $scope.surveyType = 'CROHNS_SYMPTOM_SCORE';
        getSymptomScores();
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
            row[0] = symptomScore.date;
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
                name : 'Score',
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

    var getSymptomScores = function() {
        $scope.loading = true;
        $scope.chartLoading = true;
        SymptomScoreService.getByUser($scope.loggedInUser.id).then(function(symptomScores) {
            if (symptomScores.length) {
                $scope.symptomScores = _.sortBy(symptomScores, 'date').reverse();
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
        $scope.showHideInTable(start, now);
    };

    $scope.showHideInTable = function(start, end) {
        $scope.tableSymptomScores = false;
        $scope.tableSymptomScores = [];
        $scope.tableSymptomScoresKey = [];

        for (var i=0;i<$scope.symptomScores.length;i++) {
            var symptomScore = $scope.symptomScores[i];
            if (start <= symptomScore.date && end >= symptomScore.date) {
                symptomScore.dateFormatted = $filter('date')(symptomScore.date, 'dd-MMM-yyyy HH:mm');
                symptomScore.dateFormatted = symptomScore.dateFormatted.replace(' 00:00', '');
                $scope.tableSymptomScores.push(symptomScore);
                $scope.tableSymptomScoresKey[symptomScore.date] = $scope.tableSymptomScores.length - 1;
            }
        }

        $timeout(function() {
            $scope.$apply();
        });
    };

    $scope.openModalEnterSymptoms = function () {
        // open modal and pass in required objects for use in modal scope
        var modalInstance = $modal.open({
            templateUrl: 'views/partials/symptomScoreDetailsNew.html',
            controller: SymptomScoreDetailsNewModalInstanceCtrl,
            size: 'lg',
            backdrop: 'static',
            resolve: {
                SurveyService: function(){
                    return SurveyService;
                },
                SymptomScoreService: function(){
                    return SymptomScoreService;
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
            getSymptomScores();
        }, function () {
            // close button, do nothing
        });
    };

    $scope.openModalSymptomScoreDetail = function (symptomScoreId) {
        // open modal and pass in required objects for use in modal scope
        var modalInstance = $modal.open({
            templateUrl: 'views/partials/symptomScoreDetailModal.html',
            controller: SymptomScoreDetailModalInstanceCtrl,
            size: 'lg',
            backdrop: 'static',
            resolve: {
                SymptomScoreService: function(){
                    return SymptomScoreService;
                },
                symptomScoreId: function(){
                    return symptomScoreId;
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
