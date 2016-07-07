'use strict';
var NewNewsModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'GroupService', 'RoleService', 'NewsService', 'StaticDataService', 'permissions',
    function ($scope, $rootScope, $modalInstance, GroupService, RoleService, NewsService, StaticDataService, permissions) {
        var i, group, newsLink = {};

        $scope.modalLoading = true;

        $scope.permissions = permissions;
        $scope.groupToAdd = -1;
        $scope.newNews = {};
        $scope.newNews.allRoles = [];
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

        // todo: currently gets all roles, adds public & member roles
        RoleService.getAll().then(function (roles) {
            for (i = 0; i < roles.length; i++) {
                var role = roles[i];
                if (role.visible === true || role.name === 'PUBLIC' || role.name === 'MEMBER') {
                    $scope.newNews.allRoles.push(role);
                }
            }

            // add GLOBAL_ADMIN role (no group) to all news by default
            for (i = 0; i < $scope.newNews.allRoles.length; i++) {
                if ($scope.newNews.allRoles[i] && $scope.newNews.allRoles[i].name === 'GLOBAL_ADMIN') {
                    newsLink.role = $scope.newNews.allRoles[i];
                    $scope.newNews.newsLinks.push(newsLink);
                }
            }

            StaticDataService.getLookupsByType("NEWS_TYPE").then(function (page) {
                var newsTypes = [];
                var newsTypesArray = [];
                page.forEach(function (newsType) {
                    if (newsType.value != "ALL") {
                        newsTypes.push(newsType);
                        newsTypesArray[newsType.value] = newsType.id;
                    }
                });
                $scope.newsTypesArray = newsTypesArray;
                $scope.newNews.newsTypes = newsTypes;
                $scope.newNews.newsType = newsTypes[0].id;
                $scope.modalLoading = false;
            }, function () {
                $scope.modalLoading = false;
                // error
            });

            //$scope.modalLoading = false;

        }, function () {
            alert('Error loading possible roles');
        });


        $scope.ok = function () {
            $scope.newNews.creator = {};
            $scope.newNews.creator.id = $scope.loggedInUser.id;


            //Check if the user has picked a dashboard item as a general public message
            var publicMessageError = false;
            $scope.newNews.newsLinks.forEach(function (newsLink) {
                if (newsLink.role.name == "PUBLIC" && $scope.newNews.newsType == $scope.newsTypesArray['DASHBOARD']) {
                    publicMessageError = true;
                    return;
                }
            });

            if (publicMessageError) {
                $scope.errorMessage = 'Dashboard messages cannot be set to General Public. Please correct to continue.';
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
