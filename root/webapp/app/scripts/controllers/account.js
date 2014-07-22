angular.module('patientviewApp').controller('AccountCtrl', ['UserService', 'AuthService', '$scope', '$rootScope', 'UtilService', function (UserService,AuthService,$scope,$rootScope,UtilService) {
    UserService.get($rootScope.loggedInUser.id).then(function(data) {
        $scope.userdetails = data;
        $scope.userdetails.confirmEmail = $scope.userdetails.email;
    });

    $scope.saveSettings = function () {

        // If the email field has been changed validate emails
        $scope.successMessage = null;
        $scope.errorMessage = null;
        if (!$scope.userdetails.confirmEmail) {
            $scope.errorMessage = "Please confirm the email address";
        } else {
            // Email equals and correct
            if (($scope.userdetails.confirmEmail === $scope.userdetails.email)) {

                if (UtilService.validateEmail($scope.userdetails.email)) {
                    $scope.errorMessage = "Invalid format for email";
                } else {

                    UserService.save($scope.userdetails).then(function (result) {
                        $scope.successMessage = 'The settings have been saved';
                    }, function (result) {
                        $scope.errorMessage = "The settings have not been saved " + result;
                    });
                }
            } else {
                $scope.errorMessage = "The emails do not match";
            }
         }

    };

    $scope.savePassword = function () {
        $scope.successMessage = null;
        $scope.passwordErrorMessage = null;
        if ($scope.userdetails.newPassword !== $scope.userdetails.confirmPassword) {
            $scope.passwordErrorMessage = 'The passwords do not match';
        } else {

            AuthService.login({'username': $scope.userdetails.username, 'password': $scope.userdetails.currentPassword}).then(function (authenticationResult) {

                // set the password
                $scope.userdetails.password = $scope.userdetails.newPassword;

                UserService.changePassword($scope.userdetails).then(function (successResult) {
                    // successfully changed user password
                    $scope.successMessage = 'The password has been saved';
                }, function () {
                    // error
                    $scope.passwordErrorMessage = 'There was an error';
                });

            }, function (result) {
                if (result.data) {
                    $scope.passwordErrorMessage = ' - ' + result.data;
                } else {
                    $scope.passwordErrorMessage = ' ';
                }
            });
        }

    };

}]);