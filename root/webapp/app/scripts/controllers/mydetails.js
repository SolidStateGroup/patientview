'use strict';

angular.module('patientviewApp').controller('MydetailsCtrl',['$scope', 'PatientService',
function ($scope, PatientService) {

    $scope.init = function(){
        $scope.loading = true;
        PatientService.get($scope.loggedInUser.id).then(function (patientDetails) {
            $scope.patientDetails = patientDetails;

            // set checkboxes
            for (var i=0;i<$scope.patientDetails.length;i++) {
                $scope.patientDetails[i].group.selected = true;
            }

            $scope.loading = false;
        }, function () {
            $scope.loading = false;
            alert('Error getting patient details');
        });
    };

    $scope.init();
}]);
