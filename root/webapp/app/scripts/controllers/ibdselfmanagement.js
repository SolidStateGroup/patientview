'use strict';

angular.module('patientviewApp').controller('IbdSelfManagementCtrl', ['$scope', '$rootScope', 'SurveyService',
    'SurveyResponseService',
function ($scope, $rootScope, SurveyService, SurveyResponseService) {

    $scope.cancel = function() {
        if (confirm("This will discard any changes you've made. Do you wish to continue?")) {
            getSurveyResponses();
        }
    };

    var getSurvey = function() {
        $scope.loadingMessage = 'Loading Self-Management Programme';
        $scope.loading = true;
        var i, j;

        SurveyService.getByType($scope.surveyType).then(function(survey) {
            $scope.survey = survey;
            $scope.questions = [];

            // create map of question id to question type, used when creating object to send to backend
            for (i = 0; i < survey.questionGroups.length; i++) {
                for (j = 0; j < survey.questionGroups[i].questions.length; j++) {
                    $scope.questions.push(survey.questionGroups[i].questions[j]);
                }
            }
        }, function () {
            alert('error getting self-management programme details')
        });
    };

    var getSurveyResponses = function() {
        $scope.loadingMessage = 'Loading Self-Management Programme';
        $scope.loading = true;
        SurveyResponseService.getByUserAndSurveyType($scope.loggedInUser.id, $scope.surveyType)
            .then(function(surveyResponses) {
            if (surveyResponses.length) {
                surveyResponses = _.sortBy(surveyResponses, 'date').reverse();
                var response = surveyResponses[0];
                $scope.selfManagement.date = response.date;

                for (var i=0; i<response.questionAnswers.length; i++) {
                    var answer = response.questionAnswers[i];
                    $scope.selfManagement[answer.question.type] = answer.value;
                }

                $scope.selfManagement.staffUser = response.staffUser;
            } else {
                // no existing survey responses, set defaults
                $scope.selfManagement['IBD_SELF_MANAGEMENT_PLAN_OF_CARE'] = '1. Ensure that you are taking your medications regularly \n2. Ensure that you are taking the correct dose of medication, if you are not sure you should check with the IBD Nurse team \n3. \n4.';
            }
            $scope.loading = false;
        }, function() {
            alert('Error retrieving self management programme');
            $scope.loading = false;
        });
    };

    $scope.init = function() {
        $scope.selfManagement = [];
        delete $scope.successMessage;

        // check if viewing as patient
        $scope.isStaff = $rootScope.previousLoggedInUser ? true : false;

        // set valid years 6 years in future
        var date = new Date();
        $scope.validYears = [];

        for (var i = date.getFullYear(); i<(date.getFullYear() + 6); i++) {
            $scope.validYears.push(i);
        }

        $scope.surveyType = 'IBD_SELF_MANAGEMENT';
        getSurvey();
        getSurveyResponses();
    };

    $scope.save = function() {
        // create survey responses based on ui model (similar to surveyresponses.js)
        delete $scope.successMessage;
        $scope.loadingMessage = 'Saving Self-Management Programme';
        $scope.loading = true;
        var i;

        // build object to send to back end
        var surveyResponse = {};
        surveyResponse.user = {};
        surveyResponse.user.id = $scope.loggedInUser.id;
        surveyResponse.staffToken = $rootScope.previousAuthToken;
        surveyResponse.survey = {};
        surveyResponse.survey.id = $scope.survey.id;
        surveyResponse.questionAnswers = [];
        surveyResponse.date = new Date();

        for (i = 0; i < $scope.questions.length; i++) {
            var type = $scope.questions[i].type;

            if ($scope.selfManagement[type] != null && $scope.selfManagement[type] != undefined
                && $scope.selfManagement[type].length) {
                var questionAnswer = {};
                questionAnswer.value = $scope.selfManagement[type];
                questionAnswer.question = {};
                questionAnswer.question.id = $scope.questions[i].id;
                surveyResponse.questionAnswers.push(questionAnswer);
            }
        }

        SurveyResponseService.add(surveyResponse.user.id, surveyResponse).then(function () {
            $scope.successMessage = 'Saved updated self-management programme';
            getSurveyResponses();
        }, function () {
            $scope.loading = false;
            alert('There was an error saving');
        });
    };

    $scope.init();
}]);
