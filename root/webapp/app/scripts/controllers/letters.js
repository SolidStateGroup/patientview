'use strict';
angular.module('patientviewApp').controller('LettersCtrl', ['$scope', '$modal', 'LetterService',
function ($scope, $modal, LetterService) {

    var init = function(){
        $scope.loading = true;
        $scope.currentPage = 1;
        $scope.entryLimit = 10;

        LetterService.getByUserId($scope.loggedInUser.id).then(function(letters) {
            $scope.letters = letters;
            $scope.predicate = 'date';
            $scope.reverse = true;
            $scope.loading = false;
        }, function () {
            alert('Cannot get letters');
            $scope.loading = false;
        });
    };

    $scope.openExportToCSVModal = function () {

        var modalInstance = $modal.open({
            templateUrl: 'views/partials/exportToCSVModal.html',
            controller: "ExportInfoModalInstanceCtrl",
            size: 'sm',
            windowClass: 'results-modal',
            resolve: {
                result: function(){
                    return true;
                }
            }
        });

        modalInstance.result.then(function () {
            // ok (not used)
        }, function () {
            // closed
        });
    };
    // open modal (view)
    $scope.viewLetter = function(letter) {
        $scope.successMessage = '';

        var modalInstance = $modal.open({
            templateUrl: 'views/partials/viewLetterModal.html',
            controller: ViewLetterModalInstanceCtrl,
            size: 'lg',
            resolve: {
                letter: function(){
                    return letter;
                }
            }
        });

        modalInstance.result.then(function () {
            // ok (not used)
        }, function () {
            // closed
        });
    };

    // open modal (delete)
    $scope.deleteLetter = function(letter) {
        $scope.successMessage = '';

        var modalInstance = $modal.open({
            templateUrl: 'views/partials/deleteLetterModal.html',
            controller: DeleteLetterModalInstanceCtrl,
            size: 'lg',
            resolve: {
                letter: function(){
                    return letter;
                },
                LetterService: function(){
                    return LetterService;
                }
            }
        });

        modalInstance.result.then(function () {
            // ok (not used)
        }, function () {
            init();
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

    init();
}]);
