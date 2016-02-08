'use strict';

angular.module('patientviewApp').controller('GpLoginCtrl', ['$scope', 'GpService', function ($scope, GpService) {
    var init = function() {
        $scope.details = {};
        $scope.validDetails = true;
        $scope.validatingDetails = false;
        $scope.step = 1;
    };

    $scope.selectStep = function(step) {
        if (step === 1) {
            $scope.step = 1;
        }
        if (step === 2) {
            delete $scope.detailsErrorMessage;
            $scope.validatingDetails = true;

            GpService.validateDetails($scope.details).then(function () {
                // details provided are correct
                $scope.validatingDetails = false;
                $scope.validDetails = true;

                // get practices
                $scope.practices = [];
                $scope.practices.push({'name': 'First Practice', 'code': 'E012456', 'url': 'http://www.msn.com'});
                $scope.practices.push({'name': 'Second Practice', 'code': 'E987235', 'url': 'http://www.google.com'});
                $scope.selectedPractice = $scope.practices[0];

                // get patients
                $scope.patients = [];
                $scope.patients.push({'id': 1, 'identifier': '123456789', 'gpName': 'Dr John Smith'});
                $scope.patients.push({'id': 2, 'identifier': '564736783', 'gpName': 'Dr John Smith'});
                $scope.patients.push({'id': 3, 'identifier': '826823147', 'gpName': 'Dr Paul Robson'});

                $scope.selectedPatients = [];
                $scope.selectedPatients.push($scope.patients[0]);

                $scope.step = 2;
            }, function (failure) {
                // details invalid
                $scope.validatingDetails = false;
                $scope.validDetails = false;
                $scope.detailsErrorMessage = failure.data;
            });
        }
    };

    $scope.createAccount = function() {
        // create account with patient details
    };

    $scope.togglePatient = function (patient) {
        if ($scope.selectedPatients.indexOf(patient) === -1) {
            $scope.selectedPatients.push(patient);
        } else {
            $scope.selectedPatients.splice($scope.selectedPatients.indexOf(patient), 1);
        }
    };

    init();
}]);
