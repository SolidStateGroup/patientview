'use strict';

angular.module('patientviewApp').controller('NewsDetailsCtrl', ['$scope', 'NewsService',
function ($scope, NewsService) {

    $scope.addNewsLink = function (form, newsItem, groupId, roleId) {
        var i, newsLink;

        var role = _.findWhere(newsItem.allRoles, {id: roleId});
        var group = _.findWhere(newsItem.allGroups, {id: groupId});

        // only All Groups can have PUBLIC role
        if ((groupId !== null && roleId !== null) && (role.name == 'PUBLIC' && group.id !== -1)) {
            alert('Can only add non logged in users to All Groups');
            return;
        }

        if ($scope.editMode) {
            if (groupId === -1) {
                // All Groups selected
                NewsService.addRole(newsItem.id, roleId).then(function () {
                    NewsService.get(newsItem.id).then(function (entityNewsItem) {
                        for (i = 0; i < $scope.pagedItems.length; i++) {
                            if ($scope.pagedItems[i].id === entityNewsItem.id) {
                                $scope.pagedItems[i].newsLinks = entityNewsItem.newsLinks;
                            }
                        }
                        newsItem.newsLinks = entityNewsItem.newsLinks;
                    }, function () {
                        alert('Error updating header (saved successfully)');
                    });
                }, function () {
                    alert('Error adding Role');
                });
            } else {
                NewsService.addGroupAndRole(newsItem.id, groupId, roleId).then(function () {
                    NewsService.get(newsItem.id).then(function (entityNewsItem) {
                        for (i = 0; i < $scope.pagedItems.length; i++) {
                            if ($scope.pagedItems[i].id === entityNewsItem.id) {
                                $scope.pagedItems[i].newsLinks = entityNewsItem.newsLinks;
                            }
                        }
                        newsItem.newsLinks = entityNewsItem.newsLinks;
                    }, function () {
                        alert('Error updating header (saved successfully)');
                    });
                }, function (failureResult) {
                    alert('Error adding Group and Role: ' + failureResult.data);
                });
            }
        } else {
            newsLink = {};
            newsLink.group = {};
            newsLink.role = {};

            if (groupId !== null) {
                newsLink.group = group;
            }
            if (roleId !== null) {
                newsLink.role = role;
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
            NewsService.removeNewsLink(newsItem.id, newsLink.id).then(function () {
                NewsService.get(newsItem.id).then(function (entityNewsItem) {
                    for (var i=0;i<$scope.pagedItems.length;i++) {
                        if ($scope.pagedItems[i].id === entityNewsItem.id) {
                            $scope.pagedItems[i].newsLinks = entityNewsItem.newsLinks;
                        }
                    }
                    newsItem.newsLinks = entityNewsItem.newsLinks;
                }, function () {
                    alert('Error updating header (saved successfully)');
                });

            }, function (failureResult) {
                alert('Error removing Group and Role: ' + failureResult.data);
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
}]);
