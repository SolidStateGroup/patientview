'use strict';
angular.module('patientviewApp').controller('MyConditionsEnterConditionsCtrl',['$scope', 'CodeService', 'DiagnosisService', '$timeout', 'StaticDataService',
function ($scope, CodeService, DiagnosisService, $timeout, StaticDataService) {

    // similar to enterdiagnoses.js
    $scope.init = function() {
        if (!$scope.loaded) {
            $scope.selectedConditions = [];

            DiagnosisService.getPatientEntered($scope.loggedInUser.id).then(function (conditions) {
                $scope.selectedConditions = conditions;

                $timeout(function () {
                    var $select = $('#select-diagnosis').selectize({
                        closeAfterSelect: true,
                        valueField: 'code',
                        labelField: 'description',
                        searchField: 'description',
                        sortField: 'description',
                        onChange: function (code) {
                            $scope.noResults = false;

                            if (code != null && code != undefined && code != '') {
                                if (!_.findWhere($scope.selectedConditions, {code : code})) {
                                    CodeService.getPatientViewStandardCodes(code).then(function (codes) {
                                        addCondition(codes[0]);
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
                        load: function (query, callback) {
                            if (!query.length) return callback();

                            $scope.noResults = false;

                            CodeService.getPatientViewStandardCodes(query).then(function (codes) {
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
                });

                $scope.loaded = true;
            }, function () {
                $scope.loaded = true;
                alert("There was a problem retrieving your conditions");
            });
        }

    };

    var addCondition = function(code) {
        DiagnosisService.addPatientEntered($scope.loggedInUser.id, [code.code]).then(function() {
            $scope.selectedConditions.push(code);
        }, function() {
            alert("There was a problem saving your conditions");
        });
    };

    $scope.removeCondition = function (condition) {
        DiagnosisService.removePatientEntered($scope.loggedInUser.id, condition.code).then(function() {
            var reduced = [];

            for (var i = 0; i < $scope.selectedConditions.length; i++) {
                if ($scope.selectedConditions[i].code !== condition.code) {
                    reduced.push($scope.selectedConditions[i]);
                }
            }

            $scope.selectedConditions = reduced;
        }, function() {
            alert("There was a problem removing your conditions");
        });
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
}]);
