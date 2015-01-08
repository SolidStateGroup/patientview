'use strict';

var FurtherInformationInstanceCtrl = ['$scope', '$modalInstance',
    function ($scope, $modalInstance) {
        $scope.close = function () {
            $modalInstance.dismiss('close');
        };
    }];

angular.module('patientviewApp').controller('MainCtrl', ['$scope', '$http', '$modal', 'NewsService',
function ($scope, $http, $modal, NewsService) {

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

    $scope.viewNewsItem = function(news) {
        var modalInstance = $modal.open({
            templateUrl: 'views/partials/viewNewsModal.html',
            controller: ViewNewsModalInstanceCtrl,
            size: 'lg',
            resolve: {
                news: function(){
                    return news;
                }
            }
        });

        modalInstance.result.then(function () {
            // ok (not used)
        }, function () {
            // closed
        });
    };

    $scope.init();
}]);
