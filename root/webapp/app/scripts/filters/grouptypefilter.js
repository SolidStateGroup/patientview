'use strict';

// role filter
angular.module('patientviewApp').filter('groupTypeFilter', [function () {
    return function (groups, selectedGroupTypes) {
        if (!angular.isUndefined(groups) && !angular.isUndefined(selectedGroupTypes) && selectedGroupTypes.length > 0) {
            var tempGroups = [];
            angular.forEach(groups, function (group) {
                if(_.contains(selectedGroupTypes,group.groupType.id) && !_.findWhere(tempGroups, {id:group.id})) {
                    tempGroups.push(group);
                }
            });
            return tempGroups;
        } else {
            return groups;
        }
    };
}]);