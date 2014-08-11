'use strict';

// truncate filter http://jsfiddle.net/tUyyx/
angular.module('patientviewApp').filter('truncateFilter', [function () {
    return function (text, length, end) {
        if (isNaN(length))
            length = 100;

        if (end === undefined)
            end = "...";

        if (text.length <= length || text.length - end.length <= length) {
            return text;
        }
        else {
            return String(text).substring(0, length-end.length)
                + '<span class="">'
                + String(text).substring(length-end.length, text.length)
                + end
                + '</span>' ;
        }
    }
}]);