'use strict';
var NewCodeModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'codeTypes', 'standardTypes', 'editCode', 'CodeService',
function ($scope, $rootScope, $modalInstance, codeTypes, standardTypes, editCode, CodeService) {
    $scope.editCode = editCode;
    $scope.codeTypes = codeTypes;
    $scope.standardTypes = standardTypes;
    $scope.editMode = false;

    $scope.externalStandards = _.clone($scope.loggedInUser.userInformation.externalStandards);

    CodeService.getCategories().then(function (categories) {
        $scope.editCode.codeCategories = [];

        if (categories != null && categories != undefined && categories.length) {
            categories = _.sortBy(categories, ['friendlyDescription']);
            $scope.editCode.availableCategories = categories;
        }
    }, function (error) {
        alert("Error retrieving categories: " + error.data);
    });

    $scope.ok = function () {
        CodeService.create($scope.editCode, codeTypes, standardTypes).then(function(result) {
            $scope.editCode = result;
            $modalInstance.close($scope.editCode);
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
