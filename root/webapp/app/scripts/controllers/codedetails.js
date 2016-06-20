'use strict';

angular.module('patientviewApp').controller('CodeDetailsCtrl', ['$scope', 'CodeService', 'LinkService', 'CodeExternalStandardService',
    function ($scope, CodeService, LinkService, CodeExternalStandardService) {

    $scope.addCategory = function (form, code, categoryId) {
        for (var i = 0; i < code.codeCategories.length; i++) {
            if (code.codeCategories[i].category.id == categoryId) {
                return;
            }
        }

        // only do POST if in edit mode, otherwise just add to object
        if ($scope.editMode) {
            CodeService.addCodeCategory(code.id, categoryId).then(function (codeCategory) {
                // added category
                for (var j = 0; j < code.availableCategories.length; j++) {
                    if (code.availableCategories[j].id === categoryId) {
                        code.codeCategories.push(codeCategory);
                        code.availableCategories.splice(j, 1);
                    }
                }
            }, function () {
                alert('Error saving Code Category');
            });
        } else {
            for (var j = 0; j < code.availableCategories.length; j++) {
                if (code.availableCategories[j].id === categoryId) {
                    var codeCategory = {
                        "category" : code.availableCategories[j]
                    };
                    code.codeCategories.push(codeCategory);
                    code.availableCategories.splice(j, 1);
                }
            }
            form.$setDirty(true);

            // set select
            if (code.availableCategories != null && code.availableCategories != undefined
                && code.availableCategories[0]) {
                $scope.categoryToAdd = code.availableCategories[0].id;
            }
        }
    };

    $scope.removeCodeCategory = function (form, code, codeCategory) {
        // only do DELETE if in edit mode, otherwise just remove from object
        if ($scope.editMode) {
            CodeService.deleteCodeCategory(code.id, codeCategory.category.id).then(function () {
                for (var j = 0; j < code.codeCategories.length; j++) {
                    if (code.codeCategories[j].id === codeCategory.id) {
                        code.availableCategories.push(code.codeCategories[j].category);
                        code.codeCategories.splice(j, 1);
                    }
                }
            }, function () {
                alert('Error deleting Code Category');
            });
        } else {
            for (var j = 0; j < code.codeCategories.length; j++) {
                if (code.codeCategories[j].category.id === codeCategory.category.id) {
                    code.availableCategories.push(code.codeCategories[j].category);
                    code.codeCategories.splice(j, 1);
                }
            }

            form.$setDirty(true);
        }
    };

    $scope.addCodeExternalStandard = function (form, code, codeExternalStandard) {
        if (code.externalStandards != null && code.externalStandards != undefined) {
            for (var i = 0; i < code.externalStandards.length; i++) {
                delete code.externalStandards[i].saved;
            }
        }

        // only do POST if in edit mode, otherwise just add to object
        if ($scope.editMode) {

            codeExternalStandard.externalStandard = _.findWhere($scope.externalStandards, {id: codeExternalStandard.externalStandardId});

            delete codeExternalStandard.externalStandardId;

            CodeService.addCodeExternalStandard(code.id, codeExternalStandard).then(function (successResult) {
                //codeExternalStandard.id = successResult.id;
                code.externalStandards.push(successResult);
                delete codeExternalStandard.externalStandard;
                delete codeExternalStandard.codeString;
            }, function () {
                alert('Error adding external standard');
            });
        } else {
            if (code.externalStandards == null || code.externalStandards == undefined) {
                code.externalStandards = [];
            }

            var newCodeExternalStandard = {};
            newCodeExternalStandard.codeString = codeExternalStandard.codeString;
            newCodeExternalStandard.id = (new Date()).getTime() * -1;
            newCodeExternalStandard.externalStandard = _.findWhere($scope.externalStandards, {id: codeExternalStandard.externalStandard.id});

            code.externalStandards.push(newCodeExternalStandard);

            delete codeExternalStandard.externalStandard;
            delete codeExternalStandard.codeString;

            form.$setDirty(true);
        }
    };

    $scope.updateCodeExternalStandard = function (event, form, code, codeExternalStandard) {
        if (code.externalStandards != null && code.externalStandards != undefined) {
            for (var i = 0; i < code.externalStandards.length; i++) {
                delete code.externalStandards[i].saved;
            }
        }

        var toSave = _.clone(codeExternalStandard);
        toSave.externalStandard = _.findWhere($scope.externalStandards, {id: toSave.externalStandard.id});
        delete toSave.saved;

        // try and save externalStandard
        CodeExternalStandardService.save(toSave).then(function () {
            codeExternalStandard.saved = true;
        }, function() {
            alert('Error saving external standard');
        });
    };

    $scope.removeCodeExternalStandard = function (form, code, codeExternalStandard) {
        if (code.externalStandards != null && code.externalStandards != undefined) {
            for (var i = 0; i < code.externalStandards.length; i++) {
                delete code.externalStandards[i].saved;
            }
        }

        // only do DELETE if in edit mode, otherwise just remove from object
        if ($scope.editMode) {
            CodeExternalStandardService.remove(codeExternalStandard).then(function () {
                // deleted externalStandard
                for (var j = 0; j < code.externalStandards.length; j++) {
                    if (code.externalStandards[j].id === codeExternalStandard.id) {
                        code.externalStandards.splice(j, 1);
                    }
                }
            }, function () {
                alert('Error deleting external standard');
            });
        } else {
            for (var j = 0; j < code.externalStandards.length; j++) {
                if (code.externalStandards[j].id === codeExternalStandard.id) {
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

    $scope.isDiagnosisCode = function() {
        return $scope.editCode && $scope.editCode.codeTypeId == _.findWhere($scope.codeTypes, {value: 'DIAGNOSIS'}).id;
    }
}]);
