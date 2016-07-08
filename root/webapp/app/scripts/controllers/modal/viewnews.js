'use strict';
var ViewNewsModalInstanceCtrl = ['$scope', '$modalInstance', 'news', 'newsTypesArray',
    function ($scope, $modalInstance, news, newsTypesArray) {
        $scope.news = news;
        $scope.newsTypesArray = newsTypesArray;

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }];
