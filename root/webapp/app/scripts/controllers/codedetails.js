'use strict';

angular.module('patientviewApp').controller('CodeDetailsCtrl', ['$scope', 'CodeService', 'LinkService',
    function ($scope, CodeService, LinkService) {
    $scope.addLink = function (form, code, link) {
        link.displayOrder = code.links.length +1;

        // only do POST if in edit mode, otherwise just add to object
        if ($scope.editMode) {
            CodeService.addLink(code, link).then(function (successResult) {
                // added link
                link.id = successResult.id;
                code.links.push(_.clone(link));
                link.link = link.name = '';
                form.$setDirty(true);
            }, function () {
                // failure
                alert('Error saving link');
            });
        } else {
            link.id = Math.floor(Math.random() * (9999)) -10000;
            code.links.push(_.clone(link));
            link.link = link.name = '';
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
