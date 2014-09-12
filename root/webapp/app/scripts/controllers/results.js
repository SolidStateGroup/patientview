'use strict';

angular.module('patientviewApp').controller('ResultsCtrl', ['$scope', 'ResultService',
    function ($scope, ResultService) {

    $scope.init = function() {
        ResultService.getSummary($scope.loggedInUser.id).then(function(summary) {
            console.log(summary);
        }, function () {
            alert('Cannot get results summary');
        })
    };

    $scope.init();
}]);
