'use strict';

angular.module('patientviewApp').controller('MyconditionsCtrl',['$scope', 'PatientService', 'GroupService', 'ObservationService',
function ($scope, PatientService, GroupService, ObservationService) {

    // get public listing of groups, used when finding child groups that provide patient information
    var getAllPublic = function() {
        GroupService.getAllPublic().then(function(groups) {
            $scope.unitGroups = [];

            // only need UNIT and DISEASE_GROUP groups
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

    var createMyIbd = function(patient) {
        var myIbd = {};
        var i, j, split;

        for (i = 0; i < patient.fhirObservations.length; i++) {
            var observation = patient.fhirObservations[i];
            // NonTestObservationTypes
            if (observation.name === "BODY_SITE_AFFECTED") {
                myIbd.bodySiteAffected = createSplitArray(observation.value);
            }
            if (observation.name === "COLONOSCOPY_SURVEILLANCE") {
                myIbd.colonoscopySurveillance = observation.applies;
            }
            if (observation.name === "FAMILY_HISTORY") {
                myIbd.familyHistory = createSplitArray(observation.value);
            }
            if (observation.name === "IBD_DISEASE_COMPLICATIONS") {
                myIbd.ibdDiseaseComplications = createSplitArray(observation.value);
            }
            if (observation.name === "IBD_DISEASE_EXTENT") {
                myIbd.ibdDiseaseExtent = observation.value;
                if (observation.diagram.length > 2) {
                    myIbd.ibdDiseaseExtentDiagram = 'images/ibd/' + observation.diagram;
                }
            }
            if (observation.name === "SURGICAL_HISTORY") {
                myIbd.surgicalHistory = createSplitArray(observation.value);
            }
            if (observation.name === "SMOKING_HISTORY") {
                myIbd.smokingHistory = createSplitArray(observation.value);
            }
            if (observation.name === "VACCINATION_RECORD") {
                myIbd.vaccinationRecord = createSplitArray(observation.value);
            }
        }

        // set primary diagnosis to first condition of patient (as sent in <diagnosis>), DIAGNOSIS, not EDTA_DIAGNOSIS
        if (patient.fhirConditions.length) {
            for (i = 0; i < patient.fhirConditions.length; i++) {
                if (patient.fhirConditions[i].category === 'DIAGNOSIS') {
                    myIbd.primaryDiagnosis = patient.fhirConditions[i].notes;
                    myIbd.primaryDiagnosisDate = patient.fhirConditions[i].date;
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
        $scope.patientDetails = '';

        $scope.unitGroups.forEach(function(unit) {
            if (_.findWhere(unit.parentGroups, {id: $scope.currentSpecialty.id})) {
                childGroupIds.push(unit.id);
            }
        });

        if (childGroupIds.length > 0) {
            PatientService.get($scope.loggedInUser.id, childGroupIds).then(function (patientDetails) {
                $scope.patientDetails = patientDetails;

                // set checkboxes
                for (var i = 0; i < $scope.patientDetails.length; i++) {
                    $scope.patientDetails[i].group.selected = true;

                    // create foot checkup object from most recent DPPULSE, PTPULSE observation data
                    $scope.patientDetails[i].footCheckup = createFootcheckup($scope.patientDetails[i]);

                    // create eye checkup object from most recent MGRADE, RGRADE, VA observation data
                    $scope.patientDetails[i].eyeCheckup = createEyecheckup($scope.patientDetails[i]);

                    if ($scope.currentSpecialty.code === "IBD") {
                        // create myIBD object if present
                        $scope.patientDetails[i].myIbd = createMyIbd($scope.patientDetails[i]);
                    } else if ($scope.currentSpecialty.code === "Cardiol") {
                        // create myIBD object if present
                        $scope.patientDetails[i].myIbd = {};
                        $scope.patientDetails[i].myIbd.primaryDiagnosis = 'Heart Failure';
                    }
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
            if ($scope.loggedInUser.groupRoles[i].group.groupType.value === 'SPECIALTY' &&
                $scope.loggedInUser.groupRoles[i].group.code !== 'Generic') {
                $scope.specialties.push($scope.loggedInUser.groupRoles[i].group);
            }
        }

        // get conditions based on first specialty
        if ($scope.specialties.length) {
            $scope.currentSpecialty = $scope.specialties[0];
            getAllPublic();
        } else {
            alert('Error getting specialties');
        }
    };

    $scope.changeSpecialty = function(specialty) {
        $scope.currentSpecialty = specialty;
        $scope.loading = true;
        getMyConditions();
    };

    $scope.init();
}]);
