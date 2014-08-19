'use strict';

angular.module('patientviewApp').controller('MainCtrl', ['$scope', 'NewsService',
function ($scope, NewsService) {

    $scope.init = function(){
        $scope.newsLoading = true;
        NewsService.getPublicNews(0, 5).then(function(page) {
            $scope.newsItems = page.content;
            $scope.newsLoading = false;
        }, function() {
            $scope.newsLoading = false;
        });
    };

    $scope.init();
}]);
