'use strict';

angular.module('patientviewApp').controller('ResultsEditCtrl', ['$scope', '$routeParams', '$location',
    'ObservationHeadingService', 'ObservationService', '$modal', '$timeout', '$filter', '$q',
    function ($scope, $routeParams, $location, ObservationHeadingService, ObservationService,
              $modal, $timeout, $filter, $q) {

        $scope.init = function () {
            $scope.loading = true;

            // return parameter (currentPage on results page)
            $scope.r = $routeParams.r;

            $scope.codes = [];
            $scope.observations = [];

            // handle single result type from query parameter
            var code = $routeParams.code;

            if (code instanceof Array) {
                $scope.codes = code;
            } else {
                $scope.codes.push(code);
            }

            $scope.getAvailableObservationHeadings($scope.codes[0], $scope.loggedInUser.id);
        };

        $scope.getAvailableObservationHeadings = function (code, userId) {
            ObservationHeadingService.getAvailableObservationHeadings(userId).then(function (observationHeadings) {
                $scope.observationHeadings = observationHeadings;
                $scope.observationHeading = $scope.findObservationHeadingByCode(code);
                $scope.selectedCode = code;

                $scope.getObservations();
            }, function () {
                alert('Error retrieving result types');
            });
        };

        $scope.getObservations = function () {
            $scope.loading = true;
            var promises = [];
            var obs = [];
            var selectedObs;
            alert('getObservations '+$scope.codes[0]);
            $scope.codes.forEach(function (code, index) {
                promises.push(ObservationService.getByCode($scope.loggedInUser.id, code).then(function (observations) {
                    if (observations.length) {
                        obs[code] = _.sortBy(observations, 'applies').reverse();

                        if (index == 0) {
                            selectedObs = obs[code][0];
                        }
                    } else {
                        delete obs[code];
                        //delete $scope.selectedObservation;
                    }

                }, function () {
                    alert('Error retrieving results');
                    $scope.loading = false;
                }));
            });

            $q.all(promises).then(function () {
                $scope.observations = obs;
                $scope.selectedObservation = selectedObs;
                $scope.loading = false;
                $scope.populateObservationsInTable();

            });
        };

        $scope.populateObservationsInTable = function () {
            $scope.tableObservations = false;
            $scope.tableObservations = [];
            $scope.tableObservationsKey = [];

            if ($scope.observations[$scope.selectedCode] !== undefined) {
                for (var i = 0; i < $scope.observations[$scope.selectedCode].length; i++) {
                    var observation = $scope.observations[$scope.selectedCode][i];
                    observation.appliesFormatted = $filter('date')(observation.applies, 'dd-MMM-yyyy HH:mm');
                    observation.appliesFormatted = observation.appliesFormatted.replace(' 00:00', '');
                    $scope.tableObservations.push(observation);
                    $scope.tableObservationsKey[observation.applies] = $scope.tableObservations.length - 1;
                }
            }
        };

        $scope.findObservationHeadingByCode = function (code) {
            for (var i = 0; i < $scope.observationHeadings.length; i++) {
                if ($scope.observationHeadings[i].code === code) {
                    return $scope.observationHeadings[i];
                }
            }
        };

        $scope.changeObservationHeading = function (code) {
            delete $scope.compareCode;
            $scope.codes = [];
            $scope.codes.push(code);

            $scope.observationHeading = $scope.findObservationHeadingByCode(code);
            $scope.selectedCode = $scope.observationHeading.code;
            $scope.getObservations(code);
        };

        // delete result, opens modal
        $scope.remove = function (result, observation) {
            if (/Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)) {
                var modalsize = 'md';
            } else {
                modalsize = 'sm';
            }
            var modalInstance = $modal.open({
                templateUrl: 'deleteResultModal.html',
                controller: DeleteResultModalInstanceCtrl,
                size: modalsize,
                windowClass: 'results-modal',
                resolve: {
                    result: function () {
                        return result;
                    },
                    observation: function () {
                        return observation;
                    }
                }
            });

            modalInstance.result.then(function () {
                // ok (not used)
            }, function () {
                // closed
            });
        };


        $scope.init();
    }]);
