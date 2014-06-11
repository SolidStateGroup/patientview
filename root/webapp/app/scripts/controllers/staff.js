'use strict';

angular.module('patientviewApp').controller('StaffCtrl',['$scope','$timeout', 'UserService', 'GroupService',
    function ($scope,$timeout,UserService,GroupService) {

    // Init
    $scope.init = function () {
        $scope.loading = true;
        $scope.role = ['1'];

        GroupService.getAll().then(function(data) {

            $scope.allGroups = data;

            UserService.getByRole($scope.role).then(function(data) {
                $scope.list = data;
                $scope.currentPage = 1; //current page
                $scope.entryLimit = 3; //max no of items to display in a page
                $scope.filteredItems = $scope.list.length; //Initially for no filter
                $scope.totalItems = $scope.list.length;
                delete $scope.loading;
            });
        });
    };

    $scope.opened = function (user) {

        // create list of available groups (all - users)
        user.availableGroups = $scope.allGroups;

        for(var i=0;i<user.groups.length;i++) {
            user.availableGroups = _.without(user.availableGroups, _.findWhere(user.availableGroups, {id: user.groups[i].id}));
        }

        $scope.edituser = _.clone(user);

        if (user.availableGroups[0]) {
            $scope.groupToAdd = user.availableGroups[0].id;
        }
    };

    $scope.add = function (isValid, form, code) {
        if(isValid) {

            UserService.post($scope.group, code).then(function(added) {
                $scope.list.push(added);
                $scope.newCode = '';
                $scope.addCodeForm.$setPristine(true);
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
        UserService.save(user).then(function() {
            editUserForm.$setPristine(true);
            $scope.list[index] = _.clone(user);
        });
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

    // add group to current group, remove from allowed
    $scope.addGroup = function (form, user, group) {

        user.availableGroups = _.without(user.availableGroups, _.findWhere(user.availableGroups, {id: group}));
        user.groups.push(_.findWhere($scope.allGroups, {id: group}));

        if (user.availableGroups[0]) {
            $scope.groupToAdd = user.availableGroups[0].id;
        }

        form.$setDirty(true);

    };

    $scope.init();
}]);
