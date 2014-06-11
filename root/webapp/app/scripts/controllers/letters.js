'use strict';

angular.module('patientviewApp').controller('LettersCtrl',['$scope','$timeout', 'CodeService', function ($scope,$timeout,CodeService) {

    // Init
    $scope.init = function () {
        $scope.loading = true;
        $scope.group = {'id':'1234','name':'Example Group'};
        CodeService.getGroupCodes($scope.group.id).then(function(data) {
            $scope.editing = false;
            $scope.list = data;
            $scope.currentPage = 1; //current page
            $scope.entryLimit = 2; //max no of items to display in a page
            $scope.filteredItems = $scope.list.length; //Initially for no filter
            $scope.totalItems = $scope.list.length;
            delete $scope.loading;
        });
    };

    $scope.opened = function (code, index) {
        $scope.editcode = _.clone(code);
    };

    $scope.add = function (isValid, form, code) {
        if(isValid) {

            CodeService.post($scope.group, code).then(function(addedCode) {
                $scope.list.push(addedCode);
                $scope.newCode = '';
                $scope.addCodeForm.$setPristine(true);
                $scope.totalItems = $scope.list.length;
            }, function() {

            });
        }
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

    // Save
    $scope.save = function (editCodeForm, code, index) {
        CodeService.save(code).then(function() {
            editCodeForm.$setPristine(true);
            $scope.list[index] = _.clone(code);
        });
    };

    $scope.init();
}]);
