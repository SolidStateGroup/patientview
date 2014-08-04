angular.module('patientviewApp').controller('JoinRequestAdminCtrl', ['GroupService', 'JoinRequestService', 'StaticDataService', '$scope', '$rootScope', 'UtilService', function (GroupService,JoinRequestService,StaticDataService,$scope,$rootScope,UtilService) {

    $scope.filter = {};

    JoinRequestService.getStatuses().then(function(data) {
        $scope.statuses = data;
    });


    JoinRequestService.getByUser($rootScope.loggedInUser.id).then(function(data) {
        $scope.joinRequests = data;

    }),
    $scope.filter = function () {

    };

    $scope.save = function(form, joinRequest) {

    }

}]);