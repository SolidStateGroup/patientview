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

// group filter
angular.module('patientviewApp').filter('sourceGroupFilter', [function () {
    return function (medicationStatements, selectedGroups) {
        if (selectedGroups.length) {
            var filteredMedicationStatements = [];
            angular.forEach(medicationStatements, function (medicationStatement) {
                if (_.contains(selectedGroups, medicationStatement.group.id)) {
                    filteredMedicationStatements.push(medicationStatement);
                }
            });

            return filteredMedicationStatements;
        } else {
            return medicationStatements;
        }
    };
}]);

// TODO: ECS functionality following discussion
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

    var getSourceGroups = function(medicationStatements) {
        var groups = [];
        angular.forEach(medicationStatements, function (medicationStatement) {
            if (!_.findWhere(groups, {id: medicationStatement.group.id})) {
                groups.push(medicationStatement.group);
            }
        });
        return groups;
    };

    var init = function() {
        $scope.loading = true;
        $scope.currentPage = 1;
        $scope.entryLimit = 10;

        MedicationService.getByUserId($scope.loggedInUser.id).then(function(medicationStatements) {
            $scope.filterGroups = getSourceGroups(medicationStatements);
            separateMedicationStatements(medicationStatements);
            $scope.predicate = 'date';
            $scope.reverse = true;
            $scope.loading = false;
        }, function () {
            alert('Cannot get medication');
        })
    };

    // client side sorting, pagination
    $scope.sortBy = function(predicate) {
        $scope.predicate = predicate;
        $scope.reverse = !$scope.reverse;
    };
    $scope.setPage = function(pageNo) {
        $scope.currentPage = pageNo;
    };

    // filter by group
    $scope.selectedGroups = [];
    $scope.setSelectedGroup = function (group) {
        var id = group.id;
        if (_.contains($scope.selectedGroups, id)) {
            $scope.selectedGroups = _.without($scope.selectedGroups, id);
        } else {
            $scope.selectedGroups.push(id);
        }
    };
    $scope.isGroupChecked = function (id) {
        if (_.contains($scope.selectedGroups, id)) {
            return 'glyphicon glyphicon-ok pull-right';
        }
        return false;
    };
    $scope.removeAllGroups = function () {
        $scope.selectedGroups = [];
    };

    init();
}]);
