angular.module('patientviewApp').controller('AccountCtrl', ['UserService', 'AuthService', '$scope', '$rootScope', '$location', function (UserService,AuthService,$scope,$rootScope,$location) {
    UserService.get($rootScope.loggedInUser.id).then(function(data) {
        $scope.userdetails = data;
    });

    $scope.saveSettings = function (form) {

        // If the email field has been changed validate emails

        if (!$scope.userdetails.confirmEmail) {
            $scope.errorMessage = "Please confirm the email address";
        } else {
            if ($scope.userdetails.confirmEmail === $scope.userdetails.email) {
                this.saveUser($scope);
            } else {
                $scope.errorMessage = "The emails do not match";
            }
         }

    };

    $scope.savePassword = function (form) {
        AuthService.login({'username': $scope.userdetails.username, 'password': $scope.userdetails.currentPassword}).then(function (authenticationResult) {
            this.saveUser($scope);
            $scope.successMessage = 'Password has been changed';
        }, function(result) {
            if (result.data) {
                $scope.passwordErrorMessage = ' - ' + result.data;
            } else {
                $scope.passwordErrorMessage = ' ';
            }
        });
        $scope.passwordErrorMessage = 'Unable to change password';
    };


    $scope.saveUser = function ($scope) {
        UserService.save($scope.userdetails).then(function (result) {
            $scope.successMessage = 'The settings have been saved';
        }, function (result) {
            $scope.errorMessage = "The settings have not been saved " + result;
        });
    }

}]);