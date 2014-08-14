'use strict';

angular.module('patientviewApp').controller('JoinRequestAdminCtrl', ['GroupService', 'JoinRequestService', 'StaticDataService', '$scope', '$rootScope', 'UtilService', function (GroupService,JoinRequestService,StaticDataService,$scope,$rootScope,UtilService) {

    $scope.filter = {};

    JoinRequestService.getStatuses().then(function(data) {
        $scope.statuses = data;
    });

    JoinRequestService.getByUser($rootScope.loggedInUser.id).then(function(data) {
        $scope.joinRequests = data;

    });

    $scope.filter = function (status) {
        JoinRequestService.getByType($rootScope.loggedInUser.id, status).then(function(data) {
            $scope.joinRequests = data;
        });
    };

    $scope.save = function(form, joinRequest) {
        $scope.joinRequest.status = form.joinRequest.newStatus;
        JoinRequestService.save(joinRequest);
        $scope.saved = true;
    };

}]);