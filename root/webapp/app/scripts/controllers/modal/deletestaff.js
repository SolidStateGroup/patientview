'use strict';
var DeleteStaffModalInstanceCtrl = ['$scope', '$modalInstance','user','UserService',
function ($scope, $modalInstance, user, UserService) {
    $scope.user = user;
    $scope.successMessage = '';
    $scope.errorMessage = '';
    $scope.ok = function () {
        UserService.remove(user).then(function() {
            // successfully deleted user
            $modalInstance.close();
        }, function(failure) {
            // error
            $scope.errorMessage = 'There was an error: ' + failure;
        });
    };
    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
}];
