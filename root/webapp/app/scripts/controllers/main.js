'use strict';
angular.module('patientviewApp').controller('MainCtrl', ['$scope', '$http', '$modal', 'NewsService', 'ReviewService',
function ($scope, $http, $modal, NewsService, ReviewService) {

    $scope.init = function(){
        $scope.newsLoading = true;
        $scope.reviewsLoading = true;
        NewsService.getPublicNews(0, 5).then(function(page) {
            $scope.newsItems = page.content;
            $scope.newsLoading = false;
        }, function() {
            $scope.newsLoading = false;
        });

        ReviewService.getPublicReviews().then(function(content){
            $scope.reviews = content;
            $scope.reviewsLoading = false;
        }, function() {
            $scope.newsLoading = false;
        })
    };

    $scope.furtherInformation = function () {

        var modalInstance = $modal.open({
            templateUrl: 'furtherInformationModal.html',
            controller: FurtherInformationModalCtrl
        });

        modalInstance.result.then(function () {
        }, function () {
        });
    };

    $scope.viewNewsItem = function(news) {
        var modalInstance = $modal.open({
            templateUrl: 'views/modal/viewNewsModal.html',
            controller: ViewNewsModalInstanceCtrl,
            size: 'lg',
            resolve: {
                news: function() {
                    return news;
                },
                newsTypesArray: function() {
                    return [];
                }
            }
        });

    $scope.getNumber = function(num) {
        debugger;
        return new Array(num);
    }

        modalInstance.result.then(function () {
            // ok (not used)
        }, function () {
            // closed
        });
    };

    $scope.init();
}]);
