'use strict';


// new news modal instance controller
var NewNewsModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'newNews', 'NewsService', 'recipients',
    function ($scope, $rootScope, $modalInstance, newNews, NewsService, recipients) {
        var i;
        $scope.newNews = newNews;
        newNews.availableRecipients = _.clone(recipients);
        newNews.allRecipients = [];

        for (i = 0; i < recipients.length; i++) {
            newNews.allRecipients[recipients[i].id] = recipients[i];
        }

        $scope.ok = function () {
            // build correct news from newNews
            var news = {};
            news.type = "MESSAGE";
            news.title = newNews.title;
            news.messages = [];
            news.open = true;

            // build message
            var message = {};
            message.user = $scope.loggedInUser;
            message.message = newNews.message;
            message.type = "MESSAGE";
            news.messages[0] = message;

            // add news users from list of users (temp anonymous = false)
            var newsUsers = [];
            for (i=0;i<newNews.recipients.length;i++) {
                newsUsers[i] = {};
                newsUsers[i].user = {};
                newsUsers[i].user.id = newNews.recipients[i].id;
                newsUsers[i].anonymous = false;
            }

            // add logged in user to list of news users
            var newsUser = {};
            newsUser.user = {};
            newsUser.user.id = $scope.loggedInUser.id;
            newsUser.anonymous = false;
            newsUsers.push(newsUser);

            news.newsUsers = newsUsers;

            NewsService.new($scope.loggedInUser, news).then(function() {
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
            page.content = $scope.addReadReceiptNotifications(page.content);
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
        var i;
        $scope.errorMessage = '';
        $scope.newNews = {};
        $scope.newNews.recipients = [];
        var roleIds = [], groupIds = [];

        // populate list of allowed recipients
        GroupService.getGroupsForUser($scope.loggedInUser.id).then(function (groups) {
            // get logged in user's groups
            for (i = 0; i < groups.length; i++) {
                var group = groups[i];
                if (group.visible === true) {
                    groupIds.push(group.id);
                }
            }

            // todo: how to deal with patients sending messages
            RoleService.getByType('STAFF').then(function(roles) {
                // get roles for recipients
                for (i = 0; i < roles.length; i++) {
                    var role = roles[i];
                    if (role.visible === true) {
                        roleIds.push(role.id);
                    }
                }

                // now have user's groups and list of roles, get all users
                UserService.getByGroupsAndRoles(groupIds, roleIds).then(function (users) {

                    // open modal
                    var modalInstance = $modal.open({
                        templateUrl: 'newNewsModal.html',
                        controller: NewNewsModalInstanceCtrl,
                        size: size,
                        resolve: {
                            recipients: function(){
                                return users;
                            },
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
                        NewsService.getAll($scope.loggedInUser, $scope.currentPage, $scope.itemsPerPage).then(function(page) {
                            page.content = $scope.addReadReceiptNotifications(page.content);
                            $scope.pagedItems = page.content;
                            $scope.total = page.totalElements;
                            $scope.totalPages = page.totalPages;
                            $scope.loading = false;
                            $scope.successMessage = 'News successfully created';
                        }, function() {
                            $scope.loading = false;
                            // error
                        });
                    }, function () {
                        // cancel
                        $scope.editNews = '';
                    });

                }, function () {
                    // error retrieving users
                    alert('Error loading possible message recipients [3]');
                });
            }, function () {
                // error retrieving roles
                alert('Error loading possible message recipients [2]');
            });
        }, function () {
            // error retrieving groups
            alert('Error loading possible message recipients [1]');
        });
    };
    
}]);
