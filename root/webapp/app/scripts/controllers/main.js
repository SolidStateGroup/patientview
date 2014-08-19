'use strict';

var FurtherInformationInstanceCtrl = ['$scope', '$modalInstance',
    function ($scope, $modalInstance) {
        $scope.close = function () {
            $modalInstance.dismiss('close');
        };
    }];

angular.module('patientviewApp').controller('MainCtrl', ['$scope', '$modal', 'NewsService',
function ($scope, $modal, NewsService) {

    $scope.init = function(){
        $scope.newsLoading = true;
        NewsService.getPublicNews(0, 5).then(function(page) {
            $scope.newsItems = page.content;
            $scope.newsLoading = false;
        }, function() {
            $scope.newsLoading = false;
        });
    };

    $scope.furtherInformation = function () {

        var modalInstance = $modal.open({
            templateUrl: 'furtherInformationModal.html',
            controller: FurtherInformationInstanceCtrl
        });

        modalInstance.result.then(function () {
        }, function () {
        });
    };

    $scope.init();
}]);
