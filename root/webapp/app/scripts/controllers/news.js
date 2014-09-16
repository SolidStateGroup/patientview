'use strict';

// new news modal instance controller
var NewNewsModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'GroupService', 'RoleService', 'NewsService', 'permissions',
function ($scope, $rootScope, $modalInstance, GroupService, RoleService, NewsService, permissions) {
    var i, group, newsLink = {};

    $scope.modalLoading = true;

    $scope.permissions = permissions;
    $scope.groupToAdd = -1;
    $scope.newNews = {};
    $scope.newNews.allRoles = [];
    $scope.newNews.allGroups = [];
    $scope.newNews.newsLinks = [];

    // populate list of allowed groups for current user
    GroupService.getGroupsForUser($scope.loggedInUser.id).then(function (groups) {
        // add 'All Groups' option (with id -1) if allowed
        if ($scope.permissions.canAddAllGroups) {
            group = {};
            group.id = -1;
            group.name = 'All Groups';
            $scope.newNews.allGroups.push(group);
        }

        for (i = 0; i < groups.content.length; i++) {
            group = groups.content[i];
            if (group.visible === true) {
                $scope.newNews.allGroups.push(group);
            }
        }

        // todo: currently gets all roles, adds public & member roles
        RoleService.getAll().then(function(roles) {
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

            $scope.modalLoading = false;

        }, function () {
            alert('Error loading possible roles');
        });
    }, function () {
        alert('Error loading possible groups');
    });

    $scope.ok = function () {
        $scope.newNews.creator = {};
        $scope.newNews.creator.id = $scope.loggedInUser.id;

        NewsService.create($scope.newNews).then(function() {
            $modalInstance.close();
        }, function(result) {
            if (result.data) {
                $scope.errorMessage = ' - ' + result.data;
            } else {
                $scope.errorMessage = ' ';
            }
        });
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
}];

