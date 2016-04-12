'use strict';

// add Surgery modal instance controller
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
                $scope.errorMessage = "Please select a valid date";
            } else {
                // set date
                $scope.surgery.date = new Date(parseInt($scope.surgery.selectedYear),
                    parseInt($scope.surgery.selectedMonth) - 1, parseInt($scope.surgery.selectedDay));

                // check date in future
                if ($scope.surgery.date.getTime() > new Date().getTime()) {
                    $scope.errorMessage = "Date must not be in future";
                } else {
                    // set id (for removing)
                    $scope.surgery.id = new Date().getTime();
                    $modalInstance.close($scope.surgery);
                }
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

    $scope.getLocationDiagram = function(option) {
        var val = option.value;

        // currently hardcoded to specific lookup values
        switch (val) {
            case 'E1':
                return 'images/ibd/crohns.jpg';
            case 'E2':
                return 'images/ibd/crohns.jpg';
            case 'E3':
                return 'images/ibd/crohns.jpg';
            case 'L1':
                return 'images/ibd/crohns.jpg';
            case 'L2':
                return 'images/ibd/crohns.jpg';
            case 'L3':
                return 'images/ibd/ileo-colonic-disease.jpg';
                break;
            default :
                return null;
        }
    };

    var init = function() {
        $scope.loadingPatientManagement = true;
        delete $scope.successMessage;
        var i, j;

        $scope.days = UtilService.generateDays();
        $scope.months = UtilService.generateMonths();
        $scope.years = UtilService.generateYears();
        $scope.yearsPlusSix = UtilService.generateYears(new Date().getFullYear(), new Date().getFullYear() + 6);

        // map to parent object so can be used during create patient step, build object
        $scope.patientManagement = $scope.$parent.patientManagement;
        $scope.patientManagement.lookupMap = [];
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
        $scope.patientManagement.validate = function(callback) {
            $scope.patientManagement.errorMessage = '';
            $scope.patientManagement.buildFhirObjects();

            var patientManagement = {};
            patientManagement.condition = $scope.patientManagement.condition;
            patientManagement.encounters = $scope.patientManagement.encounters;
            patientManagement.observations = $scope.patientManagement.observations;
            patientManagement.patient = $scope.patientManagement.patient;
            patientManagement.practitioners = $scope.patientManagement.practitioners;

            PatientService.validatePatientManagement(patientManagement).then(function() {
                callback(true);
            }, function (err) {
                for (var i = 0; i < err.data.length; i++) {
                    $scope.patientManagement.errorMessage += err.data[i] + '<br/>';
                }
                callback(false);
            });
        };

        // build function to store FhirObjects
        $scope.patientManagement.buildFhirObjects = function() {
            var observations = [];
            var observation, surgery, practitioner, encounter, procedure;
            var answers = $scope.patientManagement.answers;

            // build condition (diagnosis)
            if ($scope.patientManagement.diagnosis) {
                var condition = {};
                condition.code = $scope.patientManagement.diagnosis.code;
                condition.date = new Date(parseInt($scope.patientManagement.diagnosisDate.selectedYear),
                    parseInt($scope.patientManagement.diagnosisDate.selectedMonth) - 1,
                    parseInt($scope.patientManagement.diagnosisDate.selectedDay));
                $scope.patientManagement.condition = condition;
            }

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
                            if (condition) {
                                observation.applies = condition.date;
                            }
                            observations.push(observation);
                        }
                    }
                    else
                    if (answer.option !== undefined) {
                        // select
                        observation = {};
                        observation.name = type;
                        observation.value = answer.option.value;
                        if (condition) {
                            observation.applies = condition.date;
                        }
                        observations.push(observation);
                    } else if (answer.value !== undefined && answer.value !== null) {
                        // text
                        observation = {};
                        observation.name = type;
                        observation.value = answer.value;
                        if (condition) {
                            observation.applies = condition.date;
                        }
                        observations.push(observation);
                    }
                }
            }

            $scope.patientManagement.observations = observations;

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

            $scope.patientManagement.encounters = encounters;

            // practitioners (ibdNurse, namedConsultant)
            $scope.patientManagement.practitioners = [];

            if ($scope.patientManagement.ibdNurse !== undefined
                && $scope.patientManagement.ibdNurse !== null
                && $scope.patientManagement.ibdNurse.length) {
                practitioner = {};
                practitioner.role = 'IBD_NURSE';
                practitioner.name = $scope.patientManagement.ibdNurse;
                $scope.patientManagement.practitioners.push(practitioner);
            }

            if ($scope.patientManagement.namedConsultant !== undefined
                && $scope.patientManagement.namedConsultant !== null
                && $scope.patientManagement.namedConsultant.length) {
                practitioner = {};
                practitioner.role = 'NAMED_CONSULTANT';
                practitioner.name = $scope.patientManagement.namedConsultant;
                $scope.patientManagement.practitioners.push(practitioner);
            }

            // postcode
            $scope.patientManagement.patient = {};
            if ($scope.patientManagement.postcode !== undefined
                && $scope.patientManagement.postcode !== null
                && $scope.patientManagement.postcode.length) {
                $scope.patientManagement.patient.postcode = $scope.patientManagement.postcode;
            }

            // gender
            if ($scope.patientManagement.gender !== undefined
                && $scope.patientManagement.gender !== null
                && $scope.patientManagement.gender.description.length) {
                $scope.patientManagement.patient.gender = $scope.patientManagement.gender.description;
            }
        };

        // get lookups and minimal diagnoses list
        PatientService.getPatientManagementLookupTypes().then(function(lookupTypes) {
            for (i = 0; i < lookupTypes.length; i++) {
                var lookupType = lookupTypes[i].type;
                $scope.patientManagement.lookupMap[lookupType] = [];

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
                    $scope.patientManagement.lookupMap[lookupType].push(lookupTypes[i].lookups[j]);
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

    var populateUi = function() {
        var i, j, k, date;

        // fhirPatient
        if ($scope.patientManagement.patient !== undefined && $scope.patientManagement.patient !== null) {
            // postcode
            if ($scope.patientManagement.patient.postcode !== undefined) {
                $scope.patientManagement.postcode = $scope.patientManagement.patient.postcode;
            }

            // gender (set based on description)
            if ($scope.patientManagement.patient.gender !== undefined
                && $scope.patientManagement.patient.gender !== null) {
                for (i = 0; i < $scope.patientManagement.lookupMap['GENDER'].length; i++) {
                    if ($scope.patientManagement.lookupMap['GENDER'][i].description.toUpperCase()
                        == $scope.patientManagement.patient.gender.toUpperCase()) {
                        $scope.patientManagement.gender = $scope.patientManagement.lookupMap['GENDER'][i];
                    }
                }
            }
        }

        // fhirObservations
        if ($scope.patientManagement.observations !== undefined
            && $scope.patientManagement.observations !== null) {
            for (i = 0; i < $scope.patientManagement.observations.length; i++) {
                var observation = $scope.patientManagement.observations[i];
                var name = observation.name;
                var answer = $scope.patientManagement.answers[name];
                var lookup = $scope.patientManagement.lookupMap[name];

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
        if ($scope.patientManagement.practitioners !== undefined
            && $scope.patientManagement.practitioners !== null) {

            for (i = 0; i < $scope.patientManagement.practitioners.length; i++) {
                var practitioner = $scope.patientManagement.practitioners[i];
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

        if ($scope.patientManagement.encounters !== undefined && $scope.patientManagement.encounters !== null) {
            for (i = 0; i < $scope.patientManagement.encounters.length; i++) {
                var encounter = $scope.patientManagement.encounters[i];
                var surgery = {};
                surgery.selectedProcedures = [];

                // date
                surgery.date = encounter.date;
                date = new Date(encounter.date);
                surgery.selectedYear = date.getFullYear();
                surgery.selectedDay = (date.getDate() < 10 ? "0" : null) + (date.getDate());
                surgery.selectedMonth = (date.getMonth()+1 < 10 ? "0" : null) + (date.getMonth()+1);

                // procedures
                var procedureTypes = $scope.patientManagement.lookupMap['IBD_SURGERYMAINPROCEDURE'];

                if (encounter.procedures !== null && encounter.procedures !== undefined) {
                    for (j = 0; j < encounter.procedures.length; j++) {
                        for (k = 0; k < procedureTypes.length; k++) {
                            if (encounter.procedures[j].bodySite == procedureTypes[k].value) {
                                surgery.selectedProcedures.push(procedureTypes[k]);
                            }
                        }
                    }
                }

                // observations
                if (encounter.observations !== null && encounter.observations !== undefined) {
                    for (j = 0; j < encounter.observations.length; j++) {
                        if (encounter.observations[j].name == 'SURGERY_HOSPITAL_CODE') {
                            surgery.hospitalCode = encounter.observations[j].value;
                        } else if (encounter.observations[j].name == 'SURGERY_OTHER_DETAILS') {
                            surgery.otherDetails = encounter.observations[j].value;
                        }
                    }
                }

                // for removing
                surgery.id = Math.floor(Math.random() * 9999999) + 1;

                $scope.patientManagement.surgeries.push(surgery);
            }
        }

        // fhirCondition (diagnosis)
        if ($scope.patientManagement.condition !== undefined && $scope.patientManagement.condition !== null) {
            $scope.patientManagement.diagnosis = {};

            // diagnosis
            for (i = 0; i < $scope.patientManagement.diagnoses.length; i++) {
                if ($scope.patientManagement.diagnoses[i].code == $scope.patientManagement.condition.code) {
                    $scope.patientManagement.diagnosis = $scope.patientManagement.diagnoses[i];

                    // used for other my ibd tabs
                    $scope.$parent.primaryDiagnosis = $scope.patientManagement.diagnoses[i];
                }
            }

            // date
            if ($scope.patientManagement.condition.date) {
                date = new Date($scope.patientManagement.condition.date);
                $scope.patientManagement.diagnosisDate = {};
                $scope.patientManagement.diagnosisDate.selectedYear = date.getFullYear();
                $scope.patientManagement.diagnosisDate.selectedDay
                    = (date.getDate() < 10 ? "0" : null) + (date.getDate());
                $scope.patientManagement.diagnosisDate.selectedMonth
                    = (date.getMonth()+1 < 10 ? "0" : null) + (date.getMonth()+1);

                // used for read only
                $scope.patientManagement.diagnosisDate.date = date;

                // used for SALIBD myIBD if no other Conditions
                $scope.$parent.primaryDiagnosisDate = date;
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

        savePatientManagementSurgeries();
    };

    // used when saving independently of creating user
    $scope.savePatientManagement = function (section) {
        delete $scope.patientManagement.successMessage;
        delete $scope.patientManagement.section;
        $scope.patientManagement.saving = true;

        if (section) {
            $scope.patientManagement.section = section;
        }

        $scope.patientManagement.validate(function(valid) {
            if (valid) {
                $scope.patientManagement.buildFhirObjects();

                var patientManagement = {};
                patientManagement.condition = $scope.patientManagement.condition;
                patientManagement.encounters = $scope.patientManagement.encounters;
                patientManagement.observations = $scope.patientManagement.observations;
                patientManagement.patient = $scope.patientManagement.patient;
                patientManagement.practitioners = $scope.patientManagement.practitioners;

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
        }, section);
    };

    var savePatientManagementSurgeries = function() {
        // now actually save surgeries
        delete $scope.patientManagement.errorMessage;
        delete $scope.patientManagement.successMessage;
        $scope.patientManagement.saving = true;
        $scope.patientManagement.buildFhirObjects();

        var patientManagement = {};
        patientManagement.condition = $scope.patientManagement.condition;
        patientManagement.encounters = $scope.patientManagement.encounters;
        patientManagement.observations = $scope.patientManagement.observations;
        patientManagement.patient = $scope.patientManagement.patient;
        patientManagement.practitioners = $scope.patientManagement.practitioners;

        PatientService.savePatientManagementSurgeries($scope.patientManagement.userId,
            $scope.patientManagement.groupId, $scope.patientManagement.identifierId, patientManagement)
            .then(function() {
                $scope.patientManagement.saving = false;
            }, function () {
                $scope.patientManagement.errorMessage = "Error Saving Surgeries";
                $scope.patientManagement.saving = false;
            });
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
                    return $scope.patientManagement.lookupMap;
                },
                permissions: function() {
                    return $scope.permissions;
                }
            }
        });

        // handle modal close (via button click)
        modalInstance.result.then(function (surgery) {
            $scope.patientManagement.surgeries.push(surgery);
            savePatientManagementSurgeries();
        }, function () {
        });
    };
}]);
