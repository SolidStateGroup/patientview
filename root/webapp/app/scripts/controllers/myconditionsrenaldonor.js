'use strict';

angular.module('patientviewApp').controller('MyConditionsRenalDonorCtrl',['$scope', 'DonorPathwayService',
    function ($scope, DonorPathwayService) {

    var init = function() {
        $scope.loading = true;

        // Get the donor pathway
        DonorPathwayService.setUserId($scope.loggedInUser.id);
        return DonorPathwayService.getPathway('DONORPATHWAY')
            .then(function (pathway) {
                if (!pathway) {
                    $scope.fatalErrorMessage = 'Error retrieving pathway';
                    return;
                }
                $scope.stages = pathway.stages;
                $scope.editStages = $scope.stages;
                if ($scope.stages.Consultation.stageStatus != 'COMPLETED') {
                    $scope.editStage = $scope.editStages.Consultation;
                } else if ($scope.stages.Testing.stageStatus != 'COMPLETED') {
                    $scope.editStage = $scope.editStages.Testing;
                } else {
                    $scope.editStage = $scope.editStages.Review;
                    $scope.reviewStatus = $scope.editStage.stageStatus;
                }
                $scope.readOnly = true;
                $scope.loading = false;
            });
    };

    $scope.showPoint = function (point) {
        $scope.editStage = $scope.editStages[point];
    };

    init();
}]);
