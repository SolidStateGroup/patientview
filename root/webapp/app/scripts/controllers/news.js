'use strict';


// new news modal instance controller
var NewNewsModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'newNews', 'NewsService',
    function ($scope, $rootScope, $modalInstance, newNews, NewsService) {
        var i;
        $scope.newNews = newNews;
        
        $scope.newNews.availableGroups = _.clone($scope.newNews.allGroups);
        $scope.newNews.allGroups = [];
        for (i = 0; i < $scope.newNews.availableGroups.length; i++) {
            $scope.newNews.allGroups[$scope.newNews.availableGroups[i].id] = $scope.newNews.availableGroups[i];
        }
        
        $scope.newNews.availableRoles = _.clone($scope.newNews.allRoles);
        $scope.newNews.allRoles = [];
        for (i = 0; i < $scope.newNews.availableRoles.length; i++) {
            $scope.newNews.allRoles[$scope.newNews.availableRoles[i].id] = $scope.newNews.availableRoles[i];
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
            page.content = page.content;
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
        $scope.editMode = false;
        $scope.newNews = {};
        $scope.newNews.groups = [];
        $scope.newNews.roles = [];
        var roleIds = [], groupIds = [];

        // populate list of allowed groups for current user
        GroupService.getGroupsForUser($scope.loggedInUser.id).then(function (groups) {
            for (i = 0; i < groups.length; i++) {
                var group = groups[i];
                if (group.visible === true) {
                    groupIds.push(group.id);
                }
            }

            $scope.newNews.allGroups = $scope.newNews.availableGroups = groups;

            // todo: currently gets all roles
            RoleService.getAll().then(function(roles) {
                for (i = 0; i < roles.length; i++) {
                    var role = roles[i];
                    if (role.visible === true) {
                        roleIds.push(role.id);
                    }
                }

                $scope.newNews.allRoles = $scope.newNews.availableRoles = roles;

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
                        page.content = page.content;
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
                // error retrieving roles
                alert('Error loading possible message recipients [2]');
            });
        }, function () {
            // error retrieving groups
            alert('Error loading possible message recipients [1]');
        });
    };

    $scope.edit = function(news) {
        if (news.showEdit) {
            news.showEdit = false;
        } else {
            news.showEdit = true;
        }
    };

    }]);
