'use strict';

// delete code modal instance controller
var DeleteCodeModalInstanceCtrl = ['$scope', '$modalInstance','code', 'CodeService',
function ($scope, $modalInstance, code, CodeService) {
    $scope.code = code;
    $scope.ok = function () {
        CodeService.remove(code).then(function() {
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
    $scope.editMode = false;

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

angular.module('patientviewApp').controller('CodesCtrl', ['$scope','$timeout', '$modal','CodeService','StaticDataService',
function ($scope, $timeout, $modal, CodeService, StaticDataService) {

    $scope.itemsPerPage = 20;
    $scope.currentPage = 0;
    $scope.filterText = '';
    $scope.sortField = 'code';
    $scope.sortDirection = 'ASC';

    var tempFilterText = '';
    var filterTextTimeout;

    // watches
    // update page on user typed search text
    $scope.$watch('searchText', function (value) {
        if (value !== undefined) {
            if (filterTextTimeout) {
                $timeout.cancel(filterTextTimeout);
            }
            $scope.currentPage = 0;

            tempFilterText = value;
            filterTextTimeout = $timeout(function () {
                $scope.filterText = tempFilterText;
                $scope.getItems();
            }, 1000); // delay 1000 ms
        }
    });

    // update page when currentPage is changed (and at start)
    $scope.$watch('currentPage', function(value) {
        $scope.currentPage = value;
        $scope.getItems();
    });

    // Init
    $scope.init = function () {
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

    $scope.sortBy = function(sortField) {
        $scope.currentPage = 0;
        if ($scope.sortField !== sortField) {
            $scope.sortDirection = 'ASC';
            $scope.sortField = sortField;
        } else {
            if ($scope.sortDirection === 'ASC') {
                $scope.sortDirection = 'DESC';
            } else {
                $scope.sortDirection = 'ASC';
            }
        }

        $scope.getItems();
    };

    $scope.pageCount = function() {
        return Math.ceil($scope.total/$scope.itemsPerPage);
    };

    $scope.range = function() {
        var rangeSize = 10;
        var ret = [];
        var start;

        if ($scope.currentPage < 10) {
            start = 0;
        } else {
            start = $scope.currentPage;
        }

        if ( start > $scope.pageCount()-rangeSize ) {
            start = $scope.pageCount()-rangeSize;
        }

        for (var i=start; i<start+rangeSize; i++) {
            if (i > -1) {
                ret.push(i);
            }
        }

        return ret;
    };

    $scope.setPage = function(n) {
        if (n > -1 && n < $scope.totalPages) {
            $scope.currentPage = n;
        }
    };

    $scope.prevPage = function() {
        if ($scope.currentPage > 0) {
            $scope.currentPage--;
        }
    };

    $scope.prevPageDisabled = function() {
        return $scope.currentPage === 0 ? 'hidden' : '';
    };

    $scope.nextPage = function() {
        if ($scope.currentPage < $scope.totalPages - 1) {
            $scope.currentPage++;
        }
    };

    $scope.nextPageDisabled = function() {
        return $scope.currentPage === $scope.pageCount() - 1 ? 'disabled' : '';
    };

    $scope.getItems = function() {
        $scope.loading = true;
        var getParameters = {};
        getParameters.page = $scope.currentPage;
        getParameters.size = $scope.itemsPerPage;
        getParameters.filterText = $scope.filterText;
        getParameters.codeTypes = $scope.selectedCodeType;
        getParameters.standardTypes = $scope.selectedStandardType;
        getParameters.sortField = $scope.sortField;
        getParameters.sortDirection = $scope.sortDirection;

        CodeService.getAll(getParameters).then(function(page) {
            $scope.pagedItems = page.content;
            $scope.total = page.totalElements;
            $scope.totalPages = page.totalPages;
            $scope.loading = false;
        }, function() {
            $scope.loading = false;
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
        $scope.currentPage = 0;
        $scope.getItems();
    };
    $scope.isCodeTypeChecked = function (id) {
        if (_.contains($scope.selectedCodeType, id)) {
            return 'glyphicon glyphicon-ok pull-right';
        }
        return false;
    };
    $scope.removeAllCodeTypes = function () {
        $scope.selectedCodeType = [];
        $scope.currentPage = 0;
        $scope.getItems();
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
        $scope.currentPage = 0;
        $scope.getItems();
    };
    $scope.isStandardTypeChecked = function (id) {
        if (_.contains($scope.selectedStandardType, id)) {
            return 'glyphicon glyphicon-ok pull-right';
        }
        return false;
    };
    $scope.removeAllStandardTypes = function () {
        $scope.selectedStandardType = [];
        $scope.currentPage = 0;
        $scope.getItems();
    };

    // Opened for edit
    $scope.opened = function (openedCode) {
        var i;

        if (openedCode.showEdit) {
            $scope.editCode = '';
            openedCode.showEdit = false;
        } else {
            // close others
            for (i = 0; i < $scope.pagedItems.length; i++) {
                $scope.pagedItems[i].showEdit = false;
            }

            $scope.editCode = '';
            openedCode.showEdit = true;
            openedCode.editLoading = true;

            // using lightweight list, do GET on id to get full code and populate editCode
            CodeService.get(openedCode.id).then(function (code) {
                $scope.successMessage = '';
                $scope.saved = '';
                code.codeTypeId = code.codeType.id;
                code.standardTypeId = code.standardType.id;
                $scope.editCode = _.clone(code);
                $scope.editMode = true;
                openedCode.editLoading = false;
            });
        }
    };

    $scope.clone = function (codeId, $event) {
        $event.stopPropagation();
        $scope.successMessage = '';

        CodeService.clone(codeId).then(function(code) {
            $scope.successMessage = 'Successfully copied code';
            $scope.getItems();
        });
    };

    // open modal for new code
    $scope.openModalNewCode = function (size) {
        // close any open edit panels
        for (var i = 0; i < $scope.pagedItems.length; i++) {
            $scope.pagedItems[i].showEdit = false;
        }
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

        modalInstance.result.then(function () {
            $scope.getItems();
            $scope.successMessage = 'Code successfully created';
            $scope.codeCreated = true;
        }, function () {
            $scope.editCode = '';
        });
    };

    // delete code, opens modal
    $scope.remove = function (codeId, $event) {
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
                $scope.getItems();
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
            for(var i=0;i<$scope.pagedItems.length;i++) {
                if($scope.pagedItems[i].id === code.id) {
                    var headerDetails = $scope.pagedItems[i];
                    headerDetails.code = successResult.code;
                    headerDetails.codeType = successResult.codeType;
                    headerDetails.standardType = successResult.standardType;
                    headerDetails.description = successResult.description;
                }
            }

            $scope.successMessage = 'Code saved';
        }, function(failureResult) {
            if (failureResult.status === 409) {
                // conflict, code already exists
                alert('Cannot save Code, another Code with the same code exists')
            } else {
                alert('There has been an error saving');
            }
        });
    };

    $scope.init();
}]);
