'use strict';
// delete food diary modal instance controller
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


// new food diary modal instance controller
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
        $scope.dateNutrition.day = Number((date.getDate() < 10 ? "0" : "") + (date.getDate()));
        $scope.dateNutrition.month = Number((date.getMonth()+1 < 10 ? "0" : "") + (date.getMonth()+1).toString());
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
            templateUrl: 'views/partials/deleteFoodDiaryModal.html',
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
            templateUrl: 'views/partials/newFoodDiaryModal.html',
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
            templateUrl: 'views/partials/newFoodDiaryModal.html',
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
