'use strict';

angular.module('patientviewApp').controller('StaffDetailsCtrl', ['$scope', function ($scope) {
    // add group to current group, remove from allowed
    $scope.addGroup = function (form, user, groupId) {
        if(_.findWhere(user.availableGroups, {id: groupId}) && _.findWhere($scope.allowedRoles, {id: user.selectedRole})) {
            user.availableGroups = _.without(user.availableGroups, _.findWhere(user.availableGroups, {id: groupId}));
            var newGroup = _.findWhere($scope.allGroups, {id: groupId});
            newGroup.role = _.findWhere($scope.allowedRoles, {id: user.selectedRole});
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
}]);
