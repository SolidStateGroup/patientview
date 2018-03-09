'use strict';
var NewResearchStudyModalInstanceCtrl = ['$scope', '$timeout', '$rootScope', '$modalInstance', 'CodeService',
        'GroupService', 'RoleService', 'ResearchService', 'UtilService', 'permissions', 'researchStudy',
        function ($scope, $timeout, $rootScope, $modalInstance, CodeService, GroupService, RoleService,
                  ResearchService, UtilService, permissions, researchStudy) {
            var i, group, newsLink = {};
            $scope.modalLoading = true;
            $scope.loading = true;

            $scope.onlyNumbers = /^\d+$/;
            $scope.diagnosisCodes = [];
            $scope.treatmentCode = [];

            $scope.permissions = permissions;
            $scope.groupToAdd = -1;
            $scope.newResearchStudy = {};

            $scope.days = UtilService.generateDays();
            $scope.months = UtilService.generateMonths();
            $scope.years = UtilService.generateYears(new Date().getFullYear(), new Date().getFullYear() + 3).reverse();

            // populate list of allowed groups for current user
            $scope.groups = $scope.loggedInUser.userInformation.userGroups;
            // add GLOBAL_ADMIN role (no group) to all news by default
            newsLink.role = $scope.permissions.globalAdminRole;


            if (researchStudy && researchStudy.id) {
                $scope.modalTypeTitle = "Edit";
                $scope.cancelModalTypeTitle = "editing";

                ResearchService.get(researchStudy.id).then(function (savedStudy) {
                    $scope.newResearchStudy = savedStudy;
                    var availableFrom = $scope.newResearchStudy.availableFrom;
                    var availableTo = $scope.newResearchStudy.availableTo;

                    $scope.newResearchStudy.availableFrom = {day: null, month: null, year: null};
                    $scope.newResearchStudy.availableTo = {day: null, month: null, year: null};

                    $scope.newResearchStudy.availableFrom.day =
                        ('0' + new Date(availableFrom).getDate().toString()).slice(-2);
                    $scope.newResearchStudy.availableFrom.month =
                        ('0' + (new Date(availableFrom).getMonth() + 1).toString()).slice(-2);
                    $scope.newResearchStudy.availableFrom.year = (new Date(availableFrom).getFullYear()).toString();

                    $scope.newResearchStudy.availableTo.day =
                        ('0' + new Date(availableTo).getDate().toString()).slice(-2);
                    $scope.newResearchStudy.availableTo.month =
                        ('0' + (new Date(availableTo).getMonth() + 1).toString()).slice(-2);
                    $scope.newResearchStudy.availableTo.year = new Date(availableTo).getFullYear().toString();

                    selectize($scope.newResearchStudy.criteria);
                    $scope.modalLoading = false;
                    $scope.loading = false;
                });
            } else {
                $scope.modalTypeTitle = "Create";
                $scope.cancelModalTypeTitle = "creating";

                $scope.newResearchStudy = {
                    availableFrom: {
                        day: null,
                        month: null,
                        year: null
                    }
                };

                $scope.newResearchStudy.availableFrom.day =
                    ('0' + (new Date().getDate()).toString()).slice(-2);
                $scope.newResearchStudy.availableFrom.month =
                    ('0' + (new Date().getMonth() + 1).toString()).slice(-2);
                $scope.newResearchStudy.availableFrom.year = (new Date().getFullYear()).toString();
                $scope.modalLoading = false;
                $scope.loading = false;
            }

            $scope.ok = function () {

                //Check that the available to date is after the from date
                var availableFrom = new Date($scope.newResearchStudy.availableFrom.year, $scope.newResearchStudy.availableFrom.month - 1, $scope.newResearchStudy.availableFrom.day);
                var availableTo = new Date($scope.newResearchStudy.availableTo.year, $scope.newResearchStudy.availableTo.month - 1, $scope.newResearchStudy.availableTo.day);
                var today = new Date();
                today.setHours(0,0,0,0);

                if (!$scope.newResearchStudy.id) {
                    if (availableTo.getTime() < availableFrom.getTime()) {
                        $scope.availableDateError = "The 'To' date should be on or after the 'From' date";
                    } else if (availableTo.getTime() < today.getTime()) {
                        $scope.availableDateError = "The  'To' date should be today or later.";
                    } else if (availableFrom.getTime() < today.getTime()) {
                        $scope.availableDateError = "The  'From' date should be today or later.";
                    } else {
                        $scope.availableDateError = "";
                    }
                }else{
                    if (availableTo.getTime() < availableFrom.getTime()) {
                        $scope.availableDateError = "The 'To' date should be on or after the 'From' date";
                    } else {
                        $scope.availableDateError = "";
                    }
                }

                //If there are no criteria, show the error
                if ($scope.newResearchStudy.criteria == null || $scope.newResearchStudy.criteria.length == 0) {
                    $scope.criteriaError = true;
                }
                //If there is a critera, check that there is content there
                if ($scope.newResearchStudy.criteria != null && $scope.newResearchStudy.criteria.length != 0 &&
                    ($scope.newResearchStudy.criteria[0].researchStudyCriterias.diagnosisIds == null || $scope.newResearchStudy.criteria[0].researchStudyCriterias.diagnosisIds.length == 0) &&
                    ($scope.newResearchStudy.criteria[0].researchStudyCriterias.treatmentIds == null || $scope.newResearchStudy.criteria[0].researchStudyCriterias.treatmentIds.length == 0) &&
                    ($scope.newResearchStudy.criteria[0].researchStudyCriterias.groupIds == null || $scope.newResearchStudy.criteria[0].researchStudyCriterias.groupIds.length == 0) &&
                    $scope.newResearchStudy.criteria[0].researchStudyCriterias.fromAge == null &&
                    $scope.newResearchStudy.criteria[0].researchStudyCriterias.toAge == null &&
                    $scope.newResearchStudy.criteria[0].researchStudyCriterias.gender == null) {
                    $scope.criteriaError = true;
                }

                //If we have an error, stop!
                if ($scope.criteriaError || $scope.availableDateError) {
                    return;
                }

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

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };


            $scope.addCriteriaCluster = function () {
                delete $scope.successMessage;
                if ($scope.newResearchStudy.criteria == undefined) {
                    $scope.newResearchStudy.criteria = [];
                }
                $scope.criteriaError = false;

                $scope.newResearchStudy.criteria.push({
                    researchStudyCriterias: {
                        diagnosisIds: [],
                        treatmentIds: [],
                        groupIds: [],
                        fromAge: 18,
                        toAge: 99,
                        gender: 'Any'
                    }
                });
                selectize($scope.newResearchStudy.criteria);
            };

            $scope.removeCriteriaCluster = function (index) {
                delete $scope.newResearchStudy.criteria.splice(index, 1);
            };


            var selectize = function (model) {
                $timeout(function () {
                    //Get the selectors into an array
                    var diagnosisSelectors = [];
                    var groupsSelectors = [];
                    var treatmentSelectors = [];
                    $('.selectized-diagnosis').each(function () {
                        if (this.selectize == null && this.nodeName == "SELECT") {
                            $('#' + this.id).selectize({
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
                        }

                        if (this.selectize && this.nodeName == "SELECT") {
                            diagnosisSelectors.push(this);
                            this.selectize.clear();
                        }
                    });

                    $('.selectized-pv-group').each(function () {
                        if (this.selectize == null && this.nodeName == "SELECT") {
                            $('#' + this.id).selectize({
                                valueField: 'id',
                                labelField: 'name',
                                searchField: 'name',
                                sortField: 'name',
                                closeAfterSelect: true,
                                maxItems: 10,
                                options: $scope.groups
                            });
                        }

                        if (this.selectize && this.nodeName == "SELECT") {
                            groupsSelectors.push(this);
                            this.selectize.clear();
                        }
                    });

                    //Reinitialise the treatment selectors if necessary
                    $('.selectized-treatment').each(function () {
                            if (this.selectize == null && this.nodeName == "SELECT") {
                                $('#' + this.id).selectize({
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
                            }
                            if (this.selectize && this.nodeName == "SELECT") {
                                treatmentSelectors.push(this);
                                this.selectize.clear();
                            }
                        }
                    );


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

                            if (criteria.researchStudyCriterias.gender == null) {
                                criteria.researchStudyCriterias.gender = 'Any';
                            }
                        });
                    }
                    $scope.loading = false;
                }) ;
            }
        }
    ];