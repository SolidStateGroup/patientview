'use strict';

// only allow keypress of alphanumerics and capitalise
angular.module('patientviewApp').directive('onlyCapitalLetters', [function () {
    return {
        require: 'ngModel',
        link: function(scope, element, attrs, modelCtrl) {
            var capitalize = function(inputValue) {
                if (inputValue == undefined) {
                    inputValue = '';
                }
                var capitalized = inputValue.replace(/[^a-zA-Z]/g, '').toUpperCase();
                if (capitalized !== inputValue) {
                    modelCtrl.$setViewValue(capitalized);
                    modelCtrl.$render();
                }
                return capitalized;
            };
            modelCtrl.$parsers.push(capitalize);
            capitalize(scope[attrs.ngModel]);
        }
    };
}]);

