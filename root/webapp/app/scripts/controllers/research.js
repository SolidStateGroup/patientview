'use strict';

// pagination following http://fdietz.github.io/recipes-with-angular-js/common-user-interface-patterns/paginating-through-server-side-data.html
angular.module('patientviewApp').controller('ResearchCtrl', ['$scope', '$modal', '$q', 'ResearchService', 'GroupService',
    'RoleService', 'UserService', 'StaticDataService',
    function ($scope, $modal, $q, ResearchService, GroupService, RoleService, UserService, StaticDataService) {

        $scope.itemsPerPage = 10;
        $scope.currentPage = 0;

        $scope.init = function () {
            // set up permissions
            var permissions = {};

            // check if user is GLOBAL_ADMIN or SPECIALTY_ADMIN
            permissions.isSuperAdmin = UserService.checkRoleExists('GLOBAL_ADMIN', $scope.loggedInUser);
            permissions.isSpecialtyAdmin = UserService.checkRoleExists('SPECIALTY_ADMIN', $scope.loggedInUser);
            permissions.isUnitAdmin = UserService.checkRoleExists('UNIT_ADMIN', $scope.loggedInUser);

            $scope.permissions = permissions;
        };

        $scope.range = function () {
            var rangeSize = 5;
            var ret = [];
            var start;

            start = 1;
            if (start > $scope.totalPages - rangeSize) {
                start = $scope.totalPages - rangeSize;
            }

            for (var i = start; i < start + rangeSize; i++) {
                if (i > -1) {
                    ret.push(i);
                }
            }

            return ret;
        };

        $scope.setPage = function (n) {
            if (n > -1 && n < $scope.totalPages) {
                $scope.currentPage = n;
            }
        };

        $scope.prevPage = function () {
            if ($scope.currentPage > 0) {
                $scope.currentPage--;
            }
        };

        $scope.prevPageDisabled = function () {
            return $scope.currentPage === 0 ? 'hidden' : '';
        };

        $scope.nextPage = function () {
            if ($scope.currentPage < $scope.totalPages - 1) {
                $scope.currentPage++;
            }
        };

        $scope.nextPageDisabled = function () {
            if ($scope.totalPages > 0) {
                return $scope.currentPage === $scope.totalPages - 1 ? 'hidden' : '';
            } else {
                return 'hidden';
            }
        };

        // get page of data every time currentPage is changed
        $scope.$watch('currentPage', function (newValue) {
            $scope.loading = true;
            ResearchService.getAll().then(function (page) {
                debugger;
                $scope.pagedItems = page.content;
                $scope.total = page.totalElements;
                $scope.totalPages = page.totalPages;
                $scope.loading = false;
            }, function () {
                $scope.loading = false;
                // error
            });
        });

        $scope.init();
    }]);
