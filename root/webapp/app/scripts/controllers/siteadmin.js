'use strict';

angular.module('patientviewApp').controller('SiteAdminCtrl', ['$scope', 'GpService', 'UtilService', function ($scope, GpService, UtilService) {
    $scope.researchStudy = {};
    $scope.days = UtilService.generateDays();
    $scope.months = UtilService.generateMonths();
    $scope.years = UtilService.generateYears(new Date().getFullYear()-1,new Date().getFullYear()+10).reverse();
}]);
