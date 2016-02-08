'use strict';

angular.module('patientviewApp').controller('GpLoginCtrl', ['$scope',  function ($scope) {
    var init = function() {
        $scope.step = 1;
    };

    $scope.selectStep = function(step) {
        if (step === 1) {
            $scope.step = 1;
        }
        if (step === 2) {
            $scope.practices = [];
            $scope.practices.push({'name': 'First Practice', 'code': 'E012456', 'url': 'http://www.msn.com'});
            $scope.practices.push({'name': 'Second Practice', 'code': 'E987235', 'url': 'http://www.google.com'});
            $scope.selectedPractice = $scope.practices[0];

            $scope.patients = [];
            $scope.patients.push({'id': 1, 'identifier': '123456789', 'gpName': 'Dr John Smith'});
            $scope.patients.push({'id': 2, 'identifier': '564736783', 'gpName': 'Dr John Smith'});
            $scope.patients.push({'id': 3, 'identifier': '826823147', 'gpName': 'Dr Paul Robson'});

            $scope.selectedPatients = [];
            $scope.selectedPatients.push($scope.patients[0]);

            $scope.step = 2;
        }
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
