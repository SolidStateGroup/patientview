'use strict';
var NewResearchStudyModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'GroupService', 'RoleService', 'ResearchService', 'UtilService', 'permissions',
    function ($scope, $rootScope, $modalInstance, GroupService, RoleService, ResearchService, UtilService, permissions) {
        var i, group, newsLink = {};

        $scope.modalLoading = true;
        $scope.permissions = permissions;
        $scope.groupToAdd = -1;
        $scope.newResearchStudy = {};
        
        $scope.days = UtilService.generateDays();
        $scope.months = UtilService.generateMonths();
        $scope.years = UtilService.generateYears(new Date().getFullYear()-1,new Date().getFullYear()+10).reverse();

        // populate list of allowed groups for current user
        var groups = $scope.loggedInUser.userInformation.userGroups;
        // add GLOBAL_ADMIN role (no group) to all news by default
        newsLink.role = $scope.permissions.globalAdminRole;
        $scope.modalLoading = false;

        $scope.ok = function () {

            $scope.newResearchStudy.creator = {};
            $scope.newResearchStudy.creator.id = $scope.loggedInUser.id;

            debugger;
            ResearchService.create($scope.newResearchStudy).then(function () {
                    $modalInstance.close();
                }, function (result) {
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
