'use strict';
// new group modal instance controller
var NewGroupModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'permissions', 'editGroup', 'allFeatures',
    'contactPointTypes', 'allParentGroups', 'allChildGroups', 'GroupService',
function ($scope, $rootScope, $modalInstance, permissions, editGroup, allFeatures, contactPointTypes, allParentGroups,
          allChildGroups, GroupService) {
    $scope.permissions = permissions;
    $scope.editGroup = editGroup;
    $scope.allFeatures = allFeatures;
    $scope.contactPointTypes = contactPointTypes;
    $scope.editMode = false;
    var i;

    // restrict all but GLOBAL_ADMIN from adding new SPECIALTY type groups by reducing groupTypes
    for (i = 0; i < $scope.editGroup.groupTypes.length; i++) {
        if (!$scope.permissions.isSuperAdmin && $scope.editGroup.groupTypes[i].value === 'SPECIALTY') {
            $scope.editGroup.groupTypes.splice(i, 1);
        }
    }

    // set up groupTypesArray for use when showing/hiding parent/child group blocks for UNIT or SPECIALTY
    $scope.groupTypesArray = [];
    for (i = 0; i < $scope.editGroup.groupTypes.length; i++) {
        $scope.groupTypesArray[$scope.editGroup.groupTypes[i].value] = $scope.editGroup.groupTypes[i].id;
    }

    // set feature (avoid blank option)
    if ($scope.editGroup.availableFeatures && $scope.editGroup.availableFeatures.length > 0) {
        $scope.featureToAdd = $scope.editGroup.availableFeatures[0].feature.id;
    }

    $scope.ok = function () {
        GroupService.create($scope.editGroup, $scope.editGroup.groupTypes).then(function(result) {
            // successfully added new Group, close modal and return group
            $scope.editGroup = result;
            $modalInstance.close($scope.editGroup);
        }, function(result) {
            if (result.status === 409) {
                $scope.errorMessage = 'Group with this code already exists, please choose another';
            }
            else {
                if (result.data) {
                    $scope.errorMessage = 'There was an error: ' + result.data;
                } else {
                    $scope.errorMessage = 'There was an error';
                }
            }
        });
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
}];
