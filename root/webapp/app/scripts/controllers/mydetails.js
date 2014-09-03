'use strict';

angular.module('patientviewApp').controller('MydetailsCtrl',['$scope', 'PatientService',
function ($scope, PatientService) {

    $scope.init = function(){
        PatientService.get($scope.loggedInUser.id).then(function (patients) {
            $scope.patients = patients;
        }, function () {
            alert('Error getting patient details');
        });
    };

    $scope.init();
}]);
