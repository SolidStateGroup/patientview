'use strict';

var SurveyResponseDetailNewModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'SurveyService',
    'SurveyResponseService', 'ObservationService', 'ObservationHeadingService', 'surveyType', 'UtilService',
function ($scope, $rootScope, $modalInstance, SurveyService, SurveyResponseService, ObservationService,
          ObservationHeadingService, surveyType, UtilService) {

    var init = function() {
        $scope.surveyResponse = {};
        $scope.initalQuestion = 0;
        $scope.currentQuestion = 0;
        $scope.saving = false;
        $scope.answers = {};
        $scope.customQuestions = [];
        $scope.acceptedTerms = false;
        $scope.days = UtilService.generateDays();
        $scope.months = UtilService.generateMonths();
        $scope.years = UtilService.generateYears2000();
        $scope.zoom = 1;
        $scope.date = {};
        $scope.slider = {
            options:{
                translate: function(value, sliderId, label) {
                    if (label === 'floor' || !value) {
                        return '0 - the worst health you can imagine';
                    } else if (label === 'ceil' || value === 100) {
                        return '100 - the best health you can imagine';
                    }
                    return value;
                },
                floor: 0,
                vertical:true,
                ceil: 100,
                step:1,
                precision:1,
                showTicks: 5,
            }
        };
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
            $scope.questionMap = {};
            $scope.questions = [];
            // create map of question id to question type, used when creating object to send to backend
            for (i = 0; i < survey.questionGroups.length; i++) {
                for (j = 0; j < survey.questionGroups[i].questions.length; j++) {
                    var question = survey.questionGroups[i].questions[j];
                    $scope.questionMap[question.id] = survey.questionGroups[i].questions[j];
                    if (question.htmlType === 'TEXT') {
                        $scope.questionTypeMap[question.id] = 'TEXT';
                    } else {
                        $scope.questionTypeMap[question.id] = question.elementType;
                    }
                    $scope.questionRequiredMap[question.id] = question.required;
                    if (question.htmlType === 'SLIDER') {
                        $scope.answers[question.id] = 50;
                    }
                    $scope.questions.push(question);
                }
            }

            $scope.questions = _.sortBy($scope.questions, 'displayOrder');
            $scope.question = $scope.questions[0];
            $scope.isFirstQuestion = $scope.currentQuestion === 0;
            $scope.isLastQuestion = $scope.currentQuestion === $scope.questions.length-1;

            // only certain survey types have hours & minutes
            if (['IBD_FATIQUE', 'POS_S', 'EQ5D5L'].indexOf($scope.survey.type) !== -1) {
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

            _.defer(function () {
                $('input[type="radio"]').change(function(e) {
                    var model = $(this).data('model');
                    if (model) {
                        $scope[$(this).data('model')][e.target.name] = e.target.value;
                    }
                })
            });
        }, function () {
            alert('error getting survey')
        });
    };

    $scope.nextQuestion = function(start, end) {
        $scope.currentQuestion++;
        $scope.isFirstQuestion = $scope.currentQuestion === 0;
        $scope.isLastQuestion = $scope.currentQuestion === $scope.questions.length-1;
        $scope.question = $scope.questions[$scope.currentQuestion];
    };

    $scope.previousQuestion = function(start, end) {
        $scope.currentQuestion--;
        $scope.isFirstQuestion = $scope.currentQuestion === 0;
        $scope.isLastQuestion = $scope.currentQuestion === $scope.questions.length-1;
        $scope.question = $scope.questions[$scope.currentQuestion];
    };

    $scope.resetSymptom = function(id) {
        delete $scope.customQuestions[id];
        delete $scope.answers[id];
    };

    $scope.toggleTerms = function () {
        $scope.acceptedTerms= !$scope.acceptedTerms;
    }
    $scope.cancel = function () {
        if (window.confirm("Your answers will not be saved. Are you sure you want to cancel?")) {
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

        $scope.saving = true;

        _.defer(function () {
            _.each($scope.customQuestions, function(q, i) {
                if ($scope.customQuestions[i] && !$scope.answers[i]) {
                    err = $scope.customQuestions[i];
                }
            })
            if (err){
                alert("Please enter a value for the other symptom labelled '" + err + "'");
                $scope.saving = false;
                $scope.$apply();
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

            var keys = Object.keys($scope.answers);
            for (i = 0; i < keys.length; i++) {
                var questionId = keys[i];
                var answer = $scope.answers[questionId];
                if (answer !== null && answer !== undefined) {
                    var questionAnswer = {};
                    if ($scope.questionTypeMap[questionId] === 'SINGLE_SELECT') {
                        questionAnswer.questionOption = {};
                        questionAnswer.questionOption.id = answer;
                    }
                    if ($scope.customQuestions[questionId]) {
                        questionAnswer.questionText = $scope.customQuestions[questionId];
                    }
                    if (['SINGLE_SELECT_RANGE','TEXT','TEXT_NUMERIC'].indexOf($scope.questionTypeMap[questionId]) > -1) {
                        questionAnswer.value = answer;
                    }
                    questionAnswer.question = {};
                    questionAnswer.question.id = questionId;
                    surveyResponse.questionAnswers.push(questionAnswer);

                    requiredMap[questionId] = false;
                }
            }

            var requiredList = []
            for (i = 0; i < requiredMap.length; i++) {
                if (requiredMap[i] !== null && requiredMap[i] !== undefined && requiredMap[i]) {
                    containsAllRequired = false;
                    requiredList.push($scope.questionMap[i]);
                }
            }

            if (containsAllRequired) {
                // save, with heart symptom scoring specific code
                SurveyResponseService.add(surveyResponse.user.id, surveyResponse).then(function () {
                    $scope.saving = false;
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
                $scope.saving = false;
                $scope.$apply();

                alert('Please enter a value for: \n' + _.map(requiredList, "text").join("\n"));
            }
        });

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
        $scope.saving = true;
        ObservationService.saveResultClusters($scope.loggedInUser.id, userResultClusters).then(function() {
            $scope.saving = false;
            $modalInstance.close();
        }, function () {
            $scope.saving = false;
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
    $scope.needsAnswer = function(question) {
        if (question.elementType !== 'SINGLE_SELECT') {
            return false;
        }

        return typeof $scope.answers[question.id] === 'undefined';
    };

    $scope.canSave = function() {
        return Object.keys($scope.answers).length;
    }

    $scope.getQuestionHeader = function () {
        if (!$scope.question) return '';
        var res = $scope.question.text.split('(');
        if (res.length === 2) {
            return '<b>' + res[0].toUpperCase() + '</b><i>(' + res[1] + '</i>';
        } else {
            return '<b>' + res[0].toUpperCase() + '</b>';
        }
    }

    init();
}];
