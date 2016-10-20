'use strict';
var DeleteResultModalInstanceCtrl = ['$scope', '$modalInstance', 'observationHeading', "observation", 'ObservationService',
    function ($scope, $modalInstance, observationHeading, observation, ObservationService) {
        $scope.observationHeading = observationHeading;
        $scope.observation = observation;

        $scope.ok = function () {
            ObservationService.remove($scope.loggedInUser.id, observation).then(function () {
                $modalInstance.close();
            });
        };
        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }];
