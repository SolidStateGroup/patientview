'use strict';

angular.module('patientviewApp').controller('IbdSelfManagementCtrl', ['$scope', '$routeParams', '$location',
    'SurveyResponseService',
function ($scope, $routeParams, $location, SurveyResponseService) {

    $scope.init = function() {
        $scope.loading = true;
        var date = new Date();
        $scope.validYears = [];

        for (var i = date.getFullYear(); i<(date.getFullYear() + 6); i++) {
            $scope.validYears.push(i);
        }

        $scope.surveyType = 'IBD_SELF_MANAGEMENT';
        getSurveyResponses();
    };

    var getSurveyResponses = function() {
        $scope.loading = true;
        $scope.chartLoading = true;
        SurveyResponseService.getByUserAndSurveyType($scope.loggedInUser.id, $scope.surveyType)
        .then(function(surveyResponses) {
            if (surveyResponses.length) {
                $scope.surveyResponses = _.sortBy(surveyResponses, 'date').reverse();
            } else {
                delete $scope.surveyResponses;
            }
            $scope.loading = false;
        }, function() {
            alert('Error retrieving self management programme');
            $scope.loading = false;
        });
    };


    $scope.init();
}]);
