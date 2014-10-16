'use strict';

angular.module('patientviewApp').controller('HelpCtrl',['$scope', function ($scope) {

    // set staff or patient based on user information
    if ($scope.loggedInUser.userInformation.staffFeatures) {
        $scope.staffUser = true;
    } else {
        $scope.patientUser = true;
    }

}]);
