'use strict';

angular.module('patientviewApp').controller('FeedbackCtrl',['$scope', '$location',
    function ($scope, $location) {
        $location.path('/dashboard');
}]);
