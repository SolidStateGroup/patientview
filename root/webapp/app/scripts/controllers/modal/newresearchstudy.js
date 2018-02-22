'use strict';
var NewResearchStudyModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'CodeService', 'GroupService', 'RoleService', 'ResearchService', 'UtilService', 'permissions',
    function ($scope, $rootScope, $modalInstance, CodeService, GroupService, RoleService, ResearchService, UtilService, permissions) {
        var i, group, newsLink = {};
        $scope.modalLoading = true;

        CodeService.getAll({'codeTypes': 12, 'page': 0, 'size': 10000})
            .then(function (data) {
                $scope.diagnosisCodes = data.content;


                $scope.permissions = permissions;
                $scope.groupToAdd = -1;
                $scope.newResearchStudy = {};

                $scope.days = UtilService.generateDays();
                $scope.months = UtilService.generateMonths();
                $scope.years = UtilService.generateYears(new Date().getFullYear() - 1, new Date().getFullYear() + 10).reverse();

                // populate list of allowed groups for current user
                $scope.groups = $scope.loggedInUser.userInformation.userGroups;
                debugger;
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


            });


        $scope.addGroup = function(groupToAdd) {
            debugger;
            console.log(groupToAdd);
        }

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };


        $scope.addCriteriaCluster = function (resultCluster) {
            delete $scope.successMessage;
            if ($scope.newResearchStudy.criteria == undefined) {
                $scope.newResearchStudy.criteria = [];
            }
            $scope.newResearchStudy.criteria.push([{researchStudyCriterias: {fromAge: 0}}])
        }
    }];
