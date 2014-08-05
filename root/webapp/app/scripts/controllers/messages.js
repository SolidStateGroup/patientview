'use strict';

// pagination following http://fdietz.github.io/recipes-with-angular-js/common-user-interface-patterns/paginating-through-server-side-data.html

angular.module('patientviewApp').controller('MessagesCtrl',['$scope', 'ConversationService', function ($scope, ConversationService) {

    $scope.itemsPerPage = 5;
    $scope.currentPage = 0;

    $scope.prevPage = function() {
        if ($scope.currentPage > 0) {
            $scope.currentPage--;
        }
    };

    $scope.prevPageDisabled = function() {
        return $scope.currentPage === 0 ? "disabled" : "";
    };

    $scope.nextPage = function() {
        if ($scope.currentPage < $scope.pageCount() - 1) {
            $scope.currentPage++;
        }
    };

    $scope.nextPageDisabled = function() {
        return $scope.currentPage === $scope.pageCount() - 1 ? "disabled" : "";
    };

    $scope.pageCount = function() {
        return Math.ceil($scope.total/$scope.itemsPerPage);
    };

    $scope.$watch("currentPage", function(newValue, oldValue) {
        $scope.pagedItems = ConversationService.getAll($scope.loggedInUser, newValue*$scope.itemsPerPage, $scope.itemsPerPage);
        //$scope.total = ConversationService.total();
    });

    $scope.init = function() {
        $scope.loading = true;


        //$scope.pagedItems = ConversationService.getAll($scope.loggedInUser, $scope.itemsPerPage, $scope.currentPage);

        $scope.loading = false;
    };

    $scope.init();
}]);
