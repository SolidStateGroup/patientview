'use strict';

angular.module('patientviewApp').directive('pvPagination', ['$compile', function ($compile) {
    return {
        restrict: 'A',
        template: '<div class="col-md-8" data-ng-show="pagedItems.length > 0 && totalPages > 1">' +
            '<ul class="pagination">' +
            '<li data-ng-class="firstPageDisabled()"><a href data-ng-click="firstPage()">« First</a></li>' +
            '<li data-ng-class="prevPageDisabled()"><a href data-ng-click="prevPage()">« Prev</a></li>' +
            '<li data-ng-repeat="n in range()" data-ng-class="{active: n == currentPage}" data-ng-click="setPage(n)">' +
                '<a href>{{n+1}}</a>' +
            '</li>' +
            '<li data-ng-class="nextPageDisabled()"><a href data-ng-click="nextPage()">Next »</a></li>' +
            '<li data-ng-class="lastPageDisabled()"><a href data-ng-click="lastPage()">Last »</a></li>' +
            '</ul></div>',
        link: function($scope, element, attrs) {
            if (attrs['useParentScope'] === 'true') {
                $scope = $scope.$parent;
            }
            $scope.pageCount = function() {
                return Math.ceil($scope.total / $scope.itemsPerPage);
            };

            $scope.range = function() {
                var rangeSize = $scope.itemsPerPage;
                var pageNumbers = [];
                var startPage;

                if (($scope.currentPage - $scope.itemsPerPage / 2) < 0) {
                    startPage = 0;
                } else {
                    startPage = $scope.currentPage - $scope.itemsPerPage / 2;
                }

                if (startPage > $scope.pageCount() - rangeSize) {
                    startPage = $scope.pageCount() - rangeSize;
                }

                for (var i = startPage; i < startPage + rangeSize; i++) {
                    if (i > -1) {
                        pageNumbers.push(i);
                    }
                }

                return pageNumbers;
            };

            $scope.setPage = function(pageNumber) {
                if (pageNumber > -1 && pageNumber < $scope.totalPages) {
                    $scope.currentPage = pageNumber;
                }
            };

            $scope.firstPage = function() {
                $scope.currentPage = 0;
            };

            $scope.prevPage = function() {
                if ($scope.currentPage > 0) {
                    $scope.currentPage--;
                }
            };

            $scope.nextPage = function() {
                if ($scope.currentPage < $scope.totalPages - 1) {
                    $scope.currentPage++;
                }
            };

            $scope.lastPage = function() {
                $scope.currentPage = $scope.totalPages - 1;
            };

            $scope.firstPageDisabled = function() {
                return (($scope.currentPage - $scope.itemsPerPage / 2) < 0) ? 'hidden' : '';
            };

            $scope.prevPageDisabled = function() {
                return $scope.currentPage === 0 ? 'hidden' : '';
            };

            $scope.nextPageDisabled = function() {
                return $scope.currentPage === $scope.pageCount() - 1 ? 'hidden' : '';
            };

            $scope.lastPageDisabled = function() {
                return ($scope.currentPage + 6 > $scope.pageCount()) ? 'hidden' : '';
            };
        }
    };
}]);
