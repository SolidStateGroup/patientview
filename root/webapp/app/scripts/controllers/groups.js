'use strict';

// new group modal instance controller
var NewGroupModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'groupTypes', 'editGroup', 'allFeatures', 'GroupService',
function ($scope, $rootScope, $modalInstance, groupTypes, editGroup, allFeatures, GroupService) {
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

angular.module('patientviewApp').controller('GroupsCtrl', ['$scope','$timeout', '$modal','GroupService','StaticDataService','FeatureService',
function ($scope, $timeout, $modal, GroupService, StaticDataService, FeatureService) {

    // Init
    $scope.init = function () {

        $scope.loading = true;

        GroupService.getAll().then(function(groups) {
            $scope.list = groups;
            $scope.currentPage = 1; //current page
            $scope.entryLimit = 10; //max no of items to display in a page
            $scope.totalItems = $scope.list.length;
            $scope.predicate = 'id';
            delete $scope.loading;
        });

        $scope.groupTypes = [];
        StaticDataService.getLookupsByType('GROUP').then(function(groupTypes) {
            if (groupTypes.length > 0) {
                $scope.groupTypes = groupTypes;
            }
        });

        FeatureService.getAllGroupFeatures().then(function(allFeatures) {
            $scope.allFeatures = [];
            for (var i=0;i<allFeatures.length;i++){
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

    // Opened for edit
    $scope.opened = function (group) {
        $scope.successMessage = '';
        group.groupTypeId = group.groupType.id;
        console.log(group);

        // create list of available features (all - groups)
        group.availableFeatures = _.clone($scope.allFeatures);
        if (group.groupFeatures) {
            for (var j = 0; j < group.groupFeatures.length; j++) {
                for (var k = 0; k < group.availableFeatures.length; k++) {
                    if (group.groupFeatures[j].feature.id === group.availableFeatures[k].feature.id) {
                        group.availableFeatures.splice(k, 1);
                    }
                }
            }
        } else { group.groupFeatures = []; }

        $scope.editGroup = _.clone(group);

        if ($scope.editGroup.availableFeatures[0]) {
            $scope.featureToAdd = $scope.editGroup.availableFeatures[0].feature.id;
        }
    };

    // open modal for new group
    $scope.openModalNewGroup = function (size) {
        $scope.errorMessage = '';
        $scope.successMessage = '';
        $scope.groupCreated = '';
        $scope.editGroup = {};
        $scope.editGroup.links = [];
        $scope.editGroup.groupFeatures = [];
        $scope.editGroup.availableFeatures = _.clone($scope.allFeatures);

        var modalInstance = $modal.open({
            templateUrl: 'newGroupModal.html',
            controller: NewGroupModalInstanceCtrl,
            size: size,
            resolve: {
                groupTypes: function(){
                    return $scope.groupTypes;
                },
                allFeatures: function(){
                    return $scope.allFeatures;
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
