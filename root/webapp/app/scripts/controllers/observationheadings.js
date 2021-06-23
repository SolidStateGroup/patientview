'use strict';

angular.module('patientviewApp').controller('ObservationHeadingsCtrl', ['$scope', '$timeout', '$modal',
    'ObservationHeadingService', 'UserService', 'GroupService',
    function ($scope, $timeout, $modal, ObservationHeadingService, UserService, GroupService) {

        $scope.itemsPerPage = 20;
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

            var groups = $scope.loggedInUser.userInformation.userGroups;
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
                backdrop: 'static',
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
            $scope.errorMessage = '';
            $scope.successMessage = '';

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
                            headerDetails.minGraph = entity.minGraph;
                            headerDetails.maxGraph = entity.maxGraph;
                        }
                    }
                });

                editObservationHeadingForm.$setPristine(true);
                $scope.saved = true;
                $scope.successMessage = 'Result Heading saved';
            }, function(error) {
                $scope.errorMessage = "Error: "+error.data;
            });
        };

        $scope.groupVisible = function(group) {
            return _.findWhere($scope.groups, {id: group.id});
        };

        $scope.init();
    }]);
