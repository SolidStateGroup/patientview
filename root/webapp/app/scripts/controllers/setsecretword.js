'use strict';

angular.module('patientviewApp').controller('SetSecretWordCtrl', ['AuthService', 'UserService', '$scope', '$rootScope',
    'localStorageService', '$location',
    function (AuthService, UserService, $scope, $rootScope, localStorageService, $location) {

    $scope.saveSecretWord = function () {
        $scope.loading = true;
        $scope.secretWordSuccessMessage = null;
        $scope.secretWordErrorMessage = null;

        if ($scope.secretWord1 !== $scope.secretWord2) {
            $scope.secretWordErrorMessage = 'The secret words do not match';
        } else {
            var secretWordInput = {};
            secretWordInput.secretWord1 = $scope.secretWord1;
            secretWordInput.secretWord2 = $scope.secretWord2;
            UserService.changeSecretWord($rootScope.loggedInUser.id, secretWordInput).then(function () {
                $scope.secretWordSuccessMessage = 'Your secret word has been saved';
                $rootScope.loggedInUser.hideSecretWordNotification = true;
                $rootScope.loggedInUser.secretWordIsSet = true;
                $scope.loading = false;
            }, function () {
                $scope.secretWordErrorMessage = 'There was an error setting your secret word';
                $scope.loading = false;
            });
        }
    };

    // same procedure as login, using previously authenticated token
    $scope.getUserInformation = function() {
        $scope.loading = true;
        AuthService.getUserInformation({'token': $rootScope.authToken}).then(function (userInformation) {
            // set user information
            var user = userInformation.user;
            delete userInformation.user;
            user.userInformation = userInformation;

            // set logged in user
            $rootScope.loggedInUser = user;
            localStorageService.set('loggedInUser', user);

            // set routes
            $rootScope.routes = userInformation.routes;
            localStorageService.set('routes', userInformation.routes);

            // manually call buildroute, ios fix
            $rootScope.buildRoute();

            // redirect to dashboard
            $location.path('/dashboard');
            $scope.loading = false;
        });
    }
}]);
