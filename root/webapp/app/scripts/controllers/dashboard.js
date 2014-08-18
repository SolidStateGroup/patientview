'use strict';

angular.module('patientviewApp').controller('DashboardCtrl', ['UserService','$scope', 'GroupService',
function (UserService, $scope, GroupService) {

    // get graph every time group is changed
    $scope.$watch("graphGroupId", function(newValue, oldValue) {
        if(newValue !== undefined) {
            GroupService.getStatistics(newValue).then(function (data) {
                $scope.statistics = data;
                console.log(data);

                var chart1 = {};
                chart1.type = "LineChart";
                //chart1.cssStyle = "height:200px; width:300px;";
                chart1.data = {
                    "cols": [
                        {
                            "id": "month",
                            "label": "Month",
                            "type": "string",
                            "p": {}
                        },
                        {
                            "id": "laptop-id",
                            "label": "Laptop",
                            "type": "number",
                            "p": {}
                        },
                        {
                            "id": "desktop-id",
                            "label": "Desktop",
                            "type": "number",
                            "p": {}
                        },
                        {
                            "id": "server-id",
                            "label": "Server",
                            "type": "number",
                            "p": {}
                        }
                    ],
                        "rows": [
                        {
                            "c": [
                                {
                                    "v": "January"
                                },
                                {
                                    "v": 19,
                                    "f": "42 items"
                                },
                                {
                                    "v": 12,
                                    "f": "Ony 12 items"
                                },
                                {
                                    "v": 7,
                                    "f": "7 servers"
                                }
                            ]
                        },
                        {
                            "c": [
                                {
                                    "v": "February"
                                },
                                {
                                    "v": 13
                                },
                                {
                                    "v": 1,
                                    "f": "1 unit (Out of stock this month)"
                                },
                                {
                                    "v": 12
                                }
                            ]
                        },
                        {
                            "c": [
                                {
                                    "v": "March"
                                },
                                {
                                    "v": 24
                                },
                                {
                                    "v": 5
                                },
                                {
                                    "v": 11
                                }
                            ]
                        }
                    ]
                };

                chart1.options = {
                    "title": null,
                    "isStacked": "true",
                    "fill": 20,
                    "displayExactValues": true,
                    "vAxis": {
                        "title": "Sales unit",
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
