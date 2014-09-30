'use strict';

angular.module('patientviewApp').controller('MyconditionsCtrl',['$scope', 'PatientService', 'GroupService',
function ($scope, PatientService, GroupService) {

    var getMyConditions = function() {
        // get conditions (diagnosis etc) from groups under current specialty
        var childGroupIds = [], i;

        GroupService.getChildren($scope.currentSpecialty.id).then(function (childGroups) {
            if (childGroups.length) {

                for (i=0;i<childGroups.length;i++) {
                    childGroupIds.push(childGroups[i].id);
                }

                PatientService.get($scope.loggedInUser.id, childGroupIds).then(function (patientDetails) {
                    $scope.patientDetails = patientDetails;

                    // set checkboxes
                    for (var i = 0; i < $scope.patientDetails.length; i++) {
                        $scope.patientDetails[i].group.selected = true;
                    }

                    $scope.loading = false;
                }, function () {
                    $scope.loading = false;
                    alert('Error getting patient details');
                });
            } else {
                $scope.loading = false;
                alert('No Renal groups found');
            }

        }, function () {
            $scope.loading = false;
            alert('Error getting Renal groups');
        });
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

        // get conditions based on renal or first specialty
        if ($scope.specialties.length) {
            $scope.currentSpecialty = $scope.specialties[0];
            getMyConditions();
        } else {
            alert("Error getting specialties");
        }
    };

    $scope.changeSpecialty = function(specialty) {
        $scope.currentSpecialty = specialty;
        $scope.loading = true;
        getMyConditions();
    };

    $scope.init();
}]);
