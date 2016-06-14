'use strict';

var ObservationHeadingInfoModalInstanceCtrl = ['$scope','$modalInstance','result',
    function ($scope, $modalInstance, result) {
        $scope.result = result;
        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }];
