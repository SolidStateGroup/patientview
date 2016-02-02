'use strict';

angular.module('patientviewApp').controller('AdminCtrl', ['$scope',
    function ($scope) {

    $scope.updateGPs = function() {
        $scope.gpSuccessMessage = 'Updating master GP table';
    }
}]);