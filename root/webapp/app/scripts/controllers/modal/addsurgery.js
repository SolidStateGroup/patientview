'use strict';
var AddSurgeryModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'UtilService', 'lookupMap', 'permissions',
    function ($scope, $rootScope, $modalInstance, UtilService, lookupMap, permissions) {
        var init = function () {
            $scope.permissions = permissions;
            $scope.lookupMap = lookupMap;
            $scope.months = UtilService.generateMonths();
            $scope.years = UtilService.generateYears();
            $scope.days = UtilService.generateDays();

            $scope.surgery = {};
            $scope.surgery.selectedDay = $scope.days[0];
            $scope.surgery.selectedMonth = $scope.months[0];
            $scope.surgery.selectedYear = $scope.years[0];
            $scope.surgery.selectedProcedures = [];

            delete $scope.errorMessage;
        };

        $scope.addProcedure = function (procedure) {
            var found = false;
            for (var i = 0; i < $scope.surgery.selectedProcedures.length; i++) {
                if ($scope.surgery.selectedProcedures[i].id === procedure.id) {
                    found = true;
                }
            }

            if (!found) {
                $scope.surgery.selectedProcedures.push(procedure);
            }
        };

        $scope.removeProcedure = function (procedure) {
            var procedures = [];
            for (var i = 0; i < $scope.surgery.selectedProcedures.length; i++) {
                if ($scope.surgery.selectedProcedures[i].id !== procedure.id) {
                    procedures.push($scope.surgery.selectedProcedures[i])
                }
            }
            $scope.surgery.selectedProcedures = procedures;
        };

        $scope.ok = function () {
            delete $scope.errorMessage;

            if (!UtilService.validationDate($scope.surgery.selectedDay,
                    $scope.surgery.selectedMonth, $scope.surgery.selectedYear)) {
                $scope.errorMessage = 'Please select a valid Date of Surgery';
            } else {
                // set date
                $scope.surgery.date = new Date(parseInt($scope.surgery.selectedYear),
                    parseInt($scope.surgery.selectedMonth) - 1, parseInt($scope.surgery.selectedDay));

                // check date in future
                if ($scope.surgery.date.getTime() > new Date().getTime()) {
                    $scope.errorMessage = 'Date of Surgery must not be in future';
                } else {
                    if (!$scope.surgery.selectedProcedures.length) {
                        $scope.errorMessage = 'You must select at least one Main Surgical Procedure';
                    } else {
                        // set id (for removing)
                        $scope.surgery.id = new Date().getTime();
                        $modalInstance.close($scope.surgery);
                    }
                }
            }
        };

        $scope.cancel = function () {
            delete $scope.errorMessage;
            $modalInstance.dismiss('cancel');
        };

        init();
    }];
