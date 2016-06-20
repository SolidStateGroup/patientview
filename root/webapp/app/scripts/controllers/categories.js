'use strict';
angular.module('patientviewApp').controller('CategoriesCtrl', ['$scope','$timeout', '$modal','CategoryService',
function ($scope, $timeout, $modal, CategoryService) {

    $scope.itemsPerPage = 20;
    $scope.currentPage = 0;
    $scope.filterText = '';
    $scope.sortField = 'number';
    $scope.sortDirection = 'ASC';

    var tempFilterText = '';
    var filterTextTimeout;

    // watches
    // update page on user typed search text
    $scope.$watch('searchText', function (value) {
        if (value !== undefined) {
            if (filterTextTimeout) {
                $timeout.cancel(filterTextTimeout);
            }
            $scope.currentPage = 0;

            tempFilterText = value;
            filterTextTimeout = $timeout(function () {
                $scope.filterText = tempFilterText;
                $scope.getItems();
            }, 2000); // delay 2000 ms
        }
    });

    // update page when currentPage is changed (and at start)
    $scope.$watch('currentPage', function(value) {
        $scope.currentPage = value;
        $scope.getItems();
    });

    $scope.sortBy = function(sortField) {
        $scope.currentPage = 0;
        if ($scope.sortField !== sortField) {
            $scope.sortDirection = 'ASC';
            $scope.sortField = sortField;
        } else {
            if ($scope.sortDirection === 'ASC') {
                $scope.sortDirection = 'DESC';
            } else {
                $scope.sortDirection = 'ASC';
            }
        }

        $scope.getItems();
    };

    $scope.getItems = function() {
        $scope.loading = true;
        delete $scope.successMessage;

        var getParameters = {};
        getParameters.page = $scope.currentPage;
        getParameters.size = $scope.itemsPerPage;
        getParameters.filterText = $scope.filterText;
        getParameters.sortField = $scope.sortField;
        getParameters.sortDirection = $scope.sortDirection;

        CategoryService.getAll(getParameters).then(function(page) {
            $scope.pagedItems = page.content;
            $scope.total = page.totalElements;
            $scope.totalPages = page.totalPages;
            $scope.loading = false;
        }, function() {
            $scope.loading = false;
        });
    };


    // Opened for edit
    $scope.opened = function (openedCategory) {
        delete $scope.editSuccessMessage;
        delete $scope.editErrorMessage;

        if (openedCategory.showEdit) {
            $scope.editCategory = '';
            openedCategory.showEdit = false;
        } else {
            // close others
            for (var i = 0; i < $scope.pagedItems.length; i++) {
                $scope.pagedItems[i].showEdit = false;
            }

            $scope.editCategory = '';
            openedCategory.showEdit = true;
            openedCategory.editLoading = true;

            CategoryService.get(openedCategory.id).then(function (category) {
                $scope.successMessage = '';
                $scope.saved = '';
                $scope.editCategory = _.clone(category);
                $scope.editMode = true;
                openedCategory.editLoading = false;
            });
        }
    };

    $scope.openModalNewCategory = function () {
        delete $scope.editSuccessMessage;
        delete $scope.editErrorMessage;

        // close any open edit panels
        for (var i = 0; i < $scope.pagedItems.length; i++) {
            $scope.pagedItems[i].showEdit = false;
        }
        $scope.errorMessage = '';
        $scope.successMessage = '';
        $scope.codeCreated = '';
        $scope.editCategory = {};

        var modalInstance = $modal.open({
            templateUrl: 'views/modal/newCategoryModal.html',
            controller: NewCategoryModalInstanceCtrl,
            size: 'lg',
            backdrop: 'static',
            resolve: {
                editCategory: function(){
                    return $scope.editCategory;
                },
                CategoryService: function(){
                    return CategoryService;
                }
            }
        });

        modalInstance.result.then(function () {
            $scope.getItems();
            $scope.successMessage = 'Category successfully created';
            $scope.categoryCreated = true;
        }, function () {
            $scope.editCode = '';
        });
    };

    // delete category, opens modal
    $scope.remove = function (categoryId, $event) {
        $event.stopPropagation();
        $scope.successMessage = '';

        CategoryService.get(categoryId).then(function(category) {
            var modalInstance = $modal.open({
                templateUrl: 'views/modal/deleteCategoryModal.html',
                controller: DeleteCategoryModalInstanceCtrl,
                resolve: {
                    category: function(){
                        return category;
                    },
                    CategoryService: function(){
                        return CategoryService;
                    }
                }
            });

            modalInstance.result.then(function () {
                $scope.filterText = '';
                $scope.getItems();
                $scope.successMessage = 'Category successfully deleted';
            }, function () {
                // closed
            });
        });
    };

    // Save code details from edit
    $scope.save = function (editCategoryForm, category) {
        delete $scope.editSuccessMessage;
        delete $scope.editErrorMessage;

        CategoryService.save(category).then(function(successResult) {
            editCategoryForm.$setPristine(true);

            // update header details (code, type, standard, description)
            for(var i=0;i<$scope.pagedItems.length;i++) {
                if($scope.pagedItems[i].id === category.id) {
                    var headerDetails = $scope.pagedItems[i];
                    headerDetails.number = successResult.number;
                    headerDetails.friendlyDescription = successResult.friendlyDescription;
                    headerDetails.icd10Description = successResult.icd10Description;
                }
            }

            $scope.editSuccessMessage = 'Category saved';
        }, function(failureResult) {
            $scope.editErrorMessage = failureResult.data;
        });
    };
}]);
