'use strict';

angular.module('patientviewApp').controller('SurveysSymptomsCtrl',['$scope', 'SurveyResponseService', '$filter',
    function ($scope, SurveyResponseService, $filter) {

    var buildChart = function(type) {
        if (!$scope.surveyResponses.length) {
            return;
        }

        var i, j, xAxis = [], series = [], chartSeries = [];

        for (i = $scope.surveyResponses.length -1; i >= 0; i--) {
            var response = $scope.surveyResponses[i];

            // aXis labels
            var dateString = $filter("date")(response.date, "dd-MMM-yyyy");
            xAxis.push(dateString);

            // get question answer data for question with correct type
            for (j = 0; j < response.questionAnswers.length; j++) {
                var questionAnswer = response.questionAnswers[j];
                if (questionAnswer.question.type == type) {
                    if (series[questionAnswer.question.type] == undefined
                        || series[questionAnswer.question.type] == null) {
                        series[questionAnswer.question.type] = {};
                        series[questionAnswer.question.type].name = questionAnswer.question.text;
                        series[questionAnswer.question.type].data = [];
                    }
                    series[questionAnswer.question.type].data.push(questionAnswer.questionOption.score);
                }
            }
        }

        for (var key in series) {
            chartSeries.push(series[key]);
        }

        // set chart data
        $('#chart_div').highcharts({
            credits : {
                enabled: false
            },
            plotOptions: {
                column: {
                    pointPadding: 0.2,
                    borderWidth: 0
                }
            },
            series: chartSeries,
            title: {
                text: ''
            },
            xAxis: {
                categories: xAxis,
                crosshair: false
            },
            yAxis: {
                min: 1,
                max: 5,
                labels: {
                    formatter: function() {
                        return $scope.scoreLabels[this.value];
                    }
                },
                title: {
                    text: ''
                }
            }
        });
    };

    var buildTable = function(visibleResponses) {
        if (!visibleResponses.length) {
            return;
        }

        // format survey response suitable for table view
        var tableRows = [];
        var tableHeader = [];
        var i, j;

        // header left most column
        tableHeader.push({'text':'Your Symptoms'});

        for (i = 0; i < visibleResponses.length; i++) {
            var response = visibleResponses[i];
            var questionAnswers = _.sortBy(response.questionAnswers, 'question.displayOrder');

            // header other columns
            var dateString = $filter("date")(response.date, "dd-MMM-yyyy");
            tableHeader.push({'text':dateString, 'isLatest':response.isLatest});

            // rows
            for (j = 0; j < questionAnswers.length; j++) {
                var questionAnswer = questionAnswers[j];

                // set question text, e.g. Pain
                if (tableRows[j] == undefined || tableRows[j] == null) {
                    tableRows[j] = {};
                    tableRows[j].type = questionAnswer.question.type;
                    tableRows[j].data = [];
                    tableRows[j].data.push({'text':questionAnswer.question.text});
                }

                // set response text, e.g. Moderately
                tableRows[j].data.push({'text':questionAnswer.questionOption.text, 'isLatest':response.isLatest});
            }
        }

        $scope.tableHeader = tableHeader;
        $scope.tableRows = tableRows;
    };

    $scope.compareSurvey = function(id) {
        if (id !== undefined && id !== null) {
            var visibleSurveyResponses = [];
            visibleSurveyResponses.push(_.findWhere($scope.surveyResponses, {id: id}));
            visibleSurveyResponses.push($scope.latestSurveyResponse);
            buildTable(visibleSurveyResponses);
        }
    };

    var getSurveyResponses = function() {
        $scope.loading = true;
        SurveyResponseService.getByUserAndSurveyType($scope.loggedInUser.id, $scope.surveyType)
            .then(function(surveyResponses) {
                if (surveyResponses.length) {
                    $scope.surveyResponses = _.sortBy(surveyResponses, 'date').reverse();
                    initialiseChart();
                } else {
                    delete $scope.surveyResponses;
                }
                $scope.loading = false;
            }, function() {
                alert('Error retrieving responses');
                $scope.loading = false;
            });
    };

    var init = function() {
        $scope.surveyType = 'PROMS';
        getSurveyResponses();
    };

    var initialiseChart = function() {
        if (!$scope.surveyResponses.length) {
            return;
        }

        // set latest response property to allow styling
        $scope.surveyResponses[0].isLatest = true;
        $scope.latestSurveyResponse = $scope.surveyResponses[0];

        // generate date options used in select when comparing responses to latest
        var surveyReponseSelectOptions = [];
        for (var i = 0; i < $scope.surveyResponses.length; i++) {
            if ($scope.surveyResponses[i].id !== $scope.latestSurveyResponse.id) {
                surveyReponseSelectOptions.push({
                    'id': $scope.surveyResponses[i].id,
                    'date': $filter("date")($scope.surveyResponses[i].date, "dd-MMM-yyyy")
                });
            }
        }
        $scope.surveyReponseSelectOptions = surveyReponseSelectOptions;

        // add latest to table
        var visibleSurveyResponses = [];
        if ($scope.surveyResponses[1] !== null && $scope.surveyResponses[1] !== undefined) {
            // add second latest if exists
            visibleSurveyResponses.push($scope.surveyResponses[1]);
        }
        visibleSurveyResponses.push($scope.latestSurveyResponse);

        // build table from visible responses (2 most recent) responses
        buildTable(visibleSurveyResponses);

        // set up question options, used for chart y axis labels, assumes 1 question group and same for all questions
        var scoreLabels = [];
        var firstOptions = $scope.surveyResponses[0].survey.questionGroups[0].questions[0].questionOptions;
        for (i = 0; i < firstOptions.length; i++) {
            scoreLabels[firstOptions[i].score] = firstOptions[i].text;
        }
        $scope.scoreLabels = scoreLabels;

        // build chart from all responses, using first question's type e.g. YSQ1
        buildChart($scope.surveyResponses[0].survey.questionGroups[0].questions[0].type);
    };

    $scope.viewGraph = function(type) {
        buildChart(type);
    };

    init();
}]);
