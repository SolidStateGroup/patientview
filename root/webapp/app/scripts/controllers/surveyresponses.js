'use strict';

var SurveyResponseDetailModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'SurveyResponseService', 'surveyResponseId',
function ($scope, $rootScope, $modalInstance, SurveyResponseService, surveyResponseId) {

    var init = function() {
        $scope.loading = true;
        delete $scope.surveyResponse;
        $scope.errorMessage = '';

        if (surveyResponseId == null) {
            $scope.errorMessage = 'Error retrieving survey response';
            $scope.loading = false;
            return;
        }

        SurveyResponseService.getSurveyResponse($scope.loggedInUser.id, surveyResponseId).then(function(surveyResponse) {
            $scope.surveyResponse = surveyResponse;

            // create map of question answers to question ids
            var responseMap = [];

            for (var i = 0; i < surveyResponse.questionAnswers.length; i++) {
                var questionAnswer = surveyResponse.questionAnswers[i];
                responseMap[questionAnswer.question.id] = questionAnswer;
            }

            $scope.responseMap = responseMap;

            $scope.loading = false;
        }, function () {
            $scope.errorMessage = 'Error retrieving survey response';
            $scope.loading = false;
        });
    };

    $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
    };

    init();
}];

var SurveyResponseDetailsNewModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'SurveyService',
    'SurveyResponseService', 'surveyType', 'UtilService',
function ($scope, $rootScope, $modalInstance, SurveyService, SurveyResponseService, surveyType, UtilService) {

    var init = function() {
        $scope.surveyResponse = {};
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
        var surveyResponse = {};
        surveyResponse.user = {};
        surveyResponse.user.id = $scope.loggedInUser.id;
        surveyResponse.survey = {};
        surveyResponse.survey.id = $scope.survey.id;
        surveyResponse.questionAnswers = [];
        surveyResponse.date = new Date($scope.date.year, $scope.date.month - 1, $scope.date.day);

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
                surveyResponse.questionAnswers.push(questionAnswer);
            }
        }

        SurveyResponseService.add(surveyResponse.user.id, surveyResponse).then(function() {
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
