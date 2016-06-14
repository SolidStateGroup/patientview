'use strict';
var EnterDiagnosesModalInstanceCtrl = ['$scope', '$timeout', '$modalInstance', 'StaticDataService', 'CodeService', 'DiagnosisService',
    function ($scope, $timeout, $modalInstance, StaticDataService, CodeService, DiagnosisService) {

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
                        if (code != null && code != undefined && code != '') {
                            CodeService.getPatientViewStandardCodes(code).then(function (codes) {
                                $scope.selectedConditions.push(codes[0]);
                                $select[0].selectize.clear();
                            });
                        }
                    },
                    render: {
                        option: function (item, escape) {
                            return '<div>' + escape(item.description) + '</div>';
                        }
                    },
                    load: function(query, callback) {
                        if (!query.length) return callback();
                        //if (query.length < 3) return callback();

                        CodeService.getPatientViewStandardCodes(query).then(function(codes) {
                            callback(codes);
                            $scope.loading = false;
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

            $scope.selectedConditions = reduced;
        };

        $scope.saveConditions = function () {
            $scope.saving = true;
            var codes = [];

            for (var i = 0; i < $scope.selectedConditions.length; i++) {
                codes.push($scope.selectedConditions[i].code);
            }

            DiagnosisService.addPatientEntered($scope.loggedInUser.id, codes).then(function() {
                $scope.successMessage = "Your condition(s) have been successfully saved.";
                $scope.saving = false;
            }, function() {
                alert("There was a problem saving your conditions");
                $scope.saving = false;
            });
        };

        init();
    }];