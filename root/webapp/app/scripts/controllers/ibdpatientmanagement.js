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
        if ($scope.patientManagement.answers['IBD_EGIMCOMPLICATION'] === undefined) {
            $scope.patientManagement.answers['IBD_EGIMCOMPLICATION'] = {};
        }

        if ($scope.patientManagement.answers['IBD_EGIMCOMPLICATION'].values === undefined) {
            $scope.patientManagement.answers['IBD_EGIMCOMPLICATION'].values = [];
        }

        var found = false;
        for (var i = 0; i < $scope.patientManagement.answers['IBD_EGIMCOMPLICATION'].values.length; i++) {
            if ($scope.patientManagement.answers['IBD_EGIMCOMPLICATION'].values[i].id === complication.id) {
                found = true;
            }
        }

        if (!found) {
            $scope.patientManagement.answers['IBD_EGIMCOMPLICATION'].values.push(complication);
        }
    };

    $scope.calculateBMI = function() {
        if ($scope.patientManagement !== undefined && $scope.patientManagement.answers !== undefined) {
            var height = $scope.patientManagement.answers['HEIGHT'].value;
            var weight = $scope.patientManagement.answers['WEIGHT'].value;

            if (height !== undefined && height.length
                && weight !== undefined && weight.length) {
                height = parseFloat(height);
                weight = parseFloat(weight);

                return (weight / (height * height)).toFixed(2);
            }
        }
    };

    var init = function() {
        delete $scope.successMessage;
        var i, j;

        $scope.days = UtilService.generateDays();
        $scope.months = UtilService.generateMonths();
        $scope.years = UtilService.generateYears();
        $scope.yearsPlusSix = UtilService.generateYears(new Date().getFullYear() + 6);
        $scope.lookupMap = [];

        // map to parent object so can be used during create patient step, build object
        $scope.patientManagement = $scope.$parent.patientManagement;
        $scope.patientManagement.surgeries = [];
        $scope.patientManagement.diagnoses = [];
        $scope.patientManagement.answers = {};
        $scope.patientManagement.answers['IBD_COLONOSCOPYSURVEILLANCE'] = {};
        $scope.patientManagement.answers['IBD_COLONOSCOPYSURVEILLANCE'].value = $scope.yearsPlusSix[0];
        $scope.patientManagement.answers['IBD_ALLERGYSUBSTANCE'] = {};
        $scope.patientManagement.answers['IBD_VACCINATIONRECORD'] = {};
        $scope.patientManagement.answers['IBD_EGIMCOMPLICATIONSOTHER'] = {};
        $scope.patientManagement.answers['HEIGHT'] = {};
        $scope.patientManagement.answers['WEIGHT'] = {};

        $scope.patientManagement.diagnosisDate = {};
        $scope.patientManagement.diagnosisDate.selectedDay = $scope.days[0];
        $scope.patientManagement.diagnosisDate.selectedMonth = $scope.months[0];
        $scope.patientManagement.diagnosisDate.selectedYear = $scope.years[0];

        // validation function, called when checking patient management
        $scope.patientManagement.validate = function() {
            var valid = true;
            $scope.patientManagement.errorMessage = '';

            // date must be valid
            var diagnosisDate = $scope.patientManagement.diagnosisDate;
            if (!UtilService.validationDate(diagnosisDate.selectedDay,
                    diagnosisDate.selectedMonth, diagnosisDate.selectedYear)) {
                $scope.patientManagement.errorMessage += 'Date of Diagnosis must be valid<br/>';
                valid = false;
            }

            // diagnosis must be set
            if ($scope.patientManagement.diagnosis === undefined) {
                $scope.patientManagement.errorMessage += 'Diagnosis must be selected<br/>';
                valid = false;
            }

            return valid;
        };

        // build function to store FhirObjects
        $scope.patientManagement.buildFhirObjects = function() {
            var observations = [];
            var observation, surgery, practitioner, encounter, procedure;
            var answers = $scope.patientManagement.answers;

            // build observations (selects and text fields)
            for (var type in answers) {
                if (answers.hasOwnProperty(type)) {
                    var answer = answers[type];
                    if (answer.values !== undefined) {
                        // multi select
                        for (i = 0; i < answer.values.length; i++) {
                            observation = {};
                            observation.name = type;
                            observation.value = answer.values[i].value;
                            observations.push(observation);
                        }
                    }
                    else
                    if (answer.option !== undefined) {
                        // select
                        observation = {};
                        observation.name = type;
                        observation.value = answer.option.value;
                        observations.push(observation);
                    } else if (answer.value !== undefined && answer.value.length) {
                        // text
                        observation = {};
                        observation.name = type;
                        observation.value = answer.value;
                        observations.push(observation);
                    }
                }
            }

            $scope.patientManagement.fhirObservations = observations;

            // build condition (diagnosis)
            var condition = {};
            condition.code = $scope.patientManagement.diagnosis.code;
            condition.date = new Date(parseInt($scope.patientManagement.diagnosisDate.selectedYear),
                parseInt($scope.patientManagement.diagnosisDate.selectedMonth) - 1,
                parseInt($scope.patientManagement.diagnosisDate.selectedDay));
            $scope.patientManagement.fhirCondition = condition;

            // build encounters (surgery)
            var encounters = [];
            for (i = 0; i < $scope.patientManagement.surgeries.length; i++) {
                surgery = $scope.patientManagement.surgeries[i];
                encounter = {};
                encounter.encounterType = 'SURGERY';
                encounter.observations = [];
                encounter.procedures = [];

                // date
                encounter.date = new Date(parseInt(surgery.selectedYear),
                    parseInt(surgery.selectedMonth) - 1,
                    parseInt(surgery.selectedDay));

                // procedures
                for (j = 0; j < surgery.selectedProcedures.length; j++) {
                    procedure = {};
                    procedure.type = 'IBD_SURGERYMAINPROCEDURE';
                    procedure.bodySite = surgery.selectedProcedures[j].value;
                    encounter.procedures.push(procedure);
                }

                // hospital code
                observation = {};
                observation.name = 'SURGERY_HOSPITAL_CODE';
                observation.value = surgery.hospitalCode;
                encounter.observations.push(observation);

                // hospital code
                observation = {};
                observation.name = 'SURGERY_OTHER_DETAILS';
                observation.value = surgery.otherDetails;
                encounter.observations.push(observation);

                encounters.push(encounter);
            }

            $scope.patientManagement.fhirEncounters = encounters;

            // practitioners (ibdNurse, namedConsultant)
            $scope.patientManagement.fhirPractitioners = [];

            if ($scope.patientManagement.ibdNurse !== undefined
                && $scope.patientManagement.ibdNurse.length) {
                practitioner = {};
                practitioner.role = 'IBD_NURSE';
                practitioner.name = $scope.patientManagement.ibdNurse;
                $scope.patientManagement.fhirPractitioners.push(practitioner);
            }

            if ($scope.patientManagement.namedConsultant !== undefined
                && $scope.patientManagement.namedConsultant.length) {
                practitioner = {};
                practitioner.role = 'NAMED_CONSULTANT';
                practitioner.name = $scope.patientManagement.namedConsultant;
                $scope.patientManagement.fhirPractitioners.push(practitioner);
            }

            // postcode
            $scope.patientManagement.fhirPatient = {};
            if ($scope.patientManagement.postcode !== undefined
                && $scope.patientManagement.postcode.length) {
                $scope.patientManagement.fhirPatient.postcode = $scope.patientManagement.postcode;
            }

            // gender
            if ($scope.patientManagement.gender !== undefined
                && $scope.patientManagement.gender.description.length) {
                $scope.patientManagement.fhirPatient.gender = $scope.patientManagement.gender.description;
            }

            console.log($scope.patientManagement);
        };

        // get lookups and minimal diagnoses list
        PatientService.getPatientManagementLookupTypes().then(function(lookupTypes) {
            for (i = 0; i < lookupTypes.length; i++) {
                var lookupType = lookupTypes[i].type;
                $scope.lookupMap[lookupType] = [];

                // create answer object (not surgery procedure)
                if (lookupType !== 'IBD_SURGERYMAINPROCEDURE') {
                    $scope.patientManagement.answers[lookupType] = {};
                    $scope.patientManagement.answers[lookupType].type = 'SELECT';
                }

                // MULTI_SELECT types
                if (lookupType === 'IBD_EGIMCOMPLICATION') {
                    $scope.patientManagement.answers[lookupType].type = 'MULTI_SELECT';
                }

                for (j = 0; j < lookupTypes[i].lookups.length; j++) {
                    $scope.lookupMap[lookupType].push(lookupTypes[i].lookups[j]);
                }
            }

            PatientService.getPatientManagementDiagnoses().then(function(diagnoses) {
                for (i = 0; i < diagnoses.length; i++) {
                    $scope.patientManagement.diagnoses.push(diagnoses[i]);
                }

                // populate interface if values are set (when editing patient)
                populateUi();

            }, function () {
                alert('error getting patient management programme diagnoses')
            });
        }, function () {
            alert('error getting patient management programme details')
        });
    };

    // handle broadcast message from parent
    $scope.$on('patientManagementInit', function() {
        init();
    });

    //
    var populateUi = function() {
        var i, j;
        //console.log($scope.patientManagement);

        // fhirPatient
        if ($scope.patientManagement.fhirPatient !== undefined && $scope.patientManagement.fhirPatient !== null) {
            // postcode
            if ($scope.patientManagement.fhirPatient.postcode !== undefined) {
                $scope.patientManagement.postcode = $scope.patientManagement.fhirPatient.postcode;
            }

            // gender (set based on description)
            if ($scope.patientManagement.fhirPatient.gender !== undefined
                && $scope.patientManagement.fhirPatient.gender !== null) {
                for (i = 0; i < $scope.lookupMap['GENDER'].length; i++) {
                    if ($scope.lookupMap['GENDER'][i].description.toUpperCase()
                        == $scope.patientManagement.fhirPatient.gender.toUpperCase()) {
                        $scope.patientManagement.gender = $scope.lookupMap['GENDER'][i];
                    }
                }
            }
        }

        // fhirObservations
        if ($scope.patientManagement.fhirObservations !== undefined
            && $scope.patientManagement.fhirObservations !== null) {
            for (i = 0; i < $scope.patientManagement.fhirObservations.length; i++) {
                var observation = $scope.patientManagement.fhirObservations[i];
                var name = observation.name;
                var answer = $scope.patientManagement.answers[name];
                var lookup = $scope.lookupMap[name];

                if (lookup !== undefined  && lookup !== null && answer !== undefined && answer !== null) {
                    // is a lookup value, either SELECT or MULTI_SELECT
                    if (answer.type === 'SELECT') {
                        for (j = 0; j < lookup.length; j++) {
                            if (lookup[j].value == parseFloat(observation.value)
                                || lookup[j].value == observation.value) {
                                answer.option = lookup[j];
                            }
                        }
                    } else if (answer.type === 'MULTI_SELECT') {
                        if (answer.values == undefined || answer.values == null) {
                            answer.values = [];
                        }
                        for (j = 0; j < lookup.length; j++) {
                            if (lookup[j].value == parseFloat(observation.value)
                                || lookup[j].value == observation.value) {
                                answer.values.push(lookup[j]);
                            }
                        }
                    }
                } else if (answer !== undefined && answer !== null) {
                    // is a simple text value
                    answer.value = observation.value;
                }
            }
        }

        // fhirPractitioners
        if ($scope.patientManagement.fhirPractitioners !== undefined
            && $scope.patientManagement.fhirPractitioners !== null) {
            //console.log($scope.patientManagement.fhirPractitioners);

            for (i = 0; i < $scope.patientManagement.fhirPractitioners.length; i++) {
                var practitioner = $scope.patientManagement.fhirPractitioners[i];
                //console.log(practitioner);
                if (practitioner.role == 'IBD_NURSE') {
                    $scope.patientManagement.ibdNurse = practitioner.name;
                }
                if (practitioner.role == 'NAMED_CONSULTANT') {
                    $scope.patientManagement.namedConsultant = practitioner.name;
                }
            }
        }

        // fhirEncounters (surgery)
        if ($scope.patientManagement.fhirEncounters !== undefined
            && $scope.patientManagement.fhirEncounters !== null) {
            var surgery = {};

            console.log($scope.patientManagement.fhirEncounters);
        }
    };

    $scope.removeEgimComplication = function (complication) {
        var complications = [];
        for (var i = 0; i < $scope.patientManagement.answers['IBD_EGIMCOMPLICATION'].values.length; i++) {
            if ($scope.patientManagement.answers['IBD_EGIMCOMPLICATION'].values[i].id !== complication.id) {
                complications.push($scope.patientManagement.answers['IBD_EGIMCOMPLICATION'].values[i])
            }
        }
        $scope.patientManagement.answers['IBD_EGIMCOMPLICATION'].values = complications;
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

    // used when saving independently of creating user
    $scope.savePatientManagement = function () {
        var valid = $scope.patientManagement.validate();
        if (valid) {
            $scope.patientManagement.buildFhirObjects();

            var patientManagement = {};
            patientManagement.fhirCondition = $scope.patientManagement.fhirCondition;
            patientManagement.fhirEncounters = $scope.patientManagement.fhirEncounters;
            patientManagement.fhirObservations = $scope.patientManagement.fhirObservations;
            patientManagement.fhirPatient = $scope.patientManagement.fhirPatient;
            patientManagement.fhirPractitioners = $scope.patientManagement.fhirPractitioners;

            console.log("Save Patient Management");
            console.log($scope.patientManagement);
            console.log(patientManagement);
        }
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

        $scope.patientManagement.validate();
        if (!$scope.patientManagement.errorMessage.length) {
            $scope.patientManagement.buildFhirObjects();

            var patientManagement = {};
            patientManagement.fhirCondition = $scope.patientManagement.fhirCondition;
            patientManagement.fhirEncounters = $scope.patientManagement.fhirEncounters;
            patientManagement.fhirObservations = $scope.patientManagement.fhirObservations;
            patientManagement.fhirPatient = $scope.patientManagement.fhirPatient;
            patientManagement.fhirPractitioners = $scope.patientManagement.fhirPractitioners;

            console.log(patientManagement);
        }

        // handle modal close (via button click)
        modalInstance.result.then(function (surgery) {
            $scope.patientManagement.surgeries.push(surgery);
        }, function () {

        });
    };
}]);
