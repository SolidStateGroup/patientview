'use strict';
var NewObservationHeadingModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'editObservationHeading',
    'ObservationHeadingService',
    function ($scope, $rootScope, $modalInstance, editObservationHeading, ObservationHeadingService) {
        $scope.editObservationHeading = editObservationHeading;
        $scope.editMode = false;
        $scope.editObservationHeading.observationHeadingGroups = [];
        $scope.groups = [];

        var groups = $scope.loggedInUser.userInformation.userGroups;
        for (var i=0;i<groups.length;i++) {
            if (groups[i].groupType.value === 'SPECIALTY' && groups[i].code !== 'Generic') {
                $scope.groups.push(groups[i]);
            }
        }
        $scope.editObservationHeading.groups = $scope.groups;

        $scope.ok = function () {
            ObservationHeadingService.create($scope.editObservationHeading).then(function(result) {
                $scope.editObservationHeading = result;
                $modalInstance.close($scope.editObservationHeading);
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
