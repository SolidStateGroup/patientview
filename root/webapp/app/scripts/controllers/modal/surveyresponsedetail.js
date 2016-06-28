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
