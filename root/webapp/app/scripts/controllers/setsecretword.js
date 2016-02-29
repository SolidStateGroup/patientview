'use strict';

angular.module('patientviewApp').controller('SetSecretWordCtrl', ['UserService', '$scope', '$rootScope',
    function (UserService, $scope, $rootScope) {

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
}]);
