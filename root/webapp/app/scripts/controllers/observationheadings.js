'use strict';

// new observationHeading modal instance controller
var NewObservationHeadingModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'editObservationHeading', 'ObservationHeadingService', 'GroupService',
    function ($scope, $rootScope, $modalInstance, editObservationHeading, ObservationHeadingService, GroupService) {
        $scope.editObservationHeading = editObservationHeading;
        $scope.editMode = false;
        $scope.editObservationHeading.observationHeadingGroups = [];
        $scope.groups = [];

        var groups = $scope.loggedInUser.userGroups;
        for (var i=0;i<groups.length;i++) {
            if (groups[i].groupType.value === 'SPECIALTY' && groups[i].code !== 'Generic') {
                $scope.groups.push(groups[i]);
            }
        }
        $scope.editObservationHeading.groups = $scope.groups;

        $scope.ok = function () {
            ObservationHeadingService.create($scope.editObservationHeading).then(function(result) {
                $scope.editObservationHeading = result;
                $modalInstance.close($scope.editObservationHeading);
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

angular.module('patientviewApp').controller('ObservationHeadingsCtrl', ['$scope', '$timeout', '$modal', 'ObservationHeadingService', 'UserService', 'GroupService',
    function ($scope, $timeout, $modal, ObservationHeadingService, UserService, GroupService) {

        $scope.itemsPerPage = 999;
        $scope.currentPage = 0;
        $scope.sortField = 'code';
        $scope.sortDirection = 'ASC';

        // update page when currentPage is changed (and at start)
        $scope.$watch('currentPage', function(value) {
            $scope.currentPage = value;
            $scope.getItems();
        });

        // Init
        $scope.init = function () {
            $scope.loading = true;
            $scope.permissions = {};
            $scope.groups = [];

            // check if user is GLOBAL_ADMIN
            $scope.permissions.isSuperAdmin = UserService.checkRoleExists('GLOBAL_ADMIN', $scope.loggedInUser);

            if ($scope.permissions.isSuperAdmin) {
                $scope.permissions.canCreateObservationHeading = true;
                $scope.permissions.canEdit = true;
            }

            var groups = $scope.loggedInUser.userGroups;
            for (var i=0;i<groups.length;i++) {
                if (groups[i].groupType.value === 'SPECIALTY' && groups[i].code !== 'Generic') {
                    $scope.groups.push(groups[i]);
                }
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
        $scope.opened = function (openedObservationHeading) {
            var i;

            if (openedObservationHeading.showEdit) {
                $scope.editObservationHeading = '';
                openedObservationHeading.showEdit = false;
            } else {
                // close others
                for (i = 0; i < $scope.pagedItems.length; i++) {
                    $scope.pagedItems[i].showEdit = false;
                }

                openedObservationHeading.editLoading = true;

                $scope.editObservationHeading = '';
                openedObservationHeading.showEdit = true;

                // using lightweight list, do GET on id to get full observationHeading and populate editObservationHeading
                ObservationHeadingService.get(openedObservationHeading.id).then(function (observationHeading) {
                    $scope.successMessage = '';
                    $scope.saved = '';
                    $scope.editObservationHeading = _.clone(observationHeading);
                    $scope.editObservationHeading.groups = $scope.groups;
                    //$scope.editObservationHeading.groupId = $scope.groups[0].id;
                    $scope.editMode = true;
                    openedObservationHeading.editLoading = false;
                });
            }
        };

        // open modal for new observationHeading
        $scope.openModalNewObservationHeading = function (size) {
            // close any open edit panels
            for (var i = 0; i < $scope.pagedItems.length; i++) {
                $scope.pagedItems[i].showEdit = false;
            }
            $scope.errorMessage = '';
            $scope.successMessage = '';
            $scope.observationHeadingCreated = '';
            $scope.editObservationHeading = {};
            $scope.editObservationHeading.groups = $scope.groups;

            var modalInstance = $modal.open({
                templateUrl: 'newObservationHeadingModal.html',
                controller: NewObservationHeadingModalInstanceCtrl,
                size: size,
                resolve: {
                    GroupService: function() {
                        return GroupService;
                    },
                    editObservationHeading: function(){
                        return $scope.editObservationHeading;
                    },
                    ObservationHeadingService: function(){
                        return ObservationHeadingService;
                    }
                }
            });

            modalInstance.result.then(function () {
                $scope.getItems();
                $scope.successMessage = 'Result Heading successfully created';
                $scope.observationHeadingCreated = true;
            }, function () {
                $scope.editObservationHeading = '';
            });
        };

        // Save observationHeading details from edit
        $scope.save = function (editObservationHeadingForm, observationHeading) {

            ObservationHeadingService.save(observationHeading).then(function() {
                ObservationHeadingService.get(observationHeading.id).then(function (entity) {
                    for (var i = 0; i < $scope.pagedItems.length; i++) {
                        if ($scope.pagedItems[i].id === observationHeading.id) {
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

                editObservationHeadingForm.$setPristine(true);
                $scope.saved = true;
                $scope.successMessage = 'Result Heading saved';
            });
        };

        $scope.groupVisible = function(group) {
            return _.findWhere($scope.groups, {id: group.id});
        };

        $scope.init();
    }]);
