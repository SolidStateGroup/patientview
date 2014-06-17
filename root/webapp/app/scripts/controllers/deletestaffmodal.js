'use strict';

// Please note that $modalInstance represents a modal window (instance) dependency.
// It is not the same as the $modal service used above.
var DeleteStaffModalInstanceCtrl = ['$scope', '$modalInstance','user','UserService',
function ($scope, $modalInstance, user, UserService) {

    $scope.user = user;

    $scope.ok = function () {
        UserService.delete($scope.user).then(function(result) {
            $modalInstance.close();
        });
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
}];
