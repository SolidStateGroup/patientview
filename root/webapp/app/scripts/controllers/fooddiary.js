'use strict';
angular.module('patientviewApp').controller('FoodDiaryCtrl', ['$scope', '$modal', 'FoodDiaryService', 'UtilService',
function ($scope, $modal, FoodDiaryService, UtilService) {

    var init = function() {
        $scope.loading = true;
        $scope.currentPage = 1;
        $scope.entryLimit = 10;
        $scope.getItems();
    };

    // open modal (delete)
    $scope.deleteFoodDiary = function(foodDiary) {
        $scope.successMessage = '';

        var modalInstance = $modal.open({
            templateUrl: 'views/modal/deleteFoodDiaryModal.html',
            controller: DeleteFoodDiaryModalInstanceCtrl,
            size: 'lg',
            resolve: {
                foodDiary: function(){
                    return foodDiary;
                },
                FoodDiaryService: function(){
                    return FoodDiaryService;
                }
            }
        });

        modalInstance.result.then(function () {
            // ok (not used)
        }, function () {
            init();
        });
    };

    $scope.getItems = function() {
        FoodDiaryService.get($scope.loggedInUser.id).then(function (foodDiarys) {
            $scope.foodDiarys = foodDiarys;
            $scope.predicate = 'dateNutrition';
            $scope.reverse = true;
            $scope.loading = false;
        }, function () {
            alert('Cannot get food diary');
            $scope.loading = false;
        });
    };

    // open modal for new food diary
    $scope.openModalNewFoodDiary = function () {
        $scope.errorMessage = '';
        $scope.successMessage = '';
        $scope.foodDiaryCreated = '';
        $scope.editFoodDiary = {};

        var modalInstance = $modal.open({
            templateUrl: 'views/modal/newFoodDiaryModal.html',
            controller: NewFoodDiaryModalInstanceCtrl,
            size: 'lg',
            backdrop: 'static',
            resolve: {
                editFoodDiary: function(){
                    return $scope.editFoodDiary;
                },
                FoodDiaryService: function(){
                    return FoodDiaryService;
                },
                UtilService: function(){
                    return UtilService;
                }
            }
        });

        modalInstance.result.then(function () {
            $scope.getItems();
            $scope.successMessage = 'Food diary entry successfully created';
            $scope.codeCreated = true;
        }, function () {
            $scope.editCode = '';
        });
    };

    // client side sorting, pagination
    $scope.sortBy = function(predicate) {
        $scope.predicate = predicate;
        $scope.reverse = !$scope.reverse;
    };
    $scope.setPage = function(pageNo) {
        $scope.currentPage = pageNo;
    };

    // update modal (delete)
    $scope.updateFoodDiary = function(foodDiary) {
        $scope.successMessage = '';

        var modalInstance = $modal.open({
            templateUrl: 'views/modal/newFoodDiaryModal.html',
            controller: UpdateFoodDiaryModalInstanceCtrl,
            size: 'lg',
            resolve: {
                foodDiary: function(){
                    return foodDiary;
                },
                FoodDiaryService: function(){
                    return FoodDiaryService;
                },
                UtilService: function(){
                    return UtilService;
                }
            }
        });

        modalInstance.result.then(function () {
            // ok (not used)
        }, function () {
            init();
        });
    };

    init();
}]);
