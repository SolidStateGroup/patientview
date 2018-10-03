'use strict';
var EnterDiagnosesModalInstanceCtrl = ['$scope', '$rootScope', '$timeout', '$modalInstance', 'CodeService', 'DiagnosisService', 'fromDashboard',
    function ($scope, $rootScope, $timeout, $modalInstance, CodeService, DiagnosisService, fromDashboard) {

        $scope.addCondition = function(code) {
            if (!_.findWhere($scope.selectedConditions, {code: code.code})) {
                if ($scope.fromDashboard) {
                    CodeService.getPublic(code.id).then(function (code) {
                        $scope.selectedConditions.push(code);
                        $rootScope.loggedInUser.userInformation.shouldEnterCondition = false;
                    });
                } else {
                    $scope.saving = true;
                    DiagnosisService.addPatientEntered($scope.loggedInUser.id, [code.code]).then(function () {
                        DiagnosisService.getPatientEntered($scope.loggedInUser.id).then(function (conditions) {
                            $scope.selectedConditions = conditions;
                            $scope.saving = false;
                            selectize();
                        });
                        $rootScope.loggedInUser.userInformation.shouldEnterCondition = false;
                    }, function () {
                        alert('There was a problem saving your conditions');
                        $scope.saving = false;
                        selectize();
                    });

                }
            }
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };

        $scope.changeCategory = function(category) {
            CodeService.getByCategory(category).then(function (conditions) {
                $scope.categoryConditions = conditions;
            });
        };

        var init = function() {
            $scope.loading = true;

            if ($scope.fromDashboard) {
                $scope.selectedConditions = [];
                selectize();

                CodeService.getCategories().then(function (categories) {
                    $scope.categories = categories;
                });
            } else {
                DiagnosisService.getPatientEntered($scope.loggedInUser.id).then(function (conditions) {
                    $scope.selectedConditions = conditions;
                    selectize();
                });

                CodeService.getCategories().then(function (categories) {
                    $scope.categories = categories;
                });
            }
        };

        $scope.removeCondition = function (condition) {
            if ($scope.fromDashboard) {
                var reduced = [];

                for (var i = 0; i < $scope.selectedConditions.length; i++) {
                    if ($scope.selectedConditions[i].code !== condition.code) {
                        reduced.push($scope.selectedConditions[i]);
                    }
                }

                if (reduced.length == 0) {
                    $rootScope.loggedInUser.userInformation.shouldEnterCondition = true;
                }

                $scope.selectedConditions = reduced;
            } else {
                DiagnosisService.removePatientEntered($scope.loggedInUser.id, condition.code).then(function() {
                    DiagnosisService.getPatientEntered($scope.loggedInUser.id).then(function (conditions) {
                        $scope.selectedConditions = conditions;
                        if (conditions.length == 0) {
                            $rootScope.loggedInUser.userInformation.shouldEnterCondition = true;
                        }
                    });
                }, function() {
                    alert('There was a problem removing your conditions');
                });
            }
        };

        $scope.saveConditions = function () {
            $scope.saving = true;
            var codes = [];

            for (var i = 0; i < $scope.selectedConditions.length; i++) {
                codes.push($scope.selectedConditions[i].code);
            }

            DiagnosisService.addMultiplePatientEntered($scope.loggedInUser.id, codes).then(function() {
                $scope.successMessage = 'Your condition(s) have been successfully saved. You can view trusted info and links by clicking the "My Conditions" menu item.';
                $scope.saving = false;
            }, function() {
                alert('There was a problem saving your conditions');
                $scope.saving = false;
            });
        };

        var selectize = function() {
            $timeout(function () {
                var $select = $('#select-diagnosis').selectize({
                    closeAfterSelect: true,
                    valueField: 'code',
                    labelField: 'description',
                    searchField: 'description',
                    sortField: 'description',
                    onChange: function (searchTerm) {
                        $scope.noResults = false;

                        if (searchTerm != null && searchTerm != undefined && searchTerm != '') {
                            if (!_.findWhere($scope.selectedConditions, {code: searchTerm})) {
                                CodeService.searchDiagnosisCodesByStandard(searchTerm, "PATIENTVIEW")
                                    .then(function (codes) {
                                    var codesMap =  {};
                                    codes.forEach(function(c){
                                        codesMap[c.code] = c;
                                    });

                                    $scope.addCondition(codesMap[$select[0].selectize.getValue()]);
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

                        CodeService.searchDiagnosisCodesByStandard(searchTerm, "PATIENTVIEW").then(function (codes) {
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
        };

        $scope.fromDashboard = fromDashboard;
        init();
    }];