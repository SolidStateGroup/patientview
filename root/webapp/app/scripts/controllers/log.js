'use strict';

angular.module('patientviewApp').controller('LogCtrl',['$scope', '$timeout', 'AuditService',
function ($scope, $timeout, AuditService) {

    $scope.itemsPerPage = 20;
    $scope.currentPage = 0;
    $scope.filterText = '';
    $scope.sortField = 'creationDate';
    $scope.sortDirection = 'DESC';

    var tempFilterText = '';
    var filterTextTimeout;

    // watches
    // update page on user typed search text
    $scope.$watch('searchText', function (value) {
        if (value !== undefined) {
            if (filterTextTimeout) {
                $timeout.cancel(filterTextTimeout);
            }
            $scope.currentPage = 0;

            tempFilterText = value;
            filterTextTimeout = $timeout(function () {
                $scope.filterText = tempFilterText;
                $scope.getItems();
            }, 1000); // delay 1000 ms
        }
    });

    // update page when currentPage is changed (and at start)
    $scope.$watch('currentPage', function(value) {
        $scope.currentPage = value;
        $scope.getItems();
    });

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
    $scope.removeAllSelectedGroup = function (groupType) {
        var newSelectedGroupList = [];

        for (var i=0; i<$scope.selectedGroup.length; i++) {
            if ($scope.groupMap[$scope.selectedGroup[i]].groupType.value !== groupType) {
                newSelectedGroupList.push($scope.selectedGroup[i]);
            }
        }

        $scope.selectedGroup = newSelectedGroupList;
        $scope.currentPage = 0;
        $scope.getItems();
    };

    // filter by audit action
    $scope.selectedAuditAction = [];
    $scope.setSelectedAuditAction = function (auditAction) {
        if (_.contains($scope.selectedAuditAction, auditAction)) {
            $scope.selectedAuditAction = _.without($scope.selectedAuditAction, auditAction);
        } else {
            $scope.selectedAuditAction.push(auditAction);
        }
        $scope.currentPage = 0;
        $scope.getItems();
    };
    $scope.isAuditActionChecked = function (auditAction) {
        if (_.contains($scope.selectedAuditAction, auditAction)) {
            return 'glyphicon glyphicon-ok pull-right';
        }
        return false;
    };
    $scope.removeAllAuditActions = function () {
        $scope.selectedAuditAction = [];
        $scope.currentPage = 0;
        $scope.getItems();
    };

    $scope.getItems = function() {
        $scope.loading = true;
        var getParameters = {};
        getParameters.page = $scope.currentPage;
        getParameters.size = $scope.itemsPerPage;
        getParameters.filterText = $scope.filterText;
        getParameters.groupIds = $scope.selectedGroup;
        getParameters.auditActions = $scope.selectedAuditAction;
        getParameters.sortField = $scope.sortField;
        getParameters.sortDirection = $scope.sortDirection;
        getParameters.start = $scope.dateStart.getTime();
        getParameters.end = $scope.dateEnd.getTime();

        AuditService.getAll(getParameters).then(function(page) {
            $scope.pagedItems = page.content;
            $scope.total = page.totalElements;
            $scope.totalPages = page.totalPages;
            $scope.loading = false;
        }, function() {
            $scope.loading = false;
        });
    };

    // date picker
    // http://angular-ui.github.io/bootstrap/
    $scope.todayStart = function() {
        $scope.dateStart = new Date();
    };

    $scope.todayEnd = function() {
        $scope.dateEnd = new Date();
    };

    $scope.clearStart = function () {
        $scope.dateEnd = null;
    };
    $scope.clearEnd = function () {
        $scope.dateEnd = null;
    };
    $scope.openStart = function($event) {
        $event.preventDefault();
        $event.stopPropagation();
        $scope.openedStart = true;
    };
    $scope.openEnd = function($event) {
        $event.preventDefault();
        $event.stopPropagation();
        $scope.openedEnd = true;
    };

    $scope.setDateRange = function(start, end) {
        if (start === undefined) {
            var oneWeek = 604800000;
            $scope.dateStart = new Date(new Date().getTime() - oneWeek);
        }
        if (end === undefined) {
            $scope.dateEnd = new Date();
        }
        $scope.currentPage = 0;
        $scope.getItems();
    };

    var init = function() {

        var i, group;
        $scope.allGroups = [];
        $scope.groupIds = [];
        $scope.groupMap = {};
        $scope.diseaseGroupsAvailable = false;
        $scope.unitsAvailable = false;

        // set up datepicker
        $scope.todayEnd();
        var oneWeek = 604800000;
        $scope.dateStart = new Date(new Date().getTime() - oneWeek);

        // get logged in user's groups
        var groups = $scope.loggedInUser.userInformation.userGroups;
        $scope.auditActions = $scope.loggedInUser.userInformation.auditActions;

        // set groups that can be chosen in UI, only show users from visible groups (assuming all users are in generic which is visible==false)
        for (i = 0; i < groups.length; i++) {
            group = groups[i];
            if (group.visible === true) {
                $scope.allGroups.push(group);
                $scope.groupIds.push(group.id);
                $scope.groupMap[group.id] = group;

                if (group.groupType.value === 'DISEASE_GROUP') {
                    $scope.diseaseGroupsAvailable = true;
                }

                if (group.groupType.value === 'UNIT') {
                    $scope.unitsAvailable = true;
                }
            }
        }
    };

    init();
}]);
