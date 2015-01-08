'use strict';

angular.module('patientviewApp').controller('StatisticsCtrl', ['GroupService', '$scope', function (GroupService, $scope) {

    GroupService.getStatistics($scope.editGroup.id).then(function(data) {
        $scope.statistics = data;
    });

}]);
