'use strict';
var ViewMyMediaModalInstanceCtrl = ['$scope', '$modalInstance', 'myMedia',
    function ($scope, $modalInstance, myMedia) {
        $scope.media = myMedia;
        $scope.mediaUrl = $scope.apiEndpoint +''+ myMedia.path +'?token=' + $scope.authToken;

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }];
