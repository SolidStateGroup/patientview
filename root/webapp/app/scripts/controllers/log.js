'use strict';
angular.module('patientviewApp').controller('LogCtrl',['$scope', '$timeout', '$modal', 'AuditService', '$routeParams',
function ($scope, $timeout, $modal, AuditService, $routeParams) {

    $scope.itemsPerPage = 10;
    $scope.currentPage = 0;
    $scope.filterText = '';
    $scope.sortField = 'creationDate';
    $scope.sortDirection = 'DESC';

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

    // filter by group
    $scope.selectedGroup = [];
    $scope.setSelectedGroup = function (id) {
        if (_.contains($scope.selectedGroup, id)) {
            $scope.selectedGroup = _.without($scope.selectedGroup, id);
        } else {
            $scope.selectedGroup.push(id);
        }
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
    };
    $scope.removeSelectedGroup = function (group) {
        $scope.selectedGroup.splice($scope.selectedGroup.indexOf(group.id), 1);
    };

    // filter by audit action
    $scope.selectedAuditAction = [];
    $scope.setSelectedAuditAction = function (auditAction) {
        if (_.contains($scope.selectedAuditAction, auditAction)) {
            $scope.selectedAuditAction = _.without($scope.selectedAuditAction, auditAction);
        } else {
            $scope.selectedAuditAction.push(auditAction);
        }
    };
    $scope.isAuditActionChecked = function (auditAction) {
        if (_.contains($scope.selectedAuditAction, auditAction)) {
            return 'glyphicon glyphicon-ok pull-right';
        }
        return false;
    };
    $scope.removeAllAuditActions = function () {
        $scope.selectedAuditAction = [];
    };
    $scope.removeSelectedAuditAction = function (auditAction) {
        $scope.selectedAuditAction.splice($scope.selectedAuditAction.indexOf(auditAction), 1);
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
        getParameters.start = new Date($scope.dateStart.getTime()).setHours(0,0,0,0);
        getParameters.end = new Date($scope.dateEnd.getTime()).setHours(23,59,59,999);

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
        $scope.dateStart = null;
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

    $scope.reset = function() {
        var oneWeek = 604800000;
        $scope.dateStart = new Date(new Date().getTime() - oneWeek);
        $scope.dateEnd = new Date();
        $scope.currentPage = 0;
        $scope.selectedAuditAction = [];
        $scope.selectedGroup = [];
        $scope.filterText = '';
        $scope.sortField = 'creationDate';
        $scope.sortDirection = 'DESC';
        $scope.getItems();
    };

    $scope.search = function() {
        if ($scope.dateStart === undefined) {
            var oneWeek = 604800000;
            $scope.dateStart = new Date(new Date().getTime() - oneWeek);
        }
        if ($scope.dateEnd === undefined) {
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
        $scope.otherGroupsAvailable = false;
        $scope.unitsAvailable = false;

        // set up datepicker, with one week ago
        $scope.todayEnd();
        var oneWeek = 604800000;
        $scope.dateStart = new Date(new Date().getTime() - oneWeek);

        // get logged in user's groups
        var groups = $scope.loggedInUser.userInformation.userGroups;
        $scope.auditActions = $scope.loggedInUser.userInformation.auditActions;

        // set groups that can be chosen in UI, only show users from visible groups (assuming all users are in
        // generic which is visible==false)
        for (i = 0; i < groups.length; i++) {
            group = groups[i];
            if (group.visible === true) {
                // other groups are disease groups and GPs
                if (group.groupType.value === 'DISEASE_GROUP' || group.groupType.value === 'GENERAL_PRACTICE') {
                    $scope.otherGroupsAvailable = true;
                }

                if (group.groupType.value === 'UNIT') {
                    $scope.unitsAvailable = true;
                }

                // add (GP) to differentiate between disease group and GP
                if (group.groupType.value === 'GENERAL_PRACTICE') {
                    group.shortName = group.shortName + ' (GP)';
                }

                $scope.allGroups.push(group);
                $scope.groupIds.push(group.id);
                $scope.groupMap[group.id] = group;
            }
        }

        // handle group id parameter
        if ($routeParams.groupId != undefined) {
            if (Array.isArray($routeParams.groupId)) {
                for (i = 0; i < $routeParams.groupId.length; i++) {
                    $scope.selectedGroup.push(Number($routeParams.groupId[i]));
                }
            } else {
                $scope.selectedGroup.push(Number($routeParams.groupId));
            }
        }

        // handle audit action parameter
        if ($routeParams.auditActions != undefined) {
            if (Array.isArray($routeParams.auditActions)) {
                for (i = 0; i < $routeParams.auditActions.length; i++) {
                    $scope.selectedAuditAction.push($routeParams.auditActions[i]);
                }
            } else {
                $scope.selectedAuditAction.push($routeParams.auditActions);
            }
        }

        $timeout(function() {
            angular.element('#log-search').focus();
        }, 1000);
    };

    $scope.viewXml = function(audit) {
        var modalInstance = $modal.open({
            templateUrl: 'views/modal/viewXmlModal.html',
            controller: ViewXmlModalInstanceCtrl,
            size: 'lg',
            resolve: {
                audit: function(){
                    return audit;
                }
            }
        });

        modalInstance.result.then(function () {
            // ok (not used)
        }, function () {
            // closed
        });
    };

    init();
}]);
