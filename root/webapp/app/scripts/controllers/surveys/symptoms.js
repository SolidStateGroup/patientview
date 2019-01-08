'use strict';

// PROM
angular.module('patientviewApp').controller('SurveysSymptomsCtrl',['$scope', 'SurveyResponseService', '$filter',
    'ObservationHeadingService', 'ObservationService', 'DocumentService',
    function ($scope, SurveyResponseService, $filter, ObservationHeadingService, ObservationService, DocumentService) {

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
            var questions = $scope.nonCustomQuestions;
            var questionAnswerMap = [];
            for (j = 0; j < response.questionAnswers.length; j++) {
                questionAnswerMap[response.questionAnswers[j].question.type] = response.questionAnswers[j];
            }

            for (j = 0; j < questions.length; j++) {
                var questionType = questions[j].type;

                if (questionAnswerMap[questionType]) {
                    if (questionAnswerMap[questionType].question.type == $scope.questionType) {
                        questionData.push({'date':response.date, 'value':questionAnswerMap[questionType].questionOption.score});
                    }
                }
            }
        }

        if ($scope.observations) {
            // use min and max date to only include observations within 3 months or survey data
            var minDate = moment($scope.minDate).subtract(3, 'months').valueOf();
            var maxDate = moment($scope.maxDate).add(3, 'months').valueOf();

            for (i = $scope.observations.length - 1; i >= 0; i--) {
                var observation = $scope.observations[i];

                if (observation.applies > minDate && observation.applies < maxDate) {
                    xAxis[observation.applies] = observation.applies;

                    // don't display textual results on graph
                    if (!isNaN(parseFloat(observation.value))) {
                        obsData.push({'date': observation.applies, 'value': parseFloat(observation.value)});
                    }
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

        var questionName = _.findWhere($scope.nonCustomQuestions, {type: $scope.questionType, customQuestion:false}).text;
        $scope.comparingText = questionName;

        chartSeries.push({
            'color': '#00adc6',
            'name': questionName,
            'data': questionChartData,
            'yAxis': 0
        });

        var numericText = '';
        if (!obsData.length) {
            numericText = ' (no data)'
        }

        if ($scope.observations) {
            chartSeries.push({
                'color': '#f0ad4e',
                'name': $scope.observationHeading.heading + numericText,
                'data': observationChartData,
                'yAxis': 1
            });
        }

        var titleText = '<span style="color:#00adc6">' + questionName + '</span>';
        if ($scope.observations) {
            titleText = 'Comparing ' + titleText + ' with ' + '<span style="color:#f0ad4e">' + $scope.observationHeading.heading + '</span>';
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
                },
                series: {
                    connectNulls: true
                }
            },
            series: chartSeries,
            title: {
                text: titleText,
                useHtml: true
            },
            xAxis: {
                categories: xAxisLabels,
                crosshair: false,
                rotation: 90,
                labels: {
                    enabled: xAxisLabels.length < 6
                }
            },
            yAxis: [{
                min: 0,
                max: 4,
                gridLineWidth: 0,
                labels: {
                    formatter: function () {
                        return '<span style="color:#606060">' + $scope.scoreLabels[this.value] + '</span>';
                    }
                },
                title: {
                    text: chartSeries.length > 1 ? questionName : '',
                    style: {'color':'#00adc6'}
                }
            }, {
                gridLineWidth: 0,
                labels: {
                    formatter: function () {
                        return '<span style="color:#606060">' + this.value + '</span>';
                    }
                },
                title: {
                    text: $scope.observations ? $scope.observationHeading.heading : '',
                    style: {'color':'#f0ad4e'}
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
        var customRows = [];
        var tableHeader = [];
        var i, j;

        // min and max dates to show results
        var minDate = 9999999999999;
        var maxDate = 0;

        // header left most column
        tableHeader.push({'text':''});

        for (i = 0; i < visibleResponses.length; i++) {
            var response = visibleResponses[i];
            var questionAnswers = _.sortBy(response.questionAnswers, 'question.displayOrder');

            // header other columns
            var dateString = $scope.filterDate(response.date);
            tableHeader.push({'text':dateString, 'isLatest':response.isLatest});

            // set min and max date (used when showing results)
            if (response.date > maxDate) {
                maxDate = response.date;
            }
            if (response.date < minDate) {
                minDate = response.date;
            }

            // rows
            var questions = $scope.questions;
            var questionAnswerMap = [];
            for (j = 0; j < questionAnswers.length; j++) {
                questionAnswerMap[questionAnswers[j].question.type] = questionAnswers[j];
            }

            var tableRowIndex = 0;
            for (j = 0; j < questions.length; j++) {
                var questionText = questions[j].text;
                var questionType = questions[j].type;
                var isCustom = questions[j].customQuestion;
                var questionOptionText = '-';

                if (isCustom) {
                    if (!customRows[i] ) {
                        var responses = Object.keys(questionAnswerMap).map(function (map) {
                            var response = questionAnswerMap[map];
                            return {
                                label:response.questionText,
                                value: response.questionOption && response.questionOption.text,
                            };
                        }).filter(function (v) {
                            return v.label && v.value;
                        });
                        if (responses && responses.length) {
                            customRows[i] = {
                                text: $scope.filterDate(response.date),
                                isLatest: response.isLatest,
                                responses: responses,
                            };
                        }
                    }
                } else {
                    if (questionAnswerMap[questionType]) {
                        questionOptionText = questionAnswerMap[questionType].questionOption?  questionAnswerMap[questionType].questionOption.text: questionAnswerMap[questionType].value;
                    }

                    // set question text, e.g. Pain
                    if (tableRows[tableRowIndex] == undefined || tableRows[tableRowIndex] == null) {
                        tableRows[tableRowIndex] = {};
                        tableRows[tableRowIndex].type = questionType;
                        tableRows[tableRowIndex].nonViewable = questions[j].nonViewable;
                        tableRows[tableRowIndex].data = [];
                        tableRows[tableRowIndex].data.push({'text':questionText});
                    }

                    // set response text, e.g. Moderately
                    tableRows[tableRowIndex].data.push({'text':questionOptionText, 'isLatest':response.isLatest});
                    tableRowIndex++;
                }

            }

            // special download row
            if (tableRows[tableRowIndex] == undefined || tableRows[tableRowIndex+1] == null) {
                tableRows[tableRowIndex] = {};
                tableRows[tableRowIndex].isDownload = true;
                tableRows[tableRowIndex].data = [];
                tableRows[tableRowIndex].data.push({'text':'', 'isDownload':true});
            }

            var download = '';

            if ($scope.documentDateMap[response.date]) {
                download = '<a href="../api/user/' + $scope.loggedInUser.id +
                    '/file/' + $scope.documentDateMap[response.date].fileDataId + '/download' +
                    '?token=' + $scope.authToken
                    + '" class="btn blue"><i class="glyphicon glyphicon-download-alt"></i>&nbsp; Download</a>';
            }

            tableRows[tableRowIndex].data.push({'text': download, 'isLatest':false, 'isDownload':true});
        }
        $scope.showCustomResponses = customRows.length;
        $scope.tableHeader = tableHeader;
        $scope.tableRows = tableRows;
        $scope.customRows = customRows;
        $scope.minDate = minDate;
        $scope.maxDate = maxDate;
    };

    $scope.clearComparison = function() {
        delete $scope.observations;
        $("#result-compare-select").val('');
        buildChart();
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
        var params = document.location.href.split('type=');
        $scope.surveyType = params.length === 2 ? params[1] : 'PROM';
        DocumentService.getByUserIdAndClass($scope.loggedInUser.id, 'YOUR_HEALTH_SURVEY')
            .then(function(documents) {
                $scope.documentDateMap = {};
                if (documents.length) {
                    for (var i = 0; i < documents.length; i++) {
                        $scope.documentDateMap[documents[i].date] = documents[i];
                    }
                }

                getSurveyResponses();
            }, function() {
                alert('Error retrieving documents');
            });
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
        var surveyResponseSelectOptions = [];
        for (var i = 0; i < $scope.surveyResponses.length; i++) {
            if ($scope.surveyResponses[i].id !== $scope.latestSurveyResponse.id) {
                surveyResponseSelectOptions.push({
                    'id': $scope.surveyResponses[i].id,
                    'date': $filter("date")($scope.surveyResponses[i].date, "dd-MMM-yyyy"),
                    'order': $scope.surveyResponses[i].date
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

        // set up question options, used for chart y axis labels, assumes 1 question group and same for all questions
        var scoreLabels = [];
        var firstOptions = $scope.surveyResponses[0].survey.questionGroups[0].questions[0].questionOptions;
        for (i = 0; i < firstOptions.length; i++) {
            scoreLabels[firstOptions[i].score] = firstOptions[i].text;
        }
        $scope.scoreLabels = scoreLabels;
        $scope.questions = _.sortBy($scope.surveyResponses[0].survey.questionGroups[0].questions, 'displayOrder');

        $scope.nonCustomQuestions = _.filter($scope.questions, {customQuestion:false});
        if ($scope.surveyType ==='POS_S') {
            //force last 2 questions to be labelled differently
            $scope.questions[$scope.questions.length-1].nonViewable = true;
            $scope.questions[$scope.questions.length-2].nonViewable = true;
        }
        // build table from visible responses (2 most recent) responses
        buildTable(visibleSurveyResponses);

        // build chart from all responses, using first question's type e.g. YSQ1
        $scope.questionType = $scope.nonCustomQuestions[0].type;
        buildChart();
    };

    $scope.viewGraph = function(type) {
        $scope.questionType = type;
        buildChart();
    };

    init();
}]);
