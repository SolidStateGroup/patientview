'use strict';

angular.module('patientviewApp').controller('StatisticsCtrl', ['GroupService', '$scope', '$rootScope', function (UserService,$scope,$rootScope) {


    GroupService.getStatistics($scope.editGroup.id).then(function(data) {
        $scope.statistics = data;
    });

}]);
