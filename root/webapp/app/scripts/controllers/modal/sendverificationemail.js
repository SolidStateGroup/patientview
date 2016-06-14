'use strict';
var SendVerificationEmailModalInstanceCtrl = ['$scope', '$modalInstance','user','UserService',
function ($scope, $modalInstance, user, UserService) {
    $scope.user = user;
    $scope.ok = function () {
        UserService.sendVerificationEmail(user).then(function() {
            // successfully sent verification email
            $modalInstance.close();
        }, function(){
            // error
            $scope.errorMessage = 'There was an error';
        });
    };
    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
}];
