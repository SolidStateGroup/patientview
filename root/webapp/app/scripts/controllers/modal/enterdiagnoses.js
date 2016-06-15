'use strict';
var EnterDiagnosesModalInstanceCtrl = ['$scope', '$rootScope', '$timeout', '$modalInstance', 'StaticDataService', 'CodeService', 'DiagnosisService',
    function ($scope, $rootScope, $timeout, $modalInstance, StaticDataService, CodeService, DiagnosisService) {

        var init = function() {
            $scope.selectedConditions = [];

            $timeout(function () {
                var $select = $('#select-diagnosis').selectize({
                    closeAfterSelect: true,
                    valueField: 'code',
                    labelField: 'description',
                    searchField: 'description',
                    sortField: 'description',
                    onChange: function(code) {
                        $scope.noResults = false;

                        if (code != null && code != undefined && code != '') {
                            if (!_.findWhere($scope.selectedConditions, {code : code})) {
                                CodeService.getPatientViewStandardCodes(code).then(function (codes) {
                                    $scope.selectedConditions.push(codes[0]);
                                    $select[0].selectize.clear();
                                    $rootScope.loggedInUser.userInformation.shouldEnterCondition = false;
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
                    load: function(query, callback) {
                        if (!query.length) return callback();

                        $scope.noResults = false;

                        CodeService.getPatientViewStandardCodes(query).then(function(codes) {
                            if (!codes.length) {
                                $scope.noResults = true;
                                callback(codes);
                                $scope.loading = false;
                            } else {
                                callback(codes);
                                $scope.loading = false;
                            }
                        }, function() {
                            $scope.loading = false;
                        });
                    }
                });
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };

        $scope.removeCondition = function (condition) {
            var reduced = [];

            for (var i = 0; i < $scope.selectedConditions.length; i++) {
                if ($scope.selectedConditions[i].id !== condition.id) {
                    reduced.push($scope.selectedConditions[i]);
                }
            }

            if (reduced.length == 0) {
                $rootScope.loggedInUser.userInformation.shouldEnterCondition = true;
            }

            $scope.selectedConditions = reduced;
        };

        $scope.saveConditions = function () {
            $scope.saving = true;
            var codes = [];

            for (var i = 0; i < $scope.selectedConditions.length; i++) {
                codes.push($scope.selectedConditions[i].code);
            }

            DiagnosisService.addMultiplePatientEntered($scope.loggedInUser.id, codes).then(function() {
                $scope.successMessage = "Your condition(s) have been successfully saved.";
                $scope.saving = false;
            }, function() {
                alert("There was a problem saving your conditions");
                $scope.saving = false;
            });
        };

        init();
    }];