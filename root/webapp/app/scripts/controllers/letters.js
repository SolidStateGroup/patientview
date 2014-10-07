'use strict';
// view letter modal instance controller
var ViewLetterModalInstanceCtrl = ['$scope', '$modalInstance', 'letter',
function ($scope, $modalInstance, letter) {
    $scope.letter = letter;
    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
}];

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
        })
    };

    // open modal
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
