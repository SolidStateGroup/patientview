'use strict';

angular.module('patientviewApp').controller('LogoutCtrl', ['$rootScope', 'AuthService', function ($rootScope, AuthService) {
    AuthService.logout($rootScope.authToken).then(function () {
        $rootScope.logout();
    }, function () {
        // failure to log out of server (missing token), log out anyway
        $rootScope.logout();
    });
}]);
