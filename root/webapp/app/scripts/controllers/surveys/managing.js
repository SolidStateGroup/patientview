'use strict';

// PAMS
angular.module('patientviewApp').controller('SurveysManagingCtrl',['$scope', 'SurveyResponseService',
    function ($scope, SurveyResponseService) {

    var getSurveyResponses = function() {
        $scope.loading = true;
        SurveyResponseService.getByUserAndSurveyType($scope.loggedInUser.id, $scope.surveyType)
            .then(function(surveyResponses) {
                if (surveyResponses.length) {
                    $scope.surveyResponses = _.sortBy(surveyResponses, 'date').reverse();
                    //$scope.initialiseChart();
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
        $scope.surveyType = 'PAMS';
        $scope.loading = true;
        getSurveyResponses();
    };

    init();
}]);
