'use strict';

var SurveyResponseDetailNewModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'SurveyService',
    'SurveyResponseService', 'ObservationService', 'ObservationHeadingService', 'surveyType', 'UtilService',
function ($scope, $rootScope, $modalInstance, SurveyService, SurveyResponseService, ObservationService,
          ObservationHeadingService, surveyType, UtilService) {

    var init = function() {
        $scope.surveyResponse = {};
        $scope.initalQuestion = 0;
        $scope.answers = [];
        $scope.customQuestions = [];
        $scope.acceptedTerms = false;
        $scope.days = UtilService.generateDays();
        $scope.months = UtilService.generateMonths();
        $scope.years = UtilService.generateYears2000();
        $scope.zoom = 1;
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
            $scope.questionRequiredMap = [];
            // create map of question id to question type, used when creating object to send to backend
            for (i = 0; i < survey.questionGroups.length; i++) {
                for (j = 0; j < survey.questionGroups[i].questions.length; j++) {
                    var question = survey.questionGroups[i].questions[j];
                    if (question.htmlType === 'TEXT') {
                        $scope.questionTypeMap[question.id] = 'TEXT';
                    } else {
                        $scope.questionTypeMap[question.id] = question.elementType;
                    }
                    $scope.questionRequiredMap[question.id] = question.required;
                    if (question.htmlType === 'SLIDER') {
                        $scope.answers[question.id] = 0;
                    }
                }
            }

            // only certain survey types have hours & minutes
            if ($scope.survey.type === 'IBD_FATIGUE') {
                $scope.hours = UtilService.generateHours();
                $scope.minutes = UtilService.generateMinutes();
                for (i = 0; i < $scope.hours.length; i++) {
                    if (parseInt($scope.hours[i]) === currentDate.getHours()) {
                        $scope.date.hour = $scope.hours[i];
                    }
                }
                for (i = 0; i < $scope.minutes.length; i++) {
                    if (parseInt($scope.minutes[i]) === currentDate.getMinutes()) {
                        $scope.date.minute = $scope.minutes[i];
                    }
                }
            }
        }, function () {
            alert('error getting survey')
        });
    };

    $scope.nextQuestion = function(start, end) {
        $scope.currentQuestion++;
    };

    $scope.previousQuestion = function(start, end) {
        $scope.currentQuestion--;
    };
    $scope.resetSymptom = function(id) {
        delete $scope.customQuestions[id];
        delete $scope.answers[id];
    };

    $scope.toggleTerms = function () {
        $scope.acceptedTerms= !$scope.acceptedTerms;
    }
    $scope.cancel = function () {
        if (window.confirm("Do you really want to cancel?")) {
            if (!$scope.showEnterResults) {
                $modalInstance.dismiss('cancel');
            } else {
                $modalInstance.close();
            }
        }
    };

    $scope.save = function () {
        var i;

        var err = false;
        _.each($scope.customQuestions, (q, i)=>{
            if ($scope.customQuestions[i] && !$scope.answers[i]) {
                err = $scope.customQuestions[i];
            }
        })
        if (err){
            alert("Please enter a value for the other symptom labelled '" + err + "'");
            return
        }

        // build object to send to back end
        var surveyResponse = {};
        surveyResponse.user = {};
        surveyResponse.user.id = $scope.loggedInUser.id;
        surveyResponse.survey = {};
        surveyResponse.survey.id = $scope.survey.id;
        surveyResponse.questionAnswers = [];
        if (!$scope.date.hour) {
            surveyResponse.date = new Date($scope.date.year, $scope.date.month - 1, $scope.date.day);
        } else {
            surveyResponse.date = new Date($scope.date.year, $scope.date.month - 1,
                $scope.date.day, $scope.date.hour, $scope.date.minute, 0, 0);
        }

        var requiredMap = $scope.questionRequiredMap.slice();
        var containsAllRequired = true;

        for (i = 0; i < $scope.answers.length; i++) {
            var answer = $scope.answers[i];
            if (answer !== null && answer !== undefined) {
                var questionAnswer = {};
                if ($scope.questionTypeMap[i] === 'SINGLE_SELECT') {
                    questionAnswer.questionOption = {};
                    questionAnswer.questionOption.id = answer;
                }
                if ($scope.customQuestions[i]) {
                    questionAnswer.questionText = $scope.customQuestions[i];
                }
                if (['SINGLE_SELECT_RANGE','TEXT','TEXT_NUMERIC'].indexOf($scope.questionTypeMap[i]) > -1) {
                    questionAnswer.value = answer;
                }
                questionAnswer.question = {};
                questionAnswer.question.id = i;
                surveyResponse.questionAnswers.push(questionAnswer);

                requiredMap[i] = false;
            }
        }

        for (i = 0; i < requiredMap.length; i++) {
            if (requiredMap[i] !== null && requiredMap[i] !== undefined && requiredMap[i] && containsAllRequired) {
                containsAllRequired = false;
            }
        }

        if (containsAllRequired) {
            // save, with heart symptom scoring specific code
            SurveyResponseService.add(surveyResponse.user.id, surveyResponse).then(function () {
                if (surveyType === 'HEART_SYMPTOM_SCORE') {
                    $scope.enterResults = {};
                    $scope.showEnterResults = true;

                    ObservationHeadingService.getByCode('weight').then(function (observationHeading) {
                        $scope.weightHeading = observationHeading;
                    });

                    ObservationHeadingService.getByCode('pulse').then(function (observationHeading) {
                        $scope.pulseHeading = observationHeading;
                    });

                } else {
                    $modalInstance.close();
                }
            }, function (error) {
                $scope.errorMessage = error.data;
            });
        } else {
            alert('Please complete all required questions.');
        }
    };

    $scope.increaseFontSize = function() {
        $scope.zoom = Math.min(2, $scope.zoom + 0.1);
    }

    $scope.decreaseFontSize = function() {
        $scope.zoom = Math.max(1, $scope.zoom - 0.1)
    }

    $scope.saveResults = function () {
        var userResultClusters = [];
        var resultCluster = {};
        var date = new Date();
        resultCluster.minute = date.getMinutes();
        resultCluster.hour = date.getHours();
        resultCluster.day = date.getDate();
        resultCluster.month = date.getMonth() + 1;
        resultCluster.year = date.getFullYear();
        resultCluster.values = [];

        if ($scope.weightHeading) {
            resultCluster.values[$scope.weightHeading.id] = $scope.enterResults.weight;
        }

        if ($scope.pulseHeading) {
            resultCluster.values[$scope.pulseHeading.id] = $scope.enterResults.pulse;
        }

        userResultClusters.push(resultCluster);

        // generate result clusters to store similarly to enter own results
        ObservationService.saveResultClusters($scope.loggedInUser.id, userResultClusters).then(function() {
            $modalInstance.close();
        }, function () {
            alert('Cannot save your results');
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
