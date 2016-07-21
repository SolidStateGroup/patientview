'use strict';
var DeleteFoodDiaryModalInstanceCtrl = ['$scope', '$modalInstance', 'foodDiary', 'FoodDiaryService',
function ($scope, $modalInstance, foodDiary, FoodDiaryService) {
    $scope.foodDiary = foodDiary;
    $scope.successMessage = '';
    $scope.errorMessage = '';
    $scope.modalMessage = '';

    $scope.remove = function () {
        FoodDiaryService.remove($scope.loggedInUser.id, foodDiary.id).then(function() {
            $scope.errorMessage = '';
            $scope.successMessage = 'Food Diary entry has been deleted.';
        }, function() {
            $scope.successMessage = '';
            $scope.errorMessage = 'There was an error.';
        });
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
}];
