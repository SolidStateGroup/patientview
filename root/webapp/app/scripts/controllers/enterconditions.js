'use strict';
angular.module('patientviewApp').controller('MyConditionsEnterConditionsCtrl',['$scope', 'CodeService', 'DiagnosisService', '$modal', 'StaticDataService',
function ($scope, CodeService, DiagnosisService, $modal, StaticDataService) {

    $scope.init = function(){

    };

    $scope.showEnterDiagnosesModal = function () {
        var modalInstance = $modal.open({
            templateUrl: 'views/modal/enterDiagnosesModal.html',
            controller: EnterDiagnosesModalInstanceCtrl,
            size: 'lg',
            resolve: {
                CodeService: function () {
                    return CodeService;
                },
                DiagnosisService: function () {
                    return DiagnosisService;
                },
                StaticDataService: function () {
                    return StaticDataService;
                }
            }
        });

        modalInstance.result.then(function () {
            // ok (not used)
        }, function () {
            // closed
        });
    };

    $scope.init();
}]);
