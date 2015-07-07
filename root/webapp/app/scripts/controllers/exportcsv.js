angular.module('patientviewApp').controller('ExportInfoModalInstanceCtrl',
//var ExportInfoModalInstanceCtrl =
['$scope','$modalInstance', '$location', 'ExportService', 'ObservationHeadingService', 'UtilService', 'result',
    function ($scope, $modalInstance, $location, ExportService, ObservationHeadingService, UtilService, result) {
        $scope.selectedGroup = [];
        $scope.filterString = "";
        /**
         * Setup the default period to export
         * @type {Date}
         */
        var today = new Date();
        var currentDate = {};
        currentDate['day'] = ("0" + today.getDate().toString()).slice(-2);
        currentDate['month'] = "0" + (today.getMonth()+1).toString().slice(-2);
        currentDate['year']= today.getFullYear().toString();
        $scope.to = currentDate;

        var currentDate = {};
        currentDate['day']   = ("0" + today.getDate().toString()).slice(-2);
        currentDate['month'] = "0" + (today.getMonth()+1).toString().slice(-2);
        currentDate['year']  = (today.getFullYear()-3).toString();
        $scope.from = currentDate;
        //Add to scope, remove blank values so date will always be selected
        $scope.months =  _.without(UtilService.generateMonths(), '');
        $scope.years = _.without(UtilService.generateYears2000(), '');
        $scope.days =  _.without(UtilService.generateDays(), '');
        //Add the referrer (used for the end point)
        $scope.referrer = $location.$$path.toString();
        $scope.showResults = false;
        //Setup the title
        if($scope.referrer == ("/results") || $scope.referrer == ("/resultstable")) {
            $scope.showResults = true;
            $scope.pgtitle = "Results";
        }else if($scope.referrer == "/letters"){
            $scope.pgtitle = "Letters";
        }else if($scope.referrer == "/medicines") {
            $scope.pgtitle = "Medicines";
        }
        $scope.loading = true;
        $scope.loadingMessage = "Loading";

        $scope.init = function(){
            $scope.loading = true;

            ObservationHeadingService.getAvailableObservationHeadings($scope.loggedInUser.id).then(function(observationHeadings) {
                $scope.observationHeadings = observationHeadings;
                $scope.loading = false;
            }, function() {
                alert('Error retrieving result types');
            });
        }
        $scope.isResultTypeChecked = function (observationHeading) {
            if (_.contains($scope.selectedGroup, observationHeading)) {
                return 'glyphicon glyphicon-ok pull-right';
            }
            return false;
        };
        $scope.setSelected = function(observationHeading){
            $scope.loading = true;
            if (!_.contains($scope.selectedGroup, observationHeading)) {
                $scope.selectedGroup.push(observationHeading);
                $scope.filterString = $scope.selectedGroup.toString();
            }else{
                $scope.selectedGroup = _.without($scope.selectedGroup, observationHeading);
                $scope.filterString = $scope.selectedGroup.toString();

            }
            $scope.loading = false;
        }
        $scope.removeAllSelectedGroup = function(){
            $scope.selectedGroup = [];
        }
        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
        $scope.init();


}]);