'use strict';

angular.module('patientviewApp').controller('AdminCtrl', ['$scope', 'GpService', function ($scope, GpService) {
    $scope.updateGPs = function() {
        delete $scope.gpErrorMessage;
        $scope.gpSuccessMessage = 'Updating master GP table';

        GpService.updateMasterTable().then(function() {
            $scope.gpSuccessMessage = 'Updating master GP table - job started';
        }, function(result) {
            delete $scope.gpSuccessMessage;
            $scope.gpErrorMessage = result;
        });
    }
}]);
