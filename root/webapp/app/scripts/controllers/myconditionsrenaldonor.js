'use strict';

angular.module('patientviewApp').controller('MyConditionsRenalDonorCtrl',['$scope', 'DonorPathwayService', '$modal',
    function ($scope, DonorPathwayService, $modal) {

    $scope.user = $scope.loggedInUser;
    $scope.state = {};
    $scope.loading = true;
    $scope.state.notesLoading = true;

    var init = function() {
        // Get the donor pathway
        DonorPathwayService.setUser($scope.loggedInUser);
        DonorPathwayService.getPathway('DONORPATHWAY')
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
                    $scope.state.reviewStatus = $scope.editStage.stageStatus;
                }
                $scope.state.readOnly = true;
                $scope.loading = false;
            });

        // Get donor pathway notes
        DonorPathwayService.getNotes('DONORVIEW')
            .then(function (notes) {
                $scope.notes = notes;
                $scope.state.notesLoading = false;
            }, function () {
                $scope.state.noteErrorMessage = 'Error retrieving notes';
            });
    };

    $scope.showPoint = function (point) {
        $scope.editStage = $scope.editStages[point];
    };

    $scope.getStageStatusColour = function (stageStatus) {
        return DonorPathwayService.getStageStatusColour(stageStatus);
    };

    $scope.getStageStatusTooltipText = function (stageStatus) {
        return DonorPathwayService.getStageStatusTooltipText(stageStatus);
    };

    $scope.getDate = function (ts) {
        return DonorPathwayService.getDate(ts);
    };

    $scope.openPointsModal = function () {
        // open modal and pass in required objects for use in modal scope
        var modalInstance = $modal.open({
            templateUrl: 'views/modal/donorPathwayPointsModal.html',
            controller: ConfirmModalInstanceCtrl,
            size: 'lg',
            backdrop: 'static'
        });
    };

    init();
}]);
