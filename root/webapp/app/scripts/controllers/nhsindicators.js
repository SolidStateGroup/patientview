'use strict';

angular.module('patientviewApp').controller('NhsIndicatorsCtrl',['$scope', '$routeParams', 'GroupService',
function ($scope, $routeParams, GroupService) {

    var init = function() {
        var i;
        $scope.loading = true;
        $scope.allGroups = [];

        // set the list of groups to show in the data grid, only use UNIT type groups
        $scope.selectGroups = [];

        for (i=0; i<$scope.loggedInUser.userInformation.userGroups.length; i++) {
            var group = $scope.loggedInUser.userInformation.userGroups[i];
            if (group.groupType.value == "UNIT") {
                $scope.selectGroups.push(group);
            }
        }

        // order by name
        $scope.selectGroups = _.sortBy($scope.selectGroups, 'name');

        // set group map
        for (i = 0; i < $scope.selectGroups.length; i++) {
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
    };

    var getNhsIndicators = function(selectedDate) {
        if (selectedDate === undefined || selectedDate === null) {
            return;
        }
        delete $scope.errorMessage;
        delete $scope.successMessage;
        $scope.loading = true;

        GroupService.getNhsIndicatorsByGroupAndDate($scope.selectedGroupId, selectedDate)
            .then(function (successResult) {
            $scope.nhsIndicators = successResult;
            delete $scope.loading;
        }, function (error) {
            $scope.errorMessage = error.data;
            delete $scope.loading;
        });
    };

    var getNhsIndicatorsDates = function(groupId) {
        if (groupId === undefined || groupId === null) {
            return;
        }
        delete $scope.nhsIndicatorsDates;
        delete $scope.selectedDate;
        delete $scope.errorMessage;
        delete $scope.successMessage;
        $scope.loading = true;

        GroupService.getNhsIndicatorsDates(groupId).then(function (successResult) {
            successResult.sort().reverse();
            $scope.nhsIndicatorsDates = successResult;

            if ($scope.nhsIndicatorsDates.length > 0) {
                $scope.selectedDate = $scope.nhsIndicatorsDates[0];
            }

            delete $scope.loading;
        }, function (error) {
            $scope.errorMessage = error.data;
            delete $scope.loading;
        });
    };

    $scope.$watch('selectedGroupId', function (selectedGroupId) {
        getNhsIndicatorsDates(selectedGroupId);
    });

    $scope.$watch('selectedDate', function (selectedDate) {
        getNhsIndicators(selectedDate);
    });

    init();
}]);
