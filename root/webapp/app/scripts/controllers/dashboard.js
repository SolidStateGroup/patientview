'use strict';

angular.module('patientviewApp').controller('DashboardCtrl', ['UserService','$scope', 'GroupService',
function (UserService, $scope, GroupService) {

    // get graph every time group is changed
    $scope.$watch("graphGroupId", function(newValue, oldValue) {
        var i;

        if(newValue !== undefined) {
            GroupService.getStatistics(newValue).then(function (data) {
                $scope.statistics = data;

                var chart1 = {};
                chart1.type = "LineChart";
                chart1.data = [['date', 'Patients', 'Unique Logons', 'Logons']];

                for(i=0;i<data.length;i++) {
                    var row = [];
                    row[0] = data[i].startDate + " to " + data[i].endDate;
                    row[1] = data[i].countOfPatients;
                    row[2] = data[i].countOfUniqueLogons;
                    row[3] = data[i].countOfLogons;
                    chart1.data.push(row);
                }

                chart1.data = new google.visualization.arrayToDataTable(chart1.data);

                chart1.options = {
                    "title": null,
                    "isStacked": "true",
                    "fill": 20,
                    "displayExactValues": true,
                    "vAxis": {
                        "title": null,
                        "gridlines": {
                            "count": 10
                        }
                    },
                    "hAxis": {
                        "title": null
                    }
                };

                chart1.formatters = {};

                $scope.chart = chart1;

            });
        }
    });

    $scope.init = function() {
        GroupService.getGroupsForUser($scope.loggedInUser.id).then(function(groups) {
            // set the list of groups to show in the data grid
            $scope.graphGroups = groups;

            // set feature (avoid blank option)
            if ($scope.graphGroups && $scope.graphGroups.length > 0) {
                $scope.graphGroupId = $scope.graphGroups[0].id;
            }
        }, function () {
            alert('Error retrieving groups');
        });
    };

    $scope.init();
}]);
