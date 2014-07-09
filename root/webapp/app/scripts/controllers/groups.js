'use strict';

// new group modal instance controller
var NewGroupModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'permissions', 'groupTypes', 'editGroup', 'allFeatures', 'allParentGroups', 'allChildGroups', 'GroupService',
function ($scope, $rootScope, $modalInstance, permissions, groupTypes, editGroup, allFeatures, allParentGroups, allChildGroups, GroupService) {
    $scope.permissions = permissions;
    $scope.editGroup = editGroup;
    $scope.groupTypes = groupTypes;
    $scope.allFeatures = allFeatures;

    // set feature (avoid blank option)
    if ($scope.editGroup.availableFeatures && $scope.editGroup.availableFeatures.length > 0) {
        $scope.featureToAdd = $scope.editGroup.availableFeatures[0].feature.id;
    }

    $scope.ok = function () {
        GroupService.new($scope.editGroup, groupTypes).then(function(result) {
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

            // handle parent/child (avoiding infinite recursion using @Transient in Group.java)
            for (i=0;i<groups.length;i++) {
                group = groups[i];
                if (group.parents) {
                    group.parentGroups = group.parents;
                } else {
                    group.parentGroups = [];
                }

                if (group.children) {
                    group.childGroups = group.children;
                } else {
                    group.childGroups = [];
                }
            }

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
            if ($scope.isSuperAdmin) {
                $scope.allowedRelationshipGroups = groups;
            } else if ($scope.isSpecialtyAdmin){
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

        // check if user is SUPER_ADMIN or SPECIALTY_ADMIN
        $scope.isSuperAdmin = UserService.checkRoleExists('SUPER_ADMIN', $scope.loggedInUser);
        $scope.isSpecialtyAdmin = UserService.checkRoleExists('SPECIALTY_ADMIN', $scope.loggedInUser);

        // get all units if SPECIALTY_ADMIN, used when setting allowed groups for parent/child relationships
        if ($scope.isSpecialtyAdmin) {
            // todo: hardcoded to unit group type with id 1
            GroupService.getAllByType('1').then(function(units) {
            //GroupService.getAll().then(function(units) {
                $scope.allUnits = units;
                // get list of groups
                $scope.getGroups();
            });
        } else {
            // get list of groups
            $scope.getGroups();
        }

        // TODO: set permissions for ui, hard coded to check if user has SUPER_ADMIN, SPECIALTY_ADMIN role anywhere, if so can do:
        // add SPECIALTY groups
        // edit group code
        // edit parents groups (SUPER_ADMIN only)
        // edit child groups
        // edit features
        // create group
        $scope.permissions = {};

        if ($scope.isSuperAdmin || $scope.isSpecialtyAdmin) {
            $scope.permissions.canEditGroupCode = true;
            $scope.permissions.canEditChildGroups = true;
            $scope.permissions.canEditFeatures = true;
            $scope.permissions.canCreateGroup = true;
            $scope.permissions.canEditParentGroups = true;
        }

        // set allowed group types, used when filtering and adding groups
        $scope.groupTypes = [];
        StaticDataService.getLookupsByType('GROUP').then(function(groupTypes) {
            if (groupTypes.length > 0) {
                var allowedGroupTypes = [];

                for (i=0;i<groupTypes.length;i++) {
                    if (groupTypes[i].value === 'SPECIALTY') {
                        if ($scope.isSuperAdmin || $scope.isSpecialtyAdmin) {
                            allowedGroupTypes.push(groupTypes[i]);
                        }
                    } else {
                        allowedGroupTypes.push(groupTypes[i]);
                    }
                }
                $scope.groupTypes = allowedGroupTypes;
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

    // Group opened for edit or stats
    $scope.opened = function (group, $event) {

        // handle statistics click
        if ($event.target.className.indexOf('statistics-button') !== -1) {
            $scope.statistics = "stats";
        } else {
            // handle normal open
            $scope.statistics = '';
            $scope.successMessage = '';
            group.groupTypeId = group.groupType.id;
            var i = 0, j = 0;

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
        }
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