// pagination following http://fdietz.github.io/recipes-with-angular-js/common-user-interface-patterns/paginating-through-server-side-data.html
angular.module('patientviewApp').controller('NewsCtrl',['$scope', '$modal', '$q', 'NewsService', 'GroupService', 'RoleService', 'UserService',
    function ($scope, $modal, $q, NewsService, GroupService, RoleService, UserService) {

    $scope.itemsPerPage = 5;
    $scope.currentPage = 0;

    $scope.init = function () {
        // set up permissions
        var permissions = {};

        // check if user is GLOBAL_ADMIN or SPECIALTY_ADMIN
        permissions.isSuperAdmin = UserService.checkRoleExists('GLOBAL_ADMIN', $scope.loggedInUser);
        permissions.isSpecialtyAdmin = UserService.checkRoleExists('SPECIALTY_ADMIN', $scope.loggedInUser);
        permissions.isUnitAdmin = UserService.checkRoleExists('UNIT_ADMIN', $scope.loggedInUser);


        permissions.canAddAllGroups = permissions.isSuperAdmin || permissions.isSpecialtyAdmin;

        if (permissions.isSuperAdmin || permissions.isSpecialtyAdmin || permissions.isUnitAdmin) {
            permissions.canAddNews = true;
        }

        $scope.permissions = permissions;
    };

    // simple sorting
    $scope.orderGroups = function (group) {
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

    $scope.range = function() {
        var rangeSize = 5;
        var ret = [];
        var start;

        start = 1;
        if ( start > $scope.totalPages-rangeSize ) {
            start = $scope.totalPages-rangeSize;
        }

        for (var i=start; i<start+rangeSize; i++) {
            if (i > -1) {
                ret.push(i);
            }
        }

        return ret;
    };

    $scope.setPage = function(n) {
        if (n > -1 && n < $scope.totalPages) {
            $scope.currentPage = n;
        }
    };

    $scope.prevPage = function() {
        if ($scope.currentPage > 0) {
            $scope.currentPage--;
        }
    };

    $scope.prevPageDisabled = function() {
        return $scope.currentPage === 0 ? 'hidden' : '';
    };

    $scope.nextPage = function() {
        if ($scope.currentPage < $scope.totalPages - 1) {
            $scope.currentPage++;
        }
    };

    $scope.nextPageDisabled = function() {
        if ($scope.totalPages > 0) {
            return $scope.currentPage === $scope.totalPages - 1 ? 'hidden' : '';
        } else {
            return 'hidden';
        }
    };

    // get page of data every time currentPage is changed
    $scope.$watch('currentPage', function(newValue) {
        $scope.loading = true;
        NewsService.getByUser($scope.loggedInUser.id, newValue, $scope.itemsPerPage).then(function(page) {
            $scope.pagedItems = page.content;
            $scope.total = page.totalElements;
            $scope.totalPages = page.totalPages;
            $scope.loading = false;
        }, function() {
            $scope.loading = false;
            // error
        });
    });

    // open modal for new news
    $scope.openModalNewNews = function (size) {
        $scope.errorMessage = '';
        $scope.editMode = false;

        var modalInstance = $modal.open({
            templateUrl: 'newNewsModal.html',
            controller: NewNewsModalInstanceCtrl,
            size: size,
            resolve: {
                GroupService: function(){
                    return GroupService;
                },
                RoleService: function(){
                    return RoleService;
                },
                NewsService: function(){
                    return NewsService;
                },
                permissions: function(){
                    return $scope.permissions;
                }
            }
        });

        modalInstance.result.then(function () {
            $scope.loading = true;
            NewsService.getByUser($scope.loggedInUser.id, $scope.currentPage, $scope.itemsPerPage).then(function(page) {
                $scope.pagedItems = page.content;
                $scope.total = page.totalElements;
                $scope.totalPages = page.totalPages;
                $scope.loading = false;
                $scope.successMessage = 'News item successfully created';
            }, function() {
                $scope.loading = false;
            });
        }, function () {
            // cancel
            $scope.newNews = '';
        });

    };

    $scope.edit = function(news) {
        var i;
        $scope.saved = '';

        if (news.showEdit) {
            $scope.editNews = '';
            news.showEdit = false;
        } else {

            // close others
            for (i = 0; i < $scope.pagedItems.length; i++) {
                $scope.pagedItems[i].showEdit = false;
            }

            $scope.editMode = true;
            news.showEdit = true;

            NewsService.get(news.id).then(function(newsItem) {
                $scope.editNews = _.clone(newsItem);
                $scope.editNews.allRoles = [];
                $scope.editNews.allGroups = [];

                GroupService.getGroupsForUser($scope.loggedInUser.id).then(function (groups) {

                    // add 'All Groups' option (with id -1) if allowed
                    if ($scope.permissions.canAddAllGroups) {
                        group = {};
                        group.id = -1;
                        group.name = 'All Groups';
                        $scope.editNews.allGroups.push(group);
                    }

                    for (i = 0; i < groups.length; i++) {
                        var group = groups[i];
                        if (group.visible === true) {
                            $scope.editNews.allGroups.push(group);
                        }
                    }

                    $scope.groupToAdd = -1;

                    // todo: currently gets all roles, adds public and member role
                    RoleService.getAll().then(function(roles) {
                        for (i = 0; i < roles.length; i++) {
                            var role = roles[i];
                            if (role.visible === true || role.name === 'PUBLIC' || role.name === 'MEMBER') {
                                $scope.editNews.allRoles.push(role);
                            }
                        }
                    }, function () {
                        alert('Error loading roles');
                    });
                }, function () {
                    alert('Error loading groups');
                });
            }, function () {
                alert('Error loading news item');
            });

        }
    };

    // Save from edit
    $scope.save = function (editNewsForm, news) {
        NewsService.save(news).then(function() {

            // successfully saved, replace existing element in data grid with updated
            editNewsForm.$setPristine(true);
            $scope.saved = true;

            // update news with data from GET
            NewsService.get(news.id).then(function (entityNews) {
                for (var i=0;i<$scope.pagedItems.length;i++) {
                    if ($scope.pagedItems[i].id === entityNews.id) {
                        var newsItemToUpdate = $scope.pagedItems[i];
                        newsItemToUpdate.heading = entityNews.heading;
                        newsItemToUpdate.story = entityNews.story;
                        newsItemToUpdate.newsLinks = entityNews.newsLinks;
                        newsItemToUpdate.lastUpdate = entityNews.lastUpdated;
                        newsItemToUpdate.lastUpdater = entityNews.lastUpdater;
                        news.lastUpdate = entityNews.lastUpdate;
                        news.lastUpdater = entityNews.lastUpdater;
                    }
                }
            }, function () {
                alert('Error updating header (saved successfully)');
            });

            $scope.successMessage = 'News saved';
        });
    };

    $scope.remove = function(news) {
        NewsService.remove(news).then(function() {
            $scope.loading = true;
            NewsService.getByUser($scope.loggedInUser.id, $scope.currentPage, $scope.itemsPerPage).then(function(page) {
                $scope.pagedItems = page.content;
                $scope.total = page.totalElements;
                $scope.totalPages = page.totalPages;
                $scope.loading = false;
                $scope.successMessage = 'News item successfully created';
            }, function() {
                $scope.loading = false;
            });
        }, function() {
            alert('Error deleting news item');
            $scope.loading = false;
        });
    };

    $scope.init();
}]);
