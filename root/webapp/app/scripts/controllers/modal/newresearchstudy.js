'use strict';
var NewResearchStudyModalInstanceCtrl = ['$scope', '$timeout', '$rootScope', '$modalInstance', 'CodeService', 'GroupService', 'RoleService', 'ResearchService', 'UtilService', 'permissions',
    function ($scope, $timeout, $rootScope, $modalInstance, CodeService, GroupService, RoleService, ResearchService, UtilService, permissions) {
        var i, group, newsLink = {};
        $scope.modalLoading = true;
        $scope.loading = true;


        $scope.diagnosisCodes = [];
        $scope.treatmentCode = [];
        // for (var i in data.content) {
        //     var item = data.content[i];
        //
        //     if (item.codeType.id === 12) {
        //         $scope.diagnosisCodes.push({
        //             id: item.id,
        //             name: item.patientFriendlyName
        //         })
        //     } else if (item.codeType.id === 13) {
        //         $scope.treatmentCode.push({
        //             id: item.id,
        //             name: item.patientFriendlyName
        //         });
        //     }
        // }


        $scope.permissions = permissions;
        $scope.groupToAdd = -1;
        $scope.newResearchStudy = {};

        $scope.days = UtilService.generateDays();
        $scope.months = UtilService.generateMonths();
        $scope.years = UtilService.generateYears(new Date().getFullYear() - 1, new Date().getFullYear() + 10).reverse();

        // populate list of allowed groups for current user
        $scope.groups = $scope.loggedInUser.userInformation.userGroups;
        // add GLOBAL_ADMIN role (no group) to all news by default
        newsLink.role = $scope.permissions.globalAdminRole;
        $scope.modalLoading = false;
        $scope.loading = false;


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

        $scope.addGroup = function (groupToAdd) {
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
            selectize();
        }


        var selectize = function () {
            $timeout(function () {
                var $select = $('.selectized-diagnosis').selectize({
                    closeAfterSelect: true,
                    valueField: 'code',
                    labelField: 'description',
                    searchField: 'description',
                    sortField: 'description',
                    onChange: function (searchTerm) {
                        $scope.noResults = false;

                        if (searchTerm != null && searchTerm != undefined && searchTerm != '') {
                            if (!_.findWhere($scope.selectedConditions, {code: searchTerm})) {
                                CodeService.searchDiagnosisCodes(searchTerm)
                                    .then(function (codes) {
                                        $select[0].selectize.clear();
                                    });
                            } else {
                                $select[0].selectize.clear();
                            }
                        }
                    },
                    render: {
                        option: function (item, escape) {
                            return '<div>' + escape(item.description) + '</div>';
                        }
                    },
                    load: function (searchTerm, callback) {
                        if (!searchTerm.length) return callback();

                        $scope.noResults = false;

                        CodeService.searchDiagnosisCodes(searchTerm).then(function (codes) {
                            if (!codes.length) {
                                $scope.noResults = true;
                                callback(codes);
                                $scope.loading = false;
                            } else {
                                callback(codes);
                                $scope.loading = false;
                            }
                        }, function () {
                            $scope.loading = false;
                        });
                    }
                });
                var $select = $('.selectized-treatment').selectize({
                    closeAfterSelect: true,
                    valueField: 'code',
                    labelField: 'description',
                    searchField: 'description',
                    sortField: 'description',
                    onChange: function (searchTerm) {
                        $scope.noResults = false;

                        if (searchTerm != null && searchTerm != undefined && searchTerm != '') {
                            if (!_.findWhere($scope.selectedConditions, {code: searchTerm})) {
                                CodeService.searchTreatmentCodes(searchTerm)
                                    .then(function (codes) {
                                        $select[0].selectize.clear();
                                    });
                            } else {
                                $select[0].selectize.clear();
                            }
                        }
                    },
                    render: {
                        option: function (item, escape) {
                            return '<div>' + escape(item.description) + '</div>';
                        }
                    },
                    load: function (searchTerm, callback) {
                        if (!searchTerm.length) return callback();

                        $scope.noResults = false;

                        CodeService.searchTreatmentCodes(searchTerm).then(function (codes) {
                            if (!codes.length) {
                                $scope.noResults = true;
                                callback(codes);
                                $scope.loading = false;
                            } else {
                                callback(codes);
                                $scope.loading = false;
                            }
                        }, function () {
                            $scope.loading = false;
                        });
                    }
                });

                $scope.loading = false;
            });
        }
    }];

