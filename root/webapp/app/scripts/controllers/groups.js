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
var NewGroupModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'permissions', 'groupTypes', 'editGroup', 'allFeatures', 'allParentGroups', 'allChildGroups', 'GroupService',
function ($scope, $rootScope, $modalInstance, permissions, groupTypes, editGroup, allFeatures, allParentGroups, allChildGroups, GroupService) {
    $scope.permissions = permissions;
    $scope.editGroup = editGroup;
    $scope.groupTypes = groupTypes;
    $scope.allFeatures = allFeatures;
    $scope.editMode = false;
    var i;

    // restrict all but SUPER_ADMIN from adding new SPECIALTY type groups by reducing groupTypes
    for (i = 0; i < $scope.groupTypes.length; i++) {
        if (!$scope.permissions.isSuperAdmin && $scope.groupTypes[i].value === 'SPECIALTY') {
            $scope.groupTypes.splice(i, 1);
        }
    }

    // set up groupTypesArray for use when showing/hiding parent/child group blocks for UNIT or SPECIALTY
    $scope.groupTypesArray = [];
    for (i = 0; i < $scope.groupTypes.length; i++) {
        $scope.groupTypesArray[$scope.groupTypes[i].value] = $scope.groupTypes[i].id;
    }

    // set feature (avoid blank option)
    if ($scope.editGroup.availableFeatures && $scope.editGroup.availableFeatures.length > 0) {
        $scope.featureToAdd = $scope.editGroup.availableFeatures[0].feature.id;
    }

    $scope.ok = function () {
        GroupService.new($scope.editGroup, groupTypes).then(function(result) {
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

    // Get groups, used during initialisation
    $scope.getGroups = function () {
        var i, j, group;

        // get list of groups associated with a user
        GroupService.getGroupsForUser($scope.loggedInUser.id).then(function(groups) {

            // set the list of groups to show in the data grid
            $scope.list = groups;

            // set initial pagination
            $scope.currentPage = 1; //current page
            $scope.entryLimit = 10; //max no of items to display in a page
            $scope.totalItems = $scope.list.length;
            $scope.predicate = 'id';

            // allowed relationship groups are those that can be added as parents or children to existing groups
            // SUPER_ADMIN can see all groups so allowedRelationshipGroups is identical to those returned from getGroupsForUser
            // SPECIALTY_ADMIN can only edit their specialty and add relationships so allowedRelationshipGroups is just a
            // list of all available units found from getUnitGroups which is $scope.AllUnits
            // all other users cannot add parents/children so allowedRelationshipGroups is an empty array
            if ($scope.permissions.isSuperAdmin) {
                $scope.allowedRelationshipGroups = groups;
            } else if ($scope.permissions.isSpecialtyAdmin) {
                // add all units
                $scope.allowedRelationshipGroups = $scope.allUnits;
                // add specialty groups associated with user
                $scope.allowedRelationshipGroups = $scope.allowedRelationshipGroups.concat(groups);
            } else {
                $scope.allowedRelationshipGroups = [];
            }

            // TODO: this behaviour may need to be changed later to support cohorts and other parent type groups
            // define groups that can be parents by type, currently hardcoded to SPECIALTY (and later DISEASE_GROUP) type groups
            $scope.allParentGroups = [];
            for (i=0;i<$scope.allowedRelationshipGroups.length;i++) {
                group = $scope.allowedRelationshipGroups[i];
                if (group && group.groupType && group.groupType.value === 'SPECIALTY') {
                    $scope.allParentGroups.push(group);
                }
            }

            // similarly, child groups are all those of type NOT SPECIALTY
            $scope.allChildGroups = [];
            for (j=0;j<$scope.allowedRelationshipGroups.length;j++) {
                group = $scope.allowedRelationshipGroups[j];
                if (group && group.groupType && group.groupType.value !== 'SPECIALTY') {
                    $scope.allChildGroups.push(group);
                }
            }
        }, function () {
            // error retrieving groups
            delete $scope.loading;
            $scope.fatalErrorMessage = 'Error retrieving groups';
        });
    };

    // Init, started at page load
    $scope.init = function () {
        var i;
        $scope.loading = true;
        $scope.allUnits = [];

        // TODO: set permissions for ui, hard coded to check if user has SUPER_ADMIN, SPECIALTY_ADMIN role anywhere, if so can do:
        // add SPECIALTY groups
        // edit group code
        // edit parents groups (SUPER_ADMIN only)
        // edit child groups
        // edit features
        // create group
        $scope.permissions = {};

        // check if user is SUPER_ADMIN or SPECIALTY_ADMIN
        $scope.permissions.isSuperAdmin = UserService.checkRoleExists('SUPER_ADMIN', $scope.loggedInUser);
        $scope.permissions.isSpecialtyAdmin = UserService.checkRoleExists('SPECIALTY_ADMIN', $scope.loggedInUser);

        if ($scope.permissions.isSuperAdmin || $scope.permissions.isSpecialtyAdmin) {
            $scope.permissions.canEditGroupCode = true;
            $scope.permissions.canEditChildGroups = true;
            $scope.permissions.canEditFeatures = true;
            $scope.permissions.canCreateGroup = true;
            $scope.permissions.canEditParentGroups = true;
        }

        // get all units if SPECIALTY_ADMIN, used when setting allowed groups for parent/child relationships
        if ($scope.permissions.isSpecialtyAdmin) {
            StaticDataService.getLookupByTypeAndValue('GROUP','UNIT').then(function(lookup){
                GroupService.getAllByType(lookup.id).then(function(units) {
                    $scope.allUnits = units;
                    // get list of groups
                    $scope.getGroups();
                });
            });
        } else {
            // get list of groups
            $scope.getGroups();
        }

        // set allowed group types (when adding/editing groups)
        $scope.groupTypes = [];
        // set allowed filter group types (when filtering by group type)
        $scope.filterGroupTypes = [];

        StaticDataService.getLookupsByType('GROUP').then(function(groupTypes) {
            if (groupTypes.length > 0) {
                var allowedGroupTypes = [];
                var allowedFilterGroupTypes = [];

                for (i=0;i<groupTypes.length;i++) {
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

        // get list of features associated with groups
        FeatureService.getAllGroupFeatures().then(function(allFeatures) {
            $scope.allFeatures = [];
            for (i=0;i<allFeatures.length;i++){
                $scope.allFeatures.push({'feature':allFeatures[i]});
            }
        });
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
        return false;
    };
    $scope.isGroupTypeChecked = function (id) {
        if (_.contains($scope.selectedGroupType, id)) {
            return 'glyphicon glyphicon-ok pull-right';
        }
        return false;
    };

    // pagination, sorting, basic filter
    $scope.setPage = function(pageNo) {
        $scope.currentPage = pageNo;
    };
    $scope.filter = function() {
        $timeout(function() {
            $scope.filteredItems = $scope.filtered.length;
        }, 10);
    };
    $scope.sortBy = function(predicate) {
        $scope.predicate = predicate;
        $scope.reverse = !$scope.reverse;
    };

    // Group opened for edit
    $scope.opened = function (openedGroup, $event, status) {

        $scope.editMode = true;

        // do not load if already opened (status.open == true)
        if (!status || status.open === false) {
            $scope.editGroup = '';

            // now using lightweight group list, do GET on id to get full group and populate editGroup
            GroupService.get(openedGroup.id).then(function (group) {

                var i = 0, j = 0;
                $scope.statistics = '';
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

                $scope.editGroup = _.clone(group);

                if ($scope.editGroup.availableFeatures[0]) {
                    $scope.featureToAdd = $scope.editGroup.availableFeatures[0].feature.id;
                }
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
                // ok, delete from list
                for(var l=0;l<$scope.list.length;l++) {
                    if ($scope.list[l].id === codeId) {
                        $scope.list = _.without($scope.list, $scope.list[l]);
                    }
                }
                $scope.successMessage = 'Code successfully deleted';
            }, function () {
                // closed
            });
        });
    };

    // open modal for new group
    $scope.openModalNewGroup = function (size) {
        $scope.errorMessage = '';
        $scope.successMessage = '';
        $scope.groupCreated = '';
        $scope.editGroup = {};
        $scope.editGroup.links = [];
        $scope.editGroup.locations = [];
        $scope.editGroup.groupFeatures = [];
        $scope.editGroup.availableFeatures = _.clone($scope.allFeatures);

        // set up parent/child groups
        $scope.editGroup.parentGroups = [];
        $scope.editGroup.childGroups = [];
        $scope.editGroup.availableParentGroups = _.clone($scope.allParentGroups);
        $scope.editGroup.availableChildGroups = _.clone($scope.allChildGroups);

        var modalInstance = $modal.open({
            templateUrl: 'newGroupModal.html',
            controller: NewGroupModalInstanceCtrl,
            size: size,
            resolve: {
                permissions: function(){
                    return $scope.permissions;
                },
                groupTypes: function(){
                    return $scope.groupTypes;
                },
                allFeatures: function(){
                    return $scope.allFeatures;
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

        modalInstance.result.then(function (group) {
            // modal closed, successfully added group, add to group list
            $scope.list.push(group);
            $scope.editGroup = group;
            $scope.successMessage = 'Group successfully created';
            $scope.groupCreated = true;
        }, function () {
            // cancel
            $scope.editGroup = '';
        });
    };

    // Save from edit
    $scope.save = function (editGroupForm, group) {
        GroupService.save(group, $scope.groupTypes).then(function(successResult) {

            // successfully saved, replace existing element in data grid with updated
            editGroupForm.$setPristine(true);

            for(var i=0;i<$scope.list.length;i++) {
                if($scope.list[i].id == group.id) {
                    $scope.list[i] = _.clone(successResult);
                }
            }

            $scope.successMessage = 'Group saved';
        });
    };

    $scope.init();
}]);
