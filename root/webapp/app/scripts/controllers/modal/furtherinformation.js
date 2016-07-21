'use strict';
var FurtherInformationModalCtrl = ['$scope', '$modalInstance',
    function ($scope, $modalInstance) {
        $scope.close = function () {
            $modalInstance.dismiss('close');
        };
    }];
