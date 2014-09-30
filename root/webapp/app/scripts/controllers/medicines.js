'use strict';
// medicine filter
angular.module('patientviewApp').filter('nameAndDoseFilter', [function () {
    return function (medicationStatements, searchText) {
        if (searchText !== undefined) {
            var filteredMedicationStatements = [];
            angular.forEach(medicationStatements, function (medicationStatement) {
                if ((medicationStatement.name.toUpperCase().indexOf(searchText.toUpperCase()) !== -1)
                    || (medicationStatement.dose.toUpperCase().indexOf(searchText.toUpperCase()) !== -1)) {
                    filteredMedicationStatements.push(medicationStatement);
                }
            });

            return filteredMedicationStatements;
        } else {
            return medicationStatements;
        }
    };
}]);

angular.module('patientviewApp').controller('MedicinesCtrl', ['$scope', '$timeout', 'MedicationService',
function ($scope, $timeout, MedicationService) {

    var getNonECS = function(medicationStatements) {
        var medications = [];
        for (var i=0;i<medicationStatements.length;i++) {
            if (medicationStatements[i].group.code !== 'ECS') {
                medications.push(medicationStatements[i]);
            }
        }
        return medications;
    };

    var getECS = function(medicationStatements) {
        var medications = [];
        for (var i=0;i<medicationStatements.length;i++) {
            if (medicationStatements[i].group.code === 'ECS') {
                medications.push(medicationStatements[i]);
            }
        }
        return medications;
    };

    var separateMedicationStatements = function(medicationStatements) {
        $scope.medicationStatementsNonEcs = getNonECS(medicationStatements);
        $scope.allMedicationStatementsNonEcs = getNonECS(medicationStatements);
        $scope.medicationStatementsEcs = getECS(medicationStatements);
        $scope.allMedicationStatementsEcs = getECS(medicationStatements);
    };

    var init = function() {
        $scope.loading = true;
        MedicationService.getByUserId($scope.loggedInUser.id).then(function(medicationStatements) {
            separateMedicationStatements(medicationStatements);
            $scope.predicate = 'date';
            $scope.loading = false;
        }, function () {
            alert('Cannot get medication');
        })
    };

    $scope.sortBy = function(predicate) {
        $scope.predicate = predicate;
        $scope.reverse = !$scope.reverse;
    };

    init();
}]);
