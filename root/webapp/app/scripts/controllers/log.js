'use strict';

angular.module('patientviewApp').controller('LogCtrl',['$scope', 'AuditService',
function ($scope, AuditService) {

    var init = function() {
        $scope.loading = true;

        AuditService.getAll().then(function(audits) {
            $scope.audits = audits;
            $scope.loading = false;
        }, function () {
            alert('Cannot get logs');
            $scope.loading = false;
        })
    };

    init();
}]);
