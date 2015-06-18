'use strict';

var SymptomScoreDetailModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'SurveyResponseService', 'symptomScoreId',
function ($scope, $rootScope, $modalInstance, SurveyResponseService, symptomScoreId) {

    var init = function() {
        $scope.loading = true;
        delete $scope.symptomScore;
        $scope.errorMessage = '';

        if (symptomScoreId == null) {
            $scope.errorMessage = 'Error retrieving symptom score';
            $scope.loading = false;
            return;
        }

        SurveyResponseService.getSurveyResponse($scope.loggedInUser.id, symptomScoreId).then(function(result) {
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
    'SurveyResponseService', 'surveyType', 'UtilService',
function ($scope, $rootScope, $modalInstance, SurveyService, SurveyResponseService, surveyType, UtilService) {

    var init = function() {
        $scope.symptomScore = {};
        $scope.answers = [];
        $scope.days = UtilService.generateDays();
        $scope.months = UtilService.generateMonths();
        $scope.years = UtilService.generateYears2000();
        $scope.date = {};
        var i, j;

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

        SurveyService.getByType(surveyType).then(function(survey) {
            $scope.survey = survey;
            $scope.questionTypeMap = [];

            // create map of question id to question type, used when creating object to send to backend
            for (i = 0; i < survey.questionGroups.length; i++) {
                for (j = 0; j < survey.questionGroups[i].questions.length; j++) {
                    var question = survey.questionGroups[i].questions[j];
                    $scope.questionTypeMap[question.id] = question.elementType;
                }
            }

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
        symptomScore.date = new Date($scope.date.year, $scope.date.month - 1, $scope.date.day);

        for (var i = 0; i < $scope.answers.length; i++) {
            var answer = $scope.answers[i];
            if (answer !== null && answer !== undefined) {
                var questionAnswer = {};
                if ($scope.questionTypeMap[i] === 'SINGLE_SELECT') {
                    questionAnswer.questionOption = {};
                    questionAnswer.questionOption.id = answer;
                }
                if ($scope.questionTypeMap[i] === 'SINGLE_SELECT_RANGE') {
                    questionAnswer.value = answer;
                }
                questionAnswer.question = {};
                questionAnswer.question.id = i;
                symptomScore.questionAnswers.push(questionAnswer);
            }
        }

        SurveyResponseService.add(symptomScore.user.id, symptomScore).then(function() {
            $modalInstance.close();
        }, function (error) {
            $scope.errorMessage = error.data;
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
    'SurveyResponseService', 'SurveyService', '$modal', '$timeout', '$filter', 'UtilService',
function ($scope, $routeParams, $location, SurveyResponseService, SurveyService, $modal, $timeout, $filter, UtilService) {

    $scope.init = function() {
        $scope.loading = true;
        if ($scope.$parent.patient.myIbd && $scope.$parent.patient.myIbd.primaryDiagnosis) {
            var primaryDiagnosis = $scope.$parent.patient.myIbd.primaryDiagnosis;
            if (primaryDiagnosis === 'Crohn\'s Disease') {
                $scope.surveyType = 'CROHNS_SYMPTOM_SCORE';

                $scope.plotLines = [{
                    color: '#FF8A8A',
                    width: 160,
                    value: 20
                }, {
                    color: '#FFB347',
                    width: 48,
                    value: 7
                }, {
                    color: '#77DD77',
                    width: 35,
                    value: 2
                }];

                $scope.max = 30;

                getSymptomScores();
            } else if (primaryDiagnosis === 'Ulcerative Colitis'
                    || primaryDiagnosis === 'IBD - Unclassified (IBDU)') {
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

                getSymptomScores();
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

        for (var i = $scope.symptomScores.length -1; i >= 0; i--) {

            var symptomScore = $scope.symptomScores[i];
            var score = symptomScore.surveyResponseScores[0].score;

            var row = [];
            row[0] = symptomScore.date;
            row[1] = parseFloat(score);

            // don't display textual results on graph
            if (!isNaN(row[1])) {
                data.push(row);
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
                color: '#585858',
                tooltip: {
                    valueDecimals: 1
                },
                marker : {
                    enabled : true,
                    radius : 2
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

    var getSymptomScores = function() {
        $scope.loading = true;
        $scope.chartLoading = true;
        SurveyResponseService.getByUserAndSurveyType($scope.loggedInUser.id, $scope.surveyType)
        .then(function(symptomScores) {
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
                SurveyResponseService: function(){
                    return SurveyResponseService;
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
