'use strict';

// Please note that $modalInstance represents a modal window (instance) dependency.
// It is not the same as the $modal service used above.
var NewStaffModalInstanceCtrl = ['$scope', '$modalInstance', 'newUser', 'allGroups', 'allRoles', 'allFeatures', 'UserService',
function ($scope, $modalInstance, newUser, allGroups, allRoles, allFeatures, UserService) {

    $scope.newUser = newUser;

    // add feature to current feature, remove from allowed
    $scope.addFeature = function (form, user, featureId) {
        if(_.findWhere(user.availableFeatures, {id: featureId})) {
            user.availableFeatures = _.without(user.availableFeatures, _.findWhere(user.availableFeatures, {id: featureId}));
            var feature = _.findWhere(allFeatures, {id: featureId});
            user.features.push(feature);
            form.$setDirty(true);
        }
    };

    // remove feature from current features, add to allowed features
    $scope.removeFeature = function (form, user, feature) {
        user.features = _.without(user.features, _.findWhere(user.features, {id: feature.id}));
        user.availableFeatures.push(feature);
        form.$setDirty(true);
    };

    // add group to current group, remove from allowed
    $scope.addGroup = function (form, user, groupId) {
        if(_.findWhere(user.availableGroups, {id: groupId}) && _.findWhere(allRoles, {id: user.selectedRole})) {
            user.availableGroups = _.without(user.availableGroups, _.findWhere(user.availableGroups, {id: groupId}));
            var newGroup = _.findWhere(allGroups, {id: groupId});
            newGroup.role = _.findWhere(allRoles, {id: user.selectedRole});
            user.groups.push(newGroup);
            user.selectedRole = '';

            // for REST compatibility
            user.groupRoles = [];
            for(var i=0;i<user.groups.length;i++) {
                var group = user.groups[i];
                user.groupRoles.push({'group': group, 'role': group.role});
            }

            form.$setDirty(true);
        }
    };

    // remove group from current groups, add to allowed groups
    $scope.removeGroup = function (form, user, group) {
        user.groups = _.without(user.groups, _.findWhere(user.groups, {id: group.id}));
        user.availableGroups.push(group);

        // for REST compatibility
        user.groupRoles = [];
        for(var i=0;i<user.groups.length;i++) {
            var tempGroup = user.groups[i];
            user.groupRoles.push({'group': tempGroup, 'role': tempGroup.role});
        }

        form.$setDirty(true);
    };

    $scope.ok = function () {
        UserService.new($scope.newUser).then(function(result) {
            //$scope.newUser.id = result.id;
            $scope.newUser = result;
            $modalInstance.close($scope.newUser);
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

// angular-ui bootstrap modal, http://angular-ui.github.io/bootstrap/
angular.module('patientviewApp').controller('NewStaffModalCtrl',['$scope','$modal','UserService',
    function ($scope, $modal, UserService) {
        $scope.open = function (size) {
            $scope.errorMessage = '';
            // create new user with list of available roles, groups and features
            $scope.newUser = {};
            $scope.newUser.roles = $scope.allRoles;
            $scope.newUser.availableGroups = $scope.allGroups;
            $scope.newUser.groups = [];
            $scope.newUser.availableFeatures = $scope.allFeatures;
            $scope.newUser.features = [];
            $scope.newUser.selectedRole = '';

            var modalInstance = $modal.open({
                templateUrl: 'newStaffModal.html',
                controller: NewStaffModalInstanceCtrl,
                size: size,
                resolve: {
                    newUser: function(){
                        return $scope.newUser;
                    },
                    allGroups: function(){
                        return $scope.allGroups;
                    },
                    allRoles: function(){
                        return $scope.allRoles;
                    },
                    allFeatures: function(){
                        return $scope.allFeatures;
                    },
                    UserService: function(){
                        return UserService;
                    }
                }
            });

            modalInstance.result.then(function (newUser) {
                $scope.newUser = newUser;
                $scope.list.push(newUser);
                // ok (success)
            }, function () {
                // cancel
                $scope.newUser = '';
            });
        };
    }]);
