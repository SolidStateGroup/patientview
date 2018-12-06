'use strict';

angular.module('patientviewApp').controller('MyconditionsCtrl',['$scope', 'PatientService', 'GroupService',
    'ObservationService', '$routeParams', 'DiagnosisService', '$timeout', 'CodeService', '$modal', 'UtilService', 'SurveyService', 'SurveyResponseService',
function ($scope, PatientService, GroupService, ObservationService, $routeParams, DiagnosisService, $timeout, CodeService, $modal, UtilService, SurveyService, SurveyResponseService) {

    $scope.changeSpecialty = function(specialty) {
        $scope.currentSpecialty = specialty;
        $scope.loading = true;

        delete $scope.activeTab;
        delete $routeParams.activeTab;

        if (specialty.code === 'IBD') {
            getPatientManagement($scope.loggedInUser, function() {
                $timeout(function() {
                    getAllPublic();
                });
            });
        } else {
            getAllPublic();
        }
    };

    $scope.openModalEnterSurveyResponses = function (surveyType) {
        $scope.surveyType = surveyType;
        // open modal and pass in required objects for use in modal scope
        var modalInstance = $modal.open({
            templateUrl: surveyType === 'PROM'? 'views/partials/pos-survey.html' : 'views/partials/eq-survey.html',
            controller: SurveyResponseDetailNewModalInstanceCtrl,
            size: 'lg',
            backdrop: 'static',
            resolve: {
                SurveyService: function(){
                    return SurveyService;
                },
                SurveyResponseService: function(){
                    return SurveyResponseService;
                },
                surveyType: function(){
                    return $scope.surveyType;
                },
                UtilService: function(){
                    return UtilService;
                }
            }
        });

        // handle modal close (via button click)
        modalInstance.result.then(function () {
            getSurveyResponses();
        }, function () {
            // close button, do nothing
        });
    };

    // get public listing of groups, used when finding child groups that provide patient information
    var getAllPublic = function() {
        GroupService.getAllPublic().then(function(groups) {
            $scope.unitGroups = [];

            // only need UNIT and DISEASE_GROUP groups, also check if any has the RENAL_HEALTH_SURVEY feature
            groups.forEach(function(group) {
                if (group.groupType.value === 'UNIT' || group.groupType.value === 'DISEASE_GROUP') {
                    $scope.unitGroups.push(group);
                }
            });

            getMyConditions();
        }, function () {
            $scope.loading = false;
            alert('Error getting patient details');
        });
    };

    var createFootcheckup = function(patient) {
        var footCheckup = {};
        footCheckup.leftFoot = {};
        footCheckup.rightFoot = {};
        footCheckup.applies = 0;

        for (var j = 0; j < patient.fhirObservations.length; j++) {
            var observation = patient.fhirObservations[j];
            if (observation.name !== 'BLOOD_GROUP') {
                if (observation.applies >= footCheckup.applies) {
                    footCheckup.group = observation.group;
                    footCheckup.location = observation.location;
                    if (observation.bodySite === 'LEFT_FOOT') {
                        footCheckup.leftFoot.group = observation.group;
                        footCheckup.applies = observation.applies;
                        if (observation.name === 'PTPULSE') {
                            footCheckup.leftFoot.PTPULSE = observation.value;
                            footCheckup.applies = observation.applies;
                        }
                        if (observation.name === 'DPPULSE') {
                            footCheckup.leftFoot.DPPULSE = observation.value;
                            footCheckup.applies = observation.applies;
                        }
                    }

                    if (observation.bodySite === 'RIGHT_FOOT') {
                        footCheckup.rightFoot.group = observation.group;
                        if (observation.name === 'PTPULSE') {
                            footCheckup.rightFoot.PTPULSE = observation.value;
                            footCheckup.applies = observation.applies;
                        }
                        if (observation.name === 'DPPULSE') {
                            footCheckup.rightFoot.DPPULSE = observation.value;
                            footCheckup.applies = observation.applies;
                        }
                    }
                }
            }
        }

        return footCheckup;
    };

    var createEyecheckup = function(patient) {
        var eyeCheckup = {};
        eyeCheckup.leftEye = {};
        eyeCheckup.rightEye = {};
        eyeCheckup.applies = 0;

        for (var j = 0; j < patient.fhirObservations.length; j++) {
            var observation = patient.fhirObservations[j];
            if (observation.name !== 'BLOOD_GROUP') {
                if (observation.applies >= eyeCheckup.applies) {
                    eyeCheckup.group = observation.group;
                    eyeCheckup.location = observation.location;

                    if (observation.bodySite === 'LEFT_EYE') {
                        eyeCheckup.leftEye.group = observation.group;
                        eyeCheckup.applies = observation.applies;
                        if (observation.name === 'MGRADE') {
                            eyeCheckup.leftEye.MGRADE = observation.value;
                        }
                        if (observation.name === 'RGRADE') {
                            eyeCheckup.leftEye.RGRADE = observation.value;
                        }
                        if (observation.name === 'VA') {
                            eyeCheckup.leftEye.VA = observation.value;
                        }
                    }

                    if (observation.bodySite === 'RIGHT_EYE') {
                        eyeCheckup.rightEye.group = observation.group;
                        eyeCheckup.applies = observation.applies;
                        if (observation.name === 'MGRADE') {
                            eyeCheckup.rightEye.MGRADE = observation.value;
                        }
                        if (observation.name === 'RGRADE') {
                            eyeCheckup.rightEye.RGRADE = observation.value;
                        }
                        if (observation.name === 'VA') {
                            eyeCheckup.rightEye.VA = observation.value;
                        }
                    }
                }
            }
        }

        return eyeCheckup;
    };

    var getWeight = function () {
        $scope.weightLoading = true;
        ObservationService.getByCode($scope.loggedInUser.id, 'weight').then(function(observations) {
            if (observations.length) {
                var observations = _.sortBy(observations, 'applies').reverse();
                $scope.weight = observations[0];
            }
            $scope.weightLoading = false;
        }, function() {
            alert('Error retrieving weight');
            $scope.weightLoading = false;
        });
    };

    var createSplitArray = function(text) {
        var arr = [];

        var split = text.split(';');
        for (var j = 0; j < split.length; j++) {
            arr.push(split[j]);
        }

        return arr;
    };

    var createPatientManagementArray = function(lookupType, observationValue) {
        var toReturn = [];
        // handle information from patient management
        if ($scope.patientManagement.lookupMap[lookupType]) {
            for (var a = 0; a < $scope.patientManagement.lookupMap[lookupType].length; a++) {
                var lookup = $scope.patientManagement.lookupMap[lookupType][a];
                var value = lookup.value;
                observationValue = observationValue.replace('.0', '');

                if (value == observationValue || value == '0' + observationValue) {
                    toReturn.push(lookup.description);
                }
            }
        }
        return toReturn;
    };

    var createPatientManagementString = function(lookupType, observationValue) {
        var toReturn = '';
        // handle information from patient management
        if ($scope.patientManagement.lookupMap[lookupType]) {
            for (var a = 0; a < $scope.patientManagement.lookupMap[lookupType].length; a++) {
                var lookup = $scope.patientManagement.lookupMap[lookupType][a];
                var value = lookup.value;
                observationValue = observationValue.replace('.0', '');

                if (value == observationValue || value == '0' + observationValue) {
                    toReturn += ' ' + (lookup.description) + ', ';
                }
            }
        }
        return toReturn;
    };

    var createMyIbd = function(patient) {
        var myIbd = {};
        var i, j;

        // NonTestObservationTypes
        for (i = 0; i < patient.fhirObservations.length; i++) {
            var observation = patient.fhirObservations[i];

            // Other Parts of the Body Affected
            if (observation.name === "BODY_PARTS_AFFECTED") {
                myIbd.bodyPartsAffected = createSplitArray(observation.value);
            }
            if (observation.name === "IBD_EGIMCOMPLICATION") {
                // handle information from patient management
                if (myIbd.bodyPartsAffected == null || myIbd.bodyPartsAffected == undefined) {
                    myIbd.bodyPartsAffected = [];
                }
                myIbd.bodyPartsAffected = myIbd.bodyPartsAffected.concat(
                    createPatientManagementArray('IBD_EGIMCOMPLICATION', observation.value));
            }

            // Year for Surveillance Colonoscopy
            if (observation.name === "COLONOSCOPY_SURVEILLANCE" || observation.name === "IBD_COLONOSCOPYSURVEILLANCE") {
                myIbd.colonoscopySurveillance = observation.applies;
            }

            // Family History
            if (observation.name === "FAMILY_HISTORY") {
                myIbd.familyHistory = createSplitArray(observation.value);
            }
            if (observation.name === "IBD_FAMILYHISTORY") {
                // handle information from patient management
                if (myIbd.familyHistory == null || myIbd.familyHistory == undefined) {
                    myIbd.familyHistory = [];
                }
                myIbd.familyHistory = myIbd.familyHistory.concat(
                    createPatientManagementArray('IBD_FAMILYHISTORY', observation.value));
            }

            // Complications
            if (observation.name === "IBD_DISEASE_COMPLICATIONS") {
                myIbd.ibdDiseaseComplications = createSplitArray(observation.value);
            }

            // Disease Extent
            if (observation.name === "IBD_DISEASE_EXTENT") {
                myIbd.ibdDiseaseExtent = observation.value;
                if (observation.diagram.length > 2) {
                    myIbd.ibdDiseaseExtentDiagram = 'images/ibd/' + observation.diagram;
                }
            }

            // Surgical History
            if (observation.name === "SURGICAL_HISTORY") {
                myIbd.surgicalHistory = createSplitArray(observation.value);
            }

            // Smoking History
            if (observation.name === "SMOKING_HISTORY") {
                myIbd.smokingHistory = createSplitArray(observation.value);
            }
            if (observation.name === "IBD_SMOKINGSTATUS") {
                // handle information from patient management
                if (myIbd.smokingHistory == null || myIbd.smokingHistory == undefined) {
                    myIbd.smokingHistory = [];
                }
                myIbd.smokingHistory = myIbd.smokingHistory.concat(
                    createPatientManagementArray('IBD_SMOKINGSTATUS', observation.value));
            }

            // Vaccination Record
            if (observation.name === "VACCINATION_RECORD" || observation.name === "IBD_VACCINATIONRECORD") {
                myIbd.vaccinationRecord = createSplitArray(observation.value);
            }
        }

        // set primary diagnosis to first condition of patient (as sent in <diagnosis>), DIAGNOSIS, not EDTA_DIAGNOSIS
        if (patient.fhirConditions.length) {
            var found = false;
            for (i = 0; i < patient.fhirConditions.length; i++) {
                if (found === false && patient.fhirConditions[i].category === 'DIAGNOSIS') {
                    myIbd.primaryDiagnosis = patient.fhirConditions[i];
                    found = true;
                }
            }
        }

        // set named consultant and IBD nurse (allow multiple)
        for (i = 0; i < patient.fhirPractitioners.length; i++) {
            var practitioner = patient.fhirPractitioners[i];
            if (practitioner.role === "NAMED_CONSULTANT") {
                myIbd.namedConsultant = createSplitArray(practitioner.name);
            }
            if (practitioner.role === "IBD_NURSE") {
                myIbd.ibdNurse = createSplitArray(practitioner.name);
            }
        }

        // set links based on diagnosis (fhirConditions) only for DIAGNOSIS, not EDTA_DIAGNOSIS
        myIbd.links = [];

        if (patient.fhirConditions.length) {
            for (i = 0; i < patient.fhirConditions.length; i++) {
                var condition = patient.fhirConditions[i];
                if (condition.links && condition.category === 'DIAGNOSIS') {
                    for (j = 0; j < condition.links.length; j++) {
                        myIbd.links.push(condition.links[j]);
                    }
                }
            }
        }

        getWeight();

        return myIbd;
    };

    // get conditions (diagnosis etc) from groups under current specialty
    var getMyConditions = function() {
        var childGroupIds = [];
        var i;

        $scope.patientDetails = '';
        delete $scope.staffEnteredDiagnosis;

        $scope.unitGroups.forEach(function(unit) {
            if (_.findWhere(unit.parentGroups, {id: $scope.currentSpecialty.id})) {
                childGroupIds.push(unit.id);
            }
        });

        if (childGroupIds.length > 0) {
            // get staff entered diagnosis if present
            var canGetStaffEnteredDiagnosis = false;
            $scope.showRenalHealthSurveys = false;
            $scope.showOptEPro = false;
            $scope.showEnterConditions = false;

            for (i=0; i<$scope.loggedInUser.groupRoles.length; i++) {
                if ($scope.loggedInUser.groupRoles[i].group.code === 'Cardiol') {
                    canGetStaffEnteredDiagnosis = true;
                }

                $scope.loggedInUser.groupRoles[i].group.groupFeatures.forEach(function(feature) {
                    $scope.showOptEPro = true;
                    if (feature.feature.name == 'RENAL_HEALTH_SURVEYS') {
                        $scope.showRenalHealthSurveys = true;
                    }
                    if (feature.feature.name == 'ENTER_OWN_DIAGNOSES') {
                        $scope.showEnterConditions = true;
                    }
                })
            }

            if (canGetStaffEnteredDiagnosis) {
                DiagnosisService.getStaffEntered($scope.loggedInUser.id).then(function(conditions) {
                    if (conditions.length) {
                        var latest = conditions[0];
                        for (var i = 0; i < conditions.length; i++) {
                            if (conditions[i].date > latest.date) {
                                latest = conditions[i];
                            }
                        }
                        if (latest.status === 'confirmed') {
                            $scope.staffEnteredDiagnosis = latest;
                        }
                    }
                }, function() {
                    alert('Failed to retrieve staff entered diagnosis');
                })
            }

            if ($scope.showEnterConditions) {
                DiagnosisService.getPatientEntered($scope.loggedInUser.id).then(function (conditions) {
                    $scope.selectedConditions = conditions;
                });
            }

            PatientService.get($scope.loggedInUser.id, childGroupIds).then(function (patientDetails) {
                $scope.patientDetails = patientDetails;

                // set checkboxes
                for (i = 0; i < $scope.patientDetails.length; i++) {
                    $scope.patientDetails[i].group.selected = true;

                    // create foot checkup object from most recent DPPULSE, PTPULSE observation data
                    $scope.patientDetails[i].footCheckup = createFootcheckup($scope.patientDetails[i]);

                    // create eye checkup object from most recent MGRADE, RGRADE, VA observation data
                    $scope.patientDetails[i].eyeCheckup = createEyecheckup($scope.patientDetails[i]);

                    // only SALIBD gets 'old' myIBD, use patient management lookups as well as standard IBD observations
                    if ($scope.currentSpecialty.code === "IBD" && $scope.patientDetails[i].group.code === "SALIBD") {
                        $scope.patientDetails[i].myIbd = createMyIbd($scope.patientDetails[i]);

                        // used to show/hide correct MyIBD tab
                        $scope.useOldMyIbd = true;
                        $scope.activeTab = 'MY_IBD';
                    } else if ($scope.currentSpecialty.code === "Cardiol") {
                        // create myIBD object if present
                        $scope.patientDetails[i].myIbd = {};
                        $scope.patientDetails[i].myIbd.primaryDiagnosis = {};
                        $scope.patientDetails[i].myIbd.primaryDiagnosis.code = 'Heart Failure';
                        $scope.patientDetails[i].myIbd.primaryDiagnosis.description = 'Heart Failure';
                    }
                }

                //console.log($scope.patientDetails[0]);

                if ($scope.patientDetails[0] && $scope.patientDetails[0].myIbd) {
                    // used for other my ibd tabs
                    $scope.primaryDiagnosis = $scope.patientDetails[0].myIbd.primaryDiagnosis;
                    $scope.myIbd = $scope.patientDetails[0].myIbd;
                }

                $scope.loading = false;
            }, function () {
                $scope.loading = false;
                alert('Error getting patient details');
            });
        } else {
            $scope.loading = false;
        }
    };

    $scope.init = function(){
        var i;
        $scope.specialties = [];
        $scope.currentSpecialty = '';
        $scope.loading = true;

        // get list of specialties for logged in user
        for (i=0;i<$scope.loggedInUser.groupRoles.length;i++) {
            if ($scope.loggedInUser.groupRoles[i].group.groupType.value === 'SPECIALTY'
                && $scope.loggedInUser.groupRoles[i].group.code !== 'Generic'
                && $scope.loggedInUser.groupRoles[i].group.code !== 'GENERAL_PRACTICE') {
                $scope.specialties.push($scope.loggedInUser.groupRoles[i].group);
            }
        }

        if ($scope.specialties.length) {
            var specialty = $scope.specialties[0];
            // get conditions based on first specialty or route param if present
            if ($routeParams.s !== undefined) {
                for (i=0; i<$scope.specialties.length; i++) {
                    if ($scope.specialties[i].code.toLowerCase() === $routeParams.s.toLowerCase()) {
                        specialty = $scope.specialties[i];
                    }
                }
            }

            $scope.currentSpecialty = specialty;

            // handle routing to specific specialty
            if ($routeParams.specialty !== undefined) {
                specialty = _.findWhere($scope.specialties, {code: $routeParams.specialty});
                if (specialty != null && specialty != undefined) {
                    $scope.currentSpecialty = specialty;
                }
            }

            // handle linking to specific tabs
            if ($routeParams.activeTab !== undefined) {
                $scope.activeTab = $routeParams.activeTab;
            }

            getAllPublic();
        } else {
            alert('Error getting specialties');
        }

        // timeout required to send broadcast after everything else done
        $timeout(function() {
            getPatientManagement($scope.loggedInUser);
        });
    };

    var getPatientManagement = function (user, callback) {
        // get patient management information based on group with IBD_PATIENT_MANAGEMENT feature
        var patientManagementGroupId = null;
        var patientManagementIdentifierId = null;
        var i, j;

        for (i = 0; i < user.groupRoles.length; i++) {
            if (patientManagementGroupId == null) {
                var group = user.groupRoles[i].group;
                for (j = 0; j < group.groupFeatures.length; j++) {
                    if (group.groupFeatures[j].feature.name === 'IBD_PATIENT_MANAGEMENT') {
                        patientManagementGroupId = group.id;
                    }
                }
            }
        }

        // based on first identifier
        for (i = 0; i < user.identifiers.length; i++) {
            if (patientManagementIdentifierId == null) {
                patientManagementIdentifierId = user.identifiers[i].id;
            }
        }

        if (patientManagementGroupId !== null && patientManagementIdentifierId !== null) {
            PatientService.getPatientManagement(user.id, patientManagementGroupId, patientManagementIdentifierId)
                .then(function (patientManagement) {
                    if (patientManagement !== undefined && patientManagement !== null) {
                        $scope.patientManagement = patientManagement;
                    } else {
                        $scope.patientManagement = {};
                    }

                    $scope.patientManagement.groupId = patientManagementGroupId;
                    $scope.patientManagement.identifierId = patientManagementIdentifierId;
                    $scope.patientManagement.userId = user.id;

                    $scope.$broadcast('patientManagementInit', {});

                    if (callback) {
                        callback();
                    }
                }, function () {
                    alert('Error retrieving patient management information');
                });
        }
    };

    $scope.showEnterDiagnosesModal = function () {
        var modalInstance = $modal.open({
            templateUrl: 'views/modal/enterDiagnosesModal.html',
            controller: EnterDiagnosesModalInstanceCtrl,
            size: 'lg',
            resolve: {
                CodeService: function () {
                    return CodeService;
                },
                DiagnosisService: function () {
                    return DiagnosisService;
                },
                fromDashboard: function () {
                    return false;
                }
            }
        });

        modalInstance.result.then(function () {
            // ok (not used)
        }, function () {
            // closed
            DiagnosisService.getPatientEntered($scope.loggedInUser.id).then(function (conditions) {
                $scope.selectedConditions = conditions;
            });
        });
    };

    $scope.init();
}]);
