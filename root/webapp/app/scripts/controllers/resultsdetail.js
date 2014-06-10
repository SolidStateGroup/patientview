'use strict';

angular.module('patientviewApp').controller('ResultsDetailCtrl', ['ResultService','$rootScope','$scope','$routeParams',
function (ResultService, $rootScope, $scope, $routeParams) {

    var MS_PER_DAY = 86400000;
    var typeName = $routeParams.type;
    var chart, table;
    $scope.resultsLoading = true;
    $scope.dateRange = $routeParams.dateRange;
    $scope.resultType = typeName;
    $scope.colors = [];

    // Filter
    $scope.filter = function(range) {
        $scope.dateRange = range;
        $scope.redraw();
    };

    // Redraw
    $scope.redraw = function() {
        var dateRange;
        if ($scope.dateRange) { dateRange = $scope.dateRange; } else { $scope.dateRange = 3650000; }
        var results = $scope.results;
        var data = [['Date', 'Value']];
        var today = new Date();
        var minDate = new Date(today.getTime() - $scope.dateRange * MS_PER_DAY);
        $scope.dataExistsInDateRange = false;

        for (var i=0;i<results.length;i++) {
            if (results[i].data.name.text===typeName) {
                var issuedDate = new Date(results[i].data.issued);
                if (issuedDate > minDate) {
                    data.push([issuedDate, results[i].data.valueQuantity.value]);
                    $scope.dataExistsInDateRange = true;
                }
            }
        }

        if ($scope.dataExistsInDateRange) {

            $('#graphdiv').show();
            $('#tablediv').show();
            data = google.visualization.arrayToDataTable(data);
            data.sort([{column: 0}]);

            var graphOptions = {
                height: 300,
                colors: [$scope.resultColor],
                legend: {position: 'none'}
            };

            var tableOptions = {
                page: 'enable',
                pageSize: 10
            };

            chart.draw(data, graphOptions);
            table.draw(data, tableOptions);

        } else {
            $('#graphdiv').hide();
            $('#tablediv').hide();
        }
    };

    // Init
    $scope.init = function() {

        $('#graphdiv').html('<div class="graph" id="graph_' + typeName + '"></div>');
        chart = new google.visualization.LineChart(document.getElementById('graph_' + typeName));

        $('#tablediv').html('<div class="table" id="table_' + typeName + '"></div>');
        table = new google.visualization.Table(document.getElementById('table_' + typeName));

        google.visualization.events.addListener(table, 'select', function () {
            var selection = table.getSelection();
            chart.setSelection([selection[0]]);
        });

        google.visualization.events.addListener(chart, 'select', function () {
            var selection = chart.getSelection();
            table.setSelection([{row:selection[0].row}]);
        });

        ResultService.getResultTypes($rootScope.loggedInUser.uuid).then(function(resultTypes) {
            for (var i=0;i<resultTypes.length;i++) {
                if (resultTypes[i].data.name.text === typeName) {
                    $scope.resultColor = resultTypes[i].data.color;
                }
            }

            ResultService.getObservation($rootScope.loggedInUser.uuid, typeName).then(function(results){
                $scope.results = results;
                $scope.redraw();
                delete $scope.resultsLoading;
            });
        });
    };

    $scope.init();

}]);
