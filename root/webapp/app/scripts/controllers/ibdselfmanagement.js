'use strict';

angular.module('patientviewApp').controller('IbdSelfManagementCtrl', ['$scope', '$routeParams', '$location',
    'SurveyResponseService',
function ($scope, $routeParams, $location, SurveyResponseService) {

    $scope.cancel = function() {
        if (confirm("This will discard any changes you've made. Do you wish to continue?")) {
            getSurveyResponses();
        }
    };

    $scope.init = function() {
        $scope.loading = true;
        $scope.selfManagement = {};

        // set valid years 6 years in future
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
        SurveyResponseService.getByUserAndSurveyType($scope.loggedInUser.id, $scope.surveyType)
        .then(function(surveyResponses) {
            if (surveyResponses.length) {
                surveyResponses = _.sortBy(surveyResponses, 'date').reverse();

                // build survey response object based on hardcoded survey responses
            } else {
                // no existing survey responses, set defaults
                $scope.selfManagement.planOfCare = '1. Ensure that you are taking your medications regularly \n2. Ensure that you are taking the correct dose of medication, if you are not sure you should check with the IBD Nurse team \n3. \n4.';
            }
            $scope.loading = false;
        }, function() {
            alert('Error retrieving self management programme');
            $scope.loading = false;
        });
    };

    var save = function() {
        // create survey responses based on ui model
    };

    $scope.init();
}]);
