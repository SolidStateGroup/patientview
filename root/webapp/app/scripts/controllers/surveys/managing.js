'use strict';

// PAM
// levels modal instance controller
var LevelsModalInstanceCtrl = ['$scope', '$modalInstance',
    function ($scope, $modalInstance) {
        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }];

angular.module('patientviewApp').controller('SurveysManagingCtrl',['$scope', '$filter', '$modal', 'SurveyService', 'SurveyResponseService',
    function ($scope, $filter, $modal, SurveyService, SurveyResponseService) {

    var buildTable = function(visibleResponses) {
        if (!visibleResponses.length) {
            return;
        }

        // format survey response suitable for table view
        var tableRows = [];
        var tableHeader = [];
        var i, j;

        // header left most column
        tableHeader.push({'text':'Managing Your Health'});

        for (i = 0; i < visibleResponses.length; i++) {
            var response = visibleResponses[i];
            var questionAnswers = _.sortBy(response.questionAnswers, 'question.displayOrder');

            // header other columns
            var dateString = $scope.filterDate(response.date);
            tableHeader.push({'text':dateString, 'isLatest':response.isLatest});

            // rows
            var questions = $scope.questions;
            var questionAnswerMap = [];
            for (j = 0; j < questionAnswers.length; j++) {
                questionAnswerMap[questionAnswers[j].question.type] = questionAnswers[j];
            }

            for (j = 0; j < questions.length; j++) {
                var questionText = questions[j].text;
                var questionType = questions[j].type;
                var questionOptionText = '-';

                if (questionAnswerMap[questionType]) {
                    questionOptionText = questionAnswerMap[questionType].questionOption.text;
                }

                // set question text, e.g. Pain
                if (tableRows[j] == undefined || tableRows[j] == null) {
                    tableRows[j] = {};
                    tableRows[j].type = questionType;
                    tableRows[j].data = [];
                    tableRows[j].data.push({'text':questionText});
                }

                // set response text, e.g. Moderately
                tableRows[j].data.push({'text':questionOptionText, 'isLatest':response.isLatest});
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

    $scope.filterDate = function(date) {
        return $filter("date")(date, "dd-MMM-yyyy");
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
        $scope.surveyType = 'PAM';
        $scope.loading = true;
        delete $scope.errorMessage;

        SurveyService.getByType($scope.surveyType).then(function(survey) {
            if (survey != null) {
                $scope.survey = survey;
                $scope.questions = survey.questionGroups[0].questions;
                getSurveyResponses();
            } else {
                $scope.errorMessage = 'Error retrieving surveys';
                $scope.loading = false;
            }
        }, function () {
            $scope.errorMessage = 'Error retrieving survey';
            $scope.loading = false;
        });
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

        var scoreLabels = [];
        var firstOptions = $scope.survey.questionGroups[0].questions[0].questionOptions;
        for (i = 0; i < firstOptions.length; i++) {
            scoreLabels[firstOptions[i].score] = firstOptions[i].text;
        }
        $scope.scoreLabels = scoreLabels;
        $scope.questions = $scope.survey.questionGroups[0].questions;

        // build table from visible responses (2 most recent) responses
        buildTable(visibleSurveyResponses);
    };

    $scope.showLevels = function() {
        var modalInstance = $modal.open({
            templateUrl: 'views/partials/levelsModal.html',
            controller: LevelsModalInstanceCtrl,
            size: 'lg'
        });

        modalInstance.result.then(function () {
            // ok (not used)
        }, function () {
            init();
        });
    };

    init();
}]);
