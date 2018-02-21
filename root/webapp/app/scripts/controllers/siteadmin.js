'use strict';

angular.module('patientviewApp').controller('SiteAdminCtrl', ['$scope', '$modal', 'ResearchService', 'GroupService', 'RoleService', 'UserService', 'UtilService',
    function ($scope, $modal, ResearchService, GroupService, RoleService, UserService, UtilService) {

    $scope.researchStudy = {};

    $scope.init = function () {
        // set up permissions
        var permissions = {};

        // check if user is GLOBAL_ADMIN or SPECIALTY_ADMIN
        permissions.isSuperAdmin = UserService.checkRoleExists('GLOBAL_ADMIN', $scope.loggedInUser);
        permissions.isSpecialtyAdmin = UserService.checkRoleExists('SPECIALTY_ADMIN', $scope.loggedInUser);
        permissions.isUnitAdmin = UserService.checkRoleExists('UNIT_ADMIN', $scope.loggedInUser);

        permissions.canAddAllGroups = permissions.isSuperAdmin;
        permissions.canAddPublicRole = permissions.isSuperAdmin || permissions.isSpecialtyAdmin;

        if (permissions.isSuperAdmin || permissions.isSpecialtyAdmin || permissions.isUnitAdmin) {
            permissions.canAddStudies = true;
        }

        $scope.permissions = permissions;



    };

    // Save from edit
    $scope.save = function (research) {
        debugger;
        ResearchService.save(research).then(function () {

            // successfully saved, replace existing element in data grid with updated
            editNewsForm.$setPristine(true);
            $scope.saved = true;

            // update news with data from GET
            ResearchService.get(research.id).then(function (entityNews) {
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



        // get page of data every time currentPage is changed
        $scope.$watch('currentPage', function (newValue) {
            $scope.loading = true;
            ResearchService.getByUser($scope.loggedInUser.id).then(function (page) {
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
        $scope.openModalNewResearchStudy = function (size) {
            $scope.errorMessage = '';
            $scope.editMode = false;

            var modalInstance = $modal.open({
                templateUrl: 'newResearchStudy.html',
                controller: NewResearchStudyModalInstanceCtrl,
                size: size,
                backdrop: 'static',
                resolve: {
                    GroupService: function () {
                        return GroupService;
                    },
                    RoleService: function () {
                        return RoleService;
                    },
                    ResearchService: function () {
                        return ResearchService;
                    },
                    UtilService: function () {
                        return UtilService;
                    },
                    permissions: function () {
                        return $scope.permissions;
                    }
                }
            });

            modalInstance.result.then(function () {
                $scope.loading = true;
                ResearchService.getByUser($scope.loggedInUser.id, $scope.newsType, false, $scope.currentPage, $scope.itemsPerPage).then(function (page) {
                    $scope.pagedItems = page.content;
                    $scope.total = page.totalElements;
                    $scope.totalPages = page.totalPages;
                    $scope.loading = false;
                    $scope.successMessage = 'Research Study successfully created';
                }, function () {
                    $scope.loading = false;
                });
            }, function () {
                // cancel
                $scope.newResearchStudy = '';
            });

        };

        $scope.edit = function (research) {
            var i;
            $scope.saved = '';

            if (research.showEdit) {
                $scope.editResearchStudy = '';
                research.showEdit = false;
            } else {

                // close others
                for (i = 0; i < $scope.pagedItems.length; i++) {
                    $scope.pagedItems[i].showEdit = false;
                }

                $scope.editMode = true;
                research.showEdit = true;

                ResearchService.get(research.id).then(function (researchStudy) {
                    $scope.editResearchStudy = _.clone(researchStudy);
                    $scope.editResearchStudy.allRoles = _.clone($scope.permissions.allRoles);
                    $scope.editResearchStudy.allGroups = [];

                    var groups = $scope.loggedInUser.userInformation.userGroups;

                    // add 'All Groups' option (with id -1) if allowed
                    if ($scope.permissions.canAddAllGroups) {
                        group = {};
                        group.id = -1;
                        group.name = 'All Groups';
                        $scope.editResearchStudy.allGroups.push(group);
                    }

                    for (i = 0; i < groups.length; i++) {
                        var group = groups[i];
                        if (group.visible === true) {
                            $scope.editResearchStudy.allGroups.push(group);
                        }
                    }

                    $scope.groupToAdd = -1;
                }, function () {
                    alert('Error loading groups');
                });
            }
        };

        // Save from edit
        $scope.save = function (editResearchForm, research) {
            ResearchService.save(research).then(function () {

                // successfully saved, replace existing element in data grid with updated
                editResearchStudyForm.$setPristine(true);
                $scope.saved = true;

                // update news with data from GET
                ResearchService.get(research.id).then(function (entityNews) {
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
                    $scope.successMessage = 'News item successfully deleted';
                }, function () {
                    $scope.loading = false;
                });
            }, function () {
                alert('Error deleting news item');
                $scope.loading = false;
            });
        };


        $scope.init();
}]);
