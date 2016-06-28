'use strict';
var UpdateFoodDiaryModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'foodDiary', 'FoodDiaryService',
    'UtilService',
    function ($scope, $rootScope, $modalInstance, foodDiary, FoodDiaryService, UtilService) {
        $scope.editFoodDiary = foodDiary;
        $scope.editMode = true;

        $scope.days = UtilService.generateDays();
        $scope.months = UtilService.generateMonths();
        $scope.years = UtilService.generateYears2000();
        $scope.dateNutrition = {};
        var date = new Date($scope.editFoodDiary.dateNutrition);
        $scope.dateNutrition.day = (date.getDate() < 10 ? "0" : null) + (date.getDate());
        $scope.dateNutrition.month = (date.getMonth()+1 < 10 ? "0" : null) + (date.getMonth()+1);
        $scope.dateNutrition.year = date.getFullYear();

        $scope.save = function () {
            $scope.editFoodDiary.dateNutrition
                = new Date($scope.dateNutrition.year, $scope.dateNutrition.month - 1, $scope.dateNutrition.day);

            FoodDiaryService.update($scope.loggedInUser.id, $scope.editFoodDiary).then(function(result) {
                $scope.editFoodDiary = result;
                $modalInstance.close($scope.editFoodDiary);
            }, function(result) {
                if (result.data) {
                    $scope.errorMessage = ' - ' + result.data;
                } else {
                    $scope.errorMessage = ' ';
                }
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }];
