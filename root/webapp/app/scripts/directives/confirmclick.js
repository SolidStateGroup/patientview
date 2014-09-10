'use strict';

// http://zachsnow.com/#!/blog/2013/confirming-ng-click/
// http://stackoverflow.com/questions/18313576/confirmation-dialog-on-ng-click-angularjs
angular.module('patientviewApp').directive('ngConfirmClick', [function(){
    return {
        priority: -1,
        restrict: 'A',
        link: function(scope, element, attrs){
            element.bind('click', function(e){
                var message = attrs.ngConfirmClick;
                if(message && !confirm(message)){
                    e.stopImmediatePropagation();
                    e.preventDefault();
                }
            });
        }
    };
}]);
