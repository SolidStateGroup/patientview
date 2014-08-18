'use strict';

angular.module('patientviewApp').controller('JoinRequestAdminCtrl', ['GroupService', 'JoinRequestService', 'StaticDataService', '$scope', '$rootScope', 'UtilService', function (GroupService,JoinRequestService,StaticDataService,$scope,$rootScope,UtilService) {

    $scope.filter = {};

    JoinRequestService.getStatuses().then(function(data) {
        $scope.statuses = data;
    });

    JoinRequestService.getByUser($rootScope.loggedInUser.id).then(function(data) {
        $scope.joinRequests = $scope.initRequests(data);

    });

    $scope.filter = function (status) {
        JoinRequestService.getByType($rootScope.loggedInUser.id, status).then(function(data) {
            $scope.joinRequests = $scope.initRequests(data);
        });
    },

    $scope.save = function(form, joinRequest) {
        joinRequest.status = joinRequest.newStatus;
        JoinRequestService.save(joinRequest);
        $scope.saved = true;
        $rootScope.setSubmittedJoinRequestCount();
    },

    $scope.initRequests = function(requests) {
        requests.forEach(function(request) {
            request.newStatus = request.status;
        });
        return requests;
    };

}]);