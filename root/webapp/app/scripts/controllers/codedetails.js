'use strict';

angular.module('patientviewApp').controller('CodeDetailsCtrl', ['$scope', 'CodeService', 'LinkService',
    function ($scope, CodeService, LinkService) {

    $scope.addExternalStandard = function (form, code, externalStandardId) {
        // only do POST if in edit mode, otherwise just add to object
        if ($scope.editMode) {
            CodeService.addExternalStandard(code, externalStandardId).then(function () {
                // added externalStandard
                for (var j = 0; j < code.availableExternalStandards.length; j++) {
                    if (code.availableExternalStandards[j].externalStandard.id === externalStandardId) {
                        code.externalStandards.push(code.availableExternalStandards[j]);
                        code.availableExternalStandards.splice(j, 1);
                    }
                }

                // update accordion header with data from GET
                CodeService.get(code.id).then(function (successResult) {
                    for(var i=0;i<$scope.pagedItems.length;i++) {
                        if($scope.pagedItems[i].id === code.id) {
                            var headerDetails = $scope.pagedItems[i];
                            headerDetails.externalStandards = successResult.externalStandards;
                        }
                    }
                }, function () {
                    alert('Error updating header (saved successfully)');
                });
            }, function () {
                alert('Error saving externalStandard');
            });
        } else {
            for (var j = 0; j < code.availableExternalStandards.length; j++) {
                if (code.availableExternalStandards[j].externalStandard.id === externalStandardId) {
                    code.externalStandards.push(code.availableExternalStandards[j]);
                    code.availableExternalStandards.splice(j, 1);
                }
            }
            form.$setDirty(true);
        }
    };

    $scope.removeExternalStandard = function (form, code, externalStandard) {
        // only do DELETE if in edit mode, otherwise just remove from object
        if ($scope.editMode) {
            CodeService.deleteExternalStandard(code, externalStandard.externalStandard).then(function () {
                // deleted externalStandard
                for (var j = 0; j < code.externalStandards.length; j++) {
                    if (code.externalStandards[j].externalStandard.id === externalStandard.externalStandard.id) {
                        code.availableExternalStandards.push(code.externalStandards[j]);
                        code.externalStandards.splice(j, 1);
                    }
                }

                // update accordion header with data from GET
                CodeService.get(code.id).then(function (successResult) {
                    for(var i=0;i<$scope.pagedItems.length;i++) {
                        if($scope.pagedItems[i].id === code.id) {
                            var headerDetails = $scope.pagedItems[i];
                            headerDetails.externalStandards = successResult.externalStandards;
                        }
                    }
                }, function () {
                    alert('Error updating header (saved successfully)');
                });
            }, function () {
                // failure
                alert('Error deleting externalStandard');
            });
        } else {
            for (var j = 0; j < code.externalStandards.length; j++) {
                if (code.externalStandards[j].externalStandard.id === externalStandard.externalStandard.id) {
                    code.availableExternalStandards.push(code.externalStandards[j]);
                    code.externalStandards.splice(j, 1);
                }
            }

            form.$setDirty(true);
        }
    };
        
        
    $scope.addLink = function (form, code, link) {
        link.displayOrder = code.links.length +1;

        // only do POST if in edit mode, otherwise just add to object
        if ($scope.editMode) {
            CodeService.addLink(code, link).then(function (successResult) {
                // added link
                link.id = successResult.id;
                code.links.push(_.clone(link));
                delete link.id;
                delete link.link;
                delete link.name;
                form.$setDirty(true);
            }, function () {
                // failure
                alert('Error saving link');
            });
        } else {
            link.id = Math.floor(Math.random() * (9999)) -10000;
            code.links.push(_.clone(link));
            delete link.id;
            delete link.link;
            delete link.name;
            form.$setDirty(true);
        }
    };

    $scope.updateLink = function (event, form, code, link) {
        link.saved = false;

        // try and save link
        LinkService.save(link).then(function () {
            // saved link
            link.saved = true;
            form.$setDirty(true);
        }, function() {
            // failure
            alert('Error saving link');
        });
    };

    $scope.removeLink = function (form, code, link) {
        // only do DELETE if in edit mode, otherwise just remove from object
        if ($scope.editMode) {
            LinkService.remove(link).then(function () {
                // deleted link
                for (var j = 0; j < code.links.length; j++) {
                    if (code.links[j].id === link.id) {
                        code.links.splice(j, 1);
                    }
                }
                form.$setDirty(true);
            }, function () {
                // failure
                alert('Error deleting link');
            });
        } else {
            for (var j = 0; j < code.links.length; j++) {
                if (code.links[j].id === link.id) {
                    code.links.splice(j, 1);
                }
            }
            form.$setDirty(true);
        }
    };
}]);
