'use strict';

angular.module('patientviewApp').controller('DiagnosticsCtrl',['$scope', 'DiagnosticService',
function ($scope, DiagnosticService) {

    var init = function(){
        $scope.loading = true;

        DiagnosticService.getByUserId($scope.loggedInUser.id).then(function(diagnostics) {
            $scope.diagnostics = diagnostics;
            $scope.predicate = 'date';
            $scope.reverse = true;
            $scope.loading = false;
        }, function () {
            alert('Cannot get diagnostics');
            $scope.loading = false;
        })
    };

    init();
}]);
