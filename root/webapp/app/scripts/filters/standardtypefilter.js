'use strict';

// role filter
angular.module('patientviewApp').filter('standardTypeFilter', [function () {
    return function (codes, selectedStandardTypes) {
        if (!angular.isUndefined(codes) && !angular.isUndefined(selectedStandardTypes) && selectedStandardTypes.length > 0) {
            var tempCodes = [];
            angular.forEach(codes, function (code) {
                if(_.contains(selectedStandardTypes,code.standardType.id) && !_.findWhere(tempCodes, {id:code.id})) {
                    tempCodes.push(code);
                }
            });
            return tempCodes;
        } else {
            return codes;
        }
    };
}]);