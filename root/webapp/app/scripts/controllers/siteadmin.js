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


        // get page of data every time currentPage is changed
        $scope.$watch('currentPage', function (newValue) {
            $scope.loading = true;
            $scope.getPagedResearchStudies();

        });

        $scope.viewResearchStudyModal = function (study) {
            var modalInstance = $modal.open({
                templateUrl: 'views/modal/viewResearchStudyModal.html',
                controller: ViewResearchStudyModalInstanceCtrl,
                size: 'lg',
                resolve: {
                    researchStudy: function () {
                        return study;
                    },
                }
            });

            modalInstance.result.then(function () {
                // ok (not used)
            }, function () {
                // closed
            });
        };


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
                    },
                    researchStudy: function () {
                        return null;
                    }
                }
            });

            $scope.getPagedResearchStudies();
        };



        // Save from edit
        $scope.save = function (editResearchForm, research) {
            ResearchService.save(research).then(function () {

                // successfully saved, replace existing element in data grid with updated
                editResearchStudyForm.$setPristine(true);
                $scope.saved = true;

                $scope.getPagedResearchStudies();
                $scope.successMessage = 'Research Study saved';
            });
        };


        // open modal for new news
        $scope.edit = function (study) {
            $scope.errorMessage = '';
            $scope.editMode = false;
            $scope.currentStudy = study;
            var modalInstance = $modal.open({
                templateUrl: 'newResearchStudy.html',
                controller: NewResearchStudyModalInstanceCtrl,
                backdrop: 'static',
                size: 'lg',
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
                    },
                    researchStudy: function(){
                        return study;
                    }
                }
            });

            modalInstance.result.then(function () {
                $scope.loading = true;
                $scope.getPagedResearchStudies();
            }, function () {
                // cancel
                $scope.newResearchStudy = '';
            });
        };


        $scope.remove = function (researchStudy) {
            ResearchService.remove(researchStudy).then(function () {
                $scope.loading = true;
                ResearchService.getByUser($scope.loggedInUser.id).then(function (page) {
                    $scope.pagedItems = page.content;
                    $scope.total = page.totalElements;
                    $scope.totalPages = page.totalPages;
                    $scope.loading = false;
                    $scope.successMessage = 'Research Study successfully deleted';
                }, function () {
                    $scope.loading = false;
                });
            }, function () {
                alert('Error deleting research study');
                $scope.loading = false;
            });
        };

        $scope.getPagedResearchStudies = function(){
            ResearchService.getByUser($scope.loggedInUser.id, false, $scope.currentPage, $scope.itemsPerPage).then(function (page) {
                $scope.pagedItems = page.content;
                $scope.total = page.totalElements;
                $scope.totalPages = page.totalPages;
                $scope.loading = false;
            }, function () {
                $scope.loading = false;
                // error
            });
        };

        $scope.init();
}]);
