'use strict';
var ViewXmlModalInstanceCtrl = ['$scope', '$modalInstance', 'audit',
function ($scope, $modalInstance, audit) {
    $scope.audit = audit;
    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
}];
