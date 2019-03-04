'use strict';

angular.module('patientviewApp').controller('MyConditionsSurveysCtrl',['$scope', 'SurveyResponseService', '$modal', 'SurveyService', 'UtilService',
    function ($scope, SurveyResponseService, $modal, SurveyService, UtilService) {

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
        return true
        return !$scope.foundSurveys || !$scope.foundSurveys[surveyType] || moment($scope.foundSurveys[surveyType].date).add(1, 'y').isBefore(moment());
    }

    $scope.openModalEnterSurveyResponses = function (surveyType) {
        $scope.surveyType = surveyType;
        // open modal and pass in required objects for use in modal scope
        var modalInstance = $modal.open({
            templateUrl: surveyType === 'POS_S'? 'views/partials/pos-survey.html' : 'views/partials/eq-survey.html',
            controller: SurveyResponseDetailNewModalInstanceCtrl,
            size: 'lg',
            backdrop: 'static',
            resolve: {
                SurveyService: function(){
                    return SurveyService;
                },
                SurveyResponseService: function(){
                    return SurveyResponseService;
                },
                surveyType: function(){
                    return $scope.surveyType;
                },
                UtilService: function(){
                    return UtilService;
                }
            }
        });

        // handle modal close (via button click)
        modalInstance.result.then(function () {
            init();
        }, function () {
            // close button, do nothing
        });
    };

    init();
}]);
