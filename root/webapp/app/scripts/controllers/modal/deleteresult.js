'use strict';
var DeleteResultModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'observationHeading', "observation", 'ObservationService',
    function ($scope, $rootScope, $modalInstance, observationHeading, observation, ObservationService) {
        $scope.observationHeading = observationHeading;
        $scope.observation = observation;

        var previouseUserId = -1;
        if($rootScope.previousLoggedInUser){
            previouseUserId = $rootScope.previousLoggedInUser.id;
        }
        $scope.ok = function () {
            ObservationService.remove(previouseUserId, $scope.loggedInUser.id, observation).then(function () {
                $modalInstance.close();
            });
        };
        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }];
