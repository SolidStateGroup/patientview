'use strict';

angular.module('patientviewApp').controller('NewsDetailsCtrl', ['$scope', 'NewsService', '$sce',
    function ($scope, NewsService, $sce) {

        $scope.parseStoryPreview = function (text) {
            return $sce.trustAsHtml(text);
        };

        $scope.addNewsLink = function (form, newsItem, groupId, roleId) {
            var i, newsLink;

            if ($scope.editMode) {
                if (groupId === -1) {
                    // All Groups selected
                    NewsService.addRole(newsItem.id, roleId).then(function () {
                        NewsService.get(newsItem.id).then(function (entityNewsItem) {
                            for (var i = 0; i < $scope.pagedItems.length; i++) {
                                if ($scope.pagedItems[i].id == entityNewsItem.id) {
                                    $scope.pagedItems[i].newsLinks = entityNewsItem.newsLinks;
                                }
                            }
                            newsItem = entityNewsItem;
                        }, function () {
                            alert('Error updating header (saved successfully)');
                        });
                    }, function () {
                        alert('Error adding Role');
                    });
                } else {
                    NewsService.addGroupAndRole(newsItem.id, groupId, roleId).then(function () {
                        NewsService.get(newsItem.id).then(function (entityNewsItem) {
                            for (var i = 0; i < $scope.pagedItems.length; i++) {
                                if ($scope.pagedItems[i].id == entityNewsItem.id) {
                                    $scope.pagedItems[i].newsLinks = entityNewsItem.newsLinks;
                                }
                            }
                            newsItem = entityNewsItem;
                        }, function () {
                            alert('Error updating header (saved successfully)');
                        });
                    }, function () {
                        alert('Error adding Group and Role');
                    });
                }
            } else {
                newsLink = {};
                newsLink.group = {};
                newsLink.role = {};

                if (groupId != null) {
                    for (i=0;i<newsItem.allGroups.length;i++) {
                        if (groupId === newsItem.allGroups[i].id) {
                            newsLink.group = newsItem.allGroups[i];
                        }
                    }
                }
                if (roleId != null) {
                    for (i=0;i<newsItem.allRoles.length;i++) {
                        if (roleId === newsItem.allRoles[i].id) {
                            newsLink.role = newsItem.allRoles[i];
                        }
                    }
                }

                newsItem.newsLinks.push(newsLink);
            }
        };

        $scope.removeNewsLink = function (form, newsItem, newsLink) {
            var i, groupId = null, roleId = null;

            if (newsLink.group) {
                groupId = newsLink.group.id;
            }
            if (newsLink.role) {
                roleId = newsLink.role.id;
            }

            if ($scope.editMode) {
                NewsService.removeGroupAndRole(newsItem.id, groupId, roleId).then(function () {

                    // update header for news with data from GET
                    NewsService.get(newsItem.id).then(function (entityNewsItem) {
                        for (var i=0;i<$scope.pagedItems.length;i++) {
                            if ($scope.pagedItems[i].id == entityNewsItem.id) {
                                $scope.pagedItems[i].newsLinks = entityNewsItem.newsLinks;
                            }
                        }
                        newsItem = entityNewsItem;
                    }, function () {
                        alert('Error updating header (saved successfully)');
                    });

                }, function () {
                    alert('Error adding Group');
                });
            } else {
                for (i=0;i<newsItem.newsLinks.length;i++) {
                    newsLink = newsItem.newsLinks[i];

                    if (!newsLink.group) {
                        newsLink.group = {};
                        newsLink.group.id = null;
                    }
                    if (!newsLink.role) {
                        newsLink.role = {};
                        newsLink.role.id = null;
                    }

                    if (groupId === newsLink.group.id && roleId === newsLink.role.id) {
                        newsItem.newsLinks.splice(i, 1);
                    }
                }
            }
        };

        /*
        $scope.addGroup = function (form, newsItem, groupId) {
            var i;
            // only do POST if in edit mode, otherwise just add to object
            if ($scope.editMode) {
                NewsService.addGroup(newsItem.id, groupId).then(function () {
                    for (i = 0; i < newsItem.availableGroups.length; i++) {
                        if (newsItem.availableGroups[i].id === groupId) {
                            newsItem.groups.push(newsItem.allGroups[groupId]);
                            newsItem.availableGroups.splice(i, 1);
                            newsItem.newsLinks = NewsService.getNewsLinksFromGroupsRoles(newsItem.groups, newsItem.roles);
                        }
                    }

                    // update header for news with data from GET
                    NewsService.get(newsItem.id).then(function (entityNews) {
                        for (var i=0;i<$scope.pagedItems.length;i++) {
                            if ($scope.pagedItems[i].id == entityNews.id) {
                                $scope.pagedItems[i].newsLinks = entityNews.newsLinks;
                            }
                        }
                    }, function () {
                        // failure
                        alert('Error updating header (saved successfully)');
                    });

                }, function () {
                    alert('Error adding Group');
                });
            } else {
                for (i = 0; i < newsItem.availableGroups.length; i++) {
                    if (newsItem.availableGroups[i].id === groupId) {
                        newsItem.groups.push(newsItem.allGroups[groupId]);
                        newsItem.availableGroups.splice(i, 1);
                        newsItem.newsLinks = NewsService.getNewsLinksFromGroupsRoles(newsItem.groups, newsItem.roles);
                    }
                }
            }
        };

        $scope.removeGroup = function (form, newsItem, group) {
            var i;
            // only do DELETE if in edit mode, otherwise just remove from object
            if ($scope.editMode) {
                NewsService.deleteGroup(newsItem.id, group.id).then(function () {
                    for (i = 0; i < newsItem.groups.length; i++) {
                        if (newsItem.groups[i].id === group.id) {
                            newsItem.availableGroups.push(newsItem.allGroups[group.id]);
                            newsItem.groups.splice(i, 1);
                            newsItem.newsLinks = NewsService.getNewsLinksFromGroupsRoles(newsItem.groups, newsItem.roles);
                        }
                    }

                    // update header for news with data from GET
                    NewsService.get(newsItem.id).then(function (entityNews) {
                        for (var i=0;i<$scope.pagedItems.length;i++) {
                            if ($scope.pagedItems[i].id == entityNews.id) {
                                $scope.pagedItems[i].newsLinks = entityNews.newsLinks;
                            }
                        }
                    }, function () {
                        // failure
                        alert('Error updating header (deleted successfully)');
                    });
                }, function () {
                    alert('Error deleting Group');
                });
            } else {
                for (i = 0; i < newsItem.groups.length; i++) {
                    if (newsItem.groups[i].id === group.id) {
                        newsItem.availableGroups.push(newsItem.allGroups[group.id]);
                        newsItem.groups.splice(i, 1);
                        newsItem.newsLinks = NewsService.getNewsLinksFromGroupsRoles(newsItem.groups, newsItem.roles);
                    }
                }
            }
        };

        $scope.addRole = function (form, newsItem, roleId) {
            var i;
            // only do POST if in edit mode, otherwise just add to object
            if ($scope.editMode) {
                NewsService.addRole(newsItem.id, roleId).then(function () {
                    for (i = 0; i < newsItem.availableRoles.length; i++) {
                        if (newsItem.availableRoles[i].id === roleId) {
                            newsItem.roles.push(newsItem.allRoles[roleId]);
                            newsItem.availableRoles.splice(i, 1);
                            newsItem.newsLinks = NewsService.getNewsLinksFromGroupsRoles(newsItem.groups, newsItem.roles);
                        }
                    }

                    // update header for news with data from GET
                    NewsService.get(newsItem.id).then(function (entityNews) {
                        for (var i=0;i<$scope.pagedItems.length;i++) {
                            if ($scope.pagedItems[i].id == entityNews.id) {
                                $scope.pagedItems[i].newsLinks = entityNews.newsLinks;
                            }
                        }
                    }, function () {
                        // failure
                        alert('Error updating header (saved successfully)');
                    });
                }, function () {
                    alert('Error adding Role');
                });
            } else {
                for (i = 0; i < newsItem.availableRoles.length; i++) {
                    if (newsItem.availableRoles[i].id === roleId) {
                        newsItem.roles.push(newsItem.allRoles[roleId]);
                        newsItem.availableRoles.splice(i, 1);
                        newsItem.newsLinks = NewsService.getNewsLinksFromGroupsRoles(newsItem.groups, newsItem.roles);
                    }
                }
            }
        };

        $scope.removeRole = function (form, newsItem, role) {
            var i;
            // only do DELETE if in edit mode, otherwise just remove from object
            if ($scope.editMode) {
                NewsService.deleteRole(newsItem.id, role.id).then(function () {
                    for (i = 0; i < newsItem.roles.length; i++) {
                        if (newsItem.roles[i].id === role.id) {
                            newsItem.availableRoles.push(newsItem.allRoles[role.id]);
                            newsItem.roles.splice(i, 1);
                            newsItem.newsLinks = NewsService.getNewsLinksFromGroupsRoles(newsItem.groups, newsItem.roles);
                        }
                    }

                    // update header for news with data from GET
                    NewsService.get(newsItem.id).then(function (entityNews) {
                        for (var i=0;i<$scope.pagedItems.length;i++) {
                            if ($scope.pagedItems[i].id == entityNews.id) {
                                $scope.pagedItems[i].newsLinks = entityNews.newsLinks;
                            }
                        }
                    }, function () {
                        // failure
                        alert('Error updating header (deleted successfully)');
                    });
                }, function () {
                    alert('Error deleting Role');
                });
            } else {
                for (i = 0; i < newsItem.roles.length; i++) {
                    if (newsItem.roles[i].id === role.id) {
                        newsItem.availableRoles.push(newsItem.allRoles[role.id]);
                        newsItem.roles.splice(i, 1);
                        newsItem.newsLinks = NewsService.getNewsLinksFromGroupsRoles(newsItem.groups, newsItem.roles);
                    }
                }
            }
        };*/
}]);
