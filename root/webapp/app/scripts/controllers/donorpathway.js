'use strict';

angular.module('patientviewApp').controller('DonorPathwayCtrl', ['localStorageService', 'UserService', 'DonorPathwayService', '$scope', '$rootScope', 'UtilService', 'FileUploader', '$routeParams', '$location', '$route',
    function (localStorageService, UserService, DonorPathwayService, $scope, $rootScope, UtilService, FileUploader, $routeParams, $location, $route) {
      var userId = DonorPathwayService.getUserId();
      console.log('DonorPathwayCtrl', userId);
}]);