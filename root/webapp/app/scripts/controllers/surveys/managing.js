'use strict';

// PAM
// levels modal instance controller
var LevelsModalInstanceCtrl = ['$scope', '$modalInstance',
    function ($scope, $modalInstance) {
        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }];

angular.module('patientviewApp').controller('SurveysManagingCtrl',['$scope', '$filter', '$modal', 'SurveyService',
    'SurveyResponseService', 'DocumentService',
    function ($scope, $filter, $modal, SurveyService, SurveyResponseService, DocumentService) {

    var buildTable = function(visibleResponses) {
        if (!visibleResponses.length) {
            return;
        }

        // format survey response suitable for table view
        var tableRows = [];
        var tableHeader = [];
        var i, j;

        // header left most column
        tableHeader.push({'text':''});

        for (i = 0; i < visibleResponses.length; i++) {
            var response = visibleResponses[i];
            var questionAnswers = _.sortBy(response.questionAnswers, 'question.displayOrder');

            // score/level are special question type PAM_SCORE, may be in score element
            var score = '-';
            var level = '-';

            // header other columns
            var dateString = $scope.filterDate(response.date);
            tableHeader.push({'text':dateString, 'isLatest':response.isLatest});

            // rows
            var questions = $scope.questions;
            var questionAnswerMap = [];
            for (j = 0; j < questionAnswers.length; j++) {
                questionAnswerMap[questionAnswers[j].question.type] = questionAnswers[j];

                // handle specific question type for score (expect this to be in score element)
                if (questionAnswers[j].question.type == 'PAM_SCORE'
                    || questionAnswers[j].question.type == "PAM_13_LEVEL"
                    || questionAnswers[j].question.type == "PAM_13_CODE") {
                    score = questionAnswers[j].value;
                }
            }

            // check for scores
            if (response.surveyResponseScores != null
                && response.surveyResponseScores != undefined
                && response.surveyResponseScores.length) {
                for (j = 0; j < response.surveyResponseScores.length; j++) {
                    var surveyResponseScore = response.surveyResponseScores[j];
                    if (surveyResponseScore.type == 'PAM_SCORE'
                        || surveyResponseScore.type == "PAM_13_LEVEL"
                        || surveyResponseScore.type == "PAM_13_CODE") {
                        score = surveyResponseScore.score;
                        level = surveyResponseScore.level;
                    }
                }
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

            // special score row
            if (tableRows[questions.length] == undefined || tableRows[questions.length] == null) {
                tableRows[questions.length] = {};
                tableRows[questions.length].borderAbove = true;
                tableRows[questions.length].isScore = true;
                tableRows[questions.length].data = [];
                tableRows[questions.length].data.push({'text':'Score', 'isScore':true});
            }

            tableRows[questions.length].data.push({'text': score, 'isLatest':response.isLatest, 'isScore':true});

            // special level row
            if (tableRows[questions.length + 1] == undefined || tableRows[questions.length + 1] == null) {
                tableRows[questions.length + 1] = {};
                tableRows[questions.length + 1].isScore = true;
                tableRows[questions.length + 1].data = [];
                tableRows[questions.length + 1].data.push({'text':'Level', 'isScore':true});
            }

            tableRows[questions.length + 1].data.push({'text': level, 'isLatest':response.isLatest, 'isScore':true});

            // special download row
            if (tableRows[questions.length + 2] == undefined || tableRows[questions.length + 2] == null) {
                tableRows[questions.length + 2] = {};
                tableRows[questions.length + 2].isDownload = true;
                tableRows[questions.length + 2].data = [];
                tableRows[questions.length + 2].data.push({'text':'', 'isDownload':true});
            }

            var download = '';
            debugger;
            if ($scope.documentDateMap[response.date]) {
                download = '<a href="../api/user/' + $scope.loggedInUser.id +
                    '/file/' + $scope.documentDateMap[response.date].fileDataId + '/download' +
                    '?token=' + $scope.authToken
                    + '" class="btn blue"><i class="glyphicon glyphicon-download-alt"></i>&nbsp; Download</a>';
            }

            tableRows[questions.length + 2].data.push({'text': download, 'isLatest':false, 'isDownload':true});

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

        var i, j;

        // generate date options used in select when comparing responses to latest
        var surveyResponseSelectOptions = [];
        for (i = 0; i < $scope.surveyResponses.length; i++) {
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

        // if second group of questions exist store in array to be used for PAM score and PAM level
        if ($scope.survey.questionGroups[1] != undefined
            && $scope.survey.questionGroups[1] != null
            && $scope.survey.questionGroups[1].questions.length) {
            var otherQuestions = [];
            for (i = 0; i < $scope.survey.questionGroups[1].questions.length; i++) {
                var question = $scope.survey.questionGroups[1].questions[i];
                for (j = 0; j < $scope.latestSurveyResponse.questionAnswers.length; j++) {
                    if ($scope.latestSurveyResponse.questionAnswers[j].question.type == question.type) {
                        otherQuestions[question.type] = $scope.latestSurveyResponse.questionAnswers[j].value;
                    }
                }
            }
            $scope.otherQuestions = otherQuestions;
        }

        // set score and level if present from latest response
        if ($scope.latestSurveyResponse.surveyResponseScores != null
            && $scope.latestSurveyResponse.surveyResponseScores != undefined
            && $scope.latestSurveyResponse.surveyResponseScores.length) {
            for (i = 0; i < $scope.latestSurveyResponse.surveyResponseScores.length; i++) {
                var surveyResponseScore = $scope.latestSurveyResponse.surveyResponseScores[i];
                if (surveyResponseScore.type == 'PAM_SCORE'
                    || surveyResponseScore.type == "PAM_13_LEVEL"
                    || surveyResponseScore.type == "PAM_13_CODE") {
                    $scope.latestScore = surveyResponseScore.score;
                    $scope.latestLevel = surveyResponseScore.level;
                }
            }
        }

    };

    $scope.showLevels = function() {
        var modalInstance = $modal.open({
            templateUrl: 'views/modal/levelsModal.html',
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
