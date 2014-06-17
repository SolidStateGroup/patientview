'use strict';

angular.module('patientviewApp').controller('LogoutCtrl', ['$rootScope', 'AuthService', function ($rootScope, AuthService) {
    AuthService.logout($rootScope.authToken).then(function () {
        $rootScope.logout();
    });
}]);
