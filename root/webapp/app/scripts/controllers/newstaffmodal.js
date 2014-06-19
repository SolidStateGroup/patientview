'use strict';

// Please note that $modalInstance represents a modal window (instance) dependency.
// It is not the same as the $modal service used above.
var NewStaffModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'newUser', 'allGroups', 'allRoles', 'allFeatures', 'UserService', 'SecurityService',
function ($scope, $rootScope, $modalInstance, newUser, allGroups, allRoles, allFeatures, UserService, SecurityService) {

    $scope.user = newUser;

    // add feature to current feature, remove from allowed
    $scope.addFeature = function (form, user, featureId) {
        if(_.findWhere(user.availableFeatures, {id: featureId})) {
            user.availableFeatures = _.without(user.availableFeatures, _.findWhere(user.availableFeatures, {id: featureId}));
            var feature = _.findWhere(allFeatures, {id: featureId});
            user.userFeatures.push(feature);
            form.$setDirty(true);
        }
    };

    // remove feature from current features, add to allowed features
    $scope.removeFeature = function (form, user, feature) {
        user.userFeatures = _.without(user.userFeatures, _.findWhere(user.userFeatures, {id: feature.id}));
        user.availableFeatures.push(feature);
        form.$setDirty(true);
    };

    // on select role, update available groups
    $scope.selectRole = function(form, user, $event) {
       /* var roleId = $event.target.dataset.id;
        user.availableGroups = [];
        SecurityService.getAvailableGroupsFromUserAndRole($rootScope.loggedInUser.id, roleId).then(function(availableGroups) {
            user.availableGroups = availableGroups;
        });
        form.$setDirty(true);*/
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
        UserService.new($scope.user).then(function(result) {
            $scope.user = result;
            $modalInstance.close($scope.user);
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
angular.module('patientviewApp').controller('NewStaffModalCtrl',['$scope','$modal','UserService','SecurityService',
    function ($scope, $modal, UserService, SecurityService) {
        $scope.open = function (size) {
            $scope.errorMessage = '';
            $scope.successMessage = '';
            // create new user with list of available roles, groups and features
            $scope.user = {};
            $scope.user.roles = $scope.allRoles;
            $scope.user.availableGroups = $scope.allGroups;
            $scope.user.groups = [];
            $scope.user.availableFeatures = $scope.allFeatures;
            $scope.user.userFeatures = [];
            $scope.user.selectedRole = '';

            var modalInstance = $modal.open({
                templateUrl: 'newStaffModal.html',
                controller: NewStaffModalInstanceCtrl,
                size: size,
                resolve: {
                    newUser: function(){
                        return $scope.user;
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
                    },
                    SecurityService: function(){
                        return SecurityService;
                    }
                }
            });

            modalInstance.result.then(function (user) {
                //$scope.user = user;
                $scope.list.push(user);
                $scope.user = user;
                $scope.successMessage = 'User successfully created';
                // ok (success)
            }, function () {
                // cancel
                $scope.user = '';
            });
        };
    }]);
