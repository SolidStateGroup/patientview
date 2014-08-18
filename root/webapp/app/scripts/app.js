'use strict';

// http://blog.brunoscopelliti.com/how-to-defer-route-definition-in-an-angularjs-web-app
var $routeProviderReference;

// helper functions
// http://stackoverflow.com/questions/13153121/how-to-defer-routes-definition-in-angular-js
function pathRegExp(path, opts) {
    var insensitive = opts.caseInsensitiveMatch,
        ret = {
            originalPath: path,
            regexp: path
        },
        keys = ret.keys = [];

    path = path.replace(/([().])/g, '\\$1')
        .replace(/(\/)?:(\w+)([\?\*])?/g, function (_, slash, key, option) {
            var optional = option === '?' ? option : null;
            var star = option === '*' ? option : null;
            keys.push({
                name: key,
                optional: !! optional
            });
            slash = slash || '';
            return '' + (optional ? '' : slash) + '(?:' + (optional ? slash : '') + (star && '(.+?)' || '([^/]+)') + (optional || '') + ')' + (optional || '');
        })
        .replace(/([\/$\*])/g, '\\$1');

    ret.regexp = new RegExp('^' + path + '$', insensitive ? 'i' : '');
    return ret;
}

// module definition
var patientviewApp = angular.module('patientviewApp', [
    'LocalStorageModule',  // angular-local-storage
    'config',               // auto generated by ngConstant
    'restangular',          // restangular rest
    'ui.bootstrap',         // angular ui boostrap
    'ngSanitize',           // angular sanitize for more html parsing
    'ngCookies',
    'ngResource',
    'ngSanitize',
    'ngRoute'
]);

patientviewApp.filter('startFrom', function() {
    return function(input, start) {
        if(input) {
            start = +start; //parse to int
            return input.slice(start);
        }
        return [];
    };
});

// load google charts
google.load('visualization', '1', {
    packages: ['corechart','table']
});

patientviewApp.config(['$routeProvider', '$httpProvider', 'RestangularProvider','ENV',
    function ($routeProvider, $httpProvider, RestangularProvider, ENV) {
        $httpProvider.interceptors.push('HttpRequestInterceptor');
        $httpProvider.interceptors.push('HttpResponseInterceptor');
        RestangularProvider.setBaseUrl(ENV.apiEndpoint);
        RestangularProvider.setDefaultHeaders({ 'Content-Type': 'application/json' });
        $routeProviderReference = $routeProvider;
    }]);

