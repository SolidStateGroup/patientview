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

angular.module('patientviewApp').controller('MedicinesCtrl', ['$scope', '$modal', '$timeout', 'MedicationService',
function ($scope, $modal, $timeout, MedicationService) {

    var getNonGpMedication = function(medicationStatements) {
        var medications = [];
        for (var i=0;i<medicationStatements.length;i++) {
            if (medicationStatements[i].group.code !== 'ECS') {
                medications.push(medicationStatements[i]);
            }
        }
        return medications;
    };

    var getGpMedication = function(medicationStatements) {
        var medications = [];
        for (var i=0;i<medicationStatements.length;i++) {
            if (medicationStatements[i].group.code === 'ECS') {
                medications.push(medicationStatements[i]);
            }
        }
        return medications;
    };

    var separateMedicationStatements = function(medicationStatements) {
        $scope.medicationStatementsNonGp = getNonGpMedication(medicationStatements);
        $scope.allMedicationStatementsNonGp = getNonGpMedication(medicationStatements);
        $scope.medicationStatementsGp = getGpMedication(medicationStatements);
        $scope.allMedicationStatementsGp = getGpMedication(medicationStatements);
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
        $scope.currentPageGp = 1;
        $scope.entryLimit = 10;

        MedicationService.getByUserId($scope.loggedInUser.id).then(function(medicationStatements) {
            $scope.filterGroups = getSourceGroups(medicationStatements);
            separateMedicationStatements(medicationStatements);
            $scope.predicate = 'startDate';
            $scope.reverse = true;
            $scope.predicateGp = 'startDate';
            $scope.reverseGp = true;
            $scope.loading = false;
        }, function () {
            alert('Cannot get medication');
        });

        // GP Medicines, check to see if feature is available on any of the current user's groups and their opt in/out status
        MedicationService.getGpMedicationStatus($scope.loggedInUser.id).then(function(gpMedicationStatus) {
            $scope.gpMedicationStatus = gpMedicationStatus;
        }, function () {
            alert('Cannot get GP medication status');
        });
    };

    $scope.openExportToCSVModal = function () {

        var modalInstance = $modal.open({
            templateUrl: 'views/partials/exportToCSVModal.html',
            controller: "ExportInfoModalInstanceCtrl",
            size: 'sm',
            windowClass: 'results-modal',
            resolve: {
                result: function(){
                    return true;
                }
            }
        });

        modalInstance.result.then(function () {
            // ok (not used)
        }, function () {
            // closed
        });
    };
    // client side sorting, pagination
    $scope.sortBy = function(predicate) {
        $scope.predicate = predicate;
        $scope.reverse = !$scope.reverse;
    };
    $scope.setPage = function(pageNo) {
        $scope.currentPage = pageNo;
    };
    $scope.sortByGp = function(predicateGp) {
        $scope.predicateGp = predicateGp;
        $scope.reverseGp = !$scope.reverseGp;
    };
    $scope.setPageGp = function(pageNoGp) {
        $scope.currentPageGp = pageNoGp;
    };

    // filter by group
    $scope.selectedGroups = [];
    $scope.setSelectedGroup = function(group) {
        var id = group.id;
        if (_.contains($scope.selectedGroups, id)) {
            $scope.selectedGroups = _.without($scope.selectedGroups, id);
        } else {
            $scope.selectedGroups.push(id);
        }
    };
    $scope.isGroupChecked = function(id) {
        if (_.contains($scope.selectedGroups, id)) {
            return 'glyphicon glyphicon-ok pull-right';
        }
        return false;
    };
    $scope.removeAllGroups = function() {
        $scope.selectedGroups = [];
    };

    var saveGpMedicationStatus = function() {
        MedicationService.saveGpMedicationStatus($scope.loggedInUser.id, $scope.gpMedicationStatus)
        .then(function() {
            init();
        }, function () {
            alert('Cannot save GP medication status');
        });
    };

    $scope.gpMedicinesOptIn = function() {
        $scope.gpMedicationStatus.optInStatus = true;
        $scope.gpMedicationStatus.optInHidden = false;
        $scope.gpMedicationStatus.optOutHidden = false;
        $scope.gpMedicationStatus.optInDate = new Date().getTime();
        saveGpMedicationStatus();
    };

    $scope.gpMedicinesHideOptIn = function() {
        $scope.gpMedicationStatus.optInHidden = true;
        saveGpMedicationStatus();
    };

    $scope.gpMedicinesShowOptIn = function() {
        $scope.gpMedicationStatus.optInHidden = false;
        saveGpMedicationStatus();
    };

    $scope.gpMedicinesOptOut = function() {
        $scope.gpMedicationStatus.optInStatus = false;
        $scope.gpMedicationStatus.optInHidden = false;
        $scope.gpMedicationStatus.optOutHidden = false;
        $scope.gpMedicationStatus.optInDate = null;
        saveGpMedicationStatus();
    };

    $scope.gpMedicinesHideOptOut = function() {
        $scope.gpMedicationStatus.optOutHidden = true;
        saveGpMedicationStatus();
    };

    $scope.gpMedicinesShowOptOut = function() {
        $scope.gpMedicationStatus.optOutHidden = false;
        saveGpMedicationStatus();
    };

    init();
}]);
