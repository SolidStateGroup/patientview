'use strict';

angular.module('patientviewApp').controller('DashboardCtrl', ['UserService','$scope', '$rootScope', function (UserService,$scope,$rootScope) {
    UserService.get($rootScope.loggedInUser.id).then(function(data) {
        $scope.userdetails = data;
    });
}]);
