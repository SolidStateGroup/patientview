'use strict';
var ViewMyMediaModalInstanceCtrl = ['$scope', '$modalInstance', 'myMedia', 'message', 'UtilService',
    function ($scope, $modalInstance, myMedia, message, UtilService) {
        $scope.formatBytes = UtilService.formatBytes;

        if (typeof myMedia !== 'undefined') {
            $scope.media = myMedia;
        }

        if (typeof message.id !== 'undefined') {
            $scope.message = message;
        }

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }];
