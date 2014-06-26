'use strict';

// delete code modal instance controller
var DeleteCodeModalInstanceCtrl = ['$scope', '$modalInstance','code','CodeService',
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

angular.module('patientviewApp').controller('CodesCtrl', ['$scope','$modal','CodeService','StaticDataService',
function ($scope, $modal, CodeService, StaticDataService) {

    // Init
    $scope.init = function () {

        $scope.loading = true;

        CodeService.getAll().then(function(codes) {
            $scope.list = codes;
            $scope.currentPage = 1; //current page
            $scope.entryLimit = 10; //max no of items to display in a page
            $scope.totalItems = $scope.list.length;
            delete $scope.loading;
        });

        $scope.codeTypes = [];
        StaticDataService.getLookupsByType("CODE_TYPE").then(function(codeTypes) {
            if (codeTypes.length > 0) {
                $scope.codeTypes = codeTypes;
            }
        });

        $scope.standardTypes = [];
        StaticDataService.getLookupsByType("CODE_STANDARD").then(function(standardTypes) {
            if (standardTypes.length > 0) {
                $scope.standardTypes = standardTypes;
            }
        });
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
    $scope.opened = function (code, $event) {
        $scope.successMessage = '';

        if ($event) {
            // workaround for angular accordion and bootstrap dropdowns (clone and activate ng-click)
            if ($event.target.className.indexOf('dropdown-toggle') !== -1) {
                if ($('#' + $event.target.id).parent().children('.child-menu').length > 0) {
                    $('#' + $event.target.id).parent().children('.child-menu').remove();
                    $event.stopPropagation();
                } else {
                    $('.child-menu').remove();
                    $event.stopPropagation();
                    var childMenu = $('<div class="child-menu"></div>');
                    var dropDownMenuToAdd = $('#' + $event.target.id + '-menu').clone().attr('id', '').show();

                    // http://stackoverflow.com/questions/16949299/getting-ngclick-to-work-on-dynamic-fields
                    var compiledElement = $compile(dropDownMenuToAdd)($scope);
                    $(childMenu).append(compiledElement);
                    $('#' + $event.target.id).parent().append(childMenu);
                }
            }
            if ($event.target.className.indexOf('dropdown-menu-accordion-item') !== -1) {
                $event.stopPropagation();
            }
        }

        $scope.editing = true;
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
        CodeService.save(code).then(function() {
            editCodeForm.$setPristine(true);
            $scope.list[index] = _.clone(code);
            $scope.successMessage = 'Code saved';
        });
    };

    $scope.init();
}]);
