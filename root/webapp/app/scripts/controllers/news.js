'use strict';

// pagination following http://fdietz.github.io/recipes-with-angular-js/common-user-interface-patterns/paginating-through-server-side-data.html
angular.module('patientviewApp').controller('NewsCtrl', ['$scope', '$modal', '$q', 'NewsService', 'GroupService',
    'RoleService', 'UserService', 'StaticDataService',
    function ($scope, $modal, $q, NewsService, GroupService, RoleService, UserService, StaticDataService) {

        $scope.itemsPerPage = 10;
        $scope.currentPage = 0;

        $scope.init = function () {
            // set up permissions
            var permissions = {};

            // check if user is GLOBAL_ADMIN or SPECIALTY_ADMIN
            permissions.isSuperAdmin = UserService.checkRoleExists('GLOBAL_ADMIN', $scope.loggedInUser);
            permissions.isSpecialtyAdmin = UserService.checkRoleExists('SPECIALTY_ADMIN', $scope.loggedInUser);
            permissions.isUnitAdmin = UserService.checkRoleExists('UNIT_ADMIN', $scope.loggedInUser);

            permissions.canAddAllGroups = permissions.isSuperAdmin || permissions.isSpecialtyAdmin;
            permissions.canAddPublicRole = permissions.isSuperAdmin || permissions.isSpecialtyAdmin;

            if (permissions.isSuperAdmin || permissions.isSpecialtyAdmin || permissions.isUnitAdmin) {
                permissions.canAddNews = true;
            }

            $scope.permissions = permissions;


            StaticDataService.getLookupsByType("NEWS_TYPE").then(function (page) {
                $scope.newsTypes = page;
                $scope.newsType = page[2].id;
            });
        };

        $scope.range = function () {
            var rangeSize = 5;
            var ret = [];
            var start;

            start = 1;
            if (start > $scope.totalPages - rangeSize) {
                start = $scope.totalPages - rangeSize;
            }

            for (var i = start; i < start + rangeSize; i++) {
                if (i > -1) {
                    ret.push(i);
                }
            }

            return ret;
        };

        $scope.setPage = function (n) {
            if (n > -1 && n < $scope.totalPages) {
                $scope.currentPage = n;
            }
        };

        $scope.prevPage = function () {
            if ($scope.currentPage > 0) {
                $scope.currentPage--;
            }
        };

        $scope.prevPageDisabled = function () {
            return $scope.currentPage === 0 ? 'hidden' : '';
        };

        $scope.nextPage = function () {
            if ($scope.currentPage < $scope.totalPages - 1) {
                $scope.currentPage++;
            }
        };

        $scope.nextPageDisabled = function () {
            if ($scope.totalPages > 0) {
                return $scope.currentPage === $scope.totalPages - 1 ? 'hidden' : '';
            } else {
                return 'hidden';
            }
        };

        // get page of data every time currentPage is changed
        $scope.$watch('currentPage', function (newValue) {
            $scope.loading = true;
            NewsService.getByUser($scope.loggedInUser.id, $scope.newsType, false, newValue, $scope.itemsPerPage).then(function (page) {
                $scope.pagedItems = page.content;
                $scope.total = page.totalElements;
                $scope.totalPages = page.totalPages;
                $scope.loading = false;
            }, function () {
                $scope.loading = false;
                // error
            });
        });

        // get page of data every time currentPage is changed
        $scope.$watch('newsType', function (newValue) {
            $scope.loading = true;
            NewsService.getByUser($scope.loggedInUser.id, newValue, false, $scope.currentPage, $scope.itemsPerPage).then(function (page) {
                $scope.pagedItems = page.content;
                $scope.total = page.totalElements;
                $scope.totalPages = page.totalPages;
                $scope.loading = false;
            }, function () {
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
                backdrop: 'static',
                resolve: {
                    GroupService: function () {
                        return GroupService;
                    },
                    RoleService: function () {
                        return RoleService;
                    },
                    NewsService: function () {
                        return NewsService;
                    },
                    permissions: function () {
                        return $scope.permissions;
                    }
                }
            });

            modalInstance.result.then(function () {
                $scope.loading = true;
                NewsService.getByUser($scope.loggedInUser.id, $scope.newsType, false, $scope.currentPage, $scope.itemsPerPage).then(function (page) {
                    $scope.pagedItems = page.content;
                    $scope.total = page.totalElements;
                    $scope.totalPages = page.totalPages;
                    $scope.loading = false;
                    $scope.successMessage = 'News item successfully created';
                }, function () {
                    $scope.loading = false;
                });
            }, function () {
                // cancel
                $scope.newNews = '';
            });

        };

        $scope.edit = function (news) {
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

                NewsService.get(news.id).then(function (newsItem) {
                    $scope.editNews = _.clone(newsItem);
                    $scope.editNews.allRoles = [];
                    $scope.editNews.allGroups = [];

                    var groups = $scope.loggedInUser.userInformation.userGroups;

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
                    RoleService.getAll().then(function (roles) {
                        for (i = 0; i < roles.length; i++) {
                            var role = roles[i];
                            if (role.visible === true || role.name === 'PUBLIC' || role.name === 'MEMBER') {
                                if (role.name === 'PUBLIC' && !$scope.permissions.canAddPublicRole) {

                                } else {
                                    $scope.editNews.allRoles.push(role);
                                }
                            }
                        }
                    }, function () {
                        alert('Error loading roles');
                    });
                }, function () {
                    alert('Error loading groups');
                });
            }
        };

        // Save from edit
        $scope.save = function (editNewsForm, news) {
            NewsService.save(news, news.newsType).then(function () {

                // successfully saved, replace existing element in data grid with updated
                editNewsForm.$setPristine(true);
                $scope.saved = true;

                // update news with data from GET
                NewsService.get(news.id).then(function (entityNews) {
                    for (var i = 0; i < $scope.pagedItems.length; i++) {
                        if ($scope.pagedItems[i].id === entityNews.id) {
                            var newsItemToUpdate = $scope.pagedItems[i];
                            newsItemToUpdate.heading = entityNews.heading;
                            newsItemToUpdate.story = entityNews.story;
                            newsItemToUpdate.newsLinks = entityNews.newsLinks;
                            newsItemToUpdate.lastUpdate = entityNews.lastUpdated;
                            newsItemToUpdate.lastUpdater = entityNews.lastUpdater;
                            newsItemToUpdate.newsType = entityNews.newsType;
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

        $scope.remove = function (news) {
            NewsService.remove(news).then(function () {
                $scope.loading = true;
                NewsService.getByUser($scope.loggedInUser.id, $scope.newsType, false, $scope.currentPage, $scope.itemsPerPage).then(function (page) {
                    $scope.pagedItems = page.content;
                    $scope.total = page.totalElements;
                    $scope.totalPages = page.totalPages;
                    $scope.loading = false;
                    $scope.successMessage = 'News item successfully created';
                }, function () {
                    $scope.loading = false;
                });
            }, function () {
                alert('Error deleting news item');
                $scope.loading = false;
            });
        };

        $scope.removeDuplicateNewsLinks = function (newsLinks) {
            var noDuplicates = [];
            var seen = [];

            if (newsLinks != undefined) {
                for (var i = 0; i < newsLinks.length; i++) {
                    if (newsLinks[i].group && !seen[newsLinks[i].group.id]) {
                        seen[newsLinks[i].group.id] = true;
                        noDuplicates.push(newsLinks[i]);
                    }
                }
            }

            return noDuplicates;
        };

        $scope.viewNewsItem = function (news) {
            var modalInstance = $modal.open({
                templateUrl: 'views/modal/viewNewsModal.html',
                controller: ViewNewsModalInstanceCtrl,
                size: 'lg',
                resolve: {
                    news: function () {
                        return news;
                    }
                }
            });

            modalInstance.result.then(function () {
                // ok (not used)
            }, function () {
                // closed
            });
        };

        $scope.userHasGroup = function (groupId) {
            var hasGroup = false;
            $scope.loggedInUser.groupRoles.forEach(
                function (element) {
                    console.log(element)
                    console.log(groupId)
                    if (element.group.id == groupId || element.role.name == 'GLOBAL_ADMIN') {
                        hasGroup = true;
                    }
                });
            return hasGroup;
        };

        $scope.hasGroupLink = function (newsItem) {
            var hasGroup = false;
            newsItem.newsLinks.forEach(
                function (element) {
                    if (element.group != null) {
                        hasGroup = true;

                    }

                });
            return hasGroup;
        };

        $scope.init();
    }]);
