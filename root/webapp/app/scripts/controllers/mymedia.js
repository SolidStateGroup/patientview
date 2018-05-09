'use strict';

angular.module('patientviewApp').controller('MyMediaCtrl', ['$scope', '$modal', 'MyMediaService', 'UtilService',
    function ($scope, $modal, MyMediaService, UtilService) {
        $scope.formatBytes = UtilService.formatBytes;
        $scope.itemsPerPage = 10;
        $scope.currentPage = 0;

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

        $scope.viewMyMedia = function (media) {
            var modalInstance = $modal.open({
                templateUrl: 'views/modal/viewMyMedia.html',
                controller: ViewMyMediaModalInstanceCtrl,
                size: 'lg',
                resolve: {
                    myMedia: function () {
                        return media;
                    },
                    message: function() {
                        return {};
                    }
                }
            });

            modalInstance.result.then(function () {
                // ok (not used)
            }, function () {
                // closed
            });
        };


        $scope.remove = function (mymedia) {
            MyMediaService.removeMedia($scope.loggedInUser.id, mymedia.id).then(function () {
                MyMediaService.getByUser($scope.loggedInUser.id).then(function (page) {
                    $scope.pagedItems = page.content;
                    $scope.total = page.totalElements;
                    $scope.totalPages = page.totalPages;
                    $scope.loading = false;
                }, function () {
                    $scope.loading = false;
                    // error
                });
            });
        };

        // get page of data every time currentPage is changed
        $scope.$watch('currentPage', function (newValue) {
            $scope.loading = true;
            MyMediaService.getByUser($scope.loggedInUser.id).then(function (page) {
                $scope.pagedItems = page.content;
                $scope.total = page.totalElements;
                $scope.totalPages = page.totalPages;
                $scope.loading = false;
            }, function () {
                $scope.loading = false;
                // error
            });
        });

        $scope.getApiEndpoint = function () {
            return ENV.apiEndpoint;
        };
    }]);
