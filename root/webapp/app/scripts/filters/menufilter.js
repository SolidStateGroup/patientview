'use strict';

// menu filter
angular.module('patientviewApp').filter('menuFilter', [function () {
    return function (menuitems, selectedMenu) {
        if (!angular.isUndefined(menuitems) && !angular.isUndefined(selectedMenu) && selectedMenu.length > 0) {
            var tempMenuItems = [];
            angular.forEach(menuitems, function (menuitem) {
                if (menuitem.lookup !== undefined) {
                    if (menuitem.lookup.value === selectedMenu) {
                        // do not put / menu item in (assume logged in), Safari private browsing fix
                        if(menuitem.url !== '/') {
                            tempMenuItems.push(menuitem);
                        }
                    }
                }
            });
            return tempMenuItems;
        }
    };
}]);
