'use strict';

angular.module('patientviewApp').factory('CodeService', ['$q', 'Restangular', 'UtilService', function ($q, Restangular, UtilService) {
    return {
        getAll: function () {
            var deferred = $q.defer();
            Restangular.all('code').getList().then(function(successResult) {
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
        new: function (code, codeTypes, standardTypes) {
            var deferred = $q.defer();

            // convert code and standard type ids to actual objects and clean
            code.codeType = UtilService.cleanObject(_.findWhere(codeTypes, {id: code.codeTypeId}),'codeType');
            code.standardType = UtilService.cleanObject(_.findWhere(standardTypes, {id: code.standardTypeId}),'standardType');
            var codeToPost = _.clone(code);
            delete codeToPost.codeTypeId;
            delete codeToPost.standardTypeId;

            Restangular.all('code').post(codeToPost).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        },
        save: function (inputCode) {
            var deferred = $q.defer();
            Restangular.all('code').put(inputCode).then(function(successResult) {
                deferred.resolve(successResult);
            }, function(failureResult) {
                deferred.reject(failureResult);
            });
            return deferred.promise;
        }
    };
}]);
