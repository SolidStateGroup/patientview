'use strict';

angular.module('patientviewApp').factory('ResultHeadingService', ['$q', 'Restangular', 'UtilService', function ($q, Restangular, UtilService) {
    return {
        getAll: function (getParameters) {
            var deferred = $q.defer();
            // GET /code?codeTypes=1&filterText=something&page=0&size=5&sortDirection=ASC&sortField=code&standardTypes=2
            Restangular.one('code').get(getParameters).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        get: function (codeId) {
            var deferred = $q.defer();
            Restangular.one('code',codeId).get().then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // create new code
        create: function (code) {
            var deferred = $q.defer();
            Restangular.all('code').post(code).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        // save code
        save: function (inputCode) {
            var deferred = $q.defer();
            var code = UtilService.cleanObject(inputCode, 'code');
            Restangular.all('code').customPUT(code).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
