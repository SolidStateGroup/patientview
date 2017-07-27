'use strict';

angular.module('patientviewApp').controller('DashboardCtrl', ['UserService', '$modal', '$scope', 'GroupService',
    'NewsService', 'UtilService', 'StaticDataService', 'MedicationService', 'ObservationService',
    'ObservationHeadingService', 'AlertService', '$rootScope', 'DiagnosisService', 'CodeService',
    function (UserService, $modal, $scope, GroupService, NewsService, UtilService, StaticDataService, MedicationService,
              ObservationService, ObservationHeadingService, AlertService, $rootScope, DiagnosisService, CodeService) {

        // get graph every time group is changed
        $scope.$watch('graphGroupId', function (newValue) {

            if ($scope.permissions && !$scope.permissions.isPatient) {
                $scope.chartLoading = true;
                $('#chart_div').html('');

                if (newValue !== undefined) {
                    GroupService.getStatistics(newValue).then(function (statisticsArray) {
                        var patients = [];
                        var uniqueLogons = [];
                        var logons = [];
                        var xAxisCategories = [];

                        // don't show current month
                        var currentMonth = moment(new Date()).month();

                        for (var i = 0; i < statisticsArray.length; i++) {
                            var statistics = statisticsArray[i];
                            var dateObject = new Date(statistics.startDate);

                            if (moment(dateObject).month() !== currentMonth) {
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

                            if ($scope.lockedUsers && statistics.statistics.LOCKED_PATIENT_COUNT) {
                                $scope.lockedPatients = statistics.statistics.LOCKED_PATIENT_COUNT;
                                $scope.lockedStaff = $scope.lockedUsers - $scope.lockedPatients;
                            }

                            $scope.inactiveUsers = statistics.statistics.INACTIVE_USER_COUNT;
                            if ($scope.inactiveUsers && statistics.statistics.INACTIVE_PATIENT_COUNT) {
                                $scope.inactivePatients = statistics.statistics.INACTIVE_PATIENT_COUNT;
                                $scope.inactiveStaff = $scope.inactiveUsers - $scope.inactivePatients;
                            }

                            $scope.totalUsers = statistics.statistics.USER_COUNT;
                            $scope.totalPatients = statistics.statistics.PATIENT_COUNT;
                            $scope.totalStaff = $scope.totalUsers - $scope.totalPatients;

                            if ($scope.totalUsers && $scope.inactiveUsers) {
                                $scope.activeUsers = $scope.totalUsers - $scope.inactiveUsers;
                            }
                            if ($scope.totalStaff && $scope.inactiveStaff) {
                                $scope.activeStaff = $scope.totalStaff - $scope.inactiveStaff;
                            }
                            if ($scope.totalPatients && $scope.inactivePatients) {
                                $scope.activePatients = $scope.totalPatients - $scope.inactivePatients;
                            }
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
                                labels: {enabled: false}
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

        $scope.init = function () {
            if (!$scope.initStarted) {
                $scope.initStarted = true;
                $scope.loading = true;

                $scope.allGroups = [];
                $scope.permissions = {};
                var i;

                $scope.permissions.isSuperAdmin = UserService.checkRoleExists('GLOBAL_ADMIN', $scope.loggedInUser);
                $scope.permissions.isSpecialtyAdmin = UserService.checkRoleExists('SPECIALTY_ADMIN', $scope.loggedInUser);
                $scope.permissions.isUnitAdmin = UserService.checkRoleExists('UNIT_ADMIN', $scope.loggedInUser);
                $scope.permissions.isPatient = UserService.checkRoleExists('PATIENT', $scope.loggedInUser);

                if ($scope.permissions.isSuperAdmin || $scope.permissions.isSpecialtyAdmin || $scope.permissions.isUnitAdmin) {
                    $scope.permissions.showRequestButton = true;
                    $scope.permissions.showStaffAlerts = true;
                    $scope.permissions.showNhsIndicatorsLink = true;
                }

                if ($scope.permissions.isPatient) {
                    // GP Medicines, check to see if feature is available on any of the current user's groups and their opt in/out status
                    MedicationService.getGpMedicationStatus($scope.loggedInUser.id).then(function (gpMedicationStatus) {
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

                // to handle showing only for renal user
                $scope.permissions.isRenalUser = false;

                for (i = 0; i < $scope.graphGroups.length; i++) {
                    $scope.allGroups[$scope.graphGroups[i].id] = $scope.graphGroups[i];
                    if (!$scope.permissions.isRenalUser && $scope.graphGroups[i].parentCodes.indexOf("Renal") > -1) {
                        $scope.permissions.isRenalUser = true;
                    }
                }

                // set group (avoid blank option)
                if ($scope.graphGroups && $scope.graphGroups.length > 0) {
                    $scope.graphGroupId = $scope.graphGroups[0].id;
                }

                $scope.newsTypesArray = [];

                StaticDataService.getLookupsByType("NEWS_TYPE").then(function (page) {
                    var newsTypes = [];
                    page.forEach(function (newsType) {
                        if (newsType.value != "ALL") {
                            newsTypes[newsType.value] = newsType.id;
                        }
                        $scope.newsTypesArray[newsType.id] = newsType;
                    });

                    NewsService.getByUser($scope.loggedInUser.id, newsTypes['REGULAR'], false, 0, 5).then(function (page) {
                        $scope.newsItems = page.content;
                        $scope.newsItemsTotalElements = page.totalElements;
                        $scope.loading = false;
                        if (!$scope.permissions.isPatient) {
                            $scope.initFinished = true;
                        }
                    }, function () {
                        $scope.loading = false;
                        if (!$scope.permissions.isPatient) {
                            $scope.initFinished = true;
                        }
                    });

                    NewsService.getByUser($scope.loggedInUser.id, newsTypes['DASHBOARD'], true, 0, 5).then(function (page) {
                        $scope.featuredNewsItems = page.content;
                        $scope.loading = false;
                        if (!$scope.permissions.isPatient) {
                            $scope.initFinished = true;
                        }
                    }, function () {
                        $scope.loading = false;
                        if (!$scope.permissions.isPatient) {
                            $scope.initFinished = true;
                        }
                    });
                });

                if (!$scope.showedEnterDiagnosisModal) {
                    if ($scope.loggedInUser.userInformation.shouldEnterCondition) {
                        $scope.showEnterDiagnosesModal();
                        $scope.showedEnterDiagnosisModal = true;
                    }
                }
            }
        };

        $scope.getAlerts = function () {
            // get contact alerts for admin users
            if ($scope.permissions.showStaffAlerts) {
                $scope.showStaffGroupAlerts = true;
                $scope.importAlerts = [];
                $scope.contactAlerts = [];
                $scope.oldSubmissionDateGroups = [];

                // get import alerts
                $scope.importAlertsLoading = true;
                AlertService.getImportAlerts($scope.loggedInUser.id).then(function (importAlerts) {
                    for (var i = 0; i < importAlerts.length; i++) {
                        if (importAlerts[i].group.groupType.value === 'UNIT') {
                            $scope.importAlerts.push(importAlerts[i]);
                        }
                    }

                    $scope.importAlertsLoading = false;
                }, function () {
                    alert("Error getting import alerts");
                    $scope.importAlertsLoading = false;
                });

                // get contact alerts
                $scope.contactAlertsLoading = true;
                AlertService.getContactAlerts($scope.loggedInUser.id).then(function (contactAlerts) {
                    for (var i = 0; i < contactAlerts.length; i++) {
                        if (contactAlerts[i].group.groupType.value === 'UNIT') {
                            $scope.contactAlerts.push(contactAlerts[i]);
                        }
                    }
                    $scope.contactAlertsLoading = false;
                }, function () {
                    alert("Error getting contact alerts");
                    $scope.contactAlertsLoading = false;
                });

                // identify groups which have last import date more than 48 hours in past, note: could be done from user
                // information but would require log out and in again
                $scope.oldSubmissionDateGroupsLoading = true;
                GroupService.getGroupsForUser($scope.loggedInUser.id, {}).then(function (page) {
                    for (var i = 0; i < page.content.length; i++) {
                        var group = page.content[i];
                        var fortyEightHoursAgo = new Date().getTime() - 172800000;
                        if (group.groupType.value === 'UNIT') {
                            if (group.lastImportDate === null || group.lastImportDate < fortyEightHoursAgo) {
                                $scope.oldSubmissionDateGroups.push(group);
                            }
                        }
                        $scope.oldSubmissionDateGroupsLoading = false;
                    }
                }, function () {
                    alert("Error getting user groups");
                    $scope.oldSubmissionDateGroupsLoading = false;
                });
            }
        };

        $scope.hideAlertNotification = function (alert) {
            alert.webAlertViewed = true;
            AlertService.updateAlert($scope.loggedInUser.id, alert).then(function () {
                getAlerts();
            }, function () {
                alert('Error updating alert');
            });
        };

        $scope.hideSecretWordNotification = function () {
            UserService.hideSecretWordNotification($rootScope.loggedInUser.id).then(function () {
                $rootScope.loggedInUser.hideSecretWordNotification = true;
            }, function () {
                alert("Error hiding secret word notification");
            });
        };

        $scope.showGroupRole = function(groupRole) {
            return !((groupRole.role.name == 'MEMBER' && groupRole.group.code == 'GENERAL_PRACTICE')
            || (groupRole.role.name == 'PATIENT' && groupRole.group.code == 'Generic')
            || (groupRole.role.name == 'MEMBER' && groupRole.group.code == 'Generic'));
        };

        $scope.viewNewsItem = function (news) {
            var modalInstance = $modal.open({
                templateUrl: 'views/modal/viewNewsModal.html',
                controller: ViewNewsModalInstanceCtrl,
                size: 'lg',
                resolve: {
                    news: function () {
                        return news;
                    },
                    newsTypesArray: function () {
                        return $scope.newsTypesArray;
                    }
                }
            });

            modalInstance.result.then(function () {
                // ok (not used)
            }, function () {
                // closed
            });
        };

        var saveGpMedicationStatus = function () {
            MedicationService.saveGpMedicationStatus($scope.loggedInUser.id, $scope.gpMedicationStatus).then(function () {
                init();
                if ($scope.gpMedicationStatus.optInStatus === true) {
                    $scope.justOptedIn = true;
                }
            }, function () {
                alert('Cannot save GP medication status');
            });
        };

        $scope.gpMedicinesOptIn = function () {
            $scope.gpMedicationStatus.optInStatus = true;
            $scope.gpMedicationStatus.optInHidden = false;
            $scope.gpMedicationStatus.optOutHidden = false;
            $scope.gpMedicationStatus.optInDate = new Date().getTime();
            saveGpMedicationStatus();
        };

        $scope.gpMedicinesHideOptIn = function () {
            $scope.gpMedicationStatus.optInHidden = true;
            saveGpMedicationStatus();
        };

        $scope.userHasGroup = function (groupId){
            var hasGroup = false;
            $scope.loggedInUser.groupRoles.forEach(
                function (element) {
                    if(element.group.id == groupId || element.role.name == 'GLOBAL_ADMIN' ||
                        element.role.name == 'SPECIALTY_ADMIN'){
                        hasGroup = true;
                    }
                });
            return hasGroup;
        };

        $scope.listDistinctGroups = function (newsItem) {
            var groupIds = [];
            var newsLinks = [];

            newsItem.newsLinks.forEach(
                function (element) {
                    if (element.group != null) {

                        if(groupIds.indexOf(element.group.id) == -1 && $scope.userHasGroup(element.group.id)) {
                            groupIds.push(element.group.id);
                            newsLinks.push(element);
                        }
                    }
                });
            return newsLinks;
        };

        $scope.listNewsLinkGroupNames = function (newsItem) {
            var newsLinks = $scope.listDistinctGroups(newsItem);
            var text = "";

            for (var i=0; i<newsLinks.length; i++) {
                text += newsLinks[i].group.shortName;
                if (i !== newsLinks.length - 1) {
                    text += ", ";
                }
            }

            return text;
        };

        // Migration only
        $scope.startObservationMigration = function () {
            ObservationService.startObservationMigration().then(function () {

            }, function () {
                alert('Cannot start observation migration');
            });
        };

        // alerts
        $scope.addAlertObservationHeading = function (observationHeadingId) {
            var found = false;

            for (var i = 0; i < $scope.alertObservationHeadings.length; i++) {
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

        $scope.removeAlert = function (alertId) {
            AlertService.removeAlert($scope.loggedInUser.id, alertId).then(function () {
                getAlerts();
            }, function () {
                alert('Error removing result alert');
            });
        };

        $scope.updateAlert = function (alert) {
            AlertService.updateAlert($scope.loggedInUser.id, alert).then(function () {
                getAlerts();
            }, function () {
                alert('Error updating alert');
            });
        };

        $scope.updateLetterAlert = function (letterAlert) {
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

        var getAvailableObservationHeadings = function () {
            ObservationHeadingService.getAvailableAlertObservationHeadings($scope.loggedInUser.id)
                .then(function (observationHeadings) {
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
                }, function () {
                    alert('Error retrieving result types');
                });
        };

        var getAlerts = function () {
            delete $scope.alertObservationHeadings;
            delete $scope.letterAlert;

            // result alerts, multiple
            AlertService.getAlerts($scope.loggedInUser.id, 'RESULT')
                .then(function (alertObservationHeadings) {
                    $scope.alertObservationHeadings = alertObservationHeadings;
                }, function () {
                    alert('Error getting result alerts');
                });

            // letter alert, should only return one
            AlertService.getAlerts($scope.loggedInUser.id, 'LETTER')
                .then(function (alertLetters) {
                    if (alertLetters.length) {
                        $scope.letterAlert = alertLetters[0];
                    } else {
                        delete $scope.letterAlert;
                    }
                }, function () {
                    alert('Error getting letter alerts');
                });
        };

        $scope.showEnterDiagnosesModal = function () {
            var modalInstance = $modal.open({
                templateUrl: 'views/modal/enterDiagnosesModal.html',
                controller: EnterDiagnosesModalInstanceCtrl,
                size: 'lg',
                resolve: {
                    CodeService: function () {
                        return CodeService;
                    },
                    DiagnosisService: function () {
                        return DiagnosisService;
                    },
                    fromDashboard: function () {
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

    }]);
