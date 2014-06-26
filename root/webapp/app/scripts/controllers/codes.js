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

angular.module('patientviewApp').controller('CodesCtrl', ['$scope','$timeout', '$modal','CodeService','StaticDataService',
function ($scope, $timeout, $modal, CodeService, StaticDataService) {

    // Init
    $scope.init = function () {

        $scope.loading = true;

        CodeService.getAll().then(function(codes) {
            $scope.list = codes;
            $scope.currentPage = 1; //current page
            $scope.entryLimit = 10; //max no of items to display in a page
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

    $scope.addLink = function (form, code, link) {
        link.id = Math.floor(Math.random() * (9999)) -10000;
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

    // Opened for edit
    $scope.opened = function (code) {
        $scope.successMessage = '';
        code.codeTypeId = code.codeType.id;
        code.standardTypeId = code.standardType.id;
        $scope.editCode = _.clone(code);
    };

    $scope.delete = function (codeId, $event) {
        $event.stopPropagation();
        $scope.successMessage = '';

        CodeService.get(codeId).then(function(code) {
            var modalInstance = $modal.open({
                templateUrl: 'views/partials/deleteCodeModal.html',
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

    // Save from edit
    $scope.save = function (editCodeForm, code, index) {
        CodeService.save(code, $scope.codeTypes, $scope.standardTypes).then(function() {
            editCodeForm.$setPristine(true);
            $scope.list[index] = _.clone(code);
            $scope.successMessage = 'Code saved';
        });
    };

    $scope.init();
}]);
