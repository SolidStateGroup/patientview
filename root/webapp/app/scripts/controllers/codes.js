'use strict';

angular.module('patientviewApp').controller('CodesCtrl', ['$scope','CodeService', function ($scope,CodeService) {

    // Init
    $scope.init = function () {
        $scope.group = {'id':'1234','name':'Example Group'};
        CodeService.getGroupCodes('12345').then(function(codes) {
            $scope.codes = codes;
            $scope.codeCount = codes.length;
            $scope.currentPage = 1;
            $scope.pageSize = 2;
        });
    };

    $scope.save = function (code) {
        console.log(code);
    };


    $scope.add = function (isValid, form, code) {
        if(isValid) {

            CodeService.post($scope.group, code).then(function(addedCode) {
                $scope.codes.push(addedCode);
                $scope.newCode = '';
                $scope.addCodeForm.$setPristine(true);
            }, function() {

            });


        }
    };

    $scope.pageChanged = function (code) {
        console.log(code);
    };

    // http://stackoverflow.com/questions/19409492/how-to-achieve-pagination-table-layout-with-angular-js
    $scope.paginate = function(value) {
        var begin, end, index;
        begin = ($scope.currentPage - 1) * $scope.pageSize;
        end = begin + $scope.pageSize;
        index = $scope.codes.indexOf(value);
        return (begin <= index && index < end);
    };

    $scope.init();
}]);
