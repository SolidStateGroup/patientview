'use strict';

angular.module('patientviewApp').controller('SurveysSymptomsCtrl',['$scope', 'SurveyResponseService', '$filter',
    'ObservationHeadingService', 'ObservationService',
    function ($scope, SurveyResponseService, $filter, ObservationHeadingService, ObservationService) {

    var buildChart = function() {
        if (!$scope.surveyResponses.length || $scope.questionType == undefined || $scope.questionType == null) {
            return;
        }

        var i, j, xAxis = [], chartSeries = [], questionData = [], obsData = [], xAxisArr = [], xAxisLabels = [],
            observationChartData = [], questionChartData = [];

        for (i = $scope.surveyResponses.length -1; i >= 0; i--) {
            var response = $scope.surveyResponses[i];

            // x axis dates (map)
            xAxis[response.date] = response.date;

            // get question answer data for question with correct type
            for (j = 0; j < response.questionAnswers.length; j++) {
                var questionAnswer = response.questionAnswers[j];
                if (questionAnswer.question.type == $scope.questionType) {
                    questionData.push({'date':response.date, 'value':questionAnswer.questionOption.score});
                }
            }
        }

        if ($scope.observations) {
            for (i = $scope.observations.length - 1; i >= 0; i--) {
                var observation = $scope.observations[i];
                xAxis[observation.applies] = observation.applies;

                // don't display textual results on graph
                if (!isNaN(parseFloat(observation.value))) {
                    obsData.push({'date':observation.applies, 'value':parseFloat(observation.value)});
                }
            }
        }

        // convert xAxis map to array and sort
        for (var key in xAxis) {
            xAxisArr.push(xAxis[key]);
        }
        xAxisArr = _.sortBy(xAxisArr);

        // for each x axis point, get data for question and observation if present
        for (i = 0; i < xAxisArr.length; i++) {
            observationChartData[i] = null;
            questionChartData[i] = null;

            xAxisLabels[i] = $scope.filterDate(xAxisArr[i]);
            for (j = 0; j < obsData.length; j++) {
                if (obsData[j].date == xAxisArr[i]) {
                    observationChartData[i] = obsData[j].value;
                }
            }
            for (j = 0; j < questionData.length; j++) {
                if (questionData[j].date == xAxisArr[i]) {
                    questionChartData[i] = questionData[j].value;
                }
            }
        }

        var questionName = _.findWhere($scope.questions, {type: $scope.questionType}).text;

        chartSeries.push({
            'name': questionName,
            'data': questionChartData,
            'yAxis': 0
        });

        var numericText = '';
        if (!obsData.length) {
            numericText = ' (no numeric data)'
        }

        if ($scope.observations) {
            chartSeries.push({
                'name': $scope.observationHeading.heading + numericText,
                'data': observationChartData,
                'yAxis': 1
            });
        }

        // set chart data
        $('#chart_div').highcharts({
            chart: {
                alignTicks: false
            },
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
                categories: xAxisLabels,
                crosshair: false,
                rotation: 90
            },
            yAxis: [{
                min: 1,
                max: 5,
                gridLineWidth: 0,
                labels: {
                    formatter: function () {
                        return $scope.scoreLabels[this.value];
                    }
                },
                title: {
                    text: chartSeries.length > 1 ? questionName : ''
                }
            }, {
                gridLineWidth: 0,
                title: {
                    text: $scope.observations ? $scope.observationHeading.heading : ''
                },
                opposite: true
            }]
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
            var dateString = $scope.filterDate(response.date);
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

    $scope.compareResults = function(code) {
        if (code !== undefined && code !== null) {
            $scope.observationHeading = _.findWhere($scope.observationHeadings, {code: code});

            ObservationService.getByCode($scope.loggedInUser.id, code).then(function (observations) {
                if (observations.length) {
                    $scope.observations = observations;
                    buildChart();
                }
            }, function () {
                alert('Error retrieving results');
            })
        }
    };

    $scope.compareSurvey = function(id) {
        if (id !== undefined && id !== null) {
            var visibleSurveyResponses = [];
            visibleSurveyResponses.push(_.findWhere($scope.surveyResponses, {id: id}));
            visibleSurveyResponses.push($scope.latestSurveyResponse);
            buildTable(visibleSurveyResponses);
        }
    };

    $scope.filterDate = function(date) {
        return $filter("date")(date, "dd-MMM-yyyy");
    };

    var getObservationHeadings = function() {
        ObservationHeadingService.getAvailableObservationHeadings($scope.loggedInUser.id)
            .then(function(observationHeadings) {
            $scope.observationHeadings = observationHeadings;
        }, function() {
            alert('Error retrieving result types');
        });
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
        getObservationHeadings();
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
        $scope.questions = $scope.surveyResponses[0].survey.questionGroups[0].questions;

        // build chart from all responses, using first question's type e.g. YSQ1
        $scope.questionType = $scope.surveyResponses[0].survey.questionGroups[0].questions[0].type;
        buildChart();
    };

    $scope.viewGraph = function(type) {
        $scope.questionType = type;
        buildChart();
    };

    init();
}]);