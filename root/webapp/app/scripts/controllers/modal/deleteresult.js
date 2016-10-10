'use strict';
var DeleteResultModalInstanceCtrl = ['$scope','$modalInstance','result', 'observation',
    function ($scope, $modalInstance, result, observation) {
        $scope.result = result;
        $scope.observation = observation;

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }];
