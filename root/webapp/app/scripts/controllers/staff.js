'use strict';

angular.module('patientviewApp').controller('StaffCtrl',['$scope','$timeout', 'UserService', 'GroupService', 'RoleService', 'FeatureService',
    function ($scope, $timeout, UserService, GroupService, RoleService, FeatureService) {

    // Init
    $scope.init = function () {
        $scope.loading = true;
        $scope.role = ['1'];

        GroupService.getAll().then(function(data) {

            $scope.allGroups = data;

            UserService.getByRole($scope.role).then(function(data) {
                $scope.list = data;
                $scope.currentPage = 1; //current page
                $scope.entryLimit = 10; //max no of items to display in a page
                $scope.filteredItems = $scope.list.length; //Initially for no filter
                $scope.totalItems = $scope.list.length;
                delete $scope.loading;
            });

            RoleService.getAll().then(function(data) {
                $scope.allRoles = data;
            });

            FeatureService.getAll().then(function(data) {
                $scope.allFeatures = data;
            });
        });
    };

    // Opened for edit
    $scope.opened = function (user) {

        user.roles = $scope.allRoles;

        // create list of available groups (all - users)
        user.availableGroups = $scope.allGroups;
        if(user.groups) {
            for (var i = 0; i < user.groups.length; i++) {
                user.availableGroups = _.without(user.availableGroups, _.findWhere(user.availableGroups, {id: user.groups[i].id}));
            }
        }
        else {
            user.groups = [];
        }

        // create list of available features (all - users)
        user.availableFeatures = $scope.allFeatures;
        if (user.features) {
            for (var i = 0; i < user.features.length; i++) {
                user.availableFeatures = _.without(user.availableFeatures, _.findWhere(user.availableFeatures, {id: user.features[i].id}));
            }
        } else {
            user.features = [];
        }

        $scope.edituser = _.clone(user);

        if (user.availableGroups[0]) {
            $scope.groupToAdd = user.availableGroups[0].id;
        }
        if (user.availableFeatures[0]) {
            $scope.FeatureToAdd = user.availableFeatures[0].id;
        }

        $scope.edituser.selectedRole = '';
    };

    $scope.add = function (isValid, form, user) {
        if(isValid) {

            UserService.post(user).then(function(added) {
                $scope.list.push(added);
                $scope.newUser = '';
                $scope.addUserForm.$setPristine(true);
            }, function() {

            });
        }
    };

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

    // Save
    $scope.save = function (editUserForm, user, index) {
        //console.log(user);
        UserService.save(user).then(function() {
            editUserForm.$setPristine(true);
            $scope.list[index] = _.clone(user);
        });
    };

    // add group to current group, remove from allowed
    $scope.addGroup = function (form, user, groupId) {
        if(_.findWhere(user.availableGroups, {id: groupId}) && _.findWhere($scope.allRoles, {id: user.selectedRole})) {
            user.availableGroups = _.without(user.availableGroups, _.findWhere(user.availableGroups, {id: groupId}));
           // user.groups.push(_.findWhere($scope.allGroups, {id: group}));
            var group = _.findWhere($scope.allGroups, {id: groupId});
            group.role = _.findWhere($scope.allRoles, {id: user.selectedRole});
            user.groups.push(group);
            user.selectedRole = '';

            if (user.availableGroups[0]) {
                $scope.groupToAdd = user.availableGroups[0].id;
            }

            form.$setDirty(true);
        }
    };

    // remove group from current groups, add to allowed groups
    $scope.removeGroup = function (form, user, group) {
        user.groups = _.without(user.groups, _.findWhere(user.groups, {id: group.id}));
        user.availableGroups.push(group);

        if (user.availableGroups[0]) {
            $scope.groupToAdd = user.availableGroups[0].id;
        }

        form.$setDirty(true);
    };

    // add feature to current feature, remove from allowed
    $scope.addFeature = function (form, user, featureId) {
        if(_.findWhere(user.availableFeatures, {id: featureId})) {
            user.availableFeatures = _.without(user.availableFeatures, _.findWhere(user.availableFeatures, {id: featureId}));
            var feature = _.findWhere($scope.allFeatures, {id: featureId});
            user.features.push(feature);

            if (user.availableFeatures[0]) {
                $scope.featureToAdd = user.availableFeatures[0].id;
            }

            form.$setDirty(true);
        }
    };

    // remove feature from current features, add to allowed features
    $scope.removeFeature = function (form, user, feature) {
        user.features = _.without(user.features, _.findWhere(user.features, {id: feature.id}));
        user.availableFeatures.push(feature);

        if (user.availableFeatures[0]) {
            $scope.featureToAdd = user.availableFeatures[0].id;
        }

        form.$setDirty(true);
    };

    $scope.init();
}]);
