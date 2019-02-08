'use strict';

angular.module('patientviewApp').controller('MyConditionsSurveysCtrl',['$scope', 'SurveyResponseService',
    function ($scope, SurveyResponseService) {

    var init = function() {
        var types = ['PAM', 'PROM', 'EQ5D', 'EQ5D5L', 'POS_S'];
        var foundSurveys = [];

        SurveyResponseService.getLatestByUserAndSurveyType($scope.loggedInUser.id, types)
            .then(function(surveyResponses) {
                for (var i = 0; i < types.length; i++) {
                    for (var j = 0; j < surveyResponses.length; j++) {
                        if (types[i] == surveyResponses[j].survey.type) {
                            foundSurveys[types[i]] = surveyResponses[j];
                        }
                    }
                }

                $scope.foundSurveys = foundSurveys;
                $scope.loading = false;
            }, function() {
                alert('Error retrieving responses');
                $scope.loading = false;
            });
    };

    $scope.canEnterSurveyResponses = function (surveyType) {
        return !$scope.foundSurveys || !$scope.foundSurveys[surveyType] || moment($scope.foundSurveys[surveyType].date).add(1, 'y').isBefore(moment());
    }

    init();
}]);
