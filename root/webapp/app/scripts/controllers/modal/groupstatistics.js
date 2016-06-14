'use strict';
var GroupStatisticsModalInstanceCtrl = ['$scope', '$modalInstance', 'statistics', 'UtilService',
function ($scope, $modalInstance, statistics, UtilService) {
    $scope.statistics = statistics;
    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
    $scope.formatDate = function(dateString) {
        var datesplit = dateString.split('-');
        return UtilService.getMonthText(datesplit[1] - 1) + ' ' + datesplit[0];
    };
}];
