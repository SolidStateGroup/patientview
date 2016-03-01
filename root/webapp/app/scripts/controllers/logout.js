'use strict';

angular.module('patientviewApp').controller('LogoutCtrl', ['$rootScope', 'AuthService', '$routeParams',
    function ($rootScope, AuthService, $routeParams) {
    if ($rootScope.authToken) {
        AuthService.logout($rootScope.authToken).then(function () {
            $rootScope.logout($routeParams.timeout !== undefined);
        }, function () {
            // failure to log out of server (missing token), log out anyway
            $rootScope.logout();
        });
    } else {
        $rootScope.logout();
    }
}]);
