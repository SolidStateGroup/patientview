'use strict';

angular.module('patientviewApp').controller('GroupDetailsCtrl', ['$scope', function ($scope) {
    $scope.addLink = function (form, group, link) {
        link.id = Math.floor(Math.random() * (9999)) -10000;
        link.displayOrder = group.links.length +1;
        group.links.push(_.clone(link));
        link.link = link.name = '';
        form.$setDirty(true);
    };

    $scope.removeLink = function (form, group, link) {
        for (var j = 0; j < group.links.length; j++) {
            if (group.links[j].id === link.id) {
                group.links.splice(j, 1);
            }
        }
        form.$setDirty(true);
    };
}]);
