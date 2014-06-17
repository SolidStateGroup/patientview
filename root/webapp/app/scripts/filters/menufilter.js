'use strict';

// menu filter
angular.module('patientviewApp').filter('menuFilter', [function () {
    return function (menuitems, selectedMenu) {
        //console.log("started");
        //console.log(menuitems);
        //console.log(selectedMenu);
        if (!angular.isUndefined(menuitems) && !angular.isUndefined(selectedMenu) && selectedMenu.length > 0) {
            var tempMenuItems = [];
            angular.forEach(menuitems, function (menuitem) {
                if (menuitem.lookup.value === selectedMenu) {
                    tempMenuItems.push(menuitem);
                }
            });
            return tempMenuItems;
        }
    };
}]);
