'use strict';

angular.module('patientviewApp').controller('CodeDetailsCtrl', ['$scope',
function ($scope) {
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
}]);
