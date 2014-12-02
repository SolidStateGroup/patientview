'use strict';

angular.module('patientviewApp').controller('DashboardCtrl', ['UserService', '$modal', '$scope', 'GroupService', 'NewsService', 'UtilService',
function (UserService, $modal, $scope, GroupService, NewsService, UtilService) {

    // get graph every time group is changed
    $scope.$watch('graphGroupId', function(newValue) {

        if ($scope.permissions && !$scope.permissions.isPatient) {
            var i;
            $scope.chartLoading = true;
            $('#chart_div').html('');

            if (newValue !== undefined) {
                GroupService.getStatistics(newValue).then(function (statisticsArray) {
                    var patients = [];
                    var patientsAdded = [];
                    var uniqueLogons = [];
                    var logons = [];
                    var xAxisCategories = [];

                    for (var i = 0; i < statisticsArray.length; i++) {
                        var statistics = statisticsArray[i];
                        var dateObject = new Date(statistics.startDate);
                        xAxisCategories.push(
                                UtilService.getMonthText(dateObject.getMonth()) + ' ' + dateObject.getFullYear());

                        if (statistics.statistics.PATIENT_COUNT !== undefined) {
                            patients.push(statistics.statistics.PATIENT_COUNT);
                        } else {
                            patients.push(null);
                        }

                        if (statistics.statistics.PATIENT_GROUP_ROLE_ADD_COUNT !== undefined) {
                            patientsAdded.push(statistics.statistics.PATIENT_GROUP_ROLE_ADD_COUNT);
                        } else {
                            patientsAdded.push(null);
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

                        $scope.statisticsDate = statistics.endDate;
                        $scope.lockedUsers = statistics.statistics.LOCKED_USER_COUNT;
                        $scope.inactiveUsers = statistics.statistics.INACTIVE_USER_COUNT;
                    }

                    console.log(patients);

                    // using highcharts
                    $('#chart_div').highcharts({
                        chart: {
                            zoomType: 'xy'
                        },
                        title: {
                            text: null
                        },
                        xAxis: {
                            categories: xAxisCategories
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
                            },
                            {
                                name: 'Patients Added',
                                data: patientsAdded
                            }
                        ],
                        credits: {
                            text: null
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

    init();
}]);
