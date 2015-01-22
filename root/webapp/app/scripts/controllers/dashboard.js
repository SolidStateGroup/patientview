'use strict';

angular.module('patientviewApp').controller('DashboardCtrl', ['UserService', '$modal', '$scope', 'GroupService',
    'NewsService', 'UtilService', 'MedicationService', 'ObservationService', 'ObservationHeadingService', 'AlertService',
function (UserService, $modal, $scope, GroupService, NewsService, UtilService, MedicationService, ObservationService,
          ObservationHeadingService, AlertService) {

    // get graph every time group is changed
    $scope.$watch('graphGroupId', function(newValue) {

        if ($scope.permissions && !$scope.permissions.isPatient) {
            $scope.chartLoading = true;
            $('#chart_div').html('');

            if (newValue !== undefined) {
                GroupService.getStatistics(newValue).then(function (statisticsArray) {
                    var patients = [];
                    var uniqueLogons = [];
                    var logons = [];
                    var xAxisCategories = [];

                    for (var i = 0; i < statisticsArray.length; i++) {
                        var statistics = statisticsArray[i];

                        if (i !== statisticsArray.length - 1) {
                            var dateObject = new Date(statistics.startDate);

                            xAxisCategories.push(
                                    UtilService.getMonthText(dateObject.getMonth()) + ' ' + dateObject.getFullYear());

                            if (statistics.statistics.PATIENT_COUNT !== undefined) {
                                patients.push(statistics.statistics.PATIENT_COUNT);
                            } else {
                                patients.push(null);
                            }

                            if (statistics.statistics.UNIQUE_LOGGED_ON_COUNT !== undefined) {
                                uniqueLogons.push(statistics.statistics.UNIQUE_LOGGED_ON_COUNT);
                            } else {
                                uniqueLogons.push(null);
                            }

                            if (statistics.statistics.LOGGED_ON_COUNT !== undefined) {
                                logons.push(statistics.statistics.LOGGED_ON_COUNT);
                            } else {
                                logons.push(null);
                            }
                        }

                        $scope.statisticsDate = statistics.endDate;
                        $scope.lockedUsers = statistics.statistics.LOCKED_USER_COUNT;
                        $scope.inactiveUsers = statistics.statistics.INACTIVE_USER_COUNT;
                        $scope.totalUsers = statistics.statistics.USER_COUNT;
                        $scope.activeUsers = $scope.totalUsers - $scope.inactiveUsers;
                    }

                    // using highcharts
                    $('#chart_div').highcharts({
                        chart: {
                            zoomType: 'xy'
                        },
                        title: {
                            text: ''
                        },
                        xAxis: {
                            categories: xAxisCategories,
                            labels: {enabled:false}
                        },
                        yAxis: {
                            title: {
                                text: null
                            },
                            plotLines: [
                                {
                                    value: 0,
                                    width: 1,
                                    color: '#808080'
                                }
                            ],
                            min: 0,
                            allowDecimals: false
                        },
                        legend: {
                            layout: 'vertical',
                            align: 'right',
                            verticalAlign: 'middle',
                            borderWidth: 0
                        },
                        series: [
                            {
                                name: 'Patient Count',
                                data: patients
                            },
                            {
                                name: 'Unique Logons',
                                data: uniqueLogons
                            },
                            {
                                name: 'Logons',
                                data: logons
                            }
                        ],
                        credits: {
                            enabled: false
                        },
                        exporting: {
                            enabled: false
                        }
                    });
                    $scope.chartLoading = false;

                });
            }
        }
    });

    var init = function() {
        $scope.loading = true;

        $scope.allGroups = [];
        $scope.permissions = {};
        var i;

        $scope.permissions.isSuperAdmin = UserService.checkRoleExists('GLOBAL_ADMIN', $scope.loggedInUser);
        $scope.permissions.isSpecialtyAdmin = UserService.checkRoleExists('SPECIALTY_ADMIN', $scope.loggedInUser);
        $scope.permissions.isUnitAdmin = UserService.checkRoleExists('UNIT_ADMIN', $scope.loggedInUser);
        $scope.permissions.isPatient = UserService.checkRoleExists('PATIENT', $scope.loggedInUser);

        if ($scope.permissions.isSuperAdmin || $scope.permissions.isSpecialtyAdmin || $scope.permissions.isUnitAdmin) {
            $scope.permissions.showJoinRequestButton = true;
        }

        if ($scope.permissions.isPatient) {
            // GP Medicines, check to see if feature is available on any of the current user's groups and their opt in/out status
            MedicationService.getGpMedicationStatus($scope.loggedInUser.id).then(function(gpMedicationStatus) {
                $scope.gpMedicationStatus = gpMedicationStatus;
            }, function () {
                alert('Cannot get GP medication status');
            });

            getAvailableObservationHeadings();
        }

        // set the list of groups to show in the data grid
        $scope.graphGroups = $scope.loggedInUser.userInformation.userGroups;

        // hide Generic group
        _.remove($scope.graphGroups, {code: 'Generic'});

        for(i=0;i<$scope.graphGroups.length;i++) {
            $scope.allGroups[$scope.graphGroups[i].id] = $scope.graphGroups[i];
        }

        // set group (avoid blank option)
        if ($scope.graphGroups && $scope.graphGroups.length > 0) {
            $scope.graphGroupId = $scope.graphGroups[0].id;
        }

        NewsService.getByUser($scope.loggedInUser.id, 0, 5).then(function(page) {
            $scope.newsItems = page.content;
            $scope.loading = false;
        }, function() {
            $scope.loading = false;
        });
    };

    $scope.viewNewsItem = function(news) {
        var modalInstance = $modal.open({
            templateUrl: 'views/partials/viewNewsModal.html',
            controller: ViewNewsModalInstanceCtrl,
            size: 'lg',
            resolve: {
                news: function(){
                    return news;
                }
            }
        });

        modalInstance.result.then(function () {
            // ok (not used)
        }, function () {
            // closed
        });
    };

    var saveGpMedicationStatus = function() {
        MedicationService.saveGpMedicationStatus($scope.loggedInUser.id, $scope.gpMedicationStatus).then(function() {
            init();
            if ($scope.gpMedicationStatus.optInStatus === true) {
                $scope.justOptedIn = true;
            }
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

    // Migration only
    $scope.startObservationMigration = function() {
        ObservationService.startObservationMigration().then(function() {

        }, function () {
            alert('Cannot start observation migration');
        });
    };

    // alerts
    $scope.addAlertObservationHeading = function(observationHeadingId) {
        var found = false;

        for (var i=0;i<$scope.alertObservationHeadings.length;i++) {
            if ($scope.alertObservationHeadings[i].observationHeading.id === observationHeadingId) {
                found = true;
            }
        }

        if (!found) {
            var alertObservationHeading = {};
            alertObservationHeading.user = {};
            alertObservationHeading.user.id = $scope.loggedInUser.id;
            alertObservationHeading.observationHeading = {};
            alertObservationHeading.observationHeading.id = observationHeadingId;
            alertObservationHeading.webAlert = true;
            alertObservationHeading.emailAlert = true;
            alertObservationHeading.alertType = 'RESULT';

            AlertService.addAlert($scope.loggedInUser.id, alertObservationHeading).then(function () {
                getAlerts();
            }, function () {
                alert('Error adding result alert');
            });
        }
    };

    $scope.removeAlert = function(alertId) {
        AlertService.removeAlert($scope.loggedInUser.id, alertId).then(function () {
            getAlerts();
        }, function () {
            alert('Error removing result alert');
        });
    };

    $scope.updateAlert = function(alert) {
        AlertService.updateAlert($scope.loggedInUser.id, alert).then(function () {
            getAlerts();
        }, function () {
            alert('Error updating alert');
        });
    };

    $scope.updateLetterAlert = function(letterAlert) {
        if (letterAlert.id === undefined) {
            letterAlert.user = {};
            letterAlert.user.id = $scope.loggedInUser.id;
            letterAlert.alertType = 'LETTER';

            AlertService.addAlert($scope.loggedInUser.id, letterAlert).then(function () {
                getAlerts();
            }, function () {
                alert('Error adding result alert');
            });
        } else {
            AlertService.updateAlert($scope.loggedInUser.id, letterAlert).then(function () {
                getAlerts();
            }, function () {
                alert('Error updating alert');
            });
        }
    };

    $scope.hideAlertNotification = function(alert) {
        alert.webAlertViewed = true;
        AlertService.updateAlert($scope.loggedInUser.id, alert).then(function () {
            getAlerts();
        }, function () {
            alert('Error updating alert');
        });
    };

    var getAvailableObservationHeadings = function() {
        ObservationHeadingService.getAvailableAlertObservationHeadings($scope.loggedInUser.id)
            .then(function(observationHeadings) {
                if (observationHeadings.length > 0) {
                    $scope.observationHeadingMap = [];
                    var blankObservationHeading = {};
                    blankObservationHeading.id = -1;
                    blankObservationHeading.heading = ' Please Select..';
                    observationHeadings.push(blankObservationHeading);
                    $scope.observationHeadings = observationHeadings;
                    $scope.selectedObservationHeadingId = -1;
                    for (var i = 0; i < $scope.observationHeadings.length; i++) {
                        $scope.observationHeadingMap[$scope.observationHeadings[i].code] = $scope.observationHeadings[i];
                    }
                }
                getAlerts();
                $scope.initFinished = true;
            }, function() {
                alert('Error retrieving result types');
            });
    };

    var getAlerts = function() {

        delete $scope.alertObservationHeadings;
        delete $scope.letterAlert;

        // result alerts, multiple
        AlertService.getAlerts($scope.loggedInUser.id, 'RESULT')
            .then(function(alertObservationHeadings) {
                $scope.alertObservationHeadings = alertObservationHeadings;
            }, function() {
                alert('Error getting result alerts');
            });

        // letter alert, should only return one
        AlertService.getAlerts($scope.loggedInUser.id, 'LETTER')
            .then(function(alertLetters) {
                if (alertLetters.length) {
                    $scope.letterAlert = alertLetters[0];
                } else {
                    delete $scope.letterAlert;
                }
            }, function() {
                alert('Error getting letter alerts');
            });
    };

    init();
}]);
