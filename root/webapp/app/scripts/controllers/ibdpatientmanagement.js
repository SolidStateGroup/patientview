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
        $scope.loadingPatientManagement = true;
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

            // build condition (diagnosis)
            var condition = {};
            condition.code = $scope.patientManagement.diagnosis.code;
            condition.date = new Date(parseInt($scope.patientManagement.diagnosisDate.selectedYear),
                parseInt($scope.patientManagement.diagnosisDate.selectedMonth) - 1,
                parseInt($scope.patientManagement.diagnosisDate.selectedDay));
            $scope.patientManagement.fhirCondition = condition;

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
                            observation.applies = condition.date;
                            observations.push(observation);
                        }
                    }
                    else
                    if (answer.option !== undefined) {
                        // select
                        observation = {};
                        observation.name = type;
                        observation.value = answer.option.value;
                        observation.applies = condition.date;
                        observations.push(observation);
                    } else if (answer.value !== undefined && answer.value !== null) {
                        // text
                        observation = {};
                        observation.name = type;
                        observation.value = answer.value;
                        observation.applies = condition.date;
                        observations.push(observation);
                    }
                }
            }

            $scope.patientManagement.fhirObservations = observations;

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
                && $scope.patientManagement.ibdNurse !== null
                && $scope.patientManagement.ibdNurse.length) {
                practitioner = {};
                practitioner.role = 'IBD_NURSE';
                practitioner.name = $scope.patientManagement.ibdNurse;
                $scope.patientManagement.fhirPractitioners.push(practitioner);
            }

            if ($scope.patientManagement.namedConsultant !== undefined
                && $scope.patientManagement.namedConsultant !== null
                && $scope.patientManagement.namedConsultant.length) {
                practitioner = {};
                practitioner.role = 'NAMED_CONSULTANT';
                practitioner.name = $scope.patientManagement.namedConsultant;
                $scope.patientManagement.fhirPractitioners.push(practitioner);
            }

            // postcode
            $scope.patientManagement.fhirPatient = {};
            if ($scope.patientManagement.postcode !== undefined
                && $scope.patientManagement.postcode !== null
                && $scope.patientManagement.postcode.length) {
                $scope.patientManagement.fhirPatient.postcode = $scope.patientManagement.postcode;
            }

            // gender
            if ($scope.patientManagement.gender !== undefined
                && $scope.patientManagement.gender !== null
                && $scope.patientManagement.gender.description.length) {
                $scope.patientManagement.fhirPatient.gender = $scope.patientManagement.gender.description;
            }
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
        console.log('patientManagementInit');
        init();
    });

    var populateUi = function() {
        var i, j, k, date;

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

                if (lookup !== undefined && lookup !== null && answer !== undefined && answer !== null) {
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
                } else if (answer !== undefined && answer !== null
                    && observation.value !== undefined && observation.value !== null) {
                    // is a simple text value, strip .0 (added by fhir save) to handle years
                    answer.value = observation.value.replace('.0', '').toString();
                }
            }
        }

        // fhirPractitioners
        if ($scope.patientManagement.fhirPractitioners !== undefined
            && $scope.patientManagement.fhirPractitioners !== null) {

            for (i = 0; i < $scope.patientManagement.fhirPractitioners.length; i++) {
                var practitioner = $scope.patientManagement.fhirPractitioners[i];
                if (practitioner.role == 'IBD_NURSE') {
                    $scope.patientManagement.ibdNurse = practitioner.name;
                }
                if (practitioner.role == 'NAMED_CONSULTANT') {
                    $scope.patientManagement.namedConsultant = practitioner.name;
                }
            }
        }

        // fhirEncounters (surgery)
        $scope.patientManagement.surgeries = [];

        if ($scope.patientManagement.fhirEncounters !== undefined && $scope.patientManagement.fhirEncounters !== null) {
            for (i = 0; i < $scope.patientManagement.fhirEncounters.length; i++) {
                var encounter = $scope.patientManagement.fhirEncounters[i];
                var surgery = {};
                surgery.selectedProcedures = [];

                // date
                surgery.date = encounter.date;
                date = new Date(encounter.date);
                surgery.selectedYear = date.getFullYear();
                surgery.selectedDay = (date.getDate() < 10 ? "0" : null) + (date.getDate());
                surgery.selectedMonth = (date.getMonth()+1 < 10 ? "0" : null) + (date.getMonth()+1);

                // procedures
                var procedureTypes = $scope.lookupMap['IBD_SURGERYMAINPROCEDURE'];

                for (j = 0; j < encounter.procedures.length; j++) {
                    for (k = 0; k < procedureTypes.length; k++) {
                        if (encounter.procedures[j].bodySite == procedureTypes[k].value) {
                            surgery.selectedProcedures.push(procedureTypes[k]);
                        }
                    }
                }

                // observations
                for (j = 0; j < encounter.observations.length; j++) {
                    if (encounter.observations[j].name == 'SURGERY_HOSPITAL_CODE') {
                        surgery.hospitalCode = encounter.observations[j].value;
                    } else if (encounter.observations[j].name == 'SURGERY_OTHER_DETAILS') {
                        surgery.otherDetails = encounter.observations[j].value;
                    }
                }

                // for removing
                surgery.id = Math.floor(Math.random() * 9999999) + 1;

                $scope.patientManagement.surgeries.push(surgery);
            }
        }

        // fhirCondition (diagnosis)
        if ($scope.patientManagement.fhirCondition !== undefined && $scope.patientManagement.fhirCondition !== null) {
            $scope.patientManagement.diagnosis = {};

            // diagnosis
            for (i = 0; i < $scope.patientManagement.diagnoses.length; i++) {
                if ($scope.patientManagement.diagnoses[i].code == $scope.patientManagement.fhirCondition.code) {
                    $scope.patientManagement.diagnosis = $scope.patientManagement.diagnoses[i];

                    console.log($scope.patientManagement.fhirCondition);
                    // used for other my ibd tabs
                    $scope.$parent.primaryDiagnosis =  $scope.patientManagement.diagnoses[i];
                }
            }

            // date
            if ($scope.patientManagement.fhirCondition.date) {
                date = new Date($scope.patientManagement.fhirCondition.date);
                $scope.patientManagement.diagnosisDate = {};
                $scope.patientManagement.diagnosisDate.selectedYear = date.getFullYear();
                $scope.patientManagement.diagnosisDate.selectedDay
                    = (date.getDate() < 10 ? "0" : null) + (date.getDate());
                $scope.patientManagement.diagnosisDate.selectedMonth
                    = (date.getMonth()+1 < 10 ? "0" : null) + (date.getMonth()+1);

                // used for read only
                $scope.patientManagement.diagnosisDate.date = date;
            }
        }

        delete $scope.loadingPatientManagement;
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
                surgeries.push($scope.patientManagement.surgeries[i]);
            }
        }
        $scope.patientManagement.surgeries = surgeries;
    };

    // used when saving independently of creating user
    $scope.savePatientManagement = function () {
        delete $scope.patientManagement.successMessage;
        $scope.patientManagement.saving = true;

        var valid = $scope.patientManagement.validate();
        if (valid) {
            $scope.patientManagement.buildFhirObjects();

            var patientManagement = {};
            patientManagement.fhirCondition = $scope.patientManagement.fhirCondition;
            patientManagement.fhirEncounters = $scope.patientManagement.fhirEncounters;
            patientManagement.fhirObservations = $scope.patientManagement.fhirObservations;
            patientManagement.fhirPatient = $scope.patientManagement.fhirPatient;
            patientManagement.fhirPractitioners = $scope.patientManagement.fhirPractitioners;

            PatientService.savePatientManagement($scope.patientManagement.userId, $scope.patientManagement.groupId,
                $scope.patientManagement.identifierId, patientManagement).then(function() {
                $scope.patientManagement.successMessage = "Saved Patient Management Information";
                $scope.patientManagement.saving = false;
            }, function () {
                $scope.patientManagement.errorMessage = "Error Saving Patient Management Information";
                $scope.patientManagement.saving = false;
            });
        } else {
            $scope.patientManagement.saving = false;
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

        // handle modal close (via button click)
        modalInstance.result.then(function (surgery) {
            $scope.patientManagement.surgeries.push(surgery);
        }, function () {

        });
    };
}]);
