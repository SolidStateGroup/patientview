'use strict';

angular.module('patientviewApp').controller('LogoutCtrl', ['$rootScope', function ($rootScope) {
    $rootScope.logout();
}]);
