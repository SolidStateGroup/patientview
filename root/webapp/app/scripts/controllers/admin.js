'use strict';

angular.module('patientviewApp').controller('AdminCtrl', ['$scope', 'GpService', function ($scope, GpService) {
    $scope.updateGPs = function() {
        delete $scope.gpErrorMessage;
        $scope.updateGpRunning = true;
        $scope.gpSuccessMessage = 'Updating master GP table - Please wait';

        GpService.updateMasterTable().then(function(status) {
            delete $scope.updateGpRunning;
            $scope.gpSuccessMessage = 'Updating master GP table - Done, total: '
                + status.total + ', existing: ' + status.existing + ', new: ' + status.new;
        }, function(result) {
            delete $scope.updateGpRunning;
            delete $scope.gpSuccessMessage;
            $scope.gpErrorMessage = result.data;
        });
    }
}]);