patientviewApp.run(['$rootScope', '$location', '$cookieStore', '$cookies', '$sce', 'localStorageService', 'Restangular', '$route', 'RouteService', 'ENV', 'ConversationService', 'JoinRequestService',
    function($rootScope, $location, $cookieStore, $cookies, $sce, localStorageService, Restangular, $route, RouteService, ENV, ConversationService, JoinRequestService) {

    $rootScope.ieTestMode = false;

    // rebuild routes from cookie, allow refresh of page
    var buildRoute = function() {
        var data = { 'default': '/' };
        var menu = localStorageService.get('routes');

        if (menu !== null) {
            // handle caching issue where routes are []
            if (menu.length === 0) {
                $rootScope.logout();
                data.routes = RouteService.getDefault().routes;
            } else {
                data.routes = menu;
            }
        } else {
            data.routes = RouteService.getDefault().routes;
        }

        // add main/login/logout routes (for all users)
        data.routes.push(RouteService.getMainRoute());
        data.routes.push(RouteService.getLogoutRoute());
        data.routes.push(RouteService.getLoginRoute());
        data.routes.push(RouteService.getAccountRoute());
        data.routes.push(RouteService.getJoinRequestRoute());

        if (data !== undefined) {
            for (var j=0 ; j < data.routes.length; j++ ) {

                var path = data.routes[j].url;
                var route = {
                    controller: data.routes[j].controller,
                    templateUrl: data.routes[j].templateUrl,
                    title: data.routes[j].title
                };

                $route.routes[path] = angular.extend(
                    { reloadOnSearch: true },
                    route,
                        path && pathRegExp(path, route)
                );

                // create redirection for trailing slashes
                if (path)
                {
                    var redirectPath = (path[path.length - 1] === '/') ? path.substr(0, path.length - 1) : path + '/';

                    $route.routes[redirectPath] = angular.extend({
                            redirectTo: path
                        },
                        pathRegExp(redirectPath, route)
                    );
                }
            }

            $routeProviderReference.otherwise({ 'redirectTo': '/'});
            $route.reload();
        }
    };

    // global function to retrieve number of unread messages
    $rootScope.setUnreadConversationCount = function() {
        if ($rootScope.loggedInUser) {
            ConversationService.getUnreadConversationCount($rootScope.loggedInUser.id).then(function(unreadCount) {
                $rootScope.unreadConversationCount = unreadCount.toString();
            }, function() {

            });
        }
    };

    // global function to retrieve number of submitted join requests
    $rootScope.setSubmittedJoinRequestCount = function() {
        if ($rootScope.loggedInUser) {
            JoinRequestService.getSubmittedJoinRequestCount($rootScope.loggedInUser.id).then(function(unreadCount) {
                $rootScope.submittedJoinRequestCount  = unreadCount.toString();
            }, function() {

            });
        }
    };

    // global function to parse HTML (used in messaging, news)
    $rootScope.parseHTMLText = function (text) {
        if (text) {
            return $sce.trustAsHtml(text.replace(/(\r\n|\n|\r)/gm, "<br>"));
        }
    };

    // global function to order groups by type
    $rootScope.orderGroups = function (group) {
        if (group.groupType) {
            var groupTypes = [];
            groupTypes.SPECIALTY = 1;
            groupTypes.UNIT = 2;
            groupTypes.DISEASE_GROUP = 3;

            if (groupTypes[group.groupType.value]) {
                return groupTypes[group.groupType.value];
            }
        }
        return 0;
    };

    // global function to order group roles by group type
    $rootScope.orderGroupRoles = function (groupRole) {
        if (groupRole.group.groupType) {
            var groupTypes = [];
            groupTypes.SPECIALTY = 1;
            groupTypes.UNIT = 2;
            groupTypes.DISEASE_GROUP = 3;

            if (groupTypes[groupRole.group.groupType.value]) {
                return groupTypes[groupRole.group.groupType.value];
            }
        }
        return 0;
    };

    $rootScope.$on('$locationChangeStart', function() {
        buildRoute();
    });

    $rootScope.$on('$routeChangeSuccess', function(event, currentRoute) {
        $rootScope.title = currentRoute.title;
    });

    $rootScope.$on('$viewContentLoaded', function(){
        $rootScope.setUnreadConversationCount();
        $rootScope.setSubmittedJoinRequestCount();
    });

    $rootScope.logout = function() {
        delete $rootScope.routes;
        delete $rootScope.loggedInUser;
        delete $rootScope.authToken;
        localStorageService.clearAll();
        $location.path('/');
    };

    // Try getting valid user from cookie or go to login page
    // var originalPath = $location.path();
    //$location.path("/login");

    //var authToken = $cookieStore.get('authToken');
    var authToken = localStorageService.get('authToken');
    if (authToken !== undefined) {
        $rootScope.authToken = authToken;
        //  $location.path(originalPath);
    }

    // get cookie user
    //var loggedInUser = $cookieStore.get('loggedInUser');
    var loggedInUser = localStorageService.get('loggedInUser');
    if (loggedInUser !== undefined) {
        $rootScope.loggedInUser = loggedInUser;
    }

    // get cookie routes
    //var routes = $cookieStore.get('routes');
    var routes = localStorageService.get('routes');
    if (routes !== undefined) {
        $rootScope.routes = routes;
    }

    $rootScope.initialised = true;
    $rootScope.endPoint = ENV.apiEndpoint;

}]);

$('html').click(function(e){
    var target = $(e.target);
    var targetParent = $(e.target).parent();

    if($('.filter-select').hasClass('open')){
        if(!target.hasClass('dropdown-toggle')){
            if (!target.hasClass('a-filter-group') && !targetParent.hasClass('a-filter-group')){
                $('.select-container .btn-group').removeClass('open');
            }
        }
    }

});
