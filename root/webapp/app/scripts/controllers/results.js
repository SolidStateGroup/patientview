'use strict';

angular.module('patientviewApp').controller('ResultsCtrl', ['ResultService','$q','Restangular','$rootScope','$location','$cookieStore','$scope',
    function (ResultService, $q, Restangular, $rootScope, $location, $cookieStore, $scope) {

        var MS_PER_DAY = 86400000;
        var DEFAULT_DATE_RANGE = 3650000;
        var charts = [];
        var testMode = $rootScope.ieTestMode;

        // Remove
        $scope.remove = function(type) {
            $scope.typesList.splice($scope.typesList.indexOf(type), 1);
            $scope.typesListAvailable.push(type);
            $scope.typesListAvailable.sort();
            $scope.redraw();
        };

        // Add
        $scope.add = function(type) {

            $scope.typesList.push(type);
            $scope.typesListAvailable.splice($scope.typesListAvailable.indexOf(type), 1);
            if (!testMode) {
                ResultService.getObservation($rootScope.loggedInUser.uuid, type);
            }
            $scope.redraw();
        };

        // Filter
        $scope.filter = function(range) {
            $scope.dateRange = range;
            $scope.redraw();
        };

        // Redraw
        $scope.redraw = function() {
            var results = $scope.results;
            var typesList = $scope.typesList;
            var typeName;

            for (var h = 0; h < $scope.allTypes.length; h++) {
                typeName = $scope.allTypes[h];
                charts[typeName].clearChart();
                $('#graph-container_' + typeName).addClass('hidden');
            }

            for (var i = 0; i < $scope.typesList.length; i++) {
                for (var j = 0; j < results.length; j++) {
                    if (typesList[i] === results[j][0].data.name.text) {

                        typeName = typesList[i];

                        var obs = results[j],
                            data = [['date', 'value']],
                            today = new Date(),
                            minDate = new Date(today.getTime() - $scope.dateRange * MS_PER_DAY),
                            found = false;

                        for (var k = 0; k < obs.length; k++) {
                            var issuedDate = new Date(obs[k].data.issued);
                            if (issuedDate > minDate) {
                                data.push([issuedDate, obs[k].data.valueQuantity.value]);
                                found = true;
                            }
                        }

                        data = google.visualization.arrayToDataTable(data);
                        data.sort([{column: 0}]);

                        var options = {
                            title: typeName,
                            titleTextStyle: {
                                color: $scope.colors[typeName],
                                fontName: 'Helvetica Neue,Helvetica,Arial,sans-serif',
                                fontSize: 20
                            },
                            height: 150,
                            backgroundColor: { fill: 'transparent' },
                            legend: {position: 'none'},
                            colors: [$scope.colors[typeName]],
                            chartArea: {'width': '85%', 'height': '60%'}
                        };

                        if (!found) {
                            data = google.visualization.arrayToDataTable([
                                ['', { role: 'annotation' }],
                                [null, 'No results available for this date range']
                            ]);
                        }

                        $('#graph-container_' + typeName).removeClass('hidden');
                        $('#graph-link_' + typeName).attr('href', '#/resultsdetail?type=' + typeName + '&daterange=' + $scope.dateRange);
                        charts[typeName].draw(data,options);
                    }
                }
            }
        };

        // Init
        $scope.init = function () {

            $scope.resultsLoading = true;
            $scope.dateRange = DEFAULT_DATE_RANGE;

            if (testMode) {
                $scope.results = [
                    [
                        {
                            'data': {
                                'resourceType': 'Observation',
                                'name': {
                                    'text': 'adjustedcalcium'
                                },
                                'valueQuantity': {
                                    'value': 1.98
                                },
                                'issued': '2012-01-26T00:00:00-00:00',
                                'status': 'preliminary',
                                'reliability': 'ok',
                                'subject': {
                                    'reference': 'patientUuid',
                                    'display': 'c26c145e-46bf-4e7e-bbcb-81457f93bcec'
                                }
                            }
                        },
                        {
                            'data': {
                                'resourceType': 'Observation',
                                'name': {
                                    'text': 'adjustedcalcium'
                                },
                                'valueQuantity': {
                                    'value': 2.24
                                },
                                'issued': '2012-01-28T00:00:00-00:00',
                                'status': 'preliminary',
                                'reliability': 'ok',
                                'subject': {
                                    'reference': 'patientUuid',
                                    'display': 'c26c145e-46bf-4e7e-bbcb-81457f93bcec'
                                }
                            }
                        },
                        {
                            'data': {
                                'resourceType': 'Observation',
                                'name': {
                                    'text': 'adjustedcalcium'
                                },
                                'valueQuantity': {
                                    'value': 2.25
                                },
                                'issued': '2012-01-30T15:10:00-00:00',
                                'status': 'preliminary',
                                'reliability': 'ok',
                                'subject': {
                                    'reference': 'patientUuid',
                                    'display': 'c26c145e-46bf-4e7e-bbcb-81457f93bcec'
                                }
                            }
                        },
                        {
                            'data': {
                                'resourceType': 'Observation',
                                'name': {
                                    'text': 'adjustedcalcium'
                                },
                                'valueQuantity': {
                                    'value': 2.7
                                },
                                'issued': '2012-02-02T00:00:00-00:00',
                                'status': 'preliminary',
                                'reliability': 'ok',
                                'subject': {
                                    'reference': 'patientUuid',
                                    'display': 'c26c145e-46bf-4e7e-bbcb-81457f93bcec'
                                }
                            }
                        },
                        {
                            'data': {
                                'resourceType': 'Observation',
                                'name': {
                                    'text': 'adjustedcalcium'
                                },
                                'valueQuantity': {
                                    'value': 2.57
                                },
                                'issued': '2012-02-04T14:00:00-00:00',
                                'status': 'preliminary',
                                'reliability': 'ok',
                                'subject': {
                                    'reference': 'patientUuid',
                                    'display': 'c26c145e-46bf-4e7e-bbcb-81457f93bcec'
                                }
                            }
                        },
                        {
                            'data': {
                                'resourceType': 'Observation',
                                'name': {
                                    'text': 'adjustedcalcium'
                                },
                                'valueQuantity': {
                                    'value': 2.41
                                },
                                'issued': '2012-02-04T16:45:00-00:00',
                                'status': 'preliminary',
                                'reliability': 'ok',
                                'subject': {
                                    'reference': 'patientUuid',
                                    'display': 'c26c145e-46bf-4e7e-bbcb-81457f93bcec'
                                }
                            }
                        },
                        {
                            'data': {
                                'resourceType': 'Observation',
                                'name': {
                                    'text': 'adjustedcalcium'
                                },
                                'valueQuantity': {
                                    'value': 2.67
                                },
                                'issued': '2012-02-06T12:00:00-00:00',
                                'status': 'preliminary',
                                'reliability': 'ok',
                                'subject': {
                                    'reference': 'patientUuid',
                                    'display': 'c26c145e-46bf-4e7e-bbcb-81457f93bcec'
                                }
                            }
                        },
                        {
                            'data': {
                                'resourceType': 'Observation',
                                'name': {
                                    'text': 'adjustedcalcium'
                                },
                                'valueQuantity': {
                                    'value': 1.98
                                },
                                'issued': '2012-01-26T00:00:00-00:00',
                                'status': 'preliminary',
                                'reliability': 'ok',
                                'subject': {
                                    'reference': 'patientUuid',
                                    'display': 'c26c145e-46bf-4e7e-bbcb-81457f93bcec'
                                }
                            }
                        }
                    ],
                    [
                        {
                            'data': {
                                'resourceType': 'Observation',
                                'name': {
                                    'text': 'calcium'
                                },
                                'valueQuantity': {
                                    'value': 2.24
                                },
                                'issued': '2012-01-28T00:00:00-00:00',
                                'status': 'preliminary',
                                'reliability': 'ok',
                                'subject': {
                                    'reference': 'patientUuid',
                                    'display': 'c26c145e-46bf-4e7e-bbcb-81457f93bcec'
                                }
                            }
                        },
                        {
                            'data': {
                                'resourceType': 'Observation',
                                'name': {
                                    'text': 'calcium'
                                },
                                'valueQuantity': {
                                    'value': 2.25
                                },
                                'issued': '2012-01-30T15:10:00-00:00',
                                'status': 'preliminary',
                                'reliability': 'ok',
                                'subject': {
                                    'reference': 'patientUuid',
                                    'display': 'c26c145e-46bf-4e7e-bbcb-81457f93bcec'
                                }
                            }
                        },
                        {
                            'data': {
                                'resourceType': 'Observation',
                                'name': {
                                    'text': 'calcium'
                                },
                                'valueQuantity': {
                                    'value': 2.7
                                },
                                'issued': '2012-02-02T00:00:00-00:00',
                                'status': 'preliminary',
                                'reliability': 'ok',
                                'subject': {
                                    'reference': 'patientUuid',
                                    'display': 'c26c145e-46bf-4e7e-bbcb-81457f93bcec'
                                }
                            }
                        },
                        {
                            'data': {
                                'resourceType': 'Observation',
                                'name': {
                                    'text': 'calcium'
                                },
                                'valueQuantity': {
                                    'value': 2.57
                                },
                                'issued': '2012-02-04T14:00:00-00:00',
                                'status': 'preliminary',
                                'reliability': 'ok',
                                'subject': {
                                    'reference': 'patientUuid',
                                    'display': 'c26c145e-46bf-4e7e-bbcb-81457f93bcec'
                                }
                            }
                        },
                        {
                            'data': {
                                'resourceType': 'Observation',
                                'name': {
                                    'text': 'calcium'
                                },
                                'valueQuantity': {
                                    'value': 2.41
                                },
                                'issued': '2012-02-04T16:45:00-00:00',
                                'status': 'preliminary',
                                'reliability': 'ok',
                                'subject': {
                                    'reference': 'patientUuid',
                                    'display': 'c26c145e-46bf-4e7e-bbcb-81457f93bcec'
                                }
                            }
                        },
                        {
                            'data': {
                                'resourceType': 'Observation',
                                'name': {
                                    'text': 'calcium'
                                },
                                'valueQuantity': {
                                    'value': 2.67
                                },
                                'issued': '2012-02-06T12:00:00-00:00',
                                'status': 'preliminary',
                                'reliability': 'ok',
                                'subject': {
                                    'reference': 'patientUuid',
                                    'display': 'c26c145e-46bf-4e7e-bbcb-81457f93bcec'
                                }
                            }
                        }
                    ]
                ];
                $scope.colors = [];
                $scope.colors.adjustedcalcium = '#ff0000';
                $scope.colors.calcium = '#00ff00';
                $scope.typesList = ['adjustedcalcium'];
                $scope.typesListAvailable = ['calcium'];
                $scope.allTypes = $scope.typesList.concat($scope.typesListAvailable);

                for (var i = 0; i < $scope.allTypes.length; i++) {
                    var typeName = $scope.allTypes[i];
                    $('#obsdiv').append('<div class="graph-container row" id="graph-container_' + typeName + '"><div class="graph col-md-10" id="graph_' +
                        typeName + '"></div><div class="graph-link-container col-md-1"><a class="btn btn-primary graph-link" id="graph-link_' +
                        typeName + '" href="">View</a></div></div>');

                    charts[typeName] = new google.visualization.LineChart(document.getElementById('graph_' + typeName));
                    //charts[typeName] = new google.visualization.LineChart(document.getElementById('graph_' + typeName));
                }
                $scope.redraw();
                delete $scope.resultsLoading;

            } else {
                ResultService.getResultTypes($rootScope.loggedInUser.uuid).then(function(resulttypes) {

                    //var typesList = ['adjustedcalcium','calcium','albumin','alp','alt','ast','bili','bpdia','bpsys','cholesterol','creatinine','crp','egfr','ferritin'];
                    var promises = [], typesList = [];
                    $scope.colors = [];

                    for(var i=0;i<resulttypes.length;i++) {
                        var typeName = resulttypes[i].data.name.text;
                        typesList.push(typeName);
                        promises.push(ResultService.getObservation($rootScope.loggedInUser.uuid, typeName));
                        $scope.colors[typeName] = resulttypes[i].data.color;
                    }

                    $scope.typesList = typesList;
                    $scope.typesListAvailable = typesList;
                    $scope.typesListAvailable = _.difference( $scope.typesListAvailable, $scope.typesList);
                    $scope.allTypes = $scope.typesList.concat($scope.typesListAvailable);

                    $q.all(promises).then(function (results) {
                        $scope.results = results;

                        for (var i = 0; i < $scope.allTypes.length; i++) {
                            var typeName = $scope.allTypes[i];
                            $('#obsdiv').append('<div class="graph-container row" id="graph-container_' + typeName + '"><div class="graph col-md-10" id="graph_' +
                                typeName + '"></div><div class="graph-link-container col-md-1"><a class="btn btn-primary graph-link" id="graph-link_' +
                                typeName + '" href="">View</a></div></div>');

                            charts[typeName] = new google.visualization.LineChart(document.getElementById('graph_' + typeName));
                        }

                        $scope.redraw();
                        delete $scope.resultsLoading;
                    });
                });
            }
        };

        $scope.init();
    }]);
