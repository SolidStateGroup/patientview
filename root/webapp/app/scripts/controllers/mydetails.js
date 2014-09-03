'use strict';

angular.module('patientviewApp').controller('MydetailsCtrl',['$scope', 'PatientService',
function ($scope, PatientService) {

    $scope.init = function(){
        $scope.loading = true;
        PatientService.get($scope.loggedInUser.id).then(function (patientDetails) {
            $scope.patientDetails = patientDetails;
            $scope.loading = false;
        }, function () {
            $scope.loading = false;
            alert('Error getting patient details');
        });
    };

    $scope.init();
}]);
