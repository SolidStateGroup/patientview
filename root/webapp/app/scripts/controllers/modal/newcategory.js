'use strict';
var NewCategoryModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'editCategory', 'CategoryService',
function ($scope, $rootScope, $modalInstance, editCategory, CategoryService) {
    $scope.editCategory = editCategory;
    $scope.editMode = false;

    $scope.save = function () {
        CategoryService.create($scope.editCategory).then(function(result) {
            $scope.editCategory = result;
            $modalInstance.close($scope.editCategory);
        }, function(result) {
            if (result.data) {
                $scope.errorMessage = result.data;
            } else {
                $scope.errorMessage = 'There was an error';
            }
        });
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
}];
