'use strict';
var DeleteLetterModalInstanceCtrl = ['$scope', '$modalInstance', 'letter', 'LetterService',
function ($scope, $modalInstance, letter, LetterService) {
    $scope.letter = letter;
    $scope.successMessage = '';
    $scope.errorMessage = '';
    $scope.modalMessage = '';

    $scope.remove = function () {
        LetterService.remove($scope.loggedInUser.id, letter.group.id, letter.date).then(function() {
            $scope.errorMessage = '';
            $scope.successMessage = 'Letter has been deleted.';
        }, function() {
            $scope.successMessage = '';
            $scope.errorMessage = 'There was an error.';
        });
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
}];
