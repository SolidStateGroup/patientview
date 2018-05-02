'use strict';
var ViewMyMediaModalInstanceCtrl = ['$scope', '$modalInstance', 'myMedia', 'message',
    function ($scope, $modalInstance, myMedia, message) {
        debugger;
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
