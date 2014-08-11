'use strict';

// https://gist.github.com/doukasd/0744566c5494ebc8643f (modified)
angular.module('patientviewApp').directive('ddCollapseText', ['$compile', function ($compile) {
    return {
        restrict: 'A',
        replace: true,
        link: function(scope, element, attrs) {

            // start collapsed
            scope.collapsed = false;

            // create the function to toggle the collapse
            scope.toggle = function() {
                scope.collapsed = !scope.collapsed;
            };

            // get the value of the dd-collapse-text attribute
            attrs.$observe('ddCollapseText', function(maxLength) {
                // get the contents of the element
                var text = element.text();

                if (text.length > maxLength) {
                    // split the text in two parts, the first always showing
                    var firstPart = String(text).substring(0, maxLength);
                    var secondPart = String(text).substring(maxLength, text.length);

                    // create some new html elements to hold the separate info
                    var firstSpan = $compile('<span>' + firstPart + '</span>')(scope);
                    var secondSpan = $compile('<span ng-if="collapsed">' + secondPart + '&nbsp;</span>')(scope);
                    var moreIndicatorSpan = $compile('<span ng-if="!collapsed">&nbsp;...&nbsp;</span>')(scope);
                    var toggleButton = $compile('<a class="collapse-text-toggle" ng-click="toggle()">{{collapsed ? "show less" : "show more"}}</a>')(scope);

                    // remove the current contents of the element
                    // and add the new ones we created
                    element.empty();
                    element.append(firstSpan);
                    element.append(secondSpan);
                    element.append(moreIndicatorSpan);
                    element.append(toggleButton);
                }
            });
        }
    };
}]);
