'use strict';
// view letter modal instance controller
var ViewLetterModalInstanceCtrl = ['$scope', '$modalInstance', 'letter',
function ($scope, $modalInstance, letter) {
    $scope.letter = letter;
    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };

    $scope.print = function() {
        // ie8 compatibility
        var printContent = $('.modal-content').clone();
        printContent.children('.modal-footer').remove();
        var windowUrl = 'PatientView';
        var uniqueName = new Date();
        var windowName = 'Print' + uniqueName.getTime();
        var printWindow = window.open(windowUrl, windowName, 'left=50000,top=50000,width=0,height=0');
        printWindow.document.write(printContent.html());
        printWindow.document.close();
        printWindow.focus();
        printWindow.print();
        printWindow.close();
    };
}];
// delete letter modal instance controller
var DeleteLetterModalInstanceCtrl = ['$scope', '$modalInstance', 'letter', 'LetterService',
function ($scope, $modalInstance, letter, LetterService) {
    $scope.letter = letter;
    $scope.successMessage = '';
    $scope.errorMessage = '';
    $scope.modalMessage = '';

    $scope.remove = function () {
        LetterService.remove($scope.loggedInUser.id, letter.group.id, letter.date).then(function() {
            $scope.errorMessage = '';
            $scope.successMessage = 'Letter has been deleted.';
        }, function() {
            $scope.successMessage = '';
            $scope.errorMessage = 'There was an error.';
        });
    };

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
