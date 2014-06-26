'use strict';

// role filter
angular.module('patientviewApp').filter('codeTypeFilter', [function () {
    return function (codes, selectedCodeTypes) {
        if (!angular.isUndefined(codes) && !angular.isUndefined(selectedCodeTypes) && selectedCodeTypes.length > 0) {
            var tempCodes = [];
            angular.forEach(codes, function (code) {
                if(_.contains(selectedCodeTypes,code.codeType.id) && !_.findWhere(tempCodes, {id:code.id})) {
                    tempCodes.push(code);
                }
            });
            return tempCodes;
        } else {
            return codes;
        }
    };
}]);