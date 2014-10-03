'use strict';

angular.module('patientviewApp').controller('DiagnosticsCtrl',['$scope', 'PatientService', 'GroupService',
function ($scope, PatientService, GroupService) {

    var init = function(){
        $scope.loading = true;

        $scope.loading = false;
    };

    init();
}]);
