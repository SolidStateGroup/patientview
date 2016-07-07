'use strict';
var DeleteCodeModalInstanceCtrl = ['$scope', '$modalInstance','code', 'CodeService',
function ($scope, $modalInstance, code, CodeService) {
    $scope.code = code;
    $scope.ok = function () {
        CodeService.remove(code).then(function() {
            $modalInstance.close();
        });
    };
    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
}];
