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
        printContent.find('.p-download').remove();
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
