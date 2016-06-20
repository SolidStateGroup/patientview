'use strict';
var DeleteCategoryModalInstanceCtrl = ['$scope', '$modalInstance','category', 'CategoryService',
function ($scope, $modalInstance, category, CategoryService) {
    $scope.category = category;
    $scope.ok = function () {
        CategoryService.remove(category.id).then(function() {
            $modalInstance.close();
        }, function(error) {
            $scope.errorMessage = error.data;
        });
    };
    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
}];
