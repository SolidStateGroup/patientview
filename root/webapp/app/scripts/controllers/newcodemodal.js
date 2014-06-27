'use strict';

var NewCodeModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'codeTypes', 'standardTypes', 'editCode', 'CodeService',
function ($scope, $rootScope, $modalInstance, codeTypes, standardTypes, editCode, CodeService) {
    $scope.editCode = editCode;
    $scope.codeTypes = codeTypes;
    $scope.standardTypes = standardTypes;

    $scope.addLink = function (form, code, link) {
        link.id = Math.floor(Math.random() * (9999)) -10000;
        link.displayOrder = code.links.length +1;
        code.links.push(_.clone(link));
        link.link = link.name = '';
        form.$setDirty(true);
    };

    $scope.removeLink = function (form, code, link) {
        for (var j = 0; j < code.links.length; j++) {
            if (code.links[j].id === link.id) {
                code.links.splice(j, 1);
            }
        }
        form.$setDirty(true);
    };

    $scope.ok = function () {
        CodeService.new($scope.editCode, codeTypes, standardTypes).then(function(result) {
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

// angular-ui bootstrap modal, http://angular-ui.github.io/bootstrap/
angular.module('patientviewApp').controller('NewCodeModalCtrl',['$scope','$modal','CodeService',
function ($scope, $modal, CodeService) {
    $scope.open = function (size) {
        $scope.errorMessage = '';
        $scope.successMessage = '';
        $scope.codeCreated = '';
        $scope.editCode = {};
        $scope.editCode.links = [];

        var modalInstance = $modal.open({
            templateUrl: 'newCodeModal.html',
            controller: NewCodeModalInstanceCtrl,
            size: size,
            resolve: {
                codeTypes: function(){
                    return $scope.codeTypes;
                },
                standardTypes: function(){
                    return $scope.standardTypes;
                },
                editCode: function(){
                    return $scope.editCode;
                },
                CodeService: function(){
                    return CodeService;
                }
            }
        });

        modalInstance.result.then(function (code) {
            $scope.list.push(code);
            $scope.editCode = code;
            $scope.successMessage = 'Code successfully created';
            $scope.codeCreated = true;
        }, function () {
            // cancel
            $scope.editCode = '';
        });
    };
}]);
