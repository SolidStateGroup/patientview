'use strict';

angular.module('patientviewApp').controller('JoinRequestAdminCtrl', ['GroupService', 'JoinRequestService', 'StaticDataService', '$scope', '$rootScope',
function (GroupService, JoinRequestService, StaticDataService, $scope, $rootScope) {

    $scope.itemsPerPage = 20;
    $scope.currentPage = 0;
    $scope.sortField = 'created';
    $scope.sortDirection = 'DESC';

    // update page when currentPage is changed (and at start)
    $scope.$watch('currentPage', function(value) {
        if ($scope.initFinished) {
            $scope.currentPage = value;
            $scope.getItems();
        }
    });

    // Init
    $scope.init = function () {
        $scope.statuses = [];
        $scope.allGroups = [];
        $scope.initFinished = false;

        // get logged in user's groups
        var groups = $scope.loggedInUser.userInformation.userGroups;
        var i, group;

        // set groups that can be chosen in UI, only show visible groups
        for (i = 0; i < groups.length; i++) {
            group = groups[i];
            if (group.visible === true) {
                $scope.allGroups.push(group);
            }
        }

        JoinRequestService.getStatuses().then(function(statuses) {
            if (statuses.length > 0) {
                $scope.statuses = statuses.sort();
            }

            // if present set selected status to SUBMITTED
            if (statuses.indexOf('SUBMITTED')) {
                $scope.setSelectedStatus('SUBMITTED');
            }

            $scope.initFinished = true;
        });

    };

    $scope.getItems = function() {
        $scope.loading = true;

        var getParameters = {};
        getParameters.page = $scope.currentPage;
        getParameters.size = $scope.itemsPerPage;
        getParameters.statuses = $scope.selectedStatus;
        getParameters.sortField = $scope.sortField;
        getParameters.sortDirection = $scope.sortDirection;

        if ($scope.selectedGroup.length > 0) {
            getParameters.groupIds = $scope.selectedGroup;
        }

        JoinRequestService.getByUser($scope.loggedInUser.id, getParameters).then(function(page) {
            $scope.pagedItems = page.content;
            $scope.total = page.totalElements;
            $scope.totalPages = page.totalPages;
            $scope.loading = false;
        }, function() {
            $scope.loading = false;
        });
    };

    // filter by status
    $scope.selectedStatus = [];
    $scope.setSelectedStatus = function (status) {
        if (_.contains($scope.selectedStatus, status)) {
            $scope.selectedStatus = _.without($scope.selectedStatus, status);
        } else {
            $scope.selectedStatus.push(status);
        }
        $scope.currentPage = 0;
        $scope.getItems();
    };
    $scope.isStatusChecked = function (status) {
        if (_.contains($scope.selectedStatus, status)) {
            return 'glyphicon glyphicon-ok pull-right';
        }
        return false;
    };
    $scope.removeAllStatuses = function () {
        $scope.selectedStatus = [];
        $scope.currentPage = 0;
        $scope.getItems();
    };
    $scope.removeSelectedStatus = function (status) {
        $scope.selectedStatus.splice($scope.selectedGroup.indexOf(status), 1);
        $scope.currentPage = 0;
        $scope.getItems();
    };

    // filter by group
    $scope.selectedGroup = [];
    $scope.setSelectedGroup = function () {
        var id = this.group.id;
        if (_.contains($scope.selectedGroup, id)) {
            $scope.selectedGroup = _.without($scope.selectedGroup, id);
        } else {
            $scope.selectedGroup.push(id);
        }
        $scope.currentPage = 0;
        $scope.getItems();
    };
    $scope.isGroupChecked = function (id) {
        if (_.contains($scope.selectedGroup, id)) {
            return 'glyphicon glyphicon-ok pull-right';
        }
        return false;
    };
    $scope.removeAllSelectedGroup = function () {
        $scope.selectedGroup = [];
        $scope.currentPage = 0;
        $scope.getItems();
    };
    $scope.removeSelectedGroup = function (group) {
        $scope.selectedGroup.splice($scope.selectedGroup.indexOf(group.id), 1);
        $scope.currentPage = 0;
        $scope.getItems();
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

    // pagination
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
    
    $scope.save = function(form, joinRequest) {
        JoinRequestService.save(joinRequest).then(function(){
            $scope.saved = true;
            $rootScope.setSubmittedJoinRequestCount();

            // update accordion header for join request with data from GET
            JoinRequestService.get(joinRequest.id).then(function (successResult) {
                for(var i=0;i<$scope.pagedItems.length;i++) {
                    if($scope.pagedItems[i].id === successResult.id) {
                        var headerDetails = $scope.pagedItems[i];
                        headerDetails.status = successResult.status;
                    }
                }
            }, function () {
                // failure
                alert('Error updating header (saved successfully)');
            });
        }, function() {
            alert('Failed to save join request');
        });
    };

    // Opened for edit
    $scope.opened = function (openedItem) {
        var i;

        if (openedItem.showEdit) {
            $scope.editItem = '';
            openedItem.showEdit = false;
        } else {
            // close others
            for (i = 0; i < $scope.pagedItems.length; i++) {
                $scope.pagedItems[i].showEdit = false;
            }

            $scope.editItem = '';
            openedItem.showEdit = true;
            openedItem.editLoading = true;

            // using lightweight list, do GET on id to get full join request and populate editItem
            JoinRequestService.get(openedItem.id).then(function (item) {
                $scope.successMessage = '';
                $scope.saved = '';
                $scope.editItem = _.clone(item);
                $scope.editMode = true;
                openedItem.editLoading = false;
            });
        }
    };

    $scope.init();
}]);