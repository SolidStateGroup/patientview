'use strict';


// new news modal instance controller
var NewNewsModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'newNews', 'NewsService',
    function ($scope, $rootScope, $modalInstance, newNews, NewsService) {
        var i, newsLink = {};
        $scope.newNews = newNews;
        $scope.groupToAdd = -1;

        // add GLOBAL_ADMIN role (no group) to all news by default
        for (i = 0; i < $scope.newNews.allRoles.length; i++) {
            if ($scope.newNews.allRoles[i] && $scope.newNews.allRoles[i].name === 'GLOBAL_ADMIN') {
                newsLink.role = $scope.newNews.allRoles[i];
                $scope.newNews.newsLinks.push(newsLink);
            }
        }

        $scope.ok = function () {
            newNews.creator = {};
            newNews.creator.id = $scope.loggedInUser.id;

            NewsService.new(newNews).then(function() {
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
        return $scope.currentPage === 0 ? "hidden" : "";
    };

    $scope.nextPage = function() {
        if ($scope.currentPage < $scope.totalPages - 1) {
            $scope.currentPage++;
        }
    };

    $scope.nextPageDisabled = function() {
        if ($scope.totalPages > 0) {
            return $scope.currentPage === $scope.totalPages - 1 ? "hidden" : "";
        } else {
            return "hidden";
        }
    };

    // get page of data every time currentPage is changed
    $scope.$watch("currentPage", function(newValue, oldValue) {
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
        var i, group;
        $scope.errorMessage = '';
        $scope.editMode = false;
        $scope.newNews = {};
        $scope.newNews.allRoles = [];
        $scope.newNews.allGroups = [];
        $scope.newNews.newsLinks = [];

        // populate list of allowed groups for current user
        GroupService.getGroupsForUser($scope.loggedInUser.id).then(function (groups) {

            // add 'All Groups' option (with id -1)
            group = {};
            group.id = -1;
            group.name = 'All Groups';
            $scope.newNews.allGroups.push(group);

            for (i = 0; i < groups.length; i++) {
                group = groups[i];
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

                var modalInstance = $modal.open({
                    templateUrl: 'newNewsModal.html',
                    controller: NewNewsModalInstanceCtrl,
                    size: size,
                    resolve: {
                        newNews: function(){
                            return $scope.newNews;
                        },
                        NewsService: function(){
                            return NewsService;
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
            }, function () {
                alert('Error loading possible roles');
            });
        }, function () {
            alert('Error loading possible groups');
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

                    // add 'All Groups' option (with id -1)
                    group = {};
                    group.id = -1;
                    group.name = 'All Groups';
                    $scope.editNews.allGroups.push(group);

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

            // update header for news with data from GET
            NewsService.get(news.id).then(function (entityNews) {
                for (var i=0;i<$scope.pagedItems.length;i++) {
                    if ($scope.pagedItems[i].id == entityNews.id) {
                        var newsItemToUpdate = $scope.pagedItems[i];
                        newsItemToUpdate.heading = entityNews.heading;
                        newsItemToUpdate.story = entityNews.story;
                        newsItemToUpdate.newsLinks = entityNews.newsLinks;
                    }
                }
            }, function () {
                // failure
                alert('Error updating header (saved successfully)');
            });

            $scope.successMessage = 'News saved';
        });
    };

    $scope.delete = function(news) {
        NewsService.delete(news).then(function() {
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

}]);
