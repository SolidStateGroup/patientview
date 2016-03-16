'use strict';

// add Surgery modal instance controller
var AddSurgeryModalInstanceCtrl = ['$scope', '$rootScope', '$modalInstance', 'UtilService', 'lookupMap',
    function ($scope, $rootScope, $modalInstance, UtilService, lookupMap) {
        var init = function () {
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
                $scope.errorMessage = "Please select a valid date";
            } else {
                // set date
                $scope.surgery.date = new Date(parseInt($scope.surgery.selectedYear),
                    parseInt($scope.surgery.selectedMonth) - 1, parseInt($scope.surgery.selectedDay));

                // set id (for removing)
                $scope.surgery.id = new Date().getTime();
                $modalInstance.close($scope.surgery);
            }
        };

        $scope.cancel = function () {
            delete $scope.errorMessage;
            $modalInstance.dismiss('cancel');
        };

        init();
    }];


angular.module('patientviewApp').controller('IbdPatientManagementCtrl', ['$scope', '$rootScope', 'SurveyService',
    'SurveyResponseService', '$modal', 'UtilService', 'PatientService',
function ($scope, $rootScope, SurveyService, SurveyResponseService, $modal, UtilService, PatientService) {

    $scope.addEgimComplication = function(complication) {
        if ($scope.patientManagement.IBD_EGIMCOMPLICATION === undefined) {
            $scope.patientManagement.IBD_EGIMCOMPLICATION = {};
            $scope.patientManagement.IBD_EGIMCOMPLICATION.values = [];
        }

        var found = false;
        for (var i = 0; i < $scope.patientManagement.IBD_EGIMCOMPLICATION.values.length; i++) {
            if ($scope.patientManagement.IBD_EGIMCOMPLICATION.values[i].id === complication.id) {
                found = true;
            }
        }

        if (!found) {
            $scope.patientManagement.IBD_EGIMCOMPLICATION.values.push(complication);
        }
    };

    $scope.calculateBMI = function() {
        if ($scope.patientManagement.height !== undefined
            && $scope.patientManagement.height.length
            && $scope.patientManagement.weight !== undefined
            && $scope.patientManagement.weight.length) {
            var height = parseFloat($scope.patientManagement.height);
            var weight = parseFloat($scope.patientManagement.weight);

            return (weight / (height * height)).toFixed(2);
        }
    };

    $scope.init = function() {
        delete $scope.successMessage;

        var i, j;
        $scope.patientManagement = {};
        $scope.patientManagement.surgeries = [];
        $scope.patientManagement.diagnoses = [];

        $scope.years = UtilService.generateYears();
        $scope.yearsPlusSix = UtilService.generateYears(new Date().getFullYear() + 6);
        //$scope.patientManagement.IBD_COLONOSCOPYSURVEILLANCE = $scope.yearsPlusSix[0];

        $scope.lookupMap = [];

        $scope.patientManagement.answers = {};
        $scope.patientManagement.answers['IBD_COLONOSCOPYSURVEILLANCE'] = {};
        $scope.patientManagement.answers['IBD_COLONOSCOPYSURVEILLANCE'].value = $scope.yearsPlusSix[0];
        $scope.patientManagement.answers['IBD_ALLERGYSUBSTANCE'] = {};
        $scope.patientManagement.answers['IBD_VACCINATIONRECORD'] = {};
        $scope.patientManagement.answers['IBD_PATIENT_MANAGEMENT_EGIMCOMPLICATIONSOTHER'] = {};

        // object to store lookup and text based answers

        // get lookups and minimal diagnoses list
        PatientService.getPatientManagementLookupTypes().then(function(lookupTypes) {
            for (i = 0; i < lookupTypes.length; i++) {
                $scope.lookupMap[lookupTypes[i].type] = [];

                // create answer object (not surgery procedure)
                if (lookupTypes[i].type !== 'IBD_SURGERYMAINPROCEDURE') {
                    $scope.patientManagement.answers[lookupTypes[i].type] = {};
                    $scope.patientManagement.answers[lookupTypes[i].type].type = 'SELECT';
                }
                //console.log($scope.patientManagement.answers[lookupTypes[i].type]);
                //console.log($scope.patientManagement.answers);

                for (j = 0; j < lookupTypes[i].lookups.length; j++) {
                    $scope.lookupMap[lookupTypes[i].type].push(lookupTypes[i].lookups[j]);
                }
            }

            PatientService.getPatientManagementDiagnoses().then(function(diagnoses) {
                for (i = 0; i < diagnoses.length; i++) {
                    $scope.patientManagement.diagnoses.push(diagnoses[i]);
                }
            }, function () {
                alert('error getting patient management programme diagnoses')
            });
        }, function () {
            alert('error getting patient management programme details')
        });
    };

    $scope.removeEgimComplication = function (complication) {
        var complications = [];
        for (var i = 0; i < $scope.patientManagement.IBD_EGIMCOMPLICATION.values.length; i++) {
            if ($scope.patientManagement.IBD_EGIMCOMPLICATION.values[i].id !== complication.id) {
                complications.push($scope.patientManagement.IBD_EGIMCOMPLICATION.values[i])
            }
        }
        $scope.patientManagement.IBD_EGIMCOMPLICATION.values = complications;
    };

    $scope.removeSurgery = function(surgery) {
        var surgeries = [];
        for (var i = 0; i < $scope.patientManagement.surgeries.length; i++) {
            if ($scope.patientManagement.surgeries[i].id !== surgery.id) {
                surgeries.push(surgery);
            }
        }
        $scope.patientManagement.surgeries = surgeries;
    };

    $scope.showSurgeryModal = function() {
        // open modal and pass in required objects for use in modal scope
        var modalInstance = $modal.open({
            templateUrl: 'addSurgeryModal.html',
            controller: AddSurgeryModalInstanceCtrl,
            size: 'lg',
            backdrop: 'static',
            resolve: {
                UtilService: function() {
                    return UtilService;
                },
                lookupMap: function() {
                    return $scope.lookupMap;
                }
            }
        });

        // testing
        console.log($scope.patientManagement.answers);

        // handle modal close (via button click)
        modalInstance.result.then(function (surgery) {
            $scope.patientManagement.surgeries.push(surgery);
        }, function () {

        });
    };

    $scope.init();
}]);
