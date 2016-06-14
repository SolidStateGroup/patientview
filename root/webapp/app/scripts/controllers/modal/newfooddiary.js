'use strict';
// new food diary modal instance controller
var NewFoodDiaryModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'editFoodDiary', 'FoodDiaryService',
    'UtilService',
    function ($scope, $rootScope, $modalInstance, editFoodDiary, FoodDiaryService, UtilService) {
        $scope.editFoodDiary = editFoodDiary;
        $scope.editMode = false;

        $scope.days = UtilService.generateDays();
        $scope.months = UtilService.generateMonths();
        $scope.years = UtilService.generateYears2000();
        $scope.dateNutrition = {};
        var i;

        var currentDate = new Date();
        for (i=0;i<$scope.days.length;i++) {
            if (parseInt($scope.days[i]) === currentDate.getDate()) {
                $scope.dateNutrition.day = $scope.days[i];
            }
        }
        for (i=0;i<$scope.months.length;i++) {
            if (parseInt($scope.months[i]) === currentDate.getMonth() + 1) {
                $scope.dateNutrition.month = $scope.months[i];
            }
        }
        for (i=0;i<$scope.years.length;i++) {
            if (parseInt($scope.years[i]) === currentDate.getFullYear()) {
                $scope.dateNutrition.year = $scope.years[i];
            }
        }

        $scope.save = function () {
            $scope.editFoodDiary.dateNutrition
                = new Date($scope.dateNutrition.year, $scope.dateNutrition.month - 1, $scope.dateNutrition.day);

            FoodDiaryService.add($scope.loggedInUser.id, $scope.editFoodDiary).then(function(result) {
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
