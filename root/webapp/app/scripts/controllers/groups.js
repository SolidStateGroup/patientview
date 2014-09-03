'use strict';

// group statistics modal instance controller
var GroupStatisticsModalInstanceCtrl = ['$scope', '$modalInstance','statistics',
    function ($scope, $modalInstance, statistics) {
        $scope.statistics = statistics;
        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }];

// new group modal instance controller
var NewGroupModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'permissions', 'editGroup', 'allFeatures', 'contactPointTypes', 'allParentGroups', 'allChildGroups', 'GroupService',
function ($scope, $rootScope, $modalInstance, permissions, editGroup, allFeatures, contactPointTypes, allParentGroups, allChildGroups, GroupService) {
    $scope.permissions = permissions;
    $scope.editGroup = editGroup;
    $scope.allFeatures = allFeatures;
    $scope.contactPointTypes = contactPointTypes;
    $scope.editMode = false;
    var i;

    // restrict all but GLOBAL_ADMIN from adding new SPECIALTY type groups by reducing groupTypes
    for (i = 0; i < $scope.editGroup.groupTypes.length; i++) {
        if (!$scope.permissions.isSuperAdmin && $scope.editGroup.groupTypes[i].value === 'SPECIALTY') {
            $scope.editGroup.groupTypes.splice(i, 1);
        }
    }

    // set up groupTypesArray for use when showing/hiding parent/child group blocks for UNIT or SPECIALTY
    $scope.groupTypesArray = [];
    for (i = 0; i < $scope.editGroup.groupTypes.length; i++) {
        $scope.groupTypesArray[$scope.editGroup.groupTypes[i].value] = $scope.editGroup.groupTypes[i].id;
    }

    // set feature (avoid blank option)
    if ($scope.editGroup.availableFeatures && $scope.editGroup.availableFeatures.length > 0) {
        $scope.featureToAdd = $scope.editGroup.availableFeatures[0].feature.id;
    }

    $scope.ok = function () {
        GroupService.new($scope.editGroup, $scope.editGroup.groupTypes).then(function(result) {
            // successfully added new Group, close modal and return group
            $scope.editGroup = result;
            $modalInstance.close($scope.editGroup);
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

angular.module('patientviewApp').controller('GroupsCtrl', ['$scope','$timeout', '$modal','GroupService','StaticDataService','FeatureService','UserService',
function ($scope, $timeout, $modal, GroupService, StaticDataService, FeatureService, UserService) {

    $scope.itemsPerPage = 20;
    $scope.currentPage = 0;
    $scope.filterText = '';
    $scope.sortField = '';
    $scope.sortDirection = '';

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

    // Get groups based on current user selected filters etc
    $scope.getItems = function () {
        $scope.loading = true;

        var getParameters = {};
        getParameters.page = $scope.currentPage;
        getParameters.size = $scope.itemsPerPage;
        getParameters.filterText = $scope.filterText;
        getParameters.groupTypes = $scope.selectedGroupType;
        getParameters.sortField = $scope.sortField;
        getParameters.sortDirection = $scope.sortDirection;

        // get list of groups associated with a user
        GroupService.getGroupsForUser($scope.loggedInUser.id, getParameters).then(function(page) {
            $scope.pagedItems = page.content;
            $scope.total = page.totalElements;
            $scope.totalPages = page.totalPages;
            delete $scope.loading;
        }, function () {
            delete $scope.loading;
            $scope.fatalErrorMessage = 'Error retrieving groups';
        });
    };

    // TODO: this needs performance improvements, server currently retrieves all groups
    $scope.getAllowedRelationshipGroups = function() {
        // allowed relationship groups are those that can be added as parents or children to existing groups
        GroupService.getAllowedRelationshipGroups($scope.loggedInUser.id).then(function(allowedRelationshipGroups) {
            var group, i;
            $scope.allowedRelationshipGroups = allowedRelationshipGroups.content;

            // define groups that can be parents by type, currently hardcoded to SPECIALTY (and later DISEASE_GROUP)
            $scope.allParentGroups = [];
            for (i = 0; i < $scope.allowedRelationshipGroups.length; i++) {
                group = $scope.allowedRelationshipGroups[i];
                if (group && group.groupType && group.groupType.value === 'SPECIALTY') {
                    $scope.allParentGroups.push(group);
                }
            }

            // similarly, child groups are all those of type NOT SPECIALTY
            $scope.allChildGroups = [];
            for (i = 0; i < $scope.allowedRelationshipGroups.length; i++) {
                group = $scope.allowedRelationshipGroups[i];
                if (group && group.groupType && group.groupType.value !== 'SPECIALTY') {
                    $scope.allChildGroups.push(group);
                }
            }
        });
    };

    $scope.getAllowedAddEditFilterGroups = function() {
        // set allowed group types (when adding/editing groups) and filter group types (when filtering by group type)
        $scope.groupTypes = [];
        $scope.filterGroupTypes = [];

        StaticDataService.getLookupsByType('GROUP').then(function(groupTypes) {
            $scope.loading = true;
            if (groupTypes.length > 0) {
                var allowedGroupTypes = [];
                var allowedFilterGroupTypes = [];

                for (var i=0;i<groupTypes.length;i++) {
                    if (groupTypes[i].value === 'SPECIALTY') {
                        if ($scope.permissions.isSuperAdmin || $scope.permissions.isSpecialtyAdmin) {
                            allowedGroupTypes.push(groupTypes[i]);
                        }
                        if ($scope.permissions.isSuperAdmin || $scope.permissions.isSpecialtyAdmin) {
                            allowedFilterGroupTypes.push(groupTypes[i]);
                        }
                    } else {
                        allowedGroupTypes.push(groupTypes[i]);
                        allowedFilterGroupTypes.push(groupTypes[i]);
                    }
                }
                $scope.groupTypes = allowedGroupTypes;
                $scope.filterGroupTypes = allowedFilterGroupTypes;
            }
            delete $scope.loading;
        });
    };

    // Init, started at page load
    $scope.init = function () {
        var i;
        $scope.loading = true;
        $scope.allUnits = [];

        // set permissions, currently hardcoded
        $scope.permissions = {};

        // check if user is GLOBAL_ADMIN or SPECIALTY_ADMIN
        $scope.permissions.isSuperAdmin = UserService.checkRoleExists('GLOBAL_ADMIN', $scope.loggedInUser);
        $scope.permissions.isSpecialtyAdmin = UserService.checkRoleExists('SPECIALTY_ADMIN', $scope.loggedInUser);

        if ($scope.permissions.isSuperAdmin || $scope.permissions.isSpecialtyAdmin) {
            $scope.permissions.canEditGroupCode = true;
            $scope.permissions.canEditChildGroups = true;
            $scope.permissions.canEditFeatures = true;
            $scope.permissions.canCreateGroup = true;
            $scope.permissions.canEditParentGroups = true;
        }

        $scope.getAllowedRelationshipGroups();
        $scope.getAllowedAddEditFilterGroups();

        // get list of features associated with groups
        FeatureService.getAllGroupFeatures().then(function(allFeatures) {
            $scope.allFeatures = [];
            for (i=0;i<allFeatures.length;i++){
                $scope.allFeatures.push({'feature':allFeatures[i]});
            }
        });

        // get list of contact point types
        StaticDataService.getLookupsByType('CONTACT_POINT_TYPE').then(function(contactPointTypes) {
            $scope.contactPointTypes = contactPointTypes;
        });
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

    // filter by group type
    $scope.selectedGroupType = [];
    $scope.setSelectedGroupType = function () {
        var id = this.type.id;
        if (_.contains($scope.selectedGroupType, id)) {
            $scope.selectedGroupType = _.without($scope.selectedGroupType, id);
        } else {
            $scope.selectedGroupType.push(id);
        }
        $scope.getItems();
    };
    $scope.isGroupTypeChecked = function (id) {
        if (_.contains($scope.selectedGroupType, id)) {
            return 'glyphicon glyphicon-ok pull-right';
        }
        return false;
    };
    $scope.removeAllGroupTypes = function () {
        $scope.selectedGroupType = [];
        $scope.getItems();
    };

    // Group opened for edit
    $scope.opened = function (openedGroup) {
        var i;
        $scope.editMode = true;
        $scope.saved = '';

        // do not load if already opened (status.open == true)
        if (openedGroup.showEdit) {
            $scope.editGroup = '';
            openedGroup.showEdit = false;
        } else {
            // close others
            for (i = 0; i < $scope.pagedItems.length; i++) {
                $scope.pagedItems[i].showEdit = false;
            }

            $scope.editGroup = '';
            $scope.editGroup.editLoading = true;
            $scope.editGroup.editError = '';

            openedGroup.showEdit = true;

            // now using lightweight group list, do GET on id to get full group and populate editGroup
            GroupService.get(openedGroup.id).then(function (group) {
                var i, j;
                $scope.successMessage = '';
                group.groupTypeId = group.groupType.id;

                // if child/parent groups are empty arrays, set to []
                if (!group.childGroups) {
                    group.childGroups = [];
                }
                if (!group.parentGroups) {
                    group.parentGroups = [];
                }

                // set up groupTypesArray for use when showing/hiding parent/child group blocks for UNIT or SPECIALTY
                $scope.groupTypesArray = [];
                for (i = 0; i < $scope.groupTypes.length; i++) {
                    $scope.groupTypesArray[$scope.groupTypes[i].value] = $scope.groupTypes[i].id;
                }

                // set up parent/child groups, remove current group from list of available parent/child groups
                group.availableParentGroups = _.clone($scope.allParentGroups);
                group.availableChildGroups = _.clone($scope.allChildGroups);

                // remove existing parent groups and self from available
                if (group.parentGroups && group.parentGroups.length > 0) {
                    for (i = 0; i < group.parentGroups.length; i++) {
                        for (j = 0; j < group.availableParentGroups.length; j++) {
                            if (group.parentGroups[i].id === group.availableParentGroups[j].id) {
                                group.availableParentGroups.splice(j, 1);
                            }
                        }
                    }
                }
                for (i = 0; i < group.availableParentGroups.length; i++) {
                    if (group.availableParentGroups[i].id === group.id) {
                        group.availableParentGroups.splice(i, 1);
                    }
                }

                // remove existing child groups and self from available
                if (group.childGroups && group.childGroups.length > 0) {
                    for (i = 0; i < group.childGroups.length; i++) {
                        for (j = 0; j < group.availableChildGroups.length; j++) {
                            if (group.childGroups[i].id === group.availableChildGroups[j].id) {
                                group.availableChildGroups.splice(j, 1);
                            }
                        }
                    }
                }
                for (i = 0; i < group.availableChildGroups.length; i++) {
                    if (group.availableChildGroups[i].id === group.id) {
                        group.availableChildGroups.splice(i, 1);
                    }
                }

                // create list of available features (all - groups)
                group.availableFeatures = _.clone($scope.allFeatures);
                if (group.groupFeatures) {
                    for (i = 0; i < group.groupFeatures.length; i++) {
                        for (j = 0; j < group.availableFeatures.length; j++) {
                            if (group.groupFeatures[i].feature.id === group.availableFeatures[j].feature.id) {
                                group.availableFeatures.splice(j, 1);
                            }
                        }
                    }
                } else {
                    group.groupFeatures = [];
                }

                // set default of new location label to Additional Location
                group.newLocation = {};
                group.newLocation.label = 'Additional Location';

                $scope.editGroup = _.clone(group);

                if ($scope.editGroup.availableFeatures[0]) {
                    $scope.featureToAdd = $scope.editGroup.availableFeatures[0].feature.id;
                }

                $scope.editGroup.editLoading = false;
            }, function () {
                $scope.editGroup.errorMessage = 'There has been a problem retrieving this group';
            });
        }
    };

    // statistics, opens modal
    $scope.statistics = function (groupId, $event) {
        $event.stopPropagation();
        $scope.successMessage = '';

        GroupService.getStatistics(groupId).then(function(statistics) {
            var modalInstance = $modal.open({
                templateUrl: 'views/partials/groupStatisticsModal.html',
                controller: GroupStatisticsModalInstanceCtrl,
                resolve: {
                    statistics: function(){
                        return statistics;
                    }
                }
            });

            modalInstance.result.then(function () {
                // ok (not used)
            }, function () {
                // closed
            });
        });
    };

    // open modal for new group
    $scope.openModalNewGroup = function (size) {
        // close any open edit panels
        for (var i = 0; i < $scope.pagedItems.length; i++) {
            $scope.pagedItems[i].showEdit = false;
        }
        $scope.errorMessage = '';
        $scope.successMessage = '';
        $scope.groupCreated = '';
        $scope.editGroup = {};
        $scope.editGroup.links = [];
        $scope.editGroup.locations = [];
        $scope.editGroup.groupFeatures = [];
        $scope.editGroup.availableFeatures = _.clone($scope.allFeatures);
        $scope.editGroup.contactPoints = [];

        // set up parent/child groups
        $scope.editGroup.parentGroups = [];
        $scope.editGroup.childGroups = [];
        $scope.editGroup.availableParentGroups = _.clone($scope.allParentGroups);
        $scope.editGroup.availableChildGroups = _.clone($scope.allChildGroups);

        // set default of new location label to Additional Location
        $scope.editGroup.newLocation = {};
        $scope.editGroup.newLocation.label = 'Additional Location';

        $scope.editGroup.groupTypes = _.clone($scope.groupTypes);

        var modalInstance = $modal.open({
            templateUrl: 'newGroupModal.html',
            controller: NewGroupModalInstanceCtrl,
            size: size,
            resolve: {
                permissions: function(){
                    return $scope.permissions;
                },
                allFeatures: function(){
                    return $scope.allFeatures;
                },
                contactPointTypes: function(){
                    return $scope.contactPointTypes;
                },
                allParentGroups: function(){
                    return $scope.allParentGroups;
                },
                allChildGroups: function(){
                    return $scope.allChildGroups;
                },
                editGroup: function(){
                    return $scope.editGroup;
                },
                GroupService: function(){
                    return GroupService;
                }
            }
        });

        modalInstance.result.then(function () {
            $scope.getItems();
            $scope.successMessage = 'Group successfully created';
            $scope.groupCreated = true;
        }, function () {
            // cancel
            $scope.editGroup = '';
        });
    };

    // Save from edit
    $scope.save = function (editGroupForm, group) {
        GroupService.save(group, $scope.groupTypes).then(function() {

            // successfully saved, replace existing element in data grid with updated
            editGroupForm.$setPristine(true);
            $scope.saved = true;

            // update accordion header for group with data from GET
            GroupService.get(group.id).then(function (successResult) {
                for(var i=0;i<$scope.pagedItems.length;i++) {
                    if($scope.pagedItems[i].id === successResult.id) {
                        var headerDetails = $scope.pagedItems[i];
                        headerDetails.code = successResult.code;
                        headerDetails.name = successResult.name;
                        headerDetails.groupType = successResult.groupType;
                        headerDetails.groupFeatures = successResult.groupFeatures;
                        headerDetails.parentGroups = successResult.parentGroups;
                    }
                }
            }, function () {
                // failure
                alert('Error updating header (saved successfully)');
            });

            $scope.successMessage = 'Group saved';
        });
    };

    $scope.init();
}]);
