'use strict';

// add code filter, used when adding additional observation types to a chart
angular.module('patientviewApp').filter('addCodeFilter', [function () {
    return function (observationHeadings, codes) {
        if (!angular.isUndefined(observationHeadings) && !angular.isUndefined(codes) && observationHeadings.length > 0 && codes.length > 0) {
            var tempObservationHeadings = [];
            angular.forEach(observationHeadings, function (observationHeading) {
                if (!_.contains(codes, observationHeading.code)) {
                    tempObservationHeadings.push(observationHeading);
                }
            });
            return tempObservationHeadings;
        } else {
            return observationHeadings;
        }
    };
}]);