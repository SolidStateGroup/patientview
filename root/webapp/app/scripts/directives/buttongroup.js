'use strict';

angular.module('patientviewApp').directive('buttongroup', function () {
    return {
        replace: true,
        scope: { type:'@buttongroup', name:'@buttongroupname', model:'=', options:'=' },
        templateUrl:'scripts/directives/templates/buttongroup.html',
        controller: function ($scope,$element,$attrs) {
            $scope.activate = function (option) {
                $scope.model = option;
            };
        }
    };
});
