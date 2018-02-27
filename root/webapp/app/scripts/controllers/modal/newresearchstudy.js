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


        if (researchStudy && researchStudy.id) {
            ResearchService.get(researchStudy.id).then(function (savedStudy) {
                $scope.newResearchStudy = savedStudy;
                var availableFrom = $scope.newResearchStudy.availableFrom;
                var availableTo = $scope.newResearchStudy.availableTo;

                $scope.newResearchStudy.availableFrom = {day: null, month: null, year: null};
                $scope.newResearchStudy.availableTo = {day: null, month: null, year: null};

                $scope.newResearchStudy.availableFrom.day =
                    ('0' + new Date(availableFrom).getDate().toString()).slice(-2);
                $scope.newResearchStudy.availableFrom.month =
                    ('0' + new Date(availableFrom).getMonth().toString()).slice(-2);
                $scope.newResearchStudy.availableFrom.year = (new Date(availableFrom).getFullYear()).toString();

                $scope.newResearchStudy.availableTo.day =
                    ('0' + new Date(availableTo).getDate().toString()).slice(-2);
                $scope.newResearchStudy.availableTo.month =
                    ('0' + new Date(availableTo).getMonth().toString()).slice(-2);
                $scope.newResearchStudy.availableTo.year = new Date(availableTo).getFullYear().toString();

                selectize($scope.newResearchStudy.criteria);
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
            setTimeout(function(){
                selectize($scope.newResearchStudy.criteria);
            }, 500);
        };


        var selectize = function (model) {
            $timeout(function () {
                $('.selectized-diagnosis').selectize({
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
                $('.selectized-pv-group').selectize({
                    valueField: 'id',
                    labelField: 'name',
                    searchField: 'name',
                    sortField: 'name',
                    closeAfterSelect: true,
                    maxItems: 10,
                    options: $scope.groups
                });
                $('.selectized-treatment').selectize({
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

                //Get the selectors into an array
                var diagnosisSelectors = [];
                var groupsSelectors = [];
                var treatmentSelectors = [];
                $('.selectized-diagnosis').each(function () {
                    if (this.selectize && this.nodeName == "SELECT") {
                        diagnosisSelectors.push(this);
                    }
                });

                $('.selectized-pv-group').each(function () {
                    if (this.selectize && this.nodeName == "SELECT") {
                        groupsSelectors.push(this);
                    }
                });

                $('.selectized-treatment').each(function () {
                    if (this.selectize && this.nodeName == "SELECT") {
                        treatmentSelectors.push(this);
                    }
                });

                /**
                 * Selectize adds in hashes from angular, if we're creating a new criteria block
                 * only remove the 'first' row
                 */
                $('.selectized-diagnosis').each(function () {
                    if (this.selectize) {
                        this.selectize.clear();
                    }
                });

                $('.selectized-pv-group').each(function () {
                    if (this.selectize) {
                        this.selectize.clear();
                    }
                });

                $('.selectized-treatment').each(function () {
                    if (this.selectize) {
                        this.selectize.clear();
                    }
                });


                if (model) {
                    model.map(function (criteria, index) {
                        if (criteria.researchStudyCriterias.groups) {
                            var modelIds = criteria.researchStudyCriterias.groups.map(function (m) {
                                groupsSelectors[index].selectize.addOption(m);
                                return m.id
                            });
                            groupsSelectors[index].selectize.addItems(modelIds);
                        }

                        if (criteria.researchStudyCriterias.diagnosis) {
                            var modelIds = criteria.researchStudyCriterias.diagnosis.map(function (m) {
                                diagnosisSelectors[index].selectize.addOption(m);
                                return m.id
                            });
                            diagnosisSelectors[index].selectize.addItems(modelIds);
                        }

                        if (criteria.researchStudyCriterias.treatments) {
                            var modelIds = criteria.researchStudyCriterias.treatments.map(function (m) {
                                treatmentSelectors[index].selectize.addOption(m);
                                return m.id
                            });
                            treatmentSelectors[index].selectize.addItems(modelIds);
                        }
                    });
                }
                $scope.loading = false;
            });
        }
    }];

