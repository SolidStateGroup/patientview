'use strict';
var ViewResearchStudyModalInstanceCtrl = ['$scope', '$modalInstance', 'researchStudy',
    function ($scope, $modalInstance, researchStudy) {
        $scope.researchStudy = researchStudy;

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }];
