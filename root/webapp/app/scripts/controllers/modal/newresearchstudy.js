'use strict';
var NewResearchStudyModalInstanceCtrl = ['$scope', '$timeout', '$rootScope', '$modalInstance', 'CodeService',
    'GroupService', 'RoleService', 'ResearchService', 'UtilService', 'permissions', 'researchStudy',
    function ($scope, $timeout, $rootScope, $modalInstance, CodeService, GroupService, RoleService,
              ResearchService, UtilService, permissions, researchStudy) {
        var i, group, newsLink = {};
        $scope.modalLoading = true;
        $scope.loading = true;


        $scope.diagnosisCodes = [];
        $scope.treatmentCode = [];

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


        if (researchStudy.id) {
            ResearchService.get(researchStudy.id).then(function (savedStudy) {
                $scope.newResearchStudy = savedStudy;
                selectize(true);
            });
        }

        $scope.ok = function () {

            $scope.newResearchStudy.creator = {};
            $scope.newResearchStudy.creator.id = $scope.loggedInUser.id;
            
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
            $scope.newResearchStudy.criteria.push({
                researchStudyCriterias: {
                    diagnosisIds: [],
                    treatmentIds: [],
                    groupIds: []
                }
            });
            selectize();
        };


        var selectize = function (doNotClear) {
            $timeout(function () {
                var diagnosisSelector = $('.selectized-diagnosis').selectize({
                    closeAfterSelect: true,
                    valueField: 'id',
                    labelField: 'description',
                    searchField: 'description',
                    sortField: 'description',
                    maxItems: 10,
                    onChange: function (searchTerm) {
                        $scope.noResults = false;

                        if (searchTerm != null && searchTerm != undefined && searchTerm != '') {
                            if (!_.findWhere($scope.selectedConditions, {code: searchTerm})) {
                                CodeService.searchDiagnosisCodes(searchTerm);
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
                var groupSelector = $('.selectized-pv-group').selectize({
                    valueField: 'id',
                    labelField: 'name',
                    searchField: 'name',
                    sortField: 'name',
                    closeAfterSelect: true,
                    maxItems: 10,
                    options: $scope.groups
                });
                var treatmentSelector = $('.selectized-treatment').selectize({
                    valueField: 'id',
                    closeAfterSelect: true,
                    labelField: 'description',
                    searchField: 'description',
                    sortField: 'description',
                    maxItems: 10,
                    onChange: function (searchTerm) {
                        $scope.noResults = false;

                        if (searchTerm != null && searchTerm != undefined && searchTerm != '') {
                            if (!_.findWhere($scope.selectedConditions, {code: searchTerm})) {
                                CodeService.searchTreatmentCodes(searchTerm);
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
                if(!doNotClear) {
                    diagnosisSelector[0].selectize.clear();
                    treatmentSelector[0].selectize.clear();
                    groupSelector[0].selectize.clear();
                }
                $scope.loading = false;
            });
        }
    }];

