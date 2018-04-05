'use strict';
var ViewMyMediaModalInstanceCtrl = ['$scope', '$modalInstance', 'myMedia',
    function ($scope, $modalInstance, myMedia) {
        $scope.media = myMedia;

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }];
