'use strict';

var FurtherInformationInstanceCtrl = ['$scope', '$modalInstance',
    function ($scope, $modalInstance) {
        $scope.close = function () {
            $modalInstance.dismiss('close');
        };
    }];

angular.module('patientviewApp').controller('MainCtrl', ['$scope', '$http', '$modal', 'NewsService',
function ($scope, $http, $modal, NewsService) {

    /*var request = $http({
        method: "get",
        url: "/api/news/public"
    });*/
    /*var url = 'http://10.0.2.2:8080/api/news/public';

    console.log("logging...............");
    $.support.cors = true;
    $.ajax({
        type : 'GET',
        url : url,
        async : true,
        contentType : "application/json",
        crossDomain : true,
        success : function(response, textStatus, jqXHR) {
            console.log("reached here");
            console.log(response);
        },
        error : function(jqXHR, textStatus, errorThrown) {
            console.log(errorThrown);
        }
    });*/

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
