'use strict';

angular.module('patientviewApp').controller('DashboardCtrl', ['UserService','$scope', 'GroupService', 'NewsService', 'UtilService',
function (UserService, $scope, GroupService, NewsService, UtilService) {

    // get graph every time group is changed
    $scope.$watch('graphGroupId', function(newValue) {

        if ($scope.permissions && !$scope.permissions.isPatient) {
            var i;
            $scope.chartLoading = true;
            $('#chart_div').html('');

            if (newValue !== undefined) {
                GroupService.getStatistics(newValue).then(function (data) {

                    if (data.length) {
                        var patients = [];
                        var uniqueLogons = [];
                        var logons = [];
                        var xAxisCategories = [];

                        for (i = 0; i < data.length; i++) {
                            var date = new Date(data[i].endDate);
                            xAxisCategories.push(UtilService.getMonthText(date.getMonth()) + ' ' + date.getFullYear());
                            patients.push(data[i].countOfPatients);
                            uniqueLogons.push(data[i].countOfUniqueLogons);
                            logons.push(data[i].countOfLogons);
                        }
                        $scope.statisticsDate = data[data.length - 1].endDate;
                        $scope.lockedUsers = data[data.length - 1].countOfUserLocked;
                        $scope.inactiveUsers = data[data.length - 1].countOfUserInactive;

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
                                    name: 'Patients',
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
                                text: null
                            },
                            exporting: {
                                enabled: false
                            }
                        });
                    }
                    $scope.chartLoading = false;

                });
            }
        }
    });

    $scope.init = function() {
        $scope.loading = true;

        $scope.allGroups = [];
        $scope.permissions = {};
        var i;

        $scope.permissions.isPatient = UserService.checkRoleExists('PATIENT', $scope.loggedInUser);

        // set the list of groups to show in the data grid
        $scope.graphGroups = $scope.loggedInUser.userGroups;

        for(i=0;i<$scope.graphGroups.length;i++) {
            $scope.allGroups[$scope.graphGroups[i].id] = $scope.graphGroups[i];
        }

        // set feature (avoid blank option)
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

    $scope.init();
}]);
