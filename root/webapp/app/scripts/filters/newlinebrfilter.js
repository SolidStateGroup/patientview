'use strict';

angular.module('patientviewApp').filter('newlinebrFilter', [function () {
    return function (text) {
        if (text) {
            return text.replace(/(\r\n|\n|\r)/gm, "<br>");
        }
    }
}]);