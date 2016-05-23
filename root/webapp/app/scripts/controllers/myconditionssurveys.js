'use strict';

angular.module('patientviewApp').controller('MyConditionsSurveysCtrl',['$scope', 'SurveyResponseService',
    function ($scope, SurveyResponseService) {

    var init = function() {
        var types = ['PAMS', 'PROM', 'EQ5D'];
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

    init();
}]);
