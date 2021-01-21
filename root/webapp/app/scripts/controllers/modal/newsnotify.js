'use strict';
var NewsNotifyModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'newsId', 'NewsService',
    function ($scope, $rootScope, $modalInstance, newsId, NewsService) {

        $scope.modalLoading = true;
        $scope.newsId = newsId;

        $scope.ok = function () {
            NewsService.notify($scope.newsId).then(function () {
                $modalInstance.close();
            }, function (result) {
                if (result.data) {
                    $scope.errorMessage = ' - ' + result.data;
                } else {
                    $scope.errorMessage = ' ';
                }
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }];
