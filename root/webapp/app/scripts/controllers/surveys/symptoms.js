'use strict';

angular.module('patientviewApp').controller('SurveysSymptomsCtrl',['$scope', 'SurveyResponseService',
    function ($scope, SurveyResponseService) {

    var getSurveyResponses = function() {
        $scope.loading = true;
        SurveyResponseService.getByUserAndSurveyType($scope.loggedInUser.id, $scope.surveyType)
            .then(function(surveyResponses) {
                if (surveyResponses.length) {
                    $scope.surveyResponses = _.sortBy(surveyResponses, 'date').reverse();
                    initialiseChart();
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
        $scope.surveyType = 'UKRDC_SYMPTOMS';
        getSurveyResponses();
    };

    var initialiseChart = function() {
        // format latest survey response suitable for table view
        var latest = $scope.surveyResponses[0];
        var questionAnswers = _.sortBy(latest.questionAnswers, 'question.displayOrder');
        var tableRows = [];
        var tableHeader = [];
        var i;

        // header
        tableHeader.push('Your Symptoms');
        tableHeader.push(latest.date);

        // rows
        for (i = 0; i < questionAnswers.length; i++) {
            var questionAnswer = questionAnswers[i];

            if (tableRows[i] == undefined || tableRows[i] == null) {
                tableRows[i] = [];
                tableRows[i].push(questionAnswer.question.text);
            }

            tableRows[i].push(questionAnswer.questionOption.text);
        }

        $scope.tableHeader = tableHeader;
        $scope.tableRows = tableRows;
        console.log(tableHeader);
        console.log(tableRows);
    };

    init();
}]);
