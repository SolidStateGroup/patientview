'use strict';

// group filter
angular.module('patientviewApp').filter('groupFilter', [function () {
    return function (users, selectedGroups) {
        if (!angular.isUndefined(users) && !angular.isUndefined(selectedGroups) && selectedGroups.length > 0) {
            var tempUsers = [];
            angular.forEach(users, function (user) {
                angular.forEach(user.groupRoles, function (groupRole) {
                    if(_.contains(selectedGroups,groupRole.group.id) && !_.findWhere(tempUsers, {id:user.id})) {
                        tempUsers.push(user);
                    }
                });
            });
            return tempUsers;
        } else {
            return users;
        }
    };
}]);
