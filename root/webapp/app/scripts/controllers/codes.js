'use strict';

// delete code modal instance controller
var DeleteCodeModalInstanceCtrl = ['$scope', '$modalInstance','code', 'CodeService',
function ($scope, $modalInstance, code, CodeService) {
    $scope.code = code;
    $scope.ok = function () {
        CodeService.delete(code).then(function() {
            $modalInstance.close();
        });
    };
    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
}];

// new code modal instance controller
var NewCodeModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'codeTypes', 'standardTypes', 'editCode', 'CodeService',
function ($scope, $rootScope, $modalInstance, codeTypes, standardTypes, editCode, CodeService) {
    $scope.editCode = editCode;
    $scope.codeTypes = codeTypes;
    $scope.standardTypes = standardTypes;

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

angular.module('patientviewApp').controller('CodesCtrl', ['$scope','$timeout', '$modal','CodeService','StaticDataService',
function ($scope, $timeout, $modal, CodeService, StaticDataService) {

    // Init
    $scope.init = function () {

        $scope.loading = true;

        CodeService.getAll().then(function(codes) {
            $scope.list = codes;
            $scope.currentPage = 1; //current page
            $scope.entryLimit = 20; //max no of items to display in a page
            $scope.totalItems = $scope.list.length;
            $scope.predicate = 'id';
            delete $scope.loading;
        });

        $scope.codeTypes = [];
        StaticDataService.getLookupsByType('CODE_TYPE').then(function(codeTypes) {
            if (codeTypes.length > 0) {
                $scope.codeTypes = codeTypes;
            }
        });

        $scope.standardTypes = [];
        StaticDataService.getLookupsByType('CODE_STANDARD').then(function(standardTypes) {
            if (standardTypes.length > 0) {
                $scope.standardTypes = standardTypes;
            }
        });
    };

    // filter by code type
    $scope.selectedCodeType = [];
    $scope.setSelectedCodeType = function () {
        var id = this.type.id;
        if (_.contains($scope.selectedCodeType, id)) {
            $scope.selectedCodeType = _.without($scope.selectedCodeType, id);
        } else {
            $scope.selectedCodeType.push(id);
        }
        return false;
    };
    $scope.isCodeTypeChecked = function (id) {
        if (_.contains($scope.selectedCodeType, id)) {
            return 'glyphicon glyphicon-ok pull-right';
        }
        return false;
    };

    // filter by standard type
    $scope.selectedStandardType = [];
    $scope.setSelectedStandardType = function () {
        var id = this.type.id;
        if (_.contains($scope.selectedStandardType, id)) {
            $scope.selectedStandardType = _.without($scope.selectedStandardType, id);
        } else {
            $scope.selectedStandardType.push(id);
        }
        return false;
    };
    $scope.isStandardTypeChecked = function (id) {
        if (_.contains($scope.selectedStandardType, id)) {
            return 'glyphicon glyphicon-ok pull-right';
        }
        return false;
    };

    // pagination, sorting, basic filter
    $scope.setPage = function(pageNo) {
        $scope.currentPage = pageNo;
    };

    $scope.filter = function() {
        $timeout(function() {
            $scope.filteredItems = $scope.filtered.length;
        }, 10);
    };

    $scope.sortBy = function(predicate) {
        $scope.predicate = predicate;
        $scope.reverse = !$scope.reverse;
    };

    // Opened for edit
    $scope.opened = function (code) {
        $scope.successMessage = '';
        $scope.saved = '';
        code.codeTypeId = code.codeType.id;
        code.standardTypeId = code.standardType.id;
        $scope.editCode = _.clone(code);
    };

    $scope.clone = function (codeId, $event) {
        $event.stopPropagation();
        $scope.successMessage = '';

        CodeService.clone(codeId).then(function(code) {
            $scope.successMessage = 'Successfully copied code';
            $scope.list.push(code);
        });
    };

    // open modal for new code
    $scope.openModalNewCode = function (size) {
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

    // delete code, opens modal
    $scope.delete = function (codeId, $event) {
        $event.stopPropagation();
        $scope.successMessage = '';

        CodeService.get(codeId).then(function(code) {
            var modalInstance = $modal.open({
                templateUrl: 'deleteCodeModal.html',
                controller: DeleteCodeModalInstanceCtrl,
                resolve: {
                    code: function(){
                        return code;
                    },
                    CodeService: function(){
                        return CodeService;
                    }
                }
            });

            modalInstance.result.then(function () {
                // ok, delete from list
                for(var l=0;l<$scope.list.length;l++) {
                    if ($scope.list[l].id === codeId) {
                        $scope.list = _.without($scope.list, $scope.list[l]);
                    }
                }
                $scope.successMessage = 'Code successfully deleted';
            }, function () {
                // closed
            });
        });
    };

    // Save code details from edit
    $scope.save = function (editCodeForm, code) {
        CodeService.save(code, $scope.codeTypes, $scope.standardTypes).then(function(successResult) {
            editCodeForm.$setPristine(true);
            $scope.saved = true;

            // update header details (code, type, standard, description)
            for(var i=0;i<$scope.list.length;i++) {
                if($scope.list[i].id == code.id) {
                    var headerDetails = $scope.list[i];
                    headerDetails.code = successResult.code;
                    headerDetails.codeType = successResult.codeType;
                    headerDetails.standardType = successResult.standardType;
                    headerDetails.description = successResult.description;
                }
            }

            $scope.successMessage = 'Code saved';
        });
    };

    $scope.init();
}]);
