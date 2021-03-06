'use strict';

angular.module('patientviewApp').controller('GpLoginCtrl', ['$scope', 'GpService', function ($scope, GpService) {
    var init = function() {
        $scope.details = {};
        $scope.validDetails = true;
        $scope.validatingDetails = false;
        $scope.validClaim = true;
        $scope.validatingClaim = false;
        $scope.step = 1;
    };

    $scope.selectStep = function(step) {
        if (step === 1) {
            $scope.step = 1;
        }
        if (step === 2) {
            delete $scope.detailsErrorMessage;
            delete $scope.claimErrorMessage;
            delete $scope.selectedPractice;
            delete $scope.practices;
            delete $scope.selectedPatients;
            delete $scope.patients;
            $scope.validatingDetails = true;

            GpService.validateDetails($scope.details).then(function (data) {
                // details provided are correct
                $scope.validatingDetails = false;
                $scope.validDetails = true;
                $scope.centralSupportEmail = data.centralSupportEmail;

                // get practices
                $scope.practices = data.practices;
                if ($scope.practices.length == 1) {
                    $scope.selectedPractice = $scope.practices[0];
                } else {
                    delete $scope.selectedPractice;
                }

                // get patients
                $scope.patients = data.patients;
                $scope.selectedPatients = [];

                // set selected based on identifier
                for (var i=0; i<$scope.patients.length; i++) {
                    for (var j = 0; j < $scope.patients[i].identifiers.length; j++) {
                        if (($scope.patients[i].identifiers[j].identifier == $scope.details.patientIdentifier)
                                || $scope.practices.length == 1) {
                            $scope.selectedPatients.push($scope.patients[i]);
                        }
                    }
                }

                $scope.step = 2;
            }, function (failure) {
                // details invalid
                $scope.validatingDetails = false;
                $scope.validDetails = false;
                $scope.detailsErrorMessage = failure.data;
            });
        }
    };

    $scope.claim = function() {
        delete $scope.claimErrorMessage;
        $scope.validatingClaim = true;
        $scope.details.practices = [];
        if ($scope.selectedPractice !== null) {
            $scope.details.practices.push($scope.selectedPractice);
        }
        $scope.details.patients = $scope.selectedPatients;

        // create account with patient details
        GpService.claim($scope.details).then(function (data) {
            delete $scope.claimErrorMessage;
            $scope.validatingClaim = false;
            $scope.details = data;
            $scope.step = 3;
        }, function (failure) {
            // details invalid
            $scope.validatingClaim = false;
            $scope.validClaim = false;
            $scope.claimErrorMessage = failure.data;
        });
    };

    $scope.togglePatient = function (patient) {
        if ($scope.selectedPatients.indexOf(patient) === -1) {
            $scope.selectedPatients.push(patient);
        } else {
            $scope.selectedPatients.splice($scope.selectedPatients.indexOf(patient), 1);
        }
    };

    $scope.togglePractice = function (practice) {
        $scope.selectedPractice = practice;
    };

    init();
}]);
