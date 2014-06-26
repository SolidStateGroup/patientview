'use strict';

// Please note that $modalInstance represents a modal window (instance) dependency.
// It is not the same as the $modal service used above.
var NewStaffModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'newUser', 'allGroups', 'allRoles', 'allFeatures', 'UserService', 'SecurityService',
function ($scope, $rootScope, $modalInstance, newUser, allGroups, allRoles, allFeatures, UserService, SecurityService) {

    $scope.editUser = newUser;

    // add feature to current feature, remove from allowed
    $scope.addFeature = function (form, user, featureId) {
        for (var j = 0; j < user.availableFeatures.length; j++) {
            if (user.availableFeatures[j].feature.id === featureId) {
                user.userFeatures.push(user.availableFeatures[j]);
                user.availableFeatures.splice(j, 1);
            }
        }
        form.$setDirty(true);
    };

    // remove feature from current features, add to allowed features
    $scope.removeFeature = function (form, user, feature) {
        for (var j = 0; j < user.userFeatures.length; j++) {
            if (user.userFeatures[j].feature.id === feature.feature.id) {
                user.availableFeatures.push(user.userFeatures[j]);
                user.userFeatures.splice(j, 1);
            }
        }
        form.$setDirty(true);
    };

    // on select role, update available groups
    $scope.selectRole = function(form, user, $event) {
        /*var roleId = $event.target.dataset.id;
        user.availableGroups = [];
        SecurityService.getSecurityGroupsByUserAndRole($rootScope.loggedInUser.id, roleId).then(function(availableGroups) {
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
        UserService.new($scope.editUser).then(function(result) {
            $scope.editUser = result;
            $modalInstance.close($scope.editUser);
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
            $scope.userCreated = '';
            // create new user with list of available roles, groups and features
            //console.log($scope.allGroups);
            $scope.editUser = {};
            $scope.editUser.roles = $scope.allRoles;
            $scope.editUser.availableGroups = $scope.allGroups;
            $scope.editUser.groups = [];
            $scope.editUser.availableFeatures = _.clone($scope.allFeatures);
            $scope.editUser.userFeatures = [];
            $scope.editUser.selectedRole = '';

            var modalInstance = $modal.open({
                templateUrl: 'newStaffModal.html',
                controller: NewStaffModalInstanceCtrl,
                size: size,
                resolve: {
                    newUser: function(){
                        return $scope.editUser;
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
                $scope.editUser = user;
                $scope.successMessage = 'User successfully created';
                $scope.userCreated = true;
                // ok (success)
            }, function () {
                // cancel
                $scope.editUser = '';
            });
        };
    }]);
