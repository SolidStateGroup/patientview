'use strict';

// EQ5D
angular.module('patientviewApp').controller('SurveysOverallCtrl', ['$scope', 'SurveyResponseService', '$filter',
    function ($scope, SurveyResponseService, $filter) {

    var buildChart = function(visibleResponses) {
        if (!visibleResponses.length) {
            return;
        }

        var i, j, series = [], chartSeries = [];
        var colours = ['#f0ad4e', '#7CB5EC'];

        for (i = 0; i < visibleResponses.length; i++) {
            var response = visibleResponses[i];
            if (series[response.date] == undefined || series[response.date] == null) {
                series[response.date] = {};
                series[response.date].color = colours[i];
                series[response.date].name = $scope.filterDate(response.date);
                series[response.date].data = [];
            }

            // get question answer data for question with correct type
            for (j = 0; j < response.questionAnswers.length; j++) {
                series[response.date].data.push(response.questionAnswers[j].questionOption.score);
            }
        }

        for (var key in series) {
            chartSeries.push(series[key]);
        }

        var scoreLabels = ['No problem', 'Slight', 'Moderate', 'Severe', 'Extreme'];

        var titleText = '<span style="color:' + chartSeries[0].color + '">' + chartSeries[0].name + '</span>';
        if (chartSeries.length > 1) {
            titleText = '<span style="color:' + chartSeries[0].color + '">' + chartSeries[0].name
                + '</span> and <span style="color:' + chartSeries[1].color + '">'
                + chartSeries[1].name + '</span>';
        }

        $('#chart_div').highcharts({
            chart: {
                type: 'column'
            },
            credits : {
                enabled: false
            },
            title: {
                text: titleText,
                useHtml: true
            },
            xAxis: {
                categories: [
                    'Mobility',
                    'Anxiety / Depression',
                    'Usual Activities',
                    'Pain / Discomfort',
                    'Self-Care'
                ],
                crosshair: true
            },
            yAxis: {
                min: 1,
                max: 5,
                labels: {
                    formatter: function () {
                        return '<span style="color:#606060">' + scoreLabels[this.value - 1] + '</span>';
                    }
                },
                title: {
                    text: ''
                }
            },
            plotOptions: {
                column: {
                    pointPadding: 0.2,
                    borderWidth: 0
                }
            },
            series: chartSeries
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
        tableHeader.push({'text':'Your Overall Health'});

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

    $scope.compareSurvey = function(id) {
        if (id !== undefined && id !== null) {
            var visibleSurveyResponses = [];
            visibleSurveyResponses.push(_.findWhere($scope.surveyResponses, {id: id}));
            visibleSurveyResponses.push($scope.latestSurveyResponse);
            buildTable(visibleSurveyResponses);
            buildChart(visibleSurveyResponses);
        }
    };

    $scope.filterDate = function(date) {
        return $filter("date")(date, "dd-MMM-yyyy");
    };

    var getSurveyFeedbackText = function() {
        $scope.surveyFeedbackText = 'I would like to talk about the following: \n1. \n2. \n3. ';
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
        $scope.surveyType = 'EQ5D';
        $scope.loading = true;
        getSurveyResponses();
        getSurveyFeedbackText();
    };

    var initialiseChart = function() {
        if (!$scope.surveyResponses.length) {
            return;
        }

        // set latest response property to allow styling
        $scope.surveyResponses[0].isLatest = true;
        $scope.latestSurveyResponse = $scope.surveyResponses[0];

        // generate date options used in select when comparing responses to latest
        var surveyResponseSelectOptions = [];
        for (var i = 0; i < $scope.surveyResponses.length; i++) {
            if ($scope.surveyResponses[i].id !== $scope.latestSurveyResponse.id) {
                surveyResponseSelectOptions.push({
                    'id': $scope.surveyResponses[i].id,
                    'date': $filter("date")($scope.surveyResponses[i].date, "dd-MMM-yyyy")
                });
            }
        }
        $scope.surveyResponseSelectOptions = surveyResponseSelectOptions;

        // add latest to table
        var visibleSurveyResponses = [];
        if ($scope.surveyResponses[1] !== null && $scope.surveyResponses[1] !== undefined) {
            // add second latest if exists
            visibleSurveyResponses.push($scope.surveyResponses[1]);
        }
        visibleSurveyResponses.push($scope.latestSurveyResponse);

        // build table and chart from visible responses (2 most recent) responses
        buildTable(visibleSurveyResponses);
        buildChart(visibleSurveyResponses);

        // get next survey date (3 months from last survey
        var threeMonths = moment($scope.latestSurveyResponse.date).add(3, 'months');
        $scope.nextSurveyDate = threeMonths.format('MMMM') + ' ' + threeMonths.format('YYYY');
    };

    $scope.saveSurveyFeedbackText = function(text) {
        delete $scope.surveyFeedbackSuccessMessage;
        delete $scope.surveyFeedbackErrorMessage;
        $scope.savingSurveyFeedbackText = true;
        console.log(text);

        delete $scope.savingSurveyFeedbackText;
        $scope.surveyFeedbackSuccessMessage = 'Saved your comments';
    };

    $scope.sendSurveyFeedbackText = function(text) {
        delete $scope.surveyFeedbackSuccessMessage;
        delete $scope.surveyFeedbackErrorMessage;
        $scope.sendingSurveyFeedbackText = true;
        console.log(text);

        delete $scope.sendingSurveyFeedbackText;
        $scope.surveyFeedbackSuccessMessage = 'Sent your comments to your clinical staff';
    };

    init();
}]);
