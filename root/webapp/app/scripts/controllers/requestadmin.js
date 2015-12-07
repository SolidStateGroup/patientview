'use strict';

angular.module('patientviewApp').controller('RequestAdminCtrl', ['GroupService', 'RequestService', 'StaticDataService',
    '$scope', '$rootScope', 'UserService',
function (GroupService, RequestService, StaticDataService, $scope, $rootScope, UserService) {

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
        
        // basic types currently only JOIN_REQUEST and FORGOT_LOGIN
        $scope.types = [];
        $scope.types.push('JOIN_REQUEST');
        $scope.types.push('FORGOT_LOGIN');

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

        RequestService.getStatuses().then(function(statuses) {
            if (statuses.length > 0) {
                $scope.statuses = statuses.sort();
            }

            // if present set selected status to SUBMITTED
            if (statuses.indexOf('SUBMITTED')) {
                $scope.setSelectedStatus('SUBMITTED');
            }

            $scope.initFinished = true;
        });

        $scope.permissions = {};
        $scope.permissions.isSuperAdmin = UserService.checkRoleExists('GLOBAL_ADMIN', $scope.loggedInUser);
    };

    $scope.complete = function() {
        $scope.completingRequests = true;
        if (confirm('This will complete SUBMITTED requests that are no longer relevant. \n\nCompletes join requests where a user already exists (and user was created after the request came in). Also completes "Forgot Login" requests where a user has since logged in.\n')) {
            RequestService.complete().then(function(count) {
                $rootScope.setSubmittedRequestCount();
                alert(count + ' relevant SUBMITTED requests have been changed to COMPLETED');
                $scope.completingRequests = false;
                $scope.getItems();
            }, function() {
                alert('Error completing SUBMITTED requests');
                $scope.completingRequests = false;
            })
        } else {
            $scope.completingRequests = false;
        }
    };

    $scope.getItems = function() {
        $scope.loading = true;

        var getParameters = {};
        getParameters.page = $scope.currentPage;
        getParameters.size = $scope.itemsPerPage;
        getParameters.statuses = $scope.selectedStatus;
        getParameters.types = $scope.selectedType;
        getParameters.sortField = $scope.sortField;
        getParameters.sortDirection = $scope.sortDirection;

        if ($scope.selectedGroup.length > 0) {
            getParameters.groupIds = $scope.selectedGroup;
        }

        RequestService.getByUser($scope.loggedInUser.id, getParameters).then(function(page) {
            $scope.pagedItems = page.content;
            $scope.total = page.totalElements;
            $scope.totalPages = page.totalPages;
            $scope.loading = false;
        }, function() {
            $scope.loading = false;
        });
    };

    // filter by type
    $scope.selectedType = [];
    $scope.setSelectedType = function (type) {
        if (_.contains($scope.selectedType, type)) {
            $scope.selectedType = _.without($scope.selectedTypes, type);
        } else {
            $scope.selectedType.push(type);
        }
        $scope.currentPage = 0;
        $scope.getItems();
    };
    $scope.isTypeChecked = function (type) {
        if (_.contains($scope.selectedType, type)) {
            return 'glyphicon glyphicon-ok pull-right';
        }
        return false;
    };
    $scope.removeAllTypes = function () {
        $scope.selectedType = [];
        $scope.currentPage = 0;
        $scope.getItems();
    };
    $scope.removeSelectedType = function (type) {
        $scope.selectedType.splice($scope.selectedType.indexOf(type), 1);
        $scope.currentPage = 0;
        $scope.getItems();
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
        $scope.selectedStatus.splice($scope.selectedStatus.indexOf(status), 1);
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

    $scope.save = function(form, request) {
        RequestService.save(request).then(function(){
            $scope.saved = true;
            $rootScope.setSubmittedRequestCount();

            // update accordion header for request with data from GET
            RequestService.get(request.id).then(function (successResult) {
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
            alert('Failed to save request');
        });
    };

    // Opened for edit
    $scope.opened = function (openedItem) {
        var i;

        if (openedItem.showEdit) {
            $scope.editRequest = '';
            openedItem.showEdit = false;
        } else {
            // close others
            for (i = 0; i < $scope.pagedItems.length; i++) {
                $scope.pagedItems[i].showEdit = false;
            }

            $scope.editRequest = '';
            openedItem.showEdit = true;
            openedItem.editLoading = true;

            // using lightweight list, do GET on id to get full request and populate editRequest
            RequestService.get(openedItem.id).then(function (item) {
                $scope.successMessage = '';
                $scope.saved = '';
                $scope.editRequest = _.clone(item);
                $scope.editMode = true;
                openedItem.editLoading = false;
            });
        }
    };

    $scope.init();
}]);