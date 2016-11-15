'use strict';

angular.module('patientviewApp').controller('ResultsEditCtrl', ['$scope', '$routeParams', '$location',
    'ObservationHeadingService', 'ObservationService', "UtilService", '$modal', '$timeout', '$filter', '$q',
    function ($scope, $routeParams, $location, ObservationHeadingService, ObservationService, UtilService,
              $modal, $timeout, $filter, $q) {

        $scope.init = function () {
            $scope.loading = true;

            $scope.days = UtilService.generateDays();
            $scope.months = UtilService.generateMonths();
            $scope.years = UtilService.generateYears2000();
            $scope.hours = UtilService.generateHours();
            $scope.minutes = UtilService.generateMinutes();

            // return parameter (currentPage on results page)
            $scope.r = $routeParams.r;

            $scope.userResultHeadings = [];

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

            // var i, j;
            // ObservationHeadingService.getResultClusters().then(function (resultClusters) {
            //     $scope.resultClusters = resultClusters;
            //     $scope.selectedResultCluster = resultClusters[0];
            //     $scope.loading = false;
            //
            //     for (i = 0; i < $scope.resultClusters.length; i++) {
            //         var headings = $scope.resultClusters[i].resultClusterObservationHeadings;
            //         alert("Heading length "+headings.length);
            //         for (j = 0; j < headings.length; j++) {
            //             $scope.userResultHeadings.push(headings[j]);
            //
            //         }
            //     }
            //
            //
            //
            // }, function () {
            //     alert('Cannot get result clusters');
            // });
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

            $scope.codes.forEach(function (code, index) {
                promises.push(ObservationService.getByCodePatientEntered($scope.loggedInUser.id, code).then(function (observations) {
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
            delete $scope.editResult;
            delete $scope.successMessage;

            $scope.codes = [];
            $scope.codes.push(code);

            $scope.observationHeading = $scope.findObservationHeadingByCode(code);
            $scope.selectedCode = $scope.observationHeading.code;
            $scope.getObservations(code);
        };

        // delete result, opens modal
        $scope.remove = function (observationHeading, observation) {
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
                    observationHeading: function () {
                        return observationHeading;
                    },
                    observation: function () {
                        return observation;
                    },
                    ObservationService: function(){
                        return ObservationService;
                    }
                }
            });

            modalInstance.result.then(function () {
                $scope.successMessage = 'Result successfully deleted';
                $scope.getObservations();
            }, function () {
                // closed
            });
        };

        // Opened for edit
        $scope.opened = function (openedResult) {
            delete $scope.successMessage;

            if (openedResult.showEdit) {
                $scope.editResult = '';
                openedResult.showEdit = false;
            } else {
                // close others
                for (var i = 0; i < $scope.tableObservations.length; i++) {
                    $scope.tableObservations[i].showEdit = false;
                }

                $scope.editResult = '';
                openedResult.showEdit = true;
                openedResult.editLoading = true;

                var currentDate = new Date(openedResult.applies);
                var i;

                for (i = 0; i < $scope.days.length; i++) {
                    if (parseInt($scope.days[i]) === currentDate.getDate()) {
                        openedResult.day = $scope.days[i];
                    }
                }
                for (i = 0; i < $scope.months.length; i++) {
                    if (parseInt($scope.months[i]) === currentDate.getMonth() + 1) {
                        openedResult.month = $scope.months[i];
                    }
                }
                for (i = 0; i < $scope.years.length; i++) {
                    if (parseInt($scope.years[i]) === currentDate.getFullYear()) {
                        openedResult.year = $scope.years[i];
                    }
                }

                for (i = 0; i < $scope.hours.length; i++) {
                    if (parseInt($scope.hours[i]) === currentDate.getHours()) {
                        openedResult.hour = $scope.hours[i];
                    }
                }
                for (i = 0; i < $scope.minutes.length; i++) {
                    if (parseInt($scope.minutes[i]) === currentDate.getMinutes()) {
                        openedResult.minute = $scope.minutes[i];
                    }
                }

                $scope.editResult = _.clone(openedResult);
                $scope.editMode = true;
                openedResult.editLoading = false;
            }
        };

        // save changes from edit
        $scope.save = function() {
            // check date is ok
            if (!UtilService.validationDateNoFuture($scope.editResult.day, $scope.editResult.month, $scope.editResult.year)) {
                alert('Result Date cannot be in the future');
                return false;
            }
            ObservationService.saveResultCluster($scope.loggedInUser.id, $scope.editResult).then(function() {
                $scope.successMessage = 'Results successfully update in PatientView.';
                $scope.editResult = '';
                $scope.editMode = false;
                $scope.getObservations();
            }, function () {
                alert('Cannot save your result');
            });
        };


        $scope.init();
    }]);
