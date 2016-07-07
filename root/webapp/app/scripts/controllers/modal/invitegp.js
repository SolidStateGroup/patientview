'use strict';
var InviteGpModalInstanceCtrl = ['$rootScope', '$scope', '$modalInstance', 'patient', 'GpService',
    function ($rootScope, $scope, $modalInstance, patient, GpService) {
        $scope.practitioner = patient.practitioners[0];
        delete $scope.completed;
        delete $scope.loading;

        $scope.inviteGp = function () {
            $scope.loading = true;
            delete $scope.errorMessage;
            GpService.inviteGp($rootScope.loggedInUser.id, patient).then(function() {
                delete $scope.loading;
                $scope.completed = true;
            },
            function(error) {
                delete $scope.loading;
                $scope.errorMessage = error.data;
            });
        };
        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }];
