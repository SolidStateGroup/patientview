'use strict';

// new resultHeading modal instance controller
var NewResultHeadingModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'editResultHeading', 'ObservationHeadingService',
    function ($scope, $rootScope, $modalInstance, editResultHeading, ObservationHeadingService) {
        $scope.editResultHeading = editResultHeading;
        $scope.editMode = false;

        $scope.ok = function () {
            ObservationHeadingService.create($scope.editResultHeading).then(function(result) {
                $scope.editResultHeading = result;
                $modalInstance.close($scope.editResultHeading);
            }, function(result) {
                if (result.data) {
                    $scope.errorMessage = ' - ' + result.data;
                } else {
                    $scope.errorMessage = ' ';
                }
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }];

angular.module('patientviewApp').controller('ResultheadingsCtrl', ['$scope', '$timeout', '$modal', 'ObservationHeadingService', 'UserService',
    function ($scope, $timeout, $modal, ObservationHeadingService, UserService) {

        $scope.itemsPerPage = 999;
        $scope.currentPage = 0;
        $scope.sortField = '';
        $scope.sortDirection = '';

        // update page when currentPage is changed (and at start)
        $scope.$watch('currentPage', function(value) {
            $scope.currentPage = value;
            $scope.getItems();
        });

        // Init
        $scope.init = function () {
            $scope.permissions = {};

            // check if user is GLOBAL_ADMIN
            $scope.permissions.isSuperAdmin = UserService.checkRoleExists('GLOBAL_ADMIN', $scope.loggedInUser);

            if ($scope.permissions.isSuperAdmin) {
                $scope.permissions.canCreateResultHeading = true;
            }
        };

        $scope.sortBy = function(sortField) {
            $scope.currentPage = 0;
            if ($scope.sortField !== sortField) {
                $scope.sortDirection = 'ASC';
                $scope.sortField = sortField;
            } else {
                if ($scope.sortDirection === 'ASC') {
                    $scope.sortDirection = 'DESC';
                } else {
                    $scope.sortDirection = 'ASC';
                }
            }

            $scope.getItems();
        };

        $scope.pageCount = function() {
            return Math.ceil($scope.total/$scope.itemsPerPage);
        };

        $scope.range = function() {
            var rangeSize = 10;
            var ret = [];
            var start;

            if ($scope.currentPage < 10) {
                start = 0;
            } else {
                start = $scope.currentPage;
            }

            if ( start > $scope.pageCount()-rangeSize ) {
                start = $scope.pageCount()-rangeSize;
            }

            for (var i=start; i<start+rangeSize; i++) {
                if (i > -1) {
                    ret.push(i);
                }
            }

            return ret;
        };

        $scope.setPage = function(n) {
            if (n > -1 && n < $scope.totalPages) {
                $scope.currentPage = n;
            }
        };

        $scope.prevPage = function() {
            if ($scope.currentPage > 0) {
                $scope.currentPage--;
            }
        };

        $scope.prevPageDisabled = function() {
            return $scope.currentPage === 0 ? 'hidden' : '';
        };

        $scope.nextPage = function() {
            if ($scope.currentPage < $scope.totalPages - 1) {
                $scope.currentPage++;
            }
        };

        $scope.nextPageDisabled = function() {
            return $scope.currentPage === $scope.pageCount() - 1 ? 'disabled' : '';
        };

        $scope.getItems = function() {
            $scope.loading = true;
            var getParameters = {};
            getParameters.page = $scope.currentPage;
            getParameters.size = $scope.itemsPerPage;
            getParameters.sortField = $scope.sortField;
            getParameters.sortDirection = $scope.sortDirection;

            ObservationHeadingService.getAll(getParameters).then(function(page) {
                $scope.pagedItems = page.content;
                $scope.total = page.totalElements;
                $scope.totalPages = page.totalPages;
                $scope.loading = false;
            }, function() {
                $scope.loading = false;
            });
        };

        // Opened for edit
        $scope.opened = function (openedResultHeading) {
            var i;

            if (openedResultHeading.showEdit) {
                $scope.editResultHeading = '';
                openedResultHeading.showEdit = false;
            } else {
                // close others
                for (i = 0; i < $scope.pagedItems.length; i++) {
                    $scope.pagedItems[i].showEdit = false;
                }

                openedResultHeading.editLoading = true;

                $scope.editResultHeading = '';
                openedResultHeading.showEdit = true;

                // using lightweight list, do GET on id to get full resultHeading and populate editResultHeading
                ObservationHeadingService.get(openedResultHeading.id).then(function (resultHeading) {
                    $scope.successMessage = '';
                    $scope.saved = '';
                    $scope.editResultHeading = _.clone(resultHeading);
                    $scope.editMode = true;
                    openedResultHeading.editLoading = false;
                });
            }
        };

        // open modal for new resultHeading
        $scope.openModalNewResultHeading = function (size) {
            // close any open edit panels
            for (var i = 0; i < $scope.pagedItems.length; i++) {
                $scope.pagedItems[i].showEdit = false;
            }
            $scope.errorMessage = '';
            $scope.successMessage = '';
            $scope.resultHeadingCreated = '';
            $scope.editResultHeading = {};

            var modalInstance = $modal.open({
                templateUrl: 'newResultHeadingModal.html',
                controller: NewResultHeadingModalInstanceCtrl,
                size: size,
                resolve: {
                    editResultHeading: function(){
                        return $scope.editResultHeading;
                    },
                    ObservationHeadingService: function(){
                        return ObservationHeadingService;
                    }
                }
            });

            modalInstance.result.then(function () {
                $scope.getItems();
                $scope.successMessage = 'Result Heading successfully created';
                $scope.resultHeadingCreated = true;
            }, function () {
                $scope.editResultHeading = '';
            });
        };

        // Save resultHeading details from edit
        $scope.save = function (editResultHeadingForm, resultHeading) {

            ObservationHeadingService.save(resultHeading).then(function() {
                ObservationHeadingService.get(resultHeading.id).then(function (entity) {
                    for (var i = 0; i < $scope.pagedItems.length; i++) {
                        if ($scope.pagedItems[i].id === resultHeading.id) {
                            var headerDetails = $scope.pagedItems[i];
                            headerDetails.code = entity.code;
                            headerDetails.heading = entity.heading;
                            headerDetails.name = entity.name;
                            headerDetails.normalRange = entity.normalRange;
                            headerDetails.units = entity.units;
                            headerDetails.minValue = entity.minValue;
                            headerDetails.maxValue = entity.maxValue;
                        }
                    }
                });

                editResultHeadingForm.$setPristine(true);
                $scope.saved = true;
                $scope.successMessage = 'Result Heading saved';
            });
        };

        $scope.init();
    }]);
