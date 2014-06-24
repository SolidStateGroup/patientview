'use strict';

angular.module('patientviewApp').factory('UtilService', [
function () {
    return {
        generatePassword: function () {
            var password = '';
            var possible = 'ABCDEFGHKMNPQRSTUVWXYZabcdefghkmnopqrstuvwxyz123456789';
            for (var k=0;k<5;k++) {
                password += possible.charAt(Math.floor(Math.random() * possible.length));
            }
            return password;
        }
    };
}]);
