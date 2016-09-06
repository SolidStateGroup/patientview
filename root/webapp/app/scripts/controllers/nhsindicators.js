'use strict';

angular.module('patientviewApp').controller('NhsIndicatorsCtrl',['$scope', '$routeParams', 'GroupService',
function ($scope, $routeParams, GroupService) {

    var init = function() {
        $scope.allGroups = [];

        // set the list of groups to show in the data grid
        $scope.selectGroups = $scope.loggedInUser.userInformation.userGroups;

        // hide Generic group
        _.remove($scope.selectGroups, {code: 'Generic'});

        // set group map
        for (var i = 0; i < $scope.selectGroups.length; i++) {
            $scope.allGroups[$scope.selectGroups[i].id] = $scope.selectGroups[i];
        }

        // try and get group from route parameters
        if ($routeParams.groupId !== undefined) {
            var foundGroup = $scope.allGroups[$routeParams.groupId];
            if (foundGroup !== undefined && foundGroup !== null) {
                $scope.selectedGroupId = foundGroup.id;
            }
        }

        // not found from parameters, get from first group in select groups
        if ($scope.selectedGroupId === null || $scope.selectedGroupId === undefined) {
            // set group (avoid blank option)
            if ($scope.selectGroups && $scope.selectGroups.length > 0) {
                $scope.selectedGroupId = $scope.selectGroups[0].id;
            }
        }

        // get nhs indicators based on selected group's id
        $scope.getNhsIndicators($scope.selectedGroupId);
    };

    $scope.getNhsIndicators = function(groupId) {
        delete $scope.errorMessage;
        delete $scope.successMessage;

        GroupService.getNhsIndicators(groupId).then(function (successResult) {
            $scope.nhsIndicators = successResult;
        }, function (error) {
            $scope.errorMessage = error.data;
        });
    };

    init();

}]);
