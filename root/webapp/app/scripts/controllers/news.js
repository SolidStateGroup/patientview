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
            permissions.isDiseaseAdmin = UserService.checkRoleExists('DISEASE_GROUP_ADMIN', $scope.loggedInUser);
            permissions.isGpAdmin = UserService.checkRoleExists('GP_ADMIN', $scope.loggedInUser);

            permissions.canAddAllGroups = permissions.isSuperAdmin;
            permissions.canAddPublicRole = permissions.isSuperAdmin || permissions.isSpecialtyAdmin;

            if (permissions.isSuperAdmin || permissions.isSpecialtyAdmin || permissions.isUnitAdmin) {
                permissions.canAddNews = true;
            }

            if (permissions.isSuperAdmin || permissions.isSpecialtyAdmin || permissions.isUnitAdmin ||
                permissions.isDiseaseAdmin || permissions.isGpAdmin) {
                permissions.canNotifyUsers = true;
            }

            $scope.permissions = permissions;

            StaticDataService.getLookupsByType("NEWS_TYPE").then(function (page) {
                $scope.newsTypes = page;
                $scope.newsType = page[2].id;

                var newsTypesArray = [];
                page.forEach(function (newsType) {
                    newsTypesArray[newsType.id] = newsType;
                });
                $scope.newsTypesArray = newsTypesArray;

                $scope.editNewsTypes = [];
                for (var newsType in newsTypesArray) {
                    if (newsTypesArray.hasOwnProperty(newsType)) {
                        var type = newsTypesArray[newsType];
                        if (type.value != "ALL") {
                            $scope.editNewsTypes.push(type);
                        }
                    }
                }
            });

            // set roles that can be added
            var allRoles = [];

            // todo: currently gets all roles, adds public and member role
            RoleService.getAll().then(function (roles) {
                for (var i = 0; i < roles.length; i++) {
                    var role = roles[i];
                    if (role.visible === true || role.name === 'PUBLIC' || role.name === 'MEMBER') {
                        allRoles.push(role);
                    }

                    // GLOBAL_ADMIN role used when creating new news items, added to all by default
                    if (role.name == 'GLOBAL_ADMIN') {
                        $scope.permissions.globalAdminRole = role;
                    }
                }

                if (permissions.isSuperAdmin) {
                    // can do everything
                } else if (permissions.isSpecialtyAdmin) {
                    // no public news
                    _.pull(allRoles, _.findWhere(allRoles, {'name': 'PUBLIC'}));
                    _.pull(allRoles, _.findWhere(allRoles, {'name': 'GLOBAL_ADMIN'}));
                } else {
                    // #458 "Unit Admin can create news for Unit Admins/Unit Staff/Patients/Logged In Users"
                    // unit admin, staff admin, gp admin, disease group admin
                    _.pull(allRoles, _.findWhere(allRoles, {'name': 'PUBLIC'}));
                    _.pull(allRoles, _.findWhere(allRoles, {'name': 'SPECIALTY_ADMIN'}));
                    _.pull(allRoles, _.findWhere(allRoles, {'name': 'GLOBAL_ADMIN'}));
                }

                $scope.permissions.allRoles = allRoles;
            }, function () {
                alert('Error loading roles');
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
            $scope.successMessage = '';
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
                    },
                    newsTypesArray: function () {
                        return $scope.newsTypesArray;
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
            $scope.successMessage = '';

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
                    $scope.editNews.allRoles = _.clone($scope.permissions.allRoles);
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
                }, function () {
                    alert('Error loading groups');
                });
            }
        };

        // Save from edit
        $scope.save = function (editNewsForm, news) {
            $scope.successMessage = '';
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

        // notify users, opens modal
        $scope.notify = function (newsId, $event) {
            $event.stopPropagation();
            $scope.successMessage = '';

            var modalInstance = $modal.open({
                templateUrl: 'newsNotificationModal.html',
                controller: NewsNotifyModalInstanceCtrl,
                resolve: {
                    newsId: function () {
                        return newsId;
                    },
                    NewsService: function () {
                        return NewsService;
                    }
                }
            });

            modalInstance.result.then(function () {
                $scope.successMessage = 'Users have been successfully notified.';
            }, function () {
                // closed
            });

        };

        $scope.remove = function (news) {
            $scope.successMessage = '';

            NewsService.remove(news).then(function () {
                $scope.loading = true;
                NewsService.getByUser($scope.loggedInUser.id, $scope.newsType, false, $scope.currentPage, $scope.itemsPerPage).then(function (page) {
                    $scope.pagedItems = page.content;
                    $scope.total = page.totalElements;
                    $scope.totalPages = page.totalPages;
                    $scope.loading = false;
                    $scope.successMessage = 'News item successfully deleted';
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
                    },
                    newsTypesArray: function () {
                        return $scope.newsTypesArray;
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
