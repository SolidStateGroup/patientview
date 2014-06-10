'use strict';

angular.module('patientviewApp').controller('LettersCtrl',['$scope','$timeout', 'CodeService', function ($scope,$timeout,CodeService) {

    // Init
    $scope.init = function () {
        $scope.loading = true;
        $scope.group = {'id':'1234','name':'Example Group'};
        CodeService.getGroupCodes('12345').then(function(data) {
            $scope.editing = false;
            $scope.list = data;
            $scope.currentPage = 1; //current page
            $scope.entryLimit = 2; //max no of items to display in a page
            $scope.filteredItems = $scope.list.length; //Initially for no filter
            $scope.totalItems = $scope.list.length;
            delete $scope.loading;
        });

    };

    $scope.setPage = function(pageNo) {
        $scope.currentPage = pageNo;
    };

    $scope.filter = function() {
        $timeout(function() {
            $scope.filteredItems = $scope.filtered.length;
        }, 10);
    };

    $scope.sortBy = function(predicate) {
        $scope.predicate = predicate;
        $scope.reverse = !$scope.reverse;
    };

    $scope.save = function (code) {
        console.log(code);
    };

    $scope.init();
}]);
