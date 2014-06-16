'use strict';

// group filter
angular.module('patientviewApp').filter('groupFilter', [function () {
    return function (users, selectedGroup) {
        if (!angular.isUndefined(users) && !angular.isUndefined(selectedGroup) && selectedGroup.length > 0) {
            var tempUsers = [];
            angular.forEach(selectedGroup, function (id) {
                angular.forEach(users, function (user) {
                    if (_.findWhere(user.groups, {id: id}) && !_.findWhere(tempUsers, {id:user.id})) {
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
