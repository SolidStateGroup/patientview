'use strict';

angular.module('patientviewApp').controller('CodeDetailsCtrl', ['$scope', 'CodeService', 'LinkService', 'CodeExternalStandardService',
    function ($scope, CodeService, LinkService, CodeExternalStandardService) {

    $scope.addExternalStandard = function (form, code, externalStandard) {
        // only do POST if in edit mode, otherwise just add to object
        if ($scope.editMode) {

            externalStandard.externalStandard = _.findWhere($scope.externalStandards, {id: externalStandard.externalStandardId});

            delete externalStandard.externalStandardId;

            CodeService.addExternalStandard(code, externalStandard).then(function (successResult) {
                externalStandard.id = successResult.id;
                code.externalStandards.push(_.clone(externalStandard));
                delete externalStandard.id;
                delete externalStandard.externalStandard;
                delete externalStandard.codeString;
            }, function () {
                alert('Error adding external standard');
            });
        } else {
            externalStandard.id = (new Date()).getTime() * -1;
            externalStandard.externalStandard = _.findWhere($scope.externalStandards, {id: externalStandard.externalStandardId});
            code.externalStandards.push(_.clone(externalStandard));
            delete externalStandard.id;
            delete externalStandard.externalStandard;
            delete externalStandard.codeString;
            form.$setDirty(true);
        }
    };

    $scope.updateExternalStandard = function (event, form, code, externalStandard) {
        externalStandard.saved = false;
        externalStandard.externalStandard = _.findWhere($scope.externalStandards, {id: externalStandard.externalStandard.id});

        var toSave = _.clone(externalStandard);
        delete toSave.saved;

        // try and save externalStandard
        CodeExternalStandardService.save(toSave).then(function () {
            externalStandard.saved = true;
        }, function() {
            alert('Error saving external standard');
        });
    };

    $scope.removeExternalStandard = function (form, code, externalStandard) {
        // only do DELETE if in edit mode, otherwise just remove from object
        if ($scope.editMode) {
            CodeExternalStandardService.remove(externalStandard).then(function () {
                // deleted externalStandard
                for (var j = 0; j < code.externalStandards.length; j++) {
                    if (code.externalStandards[j].id === externalStandard.id) {
                        code.externalStandards.splice(j, 1);
                    }
                }
            }, function () {
                alert('Error deleting external standard');
            });
        } else {
            for (var j = 0; j < code.externalStandards.length; j++) {
                if (code.externalStandards[j].id === externalStandard.id) {
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
