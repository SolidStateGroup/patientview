'use strict';

angular.module('patientviewApp').controller('MedicinesCtrl', ['$scope', 'MedicationService',
function ($scope, MedicationService) {

    var init = function() {
        $scope.loading = true;
        MedicationService.getByUserId($scope.loggedInUser.id).then(function(medicationStatements) {

            $scope.medicationStatements = medicationStatements;
            $scope.loading = false;

        }, function () {
            alert('Cannot get medication');
        })
    };

    init();
}]);
