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

    $scope.getReviewStatusColour = function () {
        var colour;
        switch ($scope.stages.Review.stageStatus) {
            case 'PENDING':
            case 'STARTED':
            default:
                colour = 'green';
                break;

            case 'ON_HOLD':
                colour = '#ffbf00';
                break;

            case 'STOPPED':
                colour = 'red';
                break;

            case 'COMPLETED':
                colour = 'gray';
                break;
        }
        return {'background-color': colour};
    };

    $scope.getDate = function (ts) {
        return moment(ts).format('DD/MM/YYYY');
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
