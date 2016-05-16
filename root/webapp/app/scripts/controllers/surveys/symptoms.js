'use strict';

angular.module('patientviewApp').controller('SurveysSymptomsCtrl',['$scope', 'SurveyResponseService', '$filter',
    function ($scope, SurveyResponseService, $filter) {

    var buildTable = function(visibleResponses) {
        // format survey response suitable for table view
        var tableRows = [];
        var tableHeader = [];
        var i, j;

        // header left most column
        tableHeader.push({'text':'Your Symptoms'});

        for (i = 0; i < visibleResponses.length; i++) {
            var response = visibleResponses[i];
            var questionAnswers = _.sortBy(response.questionAnswers, 'question.displayOrder');

            // header other columns
            tableHeader.push({'text':$filter("date")(response.date, "dd-MMM-yyyy"), 'isLatest':response.isLatest});

            // rows
            for (j = 0; j < questionAnswers.length; j++) {
                var questionAnswer = questionAnswers[j];

                if (tableRows[j] == undefined || tableRows[j] == null) {
                    tableRows[j] = [];
                    tableRows[j].push({'text':questionAnswer.question.text});
                }

                tableRows[j].push({'text':questionAnswer.questionOption.text, 'isLatest':response.isLatest});
            }
        }


        $scope.tableHeader = tableHeader;
        $scope.tableRows = tableRows;
    };

    $scope.compareSurvey = function(id) {
        if (id !== undefined && id !== null) {
            var visibleSurveyResponses = [];
            visibleSurveyResponses.push(_.findWhere($scope.surveyResponses, {id: id}));
            visibleSurveyResponses.push($scope.latestSurveyResponse);
            buildTable(visibleSurveyResponses);
        }
    };

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
        $scope.surveyType = 'PROMS';
        getSurveyResponses();
    };

    var initialiseChart = function() {
        // set latest
        $scope.surveyResponses[0].isLatest = true;
        $scope.latestSurveyResponse = $scope.surveyResponses[0];

        // date options used in select when comparing to latest
        var surveyReponseSelectOptions = [];
        for (var i = 0; i < $scope.surveyResponses.length; i++) {
            if ($scope.surveyResponses[i].id !== $scope.latestSurveyResponse.id) {
                surveyReponseSelectOptions.push({
                    'id': $scope.surveyResponses[i].id,
                    'date': $filter("date")($scope.surveyResponses[i].date, "dd-MMM-yyyy")
                });
            }
        }
        $scope.surveyReponseSelectOptions = surveyReponseSelectOptions;

        // add latest to table
        var visibleSurveyResponses = [];
        if ($scope.surveyResponses[1] !== null && $scope.surveyResponses[1] !== undefined) {
            // add second latest if exists
            visibleSurveyResponses.push($scope.surveyResponses[1]);
        }
        visibleSurveyResponses.push($scope.latestSurveyResponse);
        buildTable(visibleSurveyResponses);
    };

    init();
}]);
