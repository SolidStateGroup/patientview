'use strict';

// role filter
angular.module('patientviewApp').filter('roleFilter', [function () {
    return function (users, selectedRoles) {
        if (!angular.isUndefined(users) && !angular.isUndefined(selectedRoles) && selectedRoles.length > 0) {
            var tempUsers = [];
            angular.forEach(users, function (user) {
                angular.forEach(user.groupRoles, function (groupRole) {
                    if(_.contains(selectedRoles,groupRole.role.id) && !_.findWhere(tempUsers, {id:user.id})) {
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