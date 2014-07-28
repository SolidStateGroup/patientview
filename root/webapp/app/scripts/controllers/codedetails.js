'use strict';

angular.module('patientviewApp').controller('CodeDetailsCtrl', ['$scope', 'CodeService', 'LinkService',
    function ($scope, CodeService, LinkService) {
    $scope.addLink = function (form, code, link) {
        link.displayOrder = code.links.length +1;

        CodeService.addLink(code, link).then(function (successResult) {
            // added link
            link.id = successResult.id;
            code.links.push(_.clone(link));
            link.link = link.name = '';
            form.$setDirty(true);
        }, function() {
            // failure
            alert("Error saving link");
        });
    };

    $scope.removeLink = function (form, code, link) {

        LinkService.delete(link).then(function () {
            // deleted link
            for (var j = 0; j < code.links.length; j++) {
                if (code.links[j].id === link.id) {
                    code.links.splice(j, 1);
                }
            }
            form.$setDirty(true);
        }, function() {
            // failure
            alert("Error deleting link");
        });

    };
}]);
