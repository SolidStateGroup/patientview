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

        if (status === 'All') {
            $scope.refresh();
        }

        JoinRequestService.getByStatus($rootScope.loggedInUser.id, selectedStatus).then(function(data) {
            $scope.joinRequests = $scope.initRequests(data);
        });
    },

    $scope.save = function(form, joinRequest) {
        joinRequest.status = joinRequest.newStatus;
        JoinRequestService.save(joinRequest);
        $scope.saved = true;
        $rootScope.setSubmittedJoinRequestCount();
        // once saved requery the join requests

    },

    $scope.refresh = function() {
        JoinRequestService.getByUser($rootScope.loggedInUser.id).then(function(data) {
            $scope.joinRequests = $scope.initRequests(data);
        });
    },

    $scope.initRequests = function(requests) {
        requests.forEach(function(request) {
            request.newStatus = request.status;
        });
        return requests;
    };

    // filter join request by status
    $scope.selectedStatus = [];
    $scope.setSelectStatus = function (status) {
        $scope.selectedStatus.push(status);
        JoinRequestService.getByStatus($rootScope.loggedInUser.id, $scope.selectedStatus).then(function(data) {
            $scope.joinRequests = $scope.initRequests(data);
        });

        return false;
    };
    $scope.isStatusChecked = function (status) {
        if (_.contains($scope.selectedStatus, status)) {
            return 'glyphicon glyphicon-ok pull-right';
        }
        return false;
    };

}]);