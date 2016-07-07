'use strict';
var ViewNewsModalInstanceCtrl = ['$scope', '$modalInstance', 'news',
    function ($scope, $modalInstance, news) {
        $scope.news = news;
        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }];
