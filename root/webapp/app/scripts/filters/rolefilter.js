'use strict';

// role filter
angular.module('patientviewApp').filter('roleFilter', [function () {
    return function (users, selectedRole) {
        if (!angular.isUndefined(users) && !angular.isUndefined(selectedRole) && selectedRole.length > 0) {
            var tempUsers = [];
            angular.forEach(selectedRole, function (id) {
                angular.forEach(users, function (user) {
                    angular.forEach(user.groups, function (group) {
                        if ((group.role.id === id) && !_.findWhere(tempUsers, {id: user.id})) {
                            tempUsers.push(user);
                        }
                    });
                });
            });
            return tempUsers;
        } else {
            return users;
        }
    };
}]);
