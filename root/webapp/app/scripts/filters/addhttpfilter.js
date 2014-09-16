'use strict';

// add http filter (adds http:// to strings without http:// present)
angular.module('patientviewApp').filter('addhttpFilter', [function () {
    return function (text) {
        if (text !== undefined) {
            if (text.indexOf('http://') > -1) {
                return text;
            } else {
                return 'http://' + text;
            }
        }
    };
}]);
