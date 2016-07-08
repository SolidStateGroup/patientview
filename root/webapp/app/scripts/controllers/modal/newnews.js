'use strict';
var NewNewsModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'GroupService', 'RoleService', 'NewsService', 'StaticDataService', 'permissions',
    function ($scope, $rootScope, $modalInstance, GroupService, RoleService, NewsService, StaticDataService, permissions) {
        var i, group, newsLink = {};

        $scope.modalLoading = true;
        $scope.permissions = permissions;
        $scope.groupToAdd = -1;
        $scope.newNews = {};
        $scope.newNews.allGroups = [];
        $scope.newNews.newsLinks = [];

        // populate list of allowed groups for current user
        var groups = $scope.loggedInUser.userInformation.userGroups;
        // add 'All Groups' option (with id -1) if allowed
        if ($scope.permissions.canAddAllGroups) {
            group = {};
            group.id = -1;
            group.name = 'All Groups';
            $scope.newNews.allGroups.push(group);
        }

        for (i = 0; i < groups.length; i++) {
            group = groups[i];
            if (group.visible === true) {
                $scope.newNews.allGroups.push(group);
            }
        }

        // add GLOBAL_ADMIN role (no group) to all news by default
        newsLink.role = $scope.permissions.globalAdminRole;
        $scope.newNews.newsLinks.push(newsLink);

        StaticDataService.getLookupsByType("NEWS_TYPE").then(function (page) {
            var newsTypes = [];
            var newsTypesArray = [];
            page.forEach(function (newsType) {
                if (newsType.value != "ALL") {
                    newsTypes.push(newsType);

                }
                newsTypesArray[newsType.id] = newsType;
            });
            $scope.newsTypesArray = newsTypesArray;
            $scope.newNews.newsTypes = newsTypes;
            $scope.newNews.newsType = newsTypes[0].id;
            $scope.modalLoading = false;
        }, function () {
            $scope.modalLoading = false;
            // error
        });

        $scope.newNews.allRoles = _.clone($scope.permissions.allRoles);

        $scope.ok = function () {
            $scope.newNews.creator = {};
            $scope.newNews.creator.id = $scope.loggedInUser.id;

            // Check if the user has picked a dashboard item as a general public message
            var roleError = null;

            $scope.newNews.newsLinks.forEach(function (newsLink) {
                if (newsLink.role.name == "PUBLIC" && $scope.newNews.newsType == $scope.newsTypesArray['DASHBOARD']) {
                    roleError  = 'Dashboard messages cannot be set to General Public. Please correct to continue.';
                }
            });

            if (roleError !== null) {
                $scope.errorMessage = roleError;
            } else {
                NewsService.create($scope.newNews).then(function () {
                    $modalInstance.close();
                }, function (result) {
                    if (result.data) {
                        $scope.errorMessage = ' - ' + result.data;
                    } else {
                        $scope.errorMessage = ' ';
                    }
                });
            }
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }];
