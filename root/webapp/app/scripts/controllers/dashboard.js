'use strict';

angular.module('patientviewApp').controller('DashboardCtrl', ['UserService','$scope', 'GroupService', 'NewsService',
function (UserService, $scope, GroupService, NewsService) {

    // get graph every time group is changed
    $scope.$watch('graphGroupId', function(newValue) {
        var i;
        $scope.chartLoading = true;

        if(newValue !== undefined) {
            GroupService.getStatistics(newValue).then(function (data) {
                var chart1 = {};
                chart1.type = 'LineChart';
                chart1.data = [['date', 'Patients', 'Unique Logons', 'Logons']];

                for(i=0;i<data.length;i++) {
                    var row = [];
                    row[0] = data[i].startDate + ' to ' + data[i].endDate;
                    row[1] = data[i].countOfPatients;
                    row[2] = data[i].countOfUniqueLogons;
                    row[3] = data[i].countOfLogons;
                    chart1.data.push(row);
                }

                // get most recent statistics of user locked and inactive
                $scope.statisticsDate = data[0].endDate;
                $scope.lockedUsers = data[0].countOfUserLocked;
                $scope.inactiveUsers = data[0].countOfUserInactive;

                chart1.data = new google.visualization.arrayToDataTable(chart1.data);

                chart1.options = {
                    'title': null,
                    'isStacked': 'true',
                    'fill': 20,
                    'displayExactValues': true,
                    'vAxis': {
                        'title': null,
                        'pointSize': 5,
                        'gridlines': {
                            'count': 10,
                            'color': '#ffffff'
                        },
                        'viewWindow': {
                            'min': 0
                        }
                    },
                    'hAxis': {
                        'title': null
                    },
                    'chartArea': {
                        left: '7%',
                        top: '7%',
                        width: '80%',
                        height: '85%'
                    }
                };

                chart1.formatters = {};
                $scope.chart = chart1;
                $scope.chartLoading = false;
            });
        }
    });

    $scope.init = function() {
        $scope.loading = true;
        $scope.allGroups = [];
        var i;

        GroupService.getGroupsForUser($scope.loggedInUser.id).then(function(page) {
            // set the list of groups to show in the data grid
            $scope.graphGroups = page.content;

            for(i=0;i<page.content.length;i++) {
                $scope.allGroups[page.content[i].id] = page.content[i];
            }

            // set feature (avoid blank option)
            if ($scope.graphGroups && $scope.graphGroups.length > 0) {
                $scope.graphGroupId = $scope.graphGroups[0].id;
            }
        }, function () {
            alert('Error retrieving groups');
        });

        NewsService.getByUser($scope.loggedInUser.id, 0, 5).then(function(page) {
            $scope.newsItems = page.content;
            $scope.loading = false;
        }, function() {
            $scope.loading = false;
        });
    };

    $scope.init();
}]);
