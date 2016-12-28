'use strict';

angular.module('patientviewApp').factory('Mixins', [function () {
    return {
        something: function () {
            alert(this.somethingtext);
        }
    }
}]);